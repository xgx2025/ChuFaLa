package com.hope.controller;


import com.hope.domain.entity.PlanHistory;
import com.hope.domain.vo.Result;
import com.hope.domain.vo.TravelItinerary;
import com.hope.service.IPlanHistoryService;
import com.hope.utils.ThreadLocalUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/agent")
public class AgentController {
    @Resource(name = "zhipu")
    private ChatClient chatClient;
    @Resource
    private IPlanHistoryService planHistoryService;
    @PostMapping("/chat")
    public Result chat(@RequestBody String message) {
        System.out.println(message);
        Claims claims = ThreadLocalUtil.get();
        Long userId = claims.get("userId", Long.class);
        System.out.println(userId);
        String conversationId = String.valueOf(userId);

        //发送请求返回响应
        TravelItinerary response =  chatClient
                .prompt()
                .user(message)
                .advisors(chatMemoryAdvisor->chatMemoryAdvisor.param(ChatMemory.CONVERSATION_ID,conversationId))
                .call()
                .entity(TravelItinerary.class);

        return Result.ok(response);
    }

    @GetMapping("/history")
    public Result history() {
        Claims claims = ThreadLocalUtil.get();
        Long userId = claims.get("userId", Long.class);
        List<PlanHistory> history = planHistoryService.queryHistory(userId);
        return Result.ok("");

    }
}
