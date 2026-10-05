package com.hope.chufala.common.exception.user;

/**
 * 上传失败异常。
 *
 * <p>头像等文件上传失败（文件为空、OSS 返回空地址）时抛出。
 *
 * @author 谢光湘
 */
public class UploadFailException extends RuntimeException {
    public UploadFailException(String message) {
        super(message);
    }
}
