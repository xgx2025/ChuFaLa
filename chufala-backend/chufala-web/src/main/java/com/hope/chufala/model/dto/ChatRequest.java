package com.hope.chufala.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ChatRequest {
    private String message;
    private String conversationId;
    private String model;
    private String planId;
    private List<String> fileIds;
}
