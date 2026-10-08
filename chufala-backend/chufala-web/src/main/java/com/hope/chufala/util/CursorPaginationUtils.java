package com.hope.chufala.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

/** 列表游标编解码与页大小校验。 */
public final class CursorPaginationUtils {
    public static final int MAX_PAGE_SIZE = 50;

    private CursorPaginationUtils() { }

    /**
     * 校验页大小，未传时使用默认值。
     *
     * @param size 请求的页大小
     * @param defaultSize 未传 size 时的默认值
     * @return 1 到 50 之间的页大小
     * @throws IllegalArgumentException 页大小超出范围时抛出
     */
    public static int size(Integer size, int defaultSize) {
        int value = size == null ? defaultSize : size;
        if (value < 1 || value > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size 必须在 1 到 " + MAX_PAGE_SIZE + " 之间");
        }
        return value;
    }

    /**
     * 将筛选条件生成稳定摘要，供游标校验使用。
     *
     * <p>调用方应以固定顺序传入条件；集合条件需先排序。距离排序还需传入用户坐标。
     *
     * @param values 决定列表范围的筛选值
     * @return 筛选条件摘要
     */
    public static String scope(Object... values) {
        StringBuilder canonical = new StringBuilder();
        for (Object value : values) {
            if (value == null) {
                canonical.append("N;");
            } else {
                String text = value.toString();
                canonical.append("S").append(text.length()).append(':').append(text).append(';');
            }
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.toString().getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest, 0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }

    /**
     * 将列表排序值与 ID 编码为旧版游标，用于兼容旧客户端的后续翻页。
     *
     * @param sort 排序方式
     * @param scope 筛选条件摘要
     * @param value 本页最后一条记录的排序原值，可为空
     * @param id 本页最后一条记录的 ID
     * @return URL 安全的游标
     */
    public static String encode(String sort, String scope, Double value, long id) {
        String raw = sort + ":" + scope + ":" + (value == null ? "null" : Double.toString(value)) + ":" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    /** 编码列表下一页游标；深度 4 表示第 4 页及以后，只用于热点页缓存判断。 */
    public static String encode(String sort, String scope, Double value, long id, int nextPage) {
        if (nextPage < 2 || nextPage > 4) throw new IllegalArgumentException("无效的分页深度");
        String raw = "v2:" + sort + ":" + scope + ":"
                + (value == null ? "null" : Double.toString(value)) + ":" + id + ":" + nextPage;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解码新旧版列表游标，并校验排序方式和筛选条件与当前请求一致。
     *
     * @param token 客户端传回的游标
     * @param expectedSort 当前请求的排序方式
     * @param expectedScope 当前请求的筛选条件摘要
     * @return 上一页最后一条记录的排序值和 ID
     * @throws IllegalArgumentException 游标格式无效或与当前请求不匹配时抛出
     */
    public static Cursor decode(String token, String expectedSort, String expectedScope) {
        if (token == null || token.length() > 256) {
            throw new IllegalArgumentException("无效的分页游标");
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = raw.split(":", -1);
            boolean current = parts.length == 6 && "v2".equals(parts[0]);
            boolean legacy = parts.length == 4;
            int offset = current ? 1 : 0;
            if ((!current && !legacy) || !expectedSort.equals(parts[offset])
                    || !expectedScope.equals(parts[offset + 1])) {
                throw new IllegalArgumentException("分页游标与排序条件不匹配");
            }
            Double value = "null".equals(parts[offset + 2]) ? null : Double.parseDouble(parts[offset + 2]);
            long id = Long.parseLong(parts[offset + 3]);
            int page = current ? Integer.parseInt(parts[5]) : 0;
            if ((value != null && !Double.isFinite(value)) || id < 1
                    || (current && (page < 2 || page > 4))) {
                throw new IllegalArgumentException("无效的分页游标");
            }
            return new Cursor(value, id, page);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("无效的分页游标", e);
        }
    }

    /** page 为 0 表示旧版游标，4 表示第 4 页及以后。 */
    public record Cursor(Double value, long id, int page) { }

    /**
     * 将订单下单时间与主键 ID 编码为下一页游标。
     *
     * @param scope 用户 ID 和订单状态的摘要
     * @param bookTime 本页最后一笔订单的下单时间
     * @param id 本页最后一笔订单的主键 ID
     * @return URL 安全的订单游标
     */
    public static String encodeOrder(String scope, LocalDateTime bookTime, long id) {
        String raw = "order|" + scope + "|" + bookTime + "|" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解码订单游标，并校验用户与订单状态范围。
     *
     * @param token 客户端传回的订单游标
     * @param expectedScope 当前用户 ID 和订单状态的摘要
     * @return 上一页最后一笔订单的下单时间和主键 ID
     * @throws IllegalArgumentException 游标格式无效或与当前请求不匹配时抛出
     */
    public static OrderCursor decodeOrder(String token, String expectedScope) {
        if (token == null || token.length() > 256) {
            throw new IllegalArgumentException("无效的订单分页游标");
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|", -1);
            if (parts.length != 4 || !"order".equals(parts[0]) || !expectedScope.equals(parts[1])) {
                throw new IllegalArgumentException("无效的订单分页游标");
            }
            LocalDateTime bookTime = LocalDateTime.parse(parts[2]);
            long id = Long.parseLong(parts[3]);
            if (id < 1) throw new IllegalArgumentException("无效的订单分页游标");
            return new OrderCursor(bookTime, id);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("无效的订单分页游标", e);
        }
    }

    public record OrderCursor(LocalDateTime bookTime, long id) { }
}
