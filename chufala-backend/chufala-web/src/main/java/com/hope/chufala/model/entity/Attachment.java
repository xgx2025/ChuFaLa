package com.hope.chufala.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话附件实体，对应 attachment 表。
 *
 * <p>记录挂在某条 AI 消息（messageId）下的上传文件，用于对话中回显附件。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
public class Attachment {
    /** 自增主键 */
    private Long id;
    /** 所属消息 ID */
    private Long messageId;
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

}
