package com.hope.chufala.domain.agent;

import lombok.Getter;

@Getter
public enum TaskStatus {

    PENDING("待处理"),
    PROCESSING("处理中"),
    COMPLETED("已完成"),
    FAILED("处理失败");
    private final String message;

    TaskStatus (String message){
        this.message = message;
    }

}
