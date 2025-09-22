package com.hope.controller;
import com.alipay.api.AlipayApiException;


import com.alipay.easysdk.factory.Factory;
import com.hope.domain.entity.Order;
import com.hope.service.IAlipayService;
import com.hope.utils.AlipayTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝接口
 */
@RestController
@RequestMapping("/alipay")
public class PayController {

    @Autowired
    private IAlipayService alipayService;

    @GetMapping(value = "/pay", produces = "text/html")
    public String createPay(
            @RequestParam("bizType") String bizType, // 商品类型：HOTEL(酒店)、TICKET(门票)
            @RequestParam("orderId") Long orderId) { // 商品ID：酒店订单ID、门票订单ID
        System.out.println("bizType"+bizType+"orderId"+orderId);
        return alipayService.createPay(bizType, orderId);
    }

//    @GetMapping(value = "/pay", produces = "text/html")
//    @ResponseBody
//    public String pay(@RequestParam long id) throws AlipayApiException {
//        Order order = new Order();
//        order.setId(120948748373624L);
//        order.setUserId(129904058947L);
//        order.setInterfaceInfoId(294389472934L);
//        order.setMoney(1.0);
//        order.setPaymentMethod("支付宝");
//        return alipayTemplate.pay(order);
//    }

    @PostMapping("/notify")  // 注意这里必须是POST接口
    public String payNotify(HttpServletRequest request) {
        return alipayService.handleNotify("支付宝", request);
    }
//    public String payNotify(HttpServletRequest request) throws Exception {
//        if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
//            System.out.println("=========支付宝异步回调========");
//
//            Map<String, String> params = new HashMap<>();
//            Map<String, String[]> requestParams = request.getParameterMap();
//            for (String name : requestParams.keySet()) {
//                params.put(name, request.getParameter(name));
//                // System.out.println(name + " = " + request.getParameter(name));
//            }
//
//            String tradeNo = params.get("out_trade_no");
//            String gmtPayment = params.get("gmt_payment");
//            String alipayTradeNo = params.get("trade_no");
//            // 支付宝验签
//            if (Factory.Payment.Common().verifyNotify(params)) {
//                // 验签通过
//                System.out.println("交易名称: " + params.get("subject"));
//                System.out.println("交易状态: " + params.get("trade_status"));
//                System.out.println("支付宝交易凭证号: " + params.get("trade_no"));
//                System.out.println("商户订单号: " + params.get("out_trade_no"));
//                System.out.println("交易金额: " + params.get("total_amount"));
//                System.out.println("买家在支付宝唯一id: " + params.get("buyer_id"));
//                System.out.println("买家付款时间: " + params.get("gmt_payment"));
//                System.out.println("买家付款金额: " + params.get("buyer_pay_amount"));
//                // 更新订单状态
//            }
//        }
//        return "success";
//    }


}

