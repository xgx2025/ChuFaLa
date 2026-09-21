package com.hope.chufala.service;

import com.hope.chufala.domain.entity.UploadedFile;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.util.List;

public interface IAiService {

    List<String> uploadChatFile(MultipartFile[] files);

    List<UploadedFile> getFilesByIds(List<String> fileIds);
}
