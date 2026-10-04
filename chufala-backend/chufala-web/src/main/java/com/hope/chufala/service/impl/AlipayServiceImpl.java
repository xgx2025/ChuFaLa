package com.hope.chufala.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.easysdk.factory.Factory;
import com.hope.chufala.common.util.EmailUtils;
import com.hope.chufala.common.util.ThreadLocalUtils;
import com.hope.chufala.exception.OrderAlreadyCancelledException;
import com.hope.chufala.model.dto.PayParamDTO;
import com.hope.chufala.model.entity.PayRecord;
import com.hope.chufala.adapter.BizAdapterFactory;
import com.hope.chufala.mapper.PayRecordMapper;
import com.hope.chufala.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hope.chufala.service.IAlipayService;
import com.hope.chufala.adapter.BizAdapter;
import com.hope.chufala.infra.AlipayTemplate;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class AlipayServiceImpl implements IAlipayService {

    @Autowired
    private AlipayTemplate alipayTemplate;
    @Autowired
    private BizAdapterFactory bizAdapterFactory;
    @Autowired
    private PayRecordMapper payRecordMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private EmailUtils emailUtils;

    @Autowired
    @Qualifier("mailExecutor")
    private Executor mailExecutor;

    @Override
    public String createPay(String bizType, Long orderId) {
        Claims claims = ThreadLocalUtils.get();
        Long userId = claims.get("userId", Long.class);
        // 1. 通过业务适配器获取统一支付参数（隔离业务差异）
        BizAdapter adapter = bizAdapterFactory.getAdapter(bizType);
        PayParamDTO payParam = adapter.buildPayParam(orderId, userId); // 由业务适配器转换参数

        // 2. 生成支付记录（通用逻辑：记录支付状态）
        PayRecord record = payRecordMapper.selectByOrderId(orderId);
        if (record == null) {
            record = new PayRecord();
            record.setUserId(userId);
            record.setBizType(bizType);
            record.setOrderId(orderId);
            record.setMoney(payParam.getMoney());
            record.setStatus("WAIT_PAY");
            payRecordMapper.insert(record);
        } else if (!"WAIT_PAY".equals(record.getStatus())
                || !userId.equals(record.getUserId()) || !bizType.equals(record.getBizType())
                || record.getMoney().compareTo(payParam.getMoney()) != 0) {
            throw new IllegalArgumentException("订单支付状态或金额异常");
        }

        // 3. 调用支付宝接口生成支付表单（通用逻辑）
        try {
            System.out.println("支付宝参数："+payParam.getOrderId());
            return alipayTemplate.pay(payParam); // 复用之前的支付宝工具类
        } catch (AlipayApiException e) {
            log.error("创建支付宝支付失败", e);
            throw new RuntimeException("支付创建失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleNotify(String channel, HttpServletRequest request) {
        // 1. 验签
        Map<String, String> params = parseParams(request);
        try {
            if (!Factory.Payment.Common().verifyNotify(params)) {
                log.error("支付宝回调验签失败：{}", params);
                return "fail";
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 2. 解析回调参数
        String orderId = params.get("out_trade_no");
        String subject = params.get("subject");
        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus)) {
            return "success"; // 只处理支付成功状态
        }

        // 3. 查询支付记录，校验状态（防重复处理）
        if (orderId == null || !alipayTemplate.getAppId().equals(params.get("app_id"))) {
            log.error("支付宝回调订单号或应用 ID 不匹配，orderId={}", orderId);
            return "fail";
        }
        Long numericOrderId;
        try {
            numericOrderId = Long.valueOf(orderId);
        } catch (NumberFormatException e) {
            log.error("支付宝回调订单号格式无效，orderId={}", orderId);
            return "fail";
        }
        PayRecord record = payRecordMapper.selectByOrderId(numericOrderId);
        if (record == null) {
            log.error("支付宝回调找不到支付记录，orderId={}", orderId);
            return "fail";
        }
        if ("SUCCESS".equals(record.getStatus()) || "REFUND_REQUIRED".equals(record.getStatus())
                || "REFUNDED".equals(record.getStatus())) {
            return "success";
        }
        if (!"WAIT_PAY".equals(record.getStatus()) || params.get("total_amount") == null
                || record.getMoney().compareTo(new BigDecimal(params.get("total_amount"))) != 0) {
            log.error("支付宝回调状态或金额不匹配，orderId={}", orderId);
            return "fail";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime payTime = LocalDateTime.parse(params.get("gmt_payment"), formatter);

        // 业务状态变更与支付记录落库处于同一事务。
        BizAdapter adapter = bizAdapterFactory.getAdapter(record.getBizType());
        try {
            adapter.handlePaySuccess(record.getOrderId(), record);
        } catch (OrderAlreadyCancelledException e) {
            if (!updatePaymentRecord(record, "REFUND_REQUIRED", payTime, params.get("trade_no"))) {
                throw new IllegalStateException("支付记录状态变更失败，orderId=" + orderId);
            }
            log.error("已取消订单收到支付成功回调，需退款，orderId={}, tradeNo={}", orderId, params.get("trade_no"));
            return "success";
        }
        if (!updatePaymentRecord(record, "SUCCESS", payTime, params.get("trade_no"))) {
            throw new IllegalStateException("支付记录状态变更失败，orderId=" + orderId);
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendSuccessEmail(record.getUserId(), subject, orderId);
            }
        });
        return "success";
    }

    private boolean updatePaymentRecord(PayRecord record, String status, LocalDateTime payTime, String tradeNo) {
        UpdateWrapper<PayRecord> update = new UpdateWrapper<>();
        update.eq("id", record.getId()).eq("status", "WAIT_PAY")
                .set("status", status).set("pay_time", payTime).set("trade_no", tradeNo);
        return payRecordMapper.update(null, update) == 1;
    }

    private void sendSuccessEmail(Long userId, String subject, String orderId) {
        try {
            CompletableFuture.runAsync(() -> {
                try {
                    String email = userMapper.findEmailById(userId);
                    if (email != null && !email.isEmpty()) {
                        emailUtils.sendEmail(email, "【出发啦】订单通知", "您已成功订购，" + subject
                                + "，订单号：" + orderId + "，您可以前往【出发啦】网站的订单中心查看详情。祝您旅途开心！\uD83E\uDD17");
                    }
                } catch (Exception e) {
                    log.error("发送订单成功邮件失败，订单ID: {}", orderId, e);
                }
            }, mailExecutor);
        } catch (RuntimeException e) {
            log.error("订单成功邮件任务提交失败，orderId={}", orderId, e);
        }
    }

    // 解析请求参数为Map
    private Map<String, String> parseParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));
        return params;
    }


}
