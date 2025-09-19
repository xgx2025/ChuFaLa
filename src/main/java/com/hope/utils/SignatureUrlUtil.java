package com.hope.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap; // 用于对参数名排序，保证签名一致性

/**
 * URL签名生成与验证工具类
 * 用于防止URL参数被篡改（例如：订单ID、用户ID等）
 */
public final class SignatureUrlUtil {

    // 建议将密钥存储在配置中心（如Apollo, Nacos）或环境变量中，硬编码在代码中不安全。
    private static final String DEFAULT_SECRET_KEY = "chufala-key";
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final String SIGNATURE_PARAM_NAME = "signature"; // 签名参数名

    /**
     * 生成带签名的URL
     *
     * @param baseUrl    基础URL，不包含查询参数（e.g., "https://hotels.ctrip.com/hotels/ctorderdetail"）
     * @param params     需要添加到URL中的查询参数Map
     * @param secretKey  加密密钥（如果为空，则使用默认密钥，生产环境建议必传）
     * @return           返回携带签名参数的完整URL
     * @throws Exception 如果加密算法不支持或密钥无效
     */
    public static String generateSignatureUrl(String baseUrl, Map<String, String> params, String secretKey) throws Exception {
        if (secretKey == null || secretKey.isEmpty()) {
            secretKey = DEFAULT_SECRET_KEY;
        }

        // 1. 将参数按Key的字典顺序排序，并构建待签名的字符串
        TreeMap<String, String> sortedParams = new TreeMap<>(params);
        String queryString = buildQueryString(sortedParams, false);
        String stringToSign = baseUrl + "?" + queryString; // 构建待签名字符串

        // 2. 生成签名
        String signature = generateSignature(stringToSign, secretKey);

        // 3. 将签名作为一个新的参数，并重新构建最终的查询字符串
        sortedParams.put(SIGNATURE_PARAM_NAME, signature);
        String finalQueryString = buildQueryString(sortedParams, true);

        // 4. 组合最终URL
        return baseUrl + "?" + finalQueryString;
    }

    /**
     * 验证请求URL中的签名是否有效
     *
     * @param receivedUrl 接收到的完整URL
     * @param secretKey   加密密钥（必须与生成时使用的密钥一致）
     * @return true表示签名有效，false表示无效或解析失败
     */
    public static boolean validateSignature(String receivedUrl, String secretKey) {
        try {
            if (secretKey == null || secretKey.isEmpty()) {
                secretKey = DEFAULT_SECRET_KEY;
            }

            URI uri = new URI(receivedUrl);
            String query = uri.getQuery();
            if (query == null || query.isEmpty()) {
                return false;
            }

            // 1. 解析URL中的查询参数
            String[] pairs = query.split("&");
            TreeMap<String, String> receivedParams = new TreeMap<>();
            String receivedSignature = null;

            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                String key = (idx > 0) ? URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name()) : pair;
                String value = (idx > 0 && pair.length() > idx + 1) ? URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name()) : "";

                if (SIGNATURE_PARAM_NAME.equals(key)) {
                    receivedSignature = value; // 提取出接收到的签名
                } else {
                    receivedParams.put(key, value); // 存储其他参数
                }
            }

            if (receivedSignature == null) {
                return false; // 根本没有签名参数
            }

            // 2. 使用接收到的参数（不包含签名参数）重新构建待签名字符串
            String queryStringWithoutSignature = buildQueryString(receivedParams, false);
            String baseUrl = uri.getScheme() + "://" + uri.getHost() + uri.getPath(); // 重构baseUrl
            String stringToSign = baseUrl + "?" + queryStringWithoutSignature;

            // 3. 根据收到的参数本地重新计算签名
            String calculatedSignature = generateSignature(stringToSign, secretKey);

            // 4. 比较计算出的签名和接收到的签名是否一致
            return calculatedSignature.equals(receivedSignature);

        } catch (Exception e) {
            // 日志记录验证过程中出现的异常
            // e.printStackTrace(); // 生产环境应使用日志框架如SLF4J
            return false;
        }
    }

    /**
     * 核心方法：使用HMAC-SHA256生成签名
     *
     * @param data      待签名的数据
     * @param secretKey 密钥
     * @return Base64编码后的签名字符串
     */
    private static String generateSignature(String data, String secretKey) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        mac.init(secretKeySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(rawHmac);
        // 注意：原URL中的签名看起来像是Base64 URL Safe编码，如果需要，可以使用`Base64.getUrlEncoder`
        // return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
    }

    /**
     * 将Map构建成查询字符串
     *
     * @param params           参数Map
     * @param encodeValue      是否对值进行URL编码
     * @return                 构建好的查询字符串
     */
    private static String buildQueryString(Map<String, String> params, boolean encodeValue) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            String value = entry.getValue();
            if (encodeValue) {
                // 对键和值都进行URL编码是关键一步，确保特殊字符正确处理
                sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()))
                        .append("=")
                        .append(URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8.name()));
            } else {
                // 构建待签名字符串时，我们不进行URL编码，或者使用一种规范化的编码方式。
                // 这里为了简单，不编码。但更健壮的做法是使用百分号编码（URLEncoder）后再签名。
                sb.append(entry.getKey())
                        .append("=")
                        .append(value != null ? value : "");
            }
        }
        return sb.toString();
    }

    // 提供一个简便方法，使用默认密钥生成URL
    public static String generateSignatureUrl(String baseUrl, Map<String, String> params) throws Exception {
        return generateSignatureUrl(baseUrl, params, null);
    }

    // 提供一个简便方法，使用默认密钥验证签名
    public static boolean validateSignature(String receivedUrl) {
        return validateSignature(receivedUrl, null);
    }
}
