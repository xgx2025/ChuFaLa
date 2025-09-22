package com.hope.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.easysdk.factory.Factory;
import com.hope.domain.entity.PayParam;
import com.hope.domain.entity.PayRecord;
import com.hope.factory.BizAdapterFactory;
import com.hope.mapper.PayRecordMapper;
import com.hope.service.IAlipayService;
import com.hope.service.adapter.BizAdapter;
import com.hope.utils.AlipayTemplate;
import com.hope.utils.ThreadLocalUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class AlipayServiceImpl implements IAlipayService {

    @Autowired
    private AlipayTemplate alipayTemplate;
    @Autowired
    private BizAdapterFactory bizAdapterFactory;
    @Autowired
    private PayRecordMapper payRecordMapper;

    private static final Logger log = LoggerFactory.getLogger(AlipayServiceImpl.class);

    @Override
    public String createPay(String bizType, Long orderId) {
        // 1. 通过业务适配器获取统一支付参数（隔离业务差异）
        BizAdapter adapter = bizAdapterFactory.getAdapter(bizType);
        PayParam payParam = adapter.buildPayParam(orderId); // 由业务适配器转换参数

        // 2. 生成支付记录（通用逻辑：记录支付状态）
        PayRecord record = new PayRecord();
        Claims claims = ThreadLocalUtil.get();
        Long userId = claims.get("userId", Long.class);
        record.setUserId(userId);
        record.setBizType(bizType);
        record.setOrderId(orderId);
        record.setMoney(payParam.getMoney());
        record.setStatus("WAIT_PAY"); // 待支付
        payRecordMapper.insert(record);

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
    public String handleNotify(String channel, HttpServletRequest request) {
        // 1. 验签（通用逻辑）
        Map<String, String> params = parseParams(request);
        try {
            if (!Factory.Payment.Common().verifyNotify(params)) {
                log.error("支付宝回调验签失败：{}", params);
                return "fail";
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 2. 解析回调参数（通用逻辑）
        String orderId = params.get("out_trade_no");
        System.out.println("商户订单号: " + orderId);
        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus)) {
            return "success"; // 只处理支付成功状态
        }

        // 3. 查询支付记录，校验状态（防重复处理）
        PayRecord record = payRecordMapper.selectByOrderId(Long.parseLong(orderId));
        if (record == null || "SUCCESS".equals(record.getStatus())) {
            return "success";
        }

        // 4. 更新支付记录状态（通用逻辑）
        record.setStatus("SUCCESS");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        record.setPayTime(LocalDateTime.parse(params.get("gmt_payment"),formatter));
        record.setTradeNo(params.get("trade_no")); // 支付宝交易号
        payRecordMapper.updateById(record);

        // 5. 通过业务适配器触发具体业务（隔离业务差异）
        BizAdapter adapter = bizAdapterFactory.getAdapter(record.getBizType());
        adapter.handlePaySuccess(record.getOrderId(), record); // 由业务适配器处理后续逻辑

        return "success";
    }

    // 解析请求参数为Map
    private Map<String, String> parseParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));
        return params;
    }
}
