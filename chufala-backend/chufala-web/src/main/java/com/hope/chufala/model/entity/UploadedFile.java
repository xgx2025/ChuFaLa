package com.hope.chufala.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.hope.chufala.model.vo.PointVO;
import com.hope.chufala.model.dto.PayParamDTO;
import com.hope.chufala.infra.RedisData;

@Data
@NoArgsConstructor
@TableName("uploaded_file")
public class UploadedFile {
    private Long id;
    private String fileType;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
}
