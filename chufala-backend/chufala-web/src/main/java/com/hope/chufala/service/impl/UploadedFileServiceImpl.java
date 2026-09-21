package com.hope.chufala.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hope.chufala.model.entity.UploadedFile;
import com.hope.chufala.mapper.UploadedFileMapper;
import com.hope.chufala.service.IUploadedFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class UploadedFileServiceImpl implements IUploadedFileService {
    @Autowired
    private UploadedFileMapper uploadedFileMapper;
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
