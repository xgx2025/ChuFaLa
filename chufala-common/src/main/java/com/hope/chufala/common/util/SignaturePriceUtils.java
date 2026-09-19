package com.hope.chufala.common.util;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public final class SignaturePriceUtils {
    // 对称密钥，通过环境变量注入，不再硬编码在源码中
    private static final String SECRET_KEY = requireEnv("PRICE_SIGN_SECRET_KEY");
    // 签名有效期（10分钟）
    private static final long SIGN_EXPIRE_MS = 5 * 60 * 1000;

    /**
     * 读取必需的环境变量，缺失时立即失败，避免回退到弱默认密钥
     */
    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                    "缺少必需的环境变量 [" + name + "]，请参考 README 配置后重试");
        }
        return value;
    }

    /**
     * 生成价格数据及签名
     */
    public static Map<String, String> generatePriceWithSignature(String roomId, String checkIn,String checkOut, long nightNum,int roomCount,double totalPrice) {
        // 1. 构建包含业务上下文的数据
        long timestamp = System.currentTimeMillis();
        String data = String.format("roomId=%s&checkIn=%s&checkOut=%s&nightNum=%d&roomCount=%d&totalPrice=%.2f&timestamp=%d",roomId, checkIn,checkOut,nightNum,roomCount, totalPrice, timestamp);

        // 2. 生成HMAC-SHA256签名
        String signature = generateHmacSHA256(data);

        // 3. 准备返回给前端的数据
      Map<String,String> result =new HashMap<>();
      result.put("data", data);
      result.put("signature", signature);
       return result;
    }

    /**
     * 验证前端传递的签名
     */
    public static boolean verifyPriceSignature(String data, String signature) {
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
     */
    private static String generateHmacSHA256(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(SignaturePriceUtils.SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
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
    public static Map<String, String> parseParams(String data) {
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
