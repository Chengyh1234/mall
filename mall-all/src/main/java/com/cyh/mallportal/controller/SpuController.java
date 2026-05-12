package com.cyh.mallportal.controller;

import cn.hutool.core.io.FileUtil;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson2.JSON;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallportal.dto.SpuDto;
import com.cyh.mallportal.entity.Spu;
import com.cyh.mallportal.service.CategoryService;
import com.cyh.mallportal.service.FileService;
import com.cyh.mallportal.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 商品SPU管理控制器
 * 提供商品的增删改查功能及图片管理
 */
@RestController
@RequestMapping("/spu")
public class SpuController {

    @Autowired
    private SpuService spuService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FileService fileService;

    @Value("${file.upload.images-path:./uploads/images}")
    private String imagesPath;

    /**
     * 上传图片时，上传图片到指定目录，返回图片路径，日期/UUID+文件名保存在数据库中
     */
    @PostMapping("/add")
    public Result<Map<String, Object>> add(@RequestPart(value = "spuDto") String spuDtoString,
                                           @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {

        SpuDto spuDto = JSON.parseObject(spuDtoString, SpuDto.class);
        // 1. 创建Spu实体并设置基本信息
        Spu spu = new Spu();
        spu.setName(spuDto.getName());
        spu.setCategoryId(spuDto.getCategoryId());
        spu.setBrandId(spuDto.getBrandId());
        spu.setDescription(spuDto.getDescription());
        spu.setUnit(spuDto.getUnit());
        spu.setKeywords(spuDto.getKeywords());
        spu.setSales(spuDto.getSales());
        spu.setStatus(spuDto.getStatus());

        String mainImageName = spuDto.getMainImageName();

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        // 3. 处理图片集
        List<String> imageList = new ArrayList<>();

        // 3.2 处理新上传的图片（imageFiles参数）
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    // 调用uploadImage方法上传文件
                    Map<String, String> imageInfo = uploadImage(file);
                    if (imageInfo != null) {
                        // 拼接图片路径，格式：2026-05-03/uuid_原名.后缀
                        imageList.add(date + "/" + imageInfo.get("fileName"));
                        if (StringUtils.equals(file.getOriginalFilename(), mainImageName)) {
                            spu.setMainImage(date + "/" + imageInfo.get("fileName"));
                        }
                    }
                }
            }
        }
        // 4. 如果有图片集，序列化为JSON字符串存入数据库
        if (!imageList.isEmpty()) {
            spu.setImages(JSON.toJSONString(imageList));
        }
        // 5. 调用service保存商品
        Long id = spuService.add(spu);
        if (id != null) {
            // 6. 返回成功结果（包含商品ID和图片路径，方便前端使用）
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("mainImage", spu.getMainImage());
            data.put("images", spu.getImages());
            return Result.success("添加成功", data);
        }
        return Result.error("添加失败");
    }

    /**
     * 删除商品（同时删除图片）
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Spu spu = spuService.getById(id);
        if (spu == null) {
            return Result.error("商品不存在");
        }

        // 删除主图
        if (spu.getMainImage() != null && !spu.getMainImage().isEmpty()) {
            deleteImageFile(spu.getMainImage());
        }

        // 删除图片集
        if (spu.getImages() != null && !spu.getImages().isEmpty()) {
            try {
                List<String> imageList = JSON.parseArray(spu.getImages(), String.class);
                for (String img : imageList) {
                    deleteImageFile(img);
                }
            } catch (Exception e) {
                System.out.println("异常消息: " + e.getMessage());
            }
        }

        boolean success = spuService.delete(id);
        if (success) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }

    /**
     * 更新商品信息（支持图片上传和删除）
     * 上传图片需要这样：
     * 1.上传旧图片时(可能不是上传数据图全部路径)，前端上传json字符串，包含旧图片路径，不包含新图片路径
     * 2.上传新图片时，前端上传图片，保存到指定目录，返回图片路径，保存在数据库中
     * 主图类似
     *
     * @param spuDtoString 商品信息DTO
     * @return 更新结果
     */
    @PutMapping("/update")
    public Result<Map<String, Object>> update(@RequestPart(value = "spuDto") String spuDtoString,
    @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {
        SpuDto spuDto = JSON.parseObject(spuDtoString, SpuDto.class);
        spuDto.setImageFiles(imageFiles);

        if (spuDto.getId() == null) {
            return Result.error("商品ID不能为空");
        }

        // 1. 获取原来的商品信息
        Spu oldSpu = spuService.getById(spuDto.getId());
        if (oldSpu == null) {
            return Result.error("商品不存在");
        }

        // 2. 创建新Spu实体并设置基本信息
        Spu spu = new Spu();
        spu.setId(spuDto.getId());
        spu.setName(spuDto.getName() != null ? spuDto.getName() : oldSpu.getName());
        spu.setCategoryId(spuDto.getCategoryId() != null ? spuDto.getCategoryId() : oldSpu.getCategoryId());
        spu.setBrandId(spuDto.getBrandId() != null ? spuDto.getBrandId() : oldSpu.getBrandId());
        spu.setDescription(spuDto.getDescription() != null ? spuDto.getDescription() : oldSpu.getDescription());
        spu.setUnit(spuDto.getUnit() != null ? spuDto.getUnit() : oldSpu.getUnit());
        spu.setKeywords(spuDto.getKeywords() != null ? spuDto.getKeywords() : oldSpu.getKeywords());
        spu.setSales(spuDto.getSales() != null ? spuDto.getSales() : oldSpu.getSales());
        spu.setStatus(spuDto.getStatus() != null ? spuDto.getStatus() : oldSpu.getStatus());

        // 3. 初始化图片列表
        List<String> imageList = new ArrayList<>();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        boolean keepOldImages = spuDto.getKeepOldImages() != null && spuDto.getKeepOldImages();


        // 4. 处理旧图片
        if (keepOldImages) {
            // 保留旧图片，全部保留
            if (oldSpu.getImages() != null && !oldSpu.getImages().isEmpty()) {
                try {
                    List<String> oldImages = JSON.parseArray(oldSpu.getImages(), String.class);
                    if (oldImages != null) {
                        imageList.addAll(oldImages);
                    }
                } catch (Exception e) {
                    System.out.println("解析旧图片列表异常: " + e.getMessage());
                }
            }
        } else {
            // 不保留旧图片，删除旧图片文件,但是不删除主图 也就是部分保留
            if (oldSpu.getImages() != null && !oldSpu.getImages().isEmpty()) {
                try {
                    List<String> oldImages = JSON.parseArray(oldSpu.getImages(), String.class);
                    if (oldImages != null) {
                        for (String img : oldImages) {
                            // 判断是否在已有图片列表中
                            if(!spuDto.getImages().contains(img)) {
                                deleteImageFile(img);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("删除旧图片异常: " + e.getMessage());
                }
            }

            // 5. 处理已有图片（images参数，JSON数组格式）为旧图偏中的部分图片
            if (spuDto.getImages() != null && !spuDto.getImages().isEmpty()) {
                try {
                    List<String> existingImages = JSON.parseArray(spuDto.getImages(), String.class);
                    if (existingImages != null) {
                        imageList.addAll(existingImages);
                    }
                } catch (Exception e) {
                    imageList.add(spuDto.getImages());
                }
            }
        }


        // 6. 处理新上传的图片（imageFiles参数）
        if (spuDto.getImageFiles() != null && !spuDto.getImageFiles().isEmpty()) {
            for (MultipartFile file : spuDto.getImageFiles()) {
                if (!file.isEmpty()) {
                    Map<String, String> imageInfo = uploadImage(file);
                    if (imageInfo != null) {
                        String imagePath = date + "/" + imageInfo.get("fileName");
                        imageList.add(imagePath);
                        // 判断是否为主图
                        if (spuDto.getMainImageName() != null && !spuDto.getMainImageName().isEmpty()) {
                            if (StringUtils.equals(file.getOriginalFilename(), spuDto.getMainImageName())) {
                                spu.setMainImage(imagePath);
                            }
                        }
                    }
                }
            }
        }

        // 7. 处理主图
        if (spu.getMainImage() == null || spu.getMainImage().isEmpty()) {
            if (keepOldImages) {
                spu.setMainImage(oldSpu.getMainImage());
            } else if (!imageList.isEmpty()) {
                // 如果不保留旧图片，但有新图片，取第一张作为主图
                spu.setMainImage(imageList.get(0));
            }
        }

        // 8. 如果有图片集，序列化为JSON字符串
        if (!imageList.isEmpty()) {
            spu.setImages(JSON.toJSONString(imageList));
        }

        // 9. 调用service更新商品
        boolean success = spuService.update(spu);
        if (success) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", spu.getId());
            data.put("mainImage", spu.getMainImage());
            data.put("images", spu.getImages());
            return Result.success("更新成功", data);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID获取商品详情
     */
    @GetMapping("/detail/{id}")
    public Result<Spu> getById(@PathVariable Long id) {
        Spu spu = spuService.getById(id);
        if (spu != null) {
            return Result.success(spu);
        }
        return Result.error("商品不存在");
    }

    /**
     * 获取商品列表
     */
    @GetMapping("/list")
    public Result<List<Spu>> getList(Spu spu) {
        List<Spu> list = spuService.getList(spu);
        return Result.success(list);
    }

    /**
     * 分页获取商品列表（支持分类及其子分类、多字段模糊搜索、品牌筛选）
     *
     * @param spu 查询条件
     * @param categoryId 分类ID（可选，传入会查询该分类及其子分类的商品）
     * @param brandId 品牌ID（可选，按品牌筛选）
     * @param keyword 搜索关键字（可选，同时匹配商品名称、分类名称、品牌名称）
     * @param page 页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 商品分页列表
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> getPage(Spu spu,
                                               @RequestParam(required = false) Long categoryId,
                                               @RequestParam(required = false) Long brandId,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        List<Spu> list;
        int total = 0;
        // 获取分类ID列表（如果传入了categoryId，包含子分类）
        List<Long> categoryIds = null;
        if (categoryId != null) {
            categoryIds = categoryService.getCategoryIdWithChildren(categoryId);
        }

        // 如果有搜索关键字或品牌ID或分类ID，使用综合搜索
        boolean hasAdvancedSearch = !StringUtils.isEmpty(keyword) || brandId != null || categoryIds != null;
        if (hasAdvancedSearch) {
            list = spuService.searchByKeyword(categoryIds, keyword, brandId, page, pageSize);
            total = spuService.countSearchByKeyword(categoryIds, keyword, brandId);
        } else {
            // 普通分页查询
            list = spuService.getPage(spu, page, pageSize);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("page", page);
        data.put("pageSize", pageSize);
        if (categoryId != null) {
            data.put("categoryId", categoryId);
        }
        if (brandId != null) {
            data.put("brandId", brandId);
        }
        if (!StringUtils.isEmpty(keyword)) {
            data.put("keyword", keyword);
        }
        if (hasAdvancedSearch) {
            data.put("total", total);
        }
        return Result.success(data);
    }

    /**
     * 上传图片（使用公共FileService）
     */
    private Map<String, String> uploadImage(MultipartFile file) {
        return fileService.uploadFile(file, "images");
    }

    /**
     * 删除图片文件（使用公共FileService）
     */
    private void deleteImageFile(String imagePath) {
        fileService.deleteFile(imagePath, "images");
    }
}
