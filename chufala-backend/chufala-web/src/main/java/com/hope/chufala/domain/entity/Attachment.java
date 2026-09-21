package com.hope.chufala.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Attachment {
    private Long id;
    private Long messageId;
    private String fileType;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private LocalDateTime createTime;

}
