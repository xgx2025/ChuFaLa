package com.hope.chufala.service;

import com.hope.chufala.domain.entity.UploadedFile;

public interface IAttachmentService {
    void saveAttachmentInfo(Long messageId, UploadedFile file);
}
