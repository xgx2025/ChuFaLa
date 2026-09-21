package com.hope.chufala.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

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
