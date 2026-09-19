package com.hope.chufala.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hope.chufala.common.util.AliOSSUtils;
import com.hope.chufala.domain.entity.UploadedFile;
import com.hope.chufala.mapper.UploadedFileMapper;
import com.hope.chufala.service.IAiService;
import com.hope.chufala.service.IUploadedFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AiServiceImpl implements IAiService {
    @Autowired
    private IUploadedFileService uploadedFileService;
    @Autowired
    private AliOSSUtils aliOSSUtils;
    @Autowired
    private UploadedFileMapper uploadedFileMapper;



    @Override
    public List<String> uploadChatFile(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (files.length > 3) {
            throw new IllegalArgumentException("一次最多上传3个文件");
        }
        List<UploadedFile> uploadedFiles = new ArrayList<>();
        Snowflake snowflake = new Snowflake(1, 1);
        try {
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;
                String fileName = file.getOriginalFilename();
                String fileUrl = aliOSSUtils.upload(file);
                UploadedFile uploadedFile = new UploadedFile();
                //TODO: 目前只支持上传图片，后续可以根据需求扩展
                uploadedFile.setId(snowflake.nextId());
                uploadedFile.setFileType("image");
                uploadedFile.setFileName(fileName);
                uploadedFile.setFileUrl(fileUrl);
                uploadedFiles.add(uploadedFile);
            }
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
        uploadedFileService.saveUploadedFileInfo(uploadedFiles);
        log.info("结果{}",uploadedFiles);
        //取出文件列表中的文件ID并返回
        List<Long> fileIds = uploadedFiles.stream().map(UploadedFile::getId).toList();
        return fileIds.stream().map(String::valueOf).toList();
    }

    @Override
    public List<UploadedFile> getFilesByIds(List<String> fileIds) {
        List<UploadedFile> fileUrls = new ArrayList<>();
        QueryWrapper<UploadedFile> queryWrapper = new QueryWrapper<>();
        for (String fileId : fileIds) {
            queryWrapper.eq("id", Long.valueOf(fileId));
            UploadedFile uploadedFile = uploadedFileMapper.selectOne(queryWrapper);
            if (uploadedFile != null) {
                fileUrls.add(uploadedFile);
            }
        }
        return fileUrls;
    }
}
