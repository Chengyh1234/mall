package com.cyh.malluser.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

/**
 * 文件服务接口
 */
public interface FileService {

    /**
     * 上传文件
     */
    Map<String, String> uploadFile(MultipartFile file, String subDir);

    /**
     * 上传图片（带图片类型校验）
     */
    Map<String, String> uploadImage(MultipartFile file, String subDir);

    /**
     * 删除文件
     */
    boolean deleteFile(String relativePath, String subDir);

    /**
     * 下载文件
     */
    File getFile(String relativePath, String subDir);

    /**
     * 获取文件的访问URL
     */
    String getFileUrl(String relativePath, String subDir);

    /**
     * 检查文件是否为图片
     */
    boolean isImage(MultipartFile file);

    /**
     * 获取文件扩展名
     */
    String getFileExtension(String filename);
}