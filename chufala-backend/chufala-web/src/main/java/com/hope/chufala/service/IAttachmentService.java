package com.hope.chufala.service;

import com.hope.chufala.model.entity.UploadedFile;

/**
 * 会话附件关联服务。
 *
 * @author 谢光湘
 */
public interface IAttachmentService {
    /**
     * 保存附件与消息的关联记录。
     *
     * @param messageId 消息 ID
     * @param file      已上传的文件记录
     */
    void saveAttachmentInfo(Long messageId, UploadedFile file);
}
