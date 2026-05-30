package com.cyh.mallportal.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallportal.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件管理控制器
 * 提供公共的文件上传、下载、删除接口
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    /**
     * 文件服务接口
     */
    @Autowired
    private FileService fileService;

    /**
     * 文件上传根路径
     */
    @Value("${file.upload.base-path:./uploads}")
    private String basePath;

    /**
     * 上传通用文件
     *
     * @param file 上传的文件
     * @param subDir 子目录（如 "images"、"logos"、"documents"）
     * @return 上传结果，包含文件路径和访问URL
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('product:add') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file,
                                                  @RequestParam(defaultValue = "common") String subDir) {
        Map<String, String> result = fileService.uploadFile(file, subDir);
        if (result != null) {
            return Result.success("上传成功", result);
        }
        return Result.error("上传失败");
    }

    /**
     * 上传图片文件
     *
     * @param file 上传的图片
     * @param subDir 子目录（如 "images"、"logos"）
     * @return 上传结果，包含文件路径和访问URL
     */
    @PostMapping("/upload/image")
    @PreAuthorize("hasAuthority('product:add') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file,
                                                    @RequestParam(defaultValue = "images") String subDir) {
        Map<String, String> result = fileService.uploadImage(file, subDir);
        if (result != null) {
            return Result.success("上传成功", result);
        }
        return Result.error("上传失败：只支持上传图片文件（jpg、png、gif、webp）");
    }

    /**
     * 删除文件
     *
     * @param relativePath 文件相对路径（如 "2026/05/07/uuid_file.jpg"）
     * @param subDir 子目录（如 "images"、"logos"）
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('product:delete') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Void> deleteFile(@RequestParam String relativePath,
                                   @RequestParam(defaultValue = "images") String subDir) {
        boolean success = fileService.deleteFile(relativePath, subDir);
        if (success) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败：文件不存在或无法删除");
    }

    /**
     * 下载文件
     *
     * @param relativePath 文件相对路径
     * @param subDir 子目录
     * @return 文件内容
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String relativePath,
                                                 @RequestParam(defaultValue = "images") String subDir) {
        File file = fileService.getFile(relativePath, subDir);
        if (file == null || !file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        String filename = file.getName();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * 预览图片
     *
     * @param relativePath 图片相对路径
     * @param subDir 子目录
     * @return 图片内容
     */
    @GetMapping("/preview")
    public ResponseEntity<Resource> previewImage(@RequestParam String relativePath,
                                                 @RequestParam(defaultValue = "images") String subDir) {
        File file = fileService.getFile(relativePath, subDir);
        if (file == null || !file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);

        // 根据文件扩展名设置Content-Type
        String extension = fileService.getFileExtension(file.getName());
        MediaType mediaType = getMediaType(extension);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }

    /**
     * 获取文件访问URL
     *
     * @param relativePath 文件相对路径
     * @param subDir 子目录
     * @return 文件访问URL
     */
    @GetMapping("/url")
    public Result<Map<String, String>> getFileUrl(@RequestParam String relativePath,
                                                   @RequestParam(defaultValue = "images") String subDir) {
        String url = fileService.getFileUrl(relativePath, subDir);
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        result.put("fullUrl", getFullUrl(url));
        return Result.success(result);
    }

    /**
     * 根据扩展名获取MediaType
     */
    private MediaType getMediaType(String extension) {
        if (extension == null) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG;
            case "png":
                return MediaType.IMAGE_PNG;
            case "gif":
                return MediaType.IMAGE_GIF;
            case "webp":
                return MediaType.parseMediaType("image/webp");
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    /**
     * 获取完整的访问URL
     */
    private String getFullUrl(String relativeUrl) {
        if (relativeUrl == null) {
            return null;
        }
        return "/uploads" + relativeUrl;
    }
}
