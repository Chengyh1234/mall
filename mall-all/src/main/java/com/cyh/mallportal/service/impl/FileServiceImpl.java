package com.cyh.mallportal.service.impl;

import com.cyh.mallportal.service.FileService;
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
 * 文件服务实现类
 * 实现文件的上传、下载、删除等公共功能
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    /**
     * 文件上传根路径
     */
    @Value("${file.upload.base-path:./uploads}")
    private String basePath;

    /**
     * 允许的图片类型
     */
    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};

    /**
     * 上传文件
     *
     * @param file 上传的文件
     * @param subDir 子目录路径（如 "images"、"logos"）
     * @return 上传结果，包含日期和文件名，失败返回null
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
            // 生成日期目录
            LocalDate today = LocalDate.now();
            String dateStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String dateDir = today.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

            // 生成新文件名：UUID_原文件名
            String newFileName = UUID.randomUUID().toString() + "_" + originalFilename;

            // 创建上传目录
            File uploadDir = new File(basePath, subDir + "/" + dateDir);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 保存文件
            File destFile = new File(uploadDir, newFileName).getCanonicalFile();
            file.transferTo(destFile);

            // 构建返回结果
            Map<String, String> result = new HashMap<>();
            result.put("date", dateStr);
            result.put("dateDir", dateDir);
            result.put("fileName", newFileName);
            result.put("relativePath", dateDir + "/" + newFileName);

            log.info("文件上传成功，原始文件名: {}, 新文件名: {}, 路径: {}", originalFilename, newFileName, subDir + "/" + dateDir);

            return result;
        } catch (IOException e) {
            log.error("文件上传失败，原始文件名: {}", originalFilename, e);
            return null;
        }
    }

    /**
     * 上传图片（带图片类型校验）
     *
     * @param file 上传的图片文件
     * @param subDir 子目录路径（如 "images"、"logos"）
     * @return 上传结果，包含日期和文件名，失败返回null
     */
    @Override
    public Map<String, String> uploadImage(MultipartFile file, String subDir) {
        // 校验是否为图片
        if (!isImage(file)) {
            log.warn("文件不是有效的图片类型: {}", file.getContentType());
            return null;
        }
        return uploadFile(file, subDir);
    }

    /**
     * 删除文件
     *
     * @param relativePath 文件相对路径（如 "2026/05/07/uuid_file.jpg"）
     * @param subDir 子目录路径（如 "images"、"logos"）
     * @return 删除成功返回true，失败返回false
     */
    @Override
    public boolean deleteFile(String relativePath, String subDir) {
        if (!StringUtils.hasText(relativePath)) {
            log.warn("删除文件路径为空");
            return false;
        }

        try {
            // 构建完整文件路径
            File file = new File(basePath, subDir + "/" + relativePath);

            // 检查文件是否存在
            if (!file.exists()) {
                log.warn("要删除的文件不存在: {}", file.getAbsolutePath());
                return false;
            }

            // 删除文件
            boolean deleted = file.delete();
            if (deleted) {
                log.info("文件删除成功: {}", file.getAbsolutePath());
            } else {
                log.warn("文件删除失败: {}", file.getAbsolutePath());
            }
            return deleted;
        } catch (Exception e) {
            log.error("删除文件异常，路径: {}", relativePath, e);
            return false;
        }
    }

    /**
     * 下载文件
     *
     * @param relativePath 文件相对路径
     * @param subDir 子目录路径
     * @return 文件对象，不存在返回null
     */
    @Override
    public File getFile(String relativePath, String subDir) {
        if (!StringUtils.hasText(relativePath)) {
            return null;
        }

        File file = new File(basePath, subDir + "/" + relativePath);
        if (file.exists() && file.isFile()) {
            return file;
        }
        return null;
    }

    /**
     * 获取文件的访问URL
     *
     * @param relativePath 文件相对路径
     * @param subDir 子目录路径
     * @return 文件访问URL
     */
    @Override
    public String getFileUrl(String relativePath, String subDir) {
        if (!StringUtils.hasText(relativePath)) {
            return null;
        }
        return "/uploads/" + subDir + "/" + relativePath;
    }

    /**
     * 检查文件是否为图片
     *
     * @param file 上传的文件
     * @return 是否为图片
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

        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equals(contentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名（不含点），如 "jpg"、"png"
     */
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
