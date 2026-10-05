package com.hope.chufala.infra;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.hope.chufala.model.dto.PayParamDTO;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝支付模板。
 *
 * <p>集中承载 alipay.* 配置：启动时用 EasySDK 初始化全局配置（供回调验签使用），
 * 下单时用官方 SDK 的 pageExecute 生成支付页表单。业务参数通过 SDK 模型对象序列化，
 * 避免商品标题中的引号破坏 JSON。
 *
 * @author 谢光湘
 */
@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {
    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    @Value("${alipay.appId}")

    public String appId;

    // 支付宝收款方 PID，回调中的 seller_id 必须与之相同。
    @Value("${alipay.sellerId:}")
    private String sellerId;

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
    /**
     * 启动时初始化 EasySDK 全局配置（回调验签依赖该全局 Factory）。
     */
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


    /**
     * 生成支付宝电脑网站支付表单。
     *
     * @param payParam 统一支付参数
     * @return 支付表单 HTML
     * @throws AlipayApiException 调用支付宝接口失败
     */
    public String pay(PayParamDTO payParam) throws AlipayApiException {
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

        //2、使用 SDK 模型序列化业务参数，避免酒店名称中的引号破坏 JSON。
        AlipayTradePagePayRequest alipayRequest = buildPayRequest(payParam);
        String result = alipayClient.pageExecute(alipayRequest).getBody();
        return result;
    }

    /**
     * 组装支付请求（同步/异步回调地址 + 业务模型）。
     *
     * @param payParam 统一支付参数
     * @return 支付请求对象
     */
    AlipayTradePagePayRequest buildPayRequest(PayParamDTO payParam) {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(returnUrl);
        request.setNotifyUrl(notifyUrl);

        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(payParam.getOrderId().toString());
        model.setTotalAmount(payParam.getMoney().toPlainString());
        model.setSubject(payParam.getSubject());
        model.setBody(payParam.getBody());
        model.setTimeoutExpress(timeout);
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        request.setBizModel(model);
        return request;
    }
}
