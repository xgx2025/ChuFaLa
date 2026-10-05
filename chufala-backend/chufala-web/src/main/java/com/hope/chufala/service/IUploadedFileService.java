package com.hope.chufala.service;

import com.hope.chufala.model.entity.UploadedFile;

import java.util.List;

/**
 * 上传文件记录服务。
 *
 * @author 谢光湘
 */
public interface IUploadedFileService {

    /**
     * 保存多个上传文件的信息（先入库暂存，配合「两阶段提交 + 文件暂存区」架构）。
     *
     * @param files 上传的文件记录列表
     */
    void saveUploadedFileInfo(List<UploadedFile> files);
}
