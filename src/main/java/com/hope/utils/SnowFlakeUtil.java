package com.hope.utils;

/**
 * 雪花算法ID生成器静态方法版
 * 特点：
 * 1. 使用静态方法实现
 * 2. 内置默认workerId和dataCenterId
 * 3. 线程安全
 * 4. 处理时钟回拨问题
 */
public class SnowFlakeUtil {
    // 起始时间戳 (2020-01-01 00:00:00)
    private static final long START_TIMESTAMP = 1577808000000L;

    // 机器ID位数
    private static final long WORKER_ID_BITS = 5L;
    // 数据中心ID位数
    private static final long DATA_CENTER_ID_BITS = 5L;
    // 序列号位数
    private static final long SEQUENCE_BITS = 12L;

    // 最大机器ID (2^5-1 = 31)
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    // 最大数据中心ID (2^5-1 = 31)
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);

    // 机器ID左移位数 (12)
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    // 数据中心ID左移位数 (12+5=17)
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    // 时间戳左移位数 (12+5+5=22)
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    // 序列号掩码 (4095)
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    // 默认使用机器ID和数据中心ID(可以改为从配置读取)
    private static final long DEFAULT_WORKER_ID = 1L;
    private static final long DEFAULT_DATA_CENTER_ID = 1L;

    // 序列号
    private static long sequence = 0L;
    // 上次生成ID的时间戳
    private static long lastTimestamp = -1L;

    // 私有构造器防止实例化
    private SnowFlakeUtil() {
    }

    /**
     * 生成下一个ID(使用默认workerId和dataCenterId)
     * @return 唯一ID
     * @throws RuntimeException 时钟回拨时抛出异常
     */
    public static synchronized long nextId() {
        return nextId(DEFAULT_WORKER_ID, DEFAULT_DATA_CENTER_ID);
    }

    /**
     * 生成下一个ID
     * @param workerId 机器ID (0-31)
     * @param dataCenterId 数据中心ID (0-31)
     * @return 唯一ID
     * @throws RuntimeException 时钟回拨时抛出异常
     */
    public static synchronized long nextId(long workerId, long dataCenterId) {
        // 参数校验
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("dataCenter Id can't be greater than %d or less than 0", MAX_DATA_CENTER_ID));
        }

        long timestamp = timeGen();

        // 处理时钟回拨
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
                            lastTimestamp - timestamp));
        }

        // 同一毫秒内生成多个ID
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) { // 当前毫秒序列号用完，等待下一毫秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else { // 新毫秒开始，序列号重置
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 组装ID
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT_SHIFT)
                | (dataCenterId << DATA_CENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 阻塞到下一毫秒
     * @param lastTimestamp 上次生成ID的时间戳
     * @return 当前时间戳
     */
    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获取当前时间戳
     * @return 当前时间(毫秒)
     */
    private static long timeGen() {
        return System.currentTimeMillis();
    }
}

