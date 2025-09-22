package com.hope.utils;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.hope.domain.entity.Order;
import com.hope.domain.entity.PayParam;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {
    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    @Value("${alipay.appId}")

    public String appId;

    // 应用私钥，就是工具生成的应用私钥
    @Value("${alipay.merchantPrivateKey}")
    public String merchantPrivateKey;
    // 支付宝公钥,对应APPID下的支付宝公钥。
    @Value("${alipay.alipayPublicKey}")
    public String alipayPublicKey;

    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    @Value("${alipay.notifyUrl}")
    public String notifyUrl;
    //同步通知，支付成功，一般跳转到成功页
    @Value("${alipay.returnUrl}")
    public String returnUrl;

    // 签名方式
    @Value("${alipay.signType}")
    private String signType;

    // 字符编码格式
    @Value("${alipay.charset}")
    private String charset;

    //订单超时时间
    private String timeout = "30m";
    // 支付宝网关
    @Value("${alipay.gatewayUrl}")
    public String gatewayUrl;

    // 新增：EasySDK初始化方法（应用启动时自动执行）
    @PostConstruct
    public void initEasySDK() {
        // 1. 创建EasySDK配置对象
        Config config = new Config();
        // 2. 填充配置参数（复用当前类已读取的配置）
        config.protocol = "https";
        // 提取网关主机地址（如从"https://openapi-sandbox.dl.alipaydev.com/gateway.do"中提取"openapi-sandbox.dl.alipaydev.com"）
        config.gatewayHost = gatewayUrl.replace("https://", "").replace("/gateway.do", "");
        config.signType = this.signType;
        config.appId = this.appId;
        config.merchantPrivateKey = this.merchantPrivateKey; // 商户私钥
        config.alipayPublicKey = this.alipayPublicKey; // 支付宝公钥
        config.notifyUrl = this.notifyUrl; // 异步通知地址

        // 3. 初始化EasySDK全局配置
        Factory.setOptions(config);
        System.out.println("支付宝EasySDK初始化成功！");
    }


    public String pay(PayParam payParam) throws AlipayApiException {
//        System.out.println(appId);
//        System.out.println(merchantPrivateKey);
//        System.out.println(alipayPublicKey);
//        System.out.println(notifyUrl);
//        System.out.println(charset);
//        System.out.println(timeout);
//        System.out.println(gatewayUrl);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new
                DefaultAlipayClient(gatewayUrl, appId, merchantPrivateKey,
                "json", charset, alipayPublicKey, signType);

        //2、创建一个支付请求，并设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(returnUrl);
        alipayRequest.setNotifyUrl(notifyUrl);

        Long orderId = payParam.getOrderId();
        String subject = payParam.getSubject();
        Double money = payParam.getMoney();
        String paymentMethod = payParam.getPaymentMethod();
        alipayRequest.setBizContent(" {\"out_trade_no\":\"" + orderId + "\","
                + "\"total_amount\":\"" + money + "\","
                + "\"subject\":\"" + subject
                + "\","
                + "\"body\":\"" + paymentMethod + "\","
                +
                "\"timeout_express\":\"" + timeout + "\","
                +
                "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        String result = alipayClient.pageExecute(alipayRequest).getBody();
        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应：" + result);
        return result;
    }
}

