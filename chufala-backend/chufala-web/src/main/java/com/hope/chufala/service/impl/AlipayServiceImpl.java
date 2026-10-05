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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 支付宝支付服务实现。
 *
 * <p>核心在回调处理的「先校验、后幂等、再落库」：验签 → 核对 appId / 收款方 PID /
 * 金额 / 交易号 → 悲观锁读取支付记录 → 同一事务内推进业务状态与支付记录 →
 * 事务提交后再异步发通知邮件。业务差异由 {@link BizAdapter} 隔离，本类只负责通用支付流程。
 *
 * @author 谢光湘
 */
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

    /**
     * 创建支付：由业务适配器给出统一支付参数，落一条 WAIT_PAY 支付记录后生成支付表单。
     *
     * <p>若已有支付记录，会校验其状态、归属、业务类型与金额是否一致，不一致直接拒绝，
     * 防止同一订单被改价或换人重复发起支付。
     *
     * @param bizType 业务类型（HOTEL / TICKET / VIP）
     * @param orderId 业务订单 ID
     * @return 支付宝支付表单 HTML
     */
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

    /**
     * 处理支付宝异步回调。
     *
     * <p>任何校验不通过都返回 "fail" 让支付宝重试；重复回调按幂等处理，
     * 已取消订单收到支付成功回调时置为 REFUND_REQUIRED 等待退款。
     *
     * @param channel 支付渠道标识（当前固定为"支付宝"）
     * @param request 回调请求
     * @return 返回给支付宝的应答（success / fail）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleNotify(String channel, HttpServletRequest request) {
        Map<String, String> params = parseParams(request);
        try {
            if (!Factory.Payment.Common().verifyNotify(params)) {
                log.warn("支付宝回调验签失败");
                return "fail";
            }
        } catch (Exception e) {
            log.warn("支付宝回调验签异常", e);
            return "fail";
        }

        String orderId = params.get("out_trade_no");
        if (orderId == null || orderId.isBlank()
                || !alipayTemplate.getAppId().equals(params.get("app_id"))
                || alipayTemplate.getSellerId() == null || alipayTemplate.getSellerId().isBlank()
                || !alipayTemplate.getSellerId().equals(params.get("seller_id"))) {
            log.warn("支付宝回调订单号、应用 ID 或收款方 PID 不匹配，orderId={}", orderId);
            return "fail";
        }
        Long numericOrderId;
        try {
            numericOrderId = Long.valueOf(orderId);
        } catch (NumberFormatException e) {
            log.error("支付宝回调订单号格式无效，orderId={}", orderId);
            return "fail";
        }
        // 历史上同一订单可能有多条 WAIT_PAY；锁住全部记录，避免并发回调分别选中不同记录。
        List<PayRecord> records = payRecordMapper.selectByOrderIdForUpdate(numericOrderId);
        if (records.isEmpty()) {
            log.error("支付宝回调找不到支付记录，orderId={}", orderId);
            return "fail";
        }
        PayRecord record = records.get(0);
        BigDecimal paidAmount;
        try {
            paidAmount = new BigDecimal(params.get("total_amount"));
        } catch (NullPointerException | NumberFormatException e) {
            log.warn("支付宝回调金额格式无效，orderId={}", orderId);
            return "fail";
        }
        String tradeNo = params.get("trade_no");
        if (record.getMoney() == null || record.getMoney().compareTo(paidAmount) != 0
                || tradeNo == null || tradeNo.isBlank()) {
            log.warn("支付宝回调金额或交易号不匹配，orderId={}", orderId);
            return "fail";
        }
        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            return "success";
        }
        if ("SUCCESS".equals(record.getStatus()) || "REFUND_REQUIRED".equals(record.getStatus())
                || "REFUNDED".equals(record.getStatus())) {
            return tradeNo.equals(record.getTradeNo()) ? "success" : "fail";
        }
        if (!"WAIT_PAY".equals(record.getStatus())) {
            log.warn("支付宝回调支付记录状态不允许处理，orderId={}", orderId);
            return "fail";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime payTime;
        try {
            payTime = LocalDateTime.parse(params.get("gmt_payment"), formatter);
        } catch (RuntimeException e) {
            log.warn("支付宝回调付款时间格式无效，orderId={}", orderId);
            return "fail";
        }

        // 业务状态变更与支付记录落库处于同一事务。
        BizAdapter adapter = bizAdapterFactory.getAdapter(record.getBizType());
        try {
            adapter.handlePaySuccess(record.getOrderId(), record);
        } catch (OrderAlreadyCancelledException e) {
            if (!updatePaymentRecord(record, "REFUND_REQUIRED", payTime, tradeNo)) {
                throw new IllegalStateException("支付记录状态变更失败，orderId=" + orderId);
            }
            log.error("已取消订单收到支付成功回调，需退款，orderId={}, tradeNo={}", orderId, tradeNo);
            return "success";
        }
        if (!updatePaymentRecord(record, "SUCCESS", payTime, tradeNo)) {
            throw new IllegalStateException("支付记录状态变更失败，orderId=" + orderId);
        }
        String subject = params.get("subject");
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendSuccessEmail(record.getUserId(), subject, orderId);
            }
        });
        return "success";
    }

    /**
     * 以 WAIT_PAY 为条件更新支付记录（CAS，保证只被推进一次）。
     *
     * @param record  支付记录
     * @param status  目标状态
     * @param payTime 支付时间
     * @param tradeNo 交易号
     * @return 是否更新成功
     */
    private boolean updatePaymentRecord(PayRecord record, String status, LocalDateTime payTime, String tradeNo) {
        UpdateWrapper<PayRecord> update = new UpdateWrapper<>();
        update.eq("id", record.getId()).eq("status", "WAIT_PAY")
                .set("status", status).set("pay_time", payTime).set("trade_no", tradeNo);
        return payRecordMapper.update(null, update) == 1;
    }

    /**
     * 异步发送支付成功通知邮件（失败只记日志，不影响主流程）。
     *
     * @param userId  用户 ID
     * @param subject 订单标题
     * @param orderId 订单号
     */
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
    /**
     * 把回调请求的参数表拍平成单值 Map（同名多值只取第一个）。
     *
     * @param request 回调请求
     * @return 参数 Map
     */
    private Map<String, String> parseParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> {
            if (v != null && v.length > 0) {
                params.put(k, v[0]);
            }
        });
        return params;
    }


}
