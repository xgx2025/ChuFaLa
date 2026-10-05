package com.hope.chufala.service.impl;

import com.hope.chufala.model.entity.UploadedFile;
import com.hope.chufala.mapper.UploadedFileMapper;
import com.hope.chufala.service.IUploadedFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 上传文件记录服务实现。
 *
 * <p>落库时统一设置创建时间与过期时间（1 小时后），过期文件可由清理任务回收；
 * 单条写入失败只记日志、不中断整批。
 *
 * @author 谢光湘
 */
@Slf4j
@Service
public class UploadedFileServiceImpl implements IUploadedFileService {
    @Autowired
    private UploadedFileMapper uploadedFileMapper;
    /**
     * 批量保存上传文件信息。
     *
     * @param files 上传的文件记录列表
     */
    @Override
    public void saveUploadedFileInfo(List<UploadedFile> files) {
        LocalDateTime createTime = LocalDateTime.now();
        LocalDateTime expireTime = createTime.plusHours(1);
        for (UploadedFile file : files) {
            try {
                file.setCreateTime(createTime);
                file.setExpireTime(expireTime);
                uploadedFileMapper.insert(file);
            } catch (Exception e) {
                log.error("保存上传文件信息失败，文件ID：{},文件名称：{}", file.getId(),file.getFileName(), e);
            }
        }
    }
}
