package com.hope.chufala.service;

import com.hope.chufala.model.entity.UploadedFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * AI 对话附件服务。
 *
 * @author 谢光湘
 */
public interface IAiService {

    /**
     * 上传对话附件。
     *
     * @param files  上传的文件数组
     * @param userId 用户 ID
     * @return 文件 ID 列表
     */
    List<String> uploadChatFile(MultipartFile[] files, Long userId);

    /**
     * 按 ID 批量查询附件（校验归属）。
     *
     * @param fileIds 文件 ID 列表
     * @param userId  用户 ID
     * @return 文件记录列表
     */
    List<UploadedFile> getFilesByIds(List<String> fileIds, Long userId);
}
