package com.cyh.mallportal.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

/**
 * 文件服务接口
 * 提供文件的上传、下载、删除等公共功能
 */
public interface FileService {

    /**
     * 上传文件
     *
     * @param file 上传的文件
     * @param subDir 子目录路径（如 "images"、"logos"）
     * @return 上传结果，包含日期和文件名，失败返回null
     */
    Map<String, String> uploadFile(MultipartFile file, String subDir);

    /**
     * 上传图片（带图片类型校验）
     *
     * @param file 上传的图片文件
     * @param subDir 子目录路径（如 "images"、"logos"）
     * @return 上传结果，包含日期和文件名，失败返回null
     */
    Map<String, String> uploadImage(MultipartFile file, String subDir);

    /**
     * 删除文件
     *
     * @param relativePath 文件相对路径（如 "2026/05/07/uuid_file.jpg"）
     * @param subDir 子目录路径（如 "images"、"logos"）
     * @return 删除成功返回true，失败返回false
     */
    boolean deleteFile(String relativePath, String subDir);

    /**
     * 下载文件
     *
     * @param relativePath 文件相对路径
     * @param subDir 子目录路径
     * @return 文件对象，不存在返回null
     */
    File getFile(String relativePath, String subDir);

    /**
     * 获取文件的访问URL
     *
     * @param relativePath 文件相对路径
     * @param subDir 子目录路径
     * @return 文件访问URL
     */
    String getFileUrl(String relativePath, String subDir);

    /**
     * 检查文件是否为图片
     *
     * @param file 上传的文件
     * @return 是否为图片
     */
    boolean isImage(MultipartFile file);

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名（不含点），如 "jpg"、"png"
     */
    String getFileExtension(String filename);
}
