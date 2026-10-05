package com.hope.chufala.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hope.chufala.common.util.AliOSSUtils;
import com.hope.chufala.model.entity.UploadedFile;
import com.hope.chufala.common.exception.ResourceNotFoundException;
import com.hope.chufala.mapper.UploadedFileMapper;
import com.hope.chufala.service.IAiService;
import com.hope.chufala.service.IUploadedFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AI 对话附件服务实现。
 *
 * <p>上传采用「先传 OSS、再落库暂存」两阶段；归属关系写入 Redis
 * （chat:upload:owner:{fileId} → userId，TTL 1 小时），读取时据此校验，
 * 避免仅凭文件 ID 就能越权读取他人附件。
 *
 * @author 谢光湘
 */
@Slf4j
@Service
public class AiServiceImpl implements IAiService {
    /** 附件归属关系的 Redis 前缀 */
    private static final String UPLOAD_OWNER_KEY_PREFIX = "chat:upload:owner:";
    @Autowired
    private IUploadedFileService uploadedFileService;
    @Autowired
    private AliOSSUtils aliOSSUtils;
    @Autowired
    private UploadedFileMapper uploadedFileMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;



    /**
     * 上传对话附件（最多 3 个），写入 OSS 与暂存表并登记归属。
     *
     * @param files  上传的文件数组
     * @param userId 上传用户 ID
     * @return 文件 ID 列表（字符串）
     */
    @Override
    public List<String> uploadChatFile(MultipartFile[] files, Long userId) {
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
        for (UploadedFile uploadedFile : uploadedFiles) {
            stringRedisTemplate.opsForValue().set(UPLOAD_OWNER_KEY_PREFIX + uploadedFile.getId(),
                    userId.toString(), 1, TimeUnit.HOURS);
        }
        log.info("结果{}",uploadedFiles);
        //取出文件列表中的文件ID并返回
        List<Long> fileIds = uploadedFiles.stream().map(UploadedFile::getId).toList();
        return fileIds.stream().map(String::valueOf).toList();
    }

    /**
     * 按 ID 批量查询附件，逐个校验归属。
     *
     * @param fileIds 文件 ID 列表
     * @param userId  用户 ID
     * @return 文件记录列表
     */
    @Override
    public List<UploadedFile> getFilesByIds(List<String> fileIds, Long userId) {
        List<UploadedFile> fileUrls = new ArrayList<>();
        for (String fileId : fileIds) {
            if (!userId.toString().equals(stringRedisTemplate.opsForValue().get(UPLOAD_OWNER_KEY_PREFIX + fileId))) {
                throw new ResourceNotFoundException("上传文件不存在");
            }
            // 每次循环新建 QueryWrapper，避免条件在多次查询之间累积
            QueryWrapper<UploadedFile> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id", Long.valueOf(fileId));
            UploadedFile uploadedFile = uploadedFileMapper.selectOne(queryWrapper);
            if (uploadedFile == null) {
                throw new ResourceNotFoundException("上传文件不存在");
            }
            fileUrls.add(uploadedFile);
        }
        return fileUrls;
    }
}
