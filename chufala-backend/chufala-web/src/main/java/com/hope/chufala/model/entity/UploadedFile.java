package com.hope.chufala.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 上传文件记录实体，对应 uploaded_file 表。
 *
 * <p>用于管理通用上传文件的生命周期：超过过期时间（expireTime）后可被清理。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
@TableName("uploaded_file")
public class UploadedFile {
    /** 自增主键 */
    private Long id;
    /** 文件类型 */
    private String fileType;
    /** 文件访问 URL */
    private String fileUrl;
    /** 原始文件名 */
    private String fileName;
    /** 文件大小（字节） */
    private Long fileSize;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 过期时间，过期后可被清理 */
    private LocalDateTime expireTime;
}
