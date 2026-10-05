package com.hope.chufala.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 价格签名工具
 * 对称密钥统一由 application.yml 的 sign.price-secret-key 提供
 *
 * <p>下单前由服务端把房型、日期、间夜数、房间数与总价拼成 data 串并做
 * HMAC-SHA256 签名，客户端回传 data + signature，服务端重新验签并检查时间戳，
 * 从而防止前端篡改价格。
 *
 * @author 谢光湘
 */
@Component
public class SignaturePriceUtils {

    @Value("${sign.price-secret-key}")
    private String secretKey;

    // 签名有效期（5分钟）
    private static final long SIGN_EXPIRE_MS = 5 * 60 * 1000;

    /**
     * 生成价格数据及签名
     *
     * @param roomId     房型 ID
     * @param checkIn    入住日期
     * @param checkOut   离店日期
     * @param nightNum   晚数
     * @param roomCount  房间数
     * @param totalPrice 总价
     * @return 含 data（原始串）与 signature（签名）的键值对
     */
    public Map<String, String> generatePriceWithSignature(String roomId, String checkIn, String checkOut, long nightNum, int roomCount, double totalPrice) {
        // 1. 构建包含业务上下文的数据
        long timestamp = System.currentTimeMillis();
        String data = String.format("roomId=%s&checkIn=%s&checkOut=%s&nightNum=%d&roomCount=%d&totalPrice=%.2f&timestamp=%d", roomId, checkIn, checkOut, nightNum, roomCount, totalPrice, timestamp);

        // 2. 生成HMAC-SHA256签名
        String signature = generateHmacSHA256(data);

        // 3. 准备返回给前端的数据
        Map<String, String> result = new HashMap<>();
        result.put("data", data);
        result.put("signature", signature);
        return result;
    }

    /**
     * 验证前端传递的签名
     *
     * <p>依次校验：参数非空 → 签名匹配 → 时间戳在 5 分钟有效期内。
     *
     * @param data      被签名的原始串
     * @param signature 待校验签名
     * @return 校验通过返回 true
     */
    public boolean verifyPriceSignature(String data, String signature) {
        // 1. 验证签名格式
        if (data == null || signature == null || data.isEmpty() || signature.isEmpty()) {
            return false;
        }

        // 2. 验证签名是否匹配
        String computedSignature = generateHmacSHA256(data);
        if (!computedSignature.equals(signature)) {
            return false;
        }

        // 3. 解析时间戳，验证是否过期
        try {
            String[] params = data.split("&");
            long timestamp = 0;
            for (String param : params) {
                if (param.startsWith("timestamp=")) {
                    timestamp = Long.parseLong(param.split("=")[1]);
                    break;
                }
            }
            // 检查是否在有效期内
            return System.currentTimeMillis() - timestamp <= SIGN_EXPIRE_MS;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 使用HMAC-SHA256生成签名
     *
     * @param data 待签名内容
     * @return Base64 编码的签名
     */
    private String generateHmacSHA256(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("签名生成失败", e);
        }
    }

    /**
     * 解析参数字符串为Map
     * @param data 参数字符串，格式为key1=value1&key2=value2...
     * @return 参数键值对Map
     */
    public Map<String, String> parseParams(String data) {
        Map<String, String> params = new HashMap<>();
        if (data == null || data.isEmpty()) {
            return params;
        }

        String[] keyValuePairs = data.split("&");
        for (String pair : keyValuePairs) {
            // 只分割第一个=，避免值中包含=的情况
            int equalsIndex = pair.indexOf('=');
            if (equalsIndex > 0 && equalsIndex < pair.length() - 1) {
                String key = pair.substring(0, equalsIndex);
                String value = pair.substring(equalsIndex + 1);
                params.put(key, value);
            }
        }
        return params;
    }
}
