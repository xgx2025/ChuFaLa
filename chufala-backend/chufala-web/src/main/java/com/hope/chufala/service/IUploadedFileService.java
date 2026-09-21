package com.hope.chufala.service;

import com.hope.chufala.domain.entity.UploadedFile;

import java.util.List;

public interface IUploadedFileService {

    /**
     * 保存多个上传文件的信息（存储到数据库中，实际上是暂存，采用 “两阶段提交 + 文件暂存区” 的架构）
     * @param files 上传的文件列表
     */
    void saveUploadedFileInfo(List<UploadedFile> files);
}
