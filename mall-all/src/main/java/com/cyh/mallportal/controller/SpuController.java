package com.cyh.mallportal.controller;

import cn.hutool.core.io.FileUtil;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson2.JSON;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallportal.dto.SpuDto;
import com.cyh.mallportal.entity.Spu;
import com.cyh.mallportal.entity.User;
import com.cyh.mallportal.service.CategoryService;
import com.cyh.mallportal.service.FileService;
import com.cyh.mallportal.service.SpuService;
import com.cyh.mallportal.vo.SpuDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
@Slf4j
@RestController
@RequestMapping("/spu")
public class SpuController {

    @Autowired
    private SpuService spuService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FileService fileService;

    //@Value("${file.upload.images-path:./uploads/images}")
    //private String imagesPath;

    /**
     * 上传图片时，上传图片到指定目录，返回图片路径，日期/UUID+文件名保存在数据库中
     */
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('product:add') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> add(@RequestPart(value = "spuDto") String spuDtoString,
                                           @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {

        SpuDto spuDto = JSON.parseObject(spuDtoString, SpuDto.class);
        // 1. 创建Spu实体并设置基本信息
        Spu spu = new Spu();
        spu.setName(spuDto.getName());
        spu.setCategoryId(spuDto.getCategoryId());
        spu.setBrandId(spuDto.getBrandId());
        spu.setStoreId(spuDto.getStoreId());
        spu.setDescription(spuDto.getDescription());
        spu.setUnit(spuDto.getUnit());
        spu.setKeywords(spuDto.getKeywords());
        spu.setSales(spuDto.getSales());
        spu.setStatus(spuDto.getStatus());
        // 2. 设置商家ID为当前登录用户ID（商品归属商家）
        Long sellerId = getCurrentUserId();
        if (sellerId == null) {
            return Result.error("用户未登录");
        }
        spu.setSellerId(sellerId);

        String mainImageName = spuDto.getMainImageName();

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        // 3. 处理图片集
        List<String> imageList = new ArrayList<>();

        // 3.2 处理新上传的图片（imageFiles参数）
        if (imageFiles != null && !imageFiles.isEmpty()) {
            boolean hasNewMainImage = false;
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    // 调用uploadImage方法上传文件
                    Map<String, String> imageInfo = uploadImage(file);
                    if (imageInfo != null) {
                        // 拼接图片路径，格式：2026-05-03/uuid_原名.后缀
                        // 处理主图，不再添加到images中了
                        if (!hasNewMainImage && StringUtils.equals(file.getOriginalFilename(), mainImageName)) {
                            spu.setMainImage(date + "/" + imageInfo.get("fileName"));
                            hasNewMainImage = true;
                            continue;
                        }
                        imageList.add(date + "/" + imageInfo.get("fileName"));

                    }
                }
            }
        }
        // 3.3 处理主图
        if (spu.getMainImage() == null || spu.getMainImage().isEmpty()) {
            if (!imageList.isEmpty()) {
                // 如果不保留旧图片，但有新图片，取第一张作为主图
                spu.setMainImage(imageList.get(0));
                imageList.remove(0);
            }
        }

        // 4. 如果有图片集，序列化为JSON字符串存入数据库
        if (!imageList.isEmpty()) {
            spu.setImages(JSON.toJSONString(imageList));
        }

        // 5. 调用service保存商品
        try {
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
        } catch (Exception e) {
            log.error("添加商品失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除商品（逻辑删除，设置 is_deleted=1，图片保留以支持恢复）
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('product:delete') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        Spu spu = spuService.getById(id);
        if (spu == null) {
            return Result.error("商品不存在");
        }

        // 权限校验：商家只能删除自己的商品
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }
        // 如果当前用户不是管理员且不是商品所有者，拒绝操作
        if (!isAdminOrSeller() && !currentUserId.equals(spu.getSellerId())) {
            return Result.error("无权删除此商品");
        }

        boolean success = spuService.delete(id);
        if (success) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }

    /**
     * 上架商品（设置 status=1）
     */
    @PutMapping("/on-shelf/{id}")
    @PreAuthorize("hasAuthority('product:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Void> onShelf(@PathVariable Long id) {
        Spu spu = spuService.getById(id);
        if (spu == null) {
            return Result.error("商品不存在");
        }

        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }
        if (!isAdminOrSeller() && !currentUserId.equals(spu.getSellerId())) {
            return Result.error("无权操作此商品");
        }

        spu.setStatus(1);
        boolean success = spuService.update(spu);
        if (success) {
            return Result.success("上架成功", null);
        }
        return Result.error("上架失败");
    }

    /**
     * 下架商品（设置 status=0）
     */
    @PutMapping("/off-shelf/{id}")
    @PreAuthorize("hasAuthority('product:offShelf') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Void> offShelf(@PathVariable Long id) {
        Spu spu = spuService.getById(id);
        if (spu == null) {
            return Result.error("商品不存在");
        }

        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }
        if (!isAdminOrSeller() && !currentUserId.equals(spu.getSellerId())) {
            return Result.error("无权操作此商品");
        }

        spu.setStatus(0);
        boolean success = spuService.update(spu);
        if (success) {
            return Result.success("下架成功", null);
        }
        return Result.error("下架失败");
    }

    /**
     * 恢复商品（设置 is_deleted=0）
     */
    @PutMapping("/restore/{id}")
    @PreAuthorize("hasAuthority('product:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Void> restore(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }
        if (!isAdminOrSeller()) {
            return Result.error("无权操作");
        }

        boolean success = spuService.restore(id);
        if (success) {
            return Result.success("恢复成功", null);
        }
        return Result.error("恢复失败，商品不存在");
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
    @PreAuthorize("hasAuthority('product:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
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

        // 2. 权限校验：商家只能修改自己的商品
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }
        // 3.如果当前用户不是管理员且不是商品所有者，拒绝操作
        if (!isAdminOrSeller() && !currentUserId.equals(oldSpu.getSellerId())) {
            return Result.error("无权修改此商品");
        }

        // 4. 创建新Spu实体并设置基本信息
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

        // 如果前端上传新主图，则更新主图。否则还使用旧主图
        if(StringUtils.isEmpty(spuDto.getMainImageName())){
            spu.setMainImage(oldSpu.getMainImage());
        }else{
            deleteImageFile(oldSpu.getMainImage());
        }

        // 5. 初始化图片列表
        List<String> imageList = new ArrayList<>();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));


        // 6. 处理旧图片列表
        //前端上传的旧图片列表
        List<String> existingImages = JSON.parseArray(spuDto.getImages(), String.class);
        // 旧图片列表(数据库图片列表）
        List<String> oldImages = JSON.parseArray(oldSpu.getImages(), String.class);
        // (1）前端上传的旧图片列表要合规，需要在旧图片列表(数据库图片列表）中
        // (2）旧图片列表(数据库图片列表）不在前端上传的旧图片列表中的图片，需要删除
        if (oldSpu.getImages() != null && !oldSpu.getImages().isEmpty() &&
                spuDto.getImages() != null && !spuDto.getImages().isEmpty()) {
            try {
                // 完成(1）遍历前端上传的图片列表，判断是否在已有图片列表中，不在则抛出异常
                if (existingImages != null) {
                    for (String img : existingImages) {
                        if (!oldSpu.getImages().contains(img)) {
                            throw new IllegalArgumentException("已有图片列表中不存在的图片: " + img);
                        } else {
                            imageList.add(img);
                        }
                    }
                }
                // 完成(2）遍历已有图片列表，判断是否在前端上传的图片列表中，不在则删除旧图
                if (oldImages != null) {
                    for (String img : oldImages) {
                        // 判断旧图是否在已有图片列表中，不在则删除旧图
                        if (!spuDto.getImages().contains(img)) {
                            deleteImageFile(img);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("删除旧图片异常: {}", e.getMessage());
            }
        }


        // 7. 处理新上传的图片（imageFiles参数）
        if (spuDto.getImageFiles() != null && !spuDto.getImageFiles().isEmpty()) {
            boolean hasNewMainImage = false;
            for (MultipartFile file : spuDto.getImageFiles()) {
                if (!file.isEmpty()) {
                    Map<String, String> imageInfo = uploadImage(file);
                    if (imageInfo != null) {
                        String imagePath = date + "/" + imageInfo.get("fileName");
                        // 判断是否为主图
                        if (!hasNewMainImage && spuDto.getMainImageName() != null && !spuDto.getMainImageName().isEmpty()) {
                            if (StringUtils.equals(file.getOriginalFilename(), spuDto.getMainImageName())) {
                                spu.setMainImage(imagePath);
                                hasNewMainImage = true;
                                continue;
                            }
                        }
                        imageList.add(imagePath);
                    }
                }
            }
        }

        // 8. 如果没有上传新主图，取图片列表第一个作为主图。不需要有主图
        if (spu.getMainImage() == null || spu.getMainImage().isEmpty()) {
            if (!imageList.isEmpty()) {
                // 如果不保留旧图片，但有新图片，取第一张作为主图
                spu.setMainImage(imageList.get(0));
                imageList.remove(0);
            }
        }

        // 9. 如果有图片集，序列化为JSON字符串
        if (!imageList.isEmpty()) {
            spu.setImages(JSON.toJSONString(imageList));
        }

        // 10. 调用service更新商品
        try {
            boolean success = spuService.update(spu);
            if (success) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", spu.getId());
                data.put("mainImage", spu.getMainImage());
                data.put("images", spu.getImages());
                return Result.success("更新成功", data);
            }
            return Result.error("更新失败");
        } catch (Exception e) {
            log.error("更新商品失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取商品详情（包含商家信息）
     * 返回商品基本信息及商家信息（用户名、头像、真实姓名、手机号）
     *
     * @param id 商品ID
     * @return 商品详情（包含商家信息）
     */
    @GetMapping("/detail/{id}")
    public Result<SpuDetailVo> getById(@PathVariable Long id) {
        SpuDetailVo spuDetail = spuService.getSpuDetailById(id);
        if (spuDetail != null) {
            return Result.success(spuDetail);
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
     * 根据商家ID获取商品列表（不分页）
     * 仅限商家及以上角色访问，用于商家管理自己的商品
     *
     * @param sellerId 商家ID
     * @return 商品列表
     */
    @GetMapping("/list-by-seller/{sellerId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<List<Spu>> getListBySellerId(@PathVariable Long sellerId) {
        List<Spu> list = spuService.getListBySellerId(sellerId);
        return Result.success(list);
    }

    /**
     * 根据商家ID分页获取商品列表
     * 仅限商家及以上角色访问，用于商家管理自己的商品
     *
     * @param sellerId 商家ID
     * @param page     页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 商品分页列表
     */
    @GetMapping("/page-by-seller/{sellerId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> getPageBySellerId(@PathVariable Long sellerId,
                                                         @RequestParam(required = false) Integer status,
                                                         @RequestParam(required = false) String keyword,
                                                         @RequestParam(defaultValue = "1") Integer page,
                                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        List<Spu> list = spuService.getPageBySellerId(sellerId, status, keyword, page, pageSize);
        int total = spuService.countBySellerId(sellerId, status, keyword);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("total", total);
        data.put("sellerId", sellerId);
        return Result.success(data);
    }

    /**
     * 分页获取商品列表（支持分类及其子分类、多字段模糊搜索、品牌筛选）
     *
     * @param spu        查询条件
     * @param categoryId 分类ID（可选，传入会查询该分类及其子分类的商品）
     * @param brandId    品牌ID（可选，按品牌筛选）
     * @param keyword    搜索关键字（可选，同时匹配商品名称、分类名称、品牌名称）
     * @param page       页码，默认第1页
     * @param pageSize   每页数量，默认10条
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
        return fileService.uploadFile(file, "spu");
    }

    private void deleteImageFile(String imagePath) {
        fileService.deleteFile(imagePath, "spu");
    }

    /**
     * 获取当前登录用户的ID
     *
     * @return 当前用户ID，未登录返回null
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return ((User) principal).getId();
            }
        }
        return null;
    }

    /**
     * 判断当前用户是否为管理员或商家
     * 管理员和商家可以操作所有商品
     *
     * @return true-是管理员或商家，false-普通用户
     */
    private boolean isAdminOrSeller() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_SUPER_ADMIN")
                            || auth.getAuthority().equals("ROLE_ADMIN")
                            || auth.getAuthority().equals("ROLE_SELLER"));
        }
        return false;
    }
}
