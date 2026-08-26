package com.cyh.mallcommon.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

/**
 * 文件上传服务接口
 * 定义文件上传、删除、获取、URL 生成、图片校验等操作
 * 支持按子目录分类存储不同业务模块的文件
 */
public interface FileService {

    /**
     * 上传文件
     *
     * @param file   上传的文件
     * @param subDir 子目录路径（如 "/images/spu"）
     * @return 上传结果，包含 dateDir、fileName、relativePath；失败返回 null
     */
    Map<String, String> uploadFile(MultipartFile file, String subDir);

    /**
     * 上传图片（带图片类型校验）
     *
     * @param file   上传的图片文件
     * @param subDir 子目录路径
     * @return 上传结果，失败返回 null
     */
    Map<String, String> uploadImage(MultipartFile file, String subDir);

    /**
     * 删除文件
     *
     * @param relativePath 文件相对路径（如 "2026/05/07/uuid_file.jpg"）
     * @param subDir       子目录路径
     * @return 删除成功返回 true
     */
    boolean deleteFile(String relativePath, String subDir);

    /**
     * 获取文件对象
     *
     * @param relativePath 文件相对路径
     * @param subDir       子目录路径
     * @return 文件对象，不存在返回 null
     */
    File getFile(String relativePath, String subDir);

    /**
     * 获取文件的访问 URL
     *
     * @param relativePath 文件相对路径
     * @param subDir       子目录路径
     * @return 文件访问 URL
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
     * @return 扩展名（不含点），如 "jpg"
     */
    String getFileExtension(String filename);
}