package com.hope.chufala.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import com.hope.chufala.model.vo.PointVO;
import com.hope.chufala.model.dto.PayParamDTO;
import com.hope.chufala.infra.RedisData;

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
