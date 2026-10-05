package com.hope.chufala.common.util;

/**
 * 线程级上下文工具。
 *
 * <p>LoginInterceptor 解析 JWT 后把 claims 存入此 ThreadLocal，业务代码通过
 * {@link #get()} 取当前登录用户信息；请求结束时必须调用 {@link #remove()}，
 * 否则线程池复用会造成身份串号。
 *
 * @author 谢光湘
 */
public class ThreadLocalUtils {
    private static final ThreadLocal<Object> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 取当前线程上下文。
     *
     * @param <T> 期望类型
     * @return 上下文对象，未设置时为 null
     */
    public static <T> T get(){
        return (T) THREAD_LOCAL.get();
    }

    /**
     * 设置当前线程上下文。
     *
     * @param value 上下文对象（通常为 JWT claims）
     */
    public static  void set(Object value){
        THREAD_LOCAL.set(value);
    }

    /**
     * 清除当前线程上下文（请求结束时必须调用）。
     */
    public static void remove(){
        THREAD_LOCAL.remove();
    }
}
