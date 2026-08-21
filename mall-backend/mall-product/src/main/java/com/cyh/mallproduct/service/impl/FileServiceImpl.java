package com.cyh.mallproduct.service.impl;

import com.cyh.mallcommon.constant.FileConstants;
import com.cyh.mallcommon.exception.BusinessException;
import com.cyh.mallproduct.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传服务实现类
 * 实现文件上传到本地磁盘、按日期目录归档、文件删除等功能
 * 支持图片类型校验，文件名使用 UUID 防重名
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload.base-path:./uploads}")
    private String basePath;

    /**
     * 通用文件上传，按日期（yyyy/MM/dd）归档，文件名使用 UUID 防重名
     * 返回包含 dateDir、fileName、relativePath 的映射
     */
    @Override
    public Map<String, String> uploadFile(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            log.warn("上传文件为空");
            return null;
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            log.warn("上传文件名为空");
            return null;
        }
        try {
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String newFileName = UUID.randomUUID().toString() + "_" + originalFilename;
            String targetPath = basePath + subDir;
            File uploadDir = new File(targetPath, dateDir);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            File destFile = new File(uploadDir, newFileName).getCanonicalFile();
            file.transferTo(destFile);

            Map<String, String> result = new HashMap<>();
            result.put("dateDir", dateDir);
            result.put("fileName", newFileName);
            result.put("relativePath", dateDir + "/" + newFileName);
            log.info("文件上传成功，原始文件名: {}, 新文件名: {}", originalFilename, newFileName);
            return result;
        } catch (IOException e) {
            log.error("文件上传失败，原始文件名: {}", originalFilename, e);
            throw new BusinessException("文件上传失败");
        }
    }

    /**
     * 图片上传，先校验文件是否为允许的图片类型（jpeg/png/gif/webp）
     */
    @Override
    public Map<String, String> uploadImage(MultipartFile file, String subDir) {
        if (!isImage(file)) {
            log.warn("文件不是有效的图片类型: {}", file.getContentType());
            return null;
        }
        return uploadFile(file, subDir);
    }

    /**
     * 删除指定文件，校验路径是否存在，避免删除不存在的文件
     */
    @Override
    public boolean deleteFile(String relativePath, String subDir) {
        if (!StringUtils.hasText(relativePath)) {
            log.warn("删除文件路径为空");
            return false;
        }
        String targetPath = basePath + subDir;
        File file = new File(targetPath, relativePath);
        if (!file.exists()) {
            log.warn("要删除的文件不存在: {}", file.getAbsolutePath());
            return false;
        }
        boolean deleted = file.delete();
        if (deleted) {
            log.info("文件删除成功: {}", file.getAbsolutePath());
        } else {
            log.warn("文件删除失败: {}", file.getAbsolutePath());
        }
        return deleted;
    }

    /**
     * 获取文件对象，仅返回存在的文件
     */
    @Override
    public File getFile(String relativePath, String subDir) {
        if (!StringUtils.hasText(relativePath)) {
            return null;
        }
        String targetPath = basePath + subDir;
        File file = new File(targetPath, relativePath);
        if (file.exists() && file.isFile()) {
            return file;
        }
        return null;
    }

    /**
     * 生成文件的访问 URL（含 URL 前缀和子目录）
     */
    @Override
    public String getFileUrl(String relativePath, String subDir) {
        if (!StringUtils.hasText(relativePath)) {
            return null;
        }
        return FileConstants.URL_PREFIX + subDir + "/" + relativePath;
    }

    /**
     * 校验文件是否为允许的图片类型，通过 Content-Type 判断
     */
    @Override
    public boolean isImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        return FileConstants.ALLOWED_IMAGE_TYPES.contains(contentType);
    }

    @Override
    public String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
}