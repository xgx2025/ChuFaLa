package com.hope.chufala.agent;

import lombok.Getter;

/**
 * 行程规划任务状态。
 *
 * @author 谢光湘
 */
@Getter
public enum TaskStatus {

    /** 待处理 */
    PENDING("待处理"),
    /** 处理中 */
    PROCESSING("处理中"),
    /** 已完成 */
    COMPLETED("已完成"),
    /** 处理失败 */
    FAILED("处理失败");
    /** 状态的中文描述 */
    private final String message;

    TaskStatus (String message){
        this.message = message;
    }

}
