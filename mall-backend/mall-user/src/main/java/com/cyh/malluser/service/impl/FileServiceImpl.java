package com.cyh.malluser.service.impl;

import com.cyh.mallcommon.constant.FileConstants;
import com.cyh.malluser.service.FileService;
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

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload.base-path:./uploads}")
    private String basePath;

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
            LocalDate today = LocalDate.now();
            String dateStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String dateDir = today.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

            String newFileName = UUID.randomUUID().toString() + "_" + originalFilename;

            String targetPath = basePath + subDir;
            File uploadDir = new File(targetPath, dateDir);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            File destFile = new File(uploadDir, newFileName).getCanonicalFile();
            file.transferTo(destFile);

            Map<String, String> result = new HashMap<>();
            result.put("date", dateStr);
            result.put("dateDir", dateDir);
            result.put("fileName", newFileName);
            result.put("relativePath", dateDir + "/" + newFileName);

            log.info("文件上传成功，原始文件名: {}, 新文件名: {}, 路径: {}", originalFilename, newFileName, targetPath + "/" + dateDir);

            return result;
        } catch (IOException e) {
            log.error("文件上传失败，原始文件名: {}", originalFilename, e);
            return null;
        }
    }

    @Override
    public Map<String, String> uploadImage(MultipartFile file, String subDir) {
        if (!isImage(file)) {
            log.warn("文件不是有效的图片类型: {}", file.getContentType());
            return null;
        }
        return uploadFile(file, subDir);
    }

    @Override
    public boolean deleteFile(String relativePath, String subDir) {
        if (!StringUtils.hasText(relativePath)) {
            log.warn("删除文件路径为空");
            return false;
        }

        try {
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
        } catch (Exception e) {
            log.error("删除文件异常，路径: {}", relativePath, e);
            return false;
        }
    }

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

    @Override
    public String getFileUrl(String relativePath, String subDir) {
        if (!StringUtils.hasText(relativePath)) {
            return null;
        }
        return FileConstants.URL_PREFIX + subDir + "/" + relativePath;
    }

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