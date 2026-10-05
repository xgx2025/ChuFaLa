package com.hope.chufala.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.hope.chufala.model.entity.Attachment;
import com.hope.chufala.model.entity.UploadedFile;
import com.hope.chufala.mapper.AttachmentMapper;
import com.hope.chufala.service.IAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 会话附件关联服务实现。
 *
 * <p>把已上传文件记录复制一份挂到具体消息上，形成「消息 → 附件」的关联。
 *
 * @author 谢光湘
 */
@Service
public class AttachmentServiceImpl implements IAttachmentService {
    @Autowired
    private AttachmentMapper attachmentMapper;

    /**
     * 保存附件与消息的关联记录（ID 由 Snowflake 生成）。
     *
     * @param messageId 消息 ID
     * @param file      已上传的文件记录
     */
    @Override
    public void saveAttachmentInfo(Long messageId, UploadedFile file) {
        Snowflake snowflake = new Snowflake(1, 1);
        Attachment attachment = new Attachment();
        attachment.setId(snowflake.nextId());
        attachment.setMessageId(messageId);
        attachment.setFileName(file.getFileName());
        attachment.setFileType(file.getFileType());
        attachment.setFileUrl(file.getFileUrl());
        attachment.setFileSize(file.getFileSize());
        attachmentMapper.insert(attachment);
    }
}
