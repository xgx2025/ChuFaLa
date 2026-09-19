package com.hope.chufala.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.hope.chufala.domain.entity.Attachment;
import com.hope.chufala.domain.entity.UploadedFile;
import com.hope.chufala.mapper.AttachmentMapper;
import com.hope.chufala.service.IAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AttachmentServiceImpl implements IAttachmentService {
    @Autowired
    private AttachmentMapper attachmentMapper;

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
