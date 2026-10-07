package com.hope.chufala.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * 异步线程池配置。
 *
 * <p>显式定义两个互相隔离的线程池：mailExecutor（邮件）与 planExecutor（行程规划）。
 * 必须隔离：否则在容器内只有一个 TaskExecutor 时，未指定名字的 @Async 会复用它，
 * 分钟级的规划任务会挤占发信线程。
 *
 * @author 谢光湘
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    /**
     * 邮件发送专用线程池。
     *
     * @return 线程池
     */
    @Bean("mailExecutor")
    public Executor mailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);          // 核心线程数
        executor.setMaxPoolSize(10);          // 最大线程数
        executor.setQueueCapacity(50);        // 队列容量
        executor.setThreadNamePrefix("mail-async-"); // 线程名前缀
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 拒绝策略
        executor.initialize(); // 必须调用
        return executor;
    }

    /**
     * AI 行程规划专用线程池
     *
     * 规划任务单次耗时可达分钟级，且内部会并发调用大模型与第三方接口。
     * 必须与邮件线程池隔离：否则在未指定线程池时，@Async 会因容器内
     * 只有一个 TaskExecutor 而默认复用 mailExecutor，长任务会挤占发信线程。
     *
     * @return 线程池
     */
    @Bean("planExecutor")
    public Executor planExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("plan-async-");
        // 规划任务耗时较长，满载时不能回退到提交请求的线程执行。
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }
}
