package com.hope.chufala.agent.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * 时间工具。
 *
 * @author 谢光湘
 */
@Component
public class TimeTools {

    /**
     * 获取当前真实时间。
     *
     * @return 当前时间的字符串表示
     */
    @Tool(description = "获取当前真实时间")
    public String getCurrentTime() {
        return java.time.LocalDateTime.now().toString();
    }
}
