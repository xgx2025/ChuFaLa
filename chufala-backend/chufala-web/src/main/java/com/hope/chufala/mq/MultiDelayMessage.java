package com.hope.chufala.mq;

import cn.hutool.core.collection.CollUtil;
import lombok.Data;


import java.util.List;

/**
 * 多级延迟消息。
 *
 * <p>把一串递增的延迟时长打包进消息体，每次消费弹出下一个时长重新投递，
 * 从而用固定 TTL 的延迟队列模拟阶梯式延迟（用于订单超时的逐级检查）。
 *
 * @author 谢光湘
 */
@Data
public class MultiDelayMessage<T> {
    /**
     * 消息体
     */
    private T data;
    /** 剩余待投递的延迟时长（毫秒），按顺序消费 */
    private List<Long> delayMillis;

    /**
     * 记录延迟时间的集合
     */
    public MultiDelayMessage(T data, List<Long> delayMillis) {
        this.data = data;
        this.delayMillis = delayMillis;
    }

    /**
     * 构造多级延迟消息。
     *
     * @param data        消息体
     * @param delayMillis 延迟时长序列（毫秒）
     * @return 多级延迟消息
     */
    public static <T> MultiDelayMessage<T> of(T data,Long...delayMillis){
        return new MultiDelayMessage<T> (data, CollUtil.newArrayList(delayMillis));
    }
    /**
     * 获取并移除下一个延迟时间
     *
     * @return 下一个延迟时长（毫秒）
     */
    public Long removeNextDelay(){
        return delayMillis.remove(0);
    }
    /**
     * 是否还有下一个延迟时间
     *
     * @return 还有则返回 true
     */
    public boolean hasNextDelay(){
        return !delayMillis.isEmpty();
    }


}
