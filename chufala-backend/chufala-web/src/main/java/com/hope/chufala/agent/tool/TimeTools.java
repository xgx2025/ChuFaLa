package com.hope.chufala.agent.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class TimeTools {

    @Tool(description = "获取当前真实时间")
    public String getCurrentTime() {
        return java.time.LocalDateTime.now().toString();
    }
}
