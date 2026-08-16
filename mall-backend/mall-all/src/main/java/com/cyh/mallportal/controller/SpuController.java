package com.cyh.mallportal.controller;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson2.JSON;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallcommon.constant.FileConstants;
import com.cyh.mallportal.dto.SpuDto;
import com.cyh.mallportal.entity.*;
import com.cyh.mallportal.mapper.AttributeMapper;
import com.cyh.mallportal.mapper.AttributeValueMapper;
import com.cyh.mallportal.es.service.SpuSearchService;
import com.cyh.mallportal.service.*;
import com.cyh.mallportal.vo.*;
import com.cyh.mallportal.vo.SpuVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 商品SPU管理控制器 已经修改响应
 * 提供商品的增删改查功能及图片管理
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/spu")
public class SpuController {

    @Autowired
    private SpuService spuService;

    @Autowired
    private SpuSearchService spuSearchService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FileService fileService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private SpuAttrService spuAttrService;

    @Autowired
    private AttributeMapper attributeMapper;

    @Autowired
    private AttributeValueMapper attributeValueMapper;

    //@Value("${file.upload.images-path:./uploads/images}")
    //private String imagesPath;

    /**
     * 新增商品
     * 店铺ID由系统根据当前登录商家自动获取，无需前端传入
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> add(@RequestPart(value = "spuDto") String spuDtoString,
                                           @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {
        SpuDto spuDto = JSON.parseObject(spuDtoString, SpuDto.class);
        // 1. 获取当前登录用户ID
        Long sellerId = getCurrentUserId();

        // 2. 根据商家ID自动获取其所属店铺ID
        com.cyh.mallportal.entity.Store store = storeService.getBySellerId(sellerId);
        if (store == null) {
            return Result.error("未找到您的店铺，请先创建店铺");
        }

        // 3. 创建Spu实体并设置基本信息
        Spu spu = new Spu();
        spu.setName(spuDto.getName());
        spu.setCategoryId(spuDto.getCategoryId());
        spu.setBrandId(spuDto.getBrandId());
        spu.setStoreId(store.getId());          // 自动设置店铺ID
        spu.setDescription(spuDto.getDescription());
        spu.setUnit(spuDto.getUnit());
        spu.setKeywords(spuDto.getKeywords());
        spu.setSales(spuDto.getSales());
        spu.setStatus(spuDto.getStatus());
        // 4. 设置商家ID为当前登录用户ID（商品归属商家）
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
     * 删除商品（逻辑删除，设置 is_deleted=1，图片保留以支持恢复）
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        Spu spu = spuService.getById(id);
        if (spu == null) {
            return Result.error("商品不存在");
        }

        // 权限校验：商家只能删除自己的商品
        Long currentUserId = getCurrentUserId();
        // 如果当前用户不是管理员且不是商品所有者，拒绝操作
        if (!currentUserId.equals(spu.getSellerId())) {
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
     * 上架前校验：SPU下必须存在启用状态的SKU，否则不允许上架
     */
    @PutMapping("/on-shelf/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Void> onShelf(@PathVariable Long id) {
        Spu spu = spuService.getById(id);
        if (spu == null) {
            return Result.error("商品不存在");
        }

        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(spu.getSellerId())) {
            return Result.error("无权操作此商品");
        }

        // 上架前校验：SPU下必须存在启用状态的SKU
        if (!spuService.hasEnabledSku(id)) {
            return Result.error("该商品下没有启用状态的SKU，无法上架，请先添加并启用SKU");
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
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Void> offShelf(@PathVariable Long id) {
        Spu spu = spuService.getById(id);
        if (spu == null) {
            return Result.error("商品不存在");
        }

        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(spu.getSellerId())) {
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
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Void> restore(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();

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
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
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
        // 3.如果当前用户不是管理员且不是商品所有者，拒绝操作
        if ( !currentUserId.equals(oldSpu.getSellerId())) {
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
        // 上架校验：如果SPU将被设为上架状态，必须存在启用SKU
        if (spu.getStatus() != null && spu.getStatus() == 1 && !spuService.hasEnabledSku(spu.getId())) {
            return Result.error("该商品下没有启用状态的SKU，无法上架，请先添加并启用SKU");
        }
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
     * 根据ID获取商品详情（公开）
     * 公开接口，仅返回上架商品（status=1）
     * 下架商品不对外公开，返回"商品不存在"
     * 返回 SpuVo 字段 + sellerId、sellerUsername、sellerAvatar
     *
     * @param id 商品ID
     * @return 商品详情
     */
    @GetMapping("/detail/{id}")
    public Result<SpuDetailVo> getById(@PathVariable Long id) {
        Spu spu = spuService.getById(id);
        if (spu == null) {
            return Result.error("商品不存在");
        }

        // 仅返回上架商品（status=1），下架商品不对外公开
        if (spu.getStatus() != 1) {
            return Result.error("商品不存在");
        }

        SpuDetailVo vo = spuService.getSpuDetailById(id);
        if (vo == null) {
            return Result.error("商品不存在");
        }
        return Result.success(vo);
    }

    /**
     * 商家端：根据ID获取商品管理详情（不限上下架状态）
     * 仅限商品所属商家查看，可查看上架和下架商品
     * 返回 SpuSellerVo 字段 + sellerUsername、sellerAvatar、sellerRealName、sellerPhone
     *
     * @param id 商品ID
     * @return 商家端商品管理详情
     */
    @GetMapping("/manage-detail/seller/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<SpuSellerDetailVo> getManageDetailForSeller(@PathVariable Long id) {
        Spu spu = spuService.getById(id);
        if (spu == null) {
            return Result.error("商品不存在");
        }

        // 校验权限：仅当前商家可查看自己的商品
        Long currentUserId = getCurrentUserId();

        if (!currentUserId.equals(spu.getSellerId())) {
            return Result.error("无权查看此商品");
        }

        SpuSellerDetailVo vo = spuService.getSpuSellerDetailById(id);
        if (vo == null) {
            return Result.error("商品不存在");
        }
        return Result.success(vo);
    }

    /**
     * 管理员端：根据ID获取商品管理详情（不限上下架状态）
     * 仅限管理员或超级管理员查看所有商品
     * 返回 SpuAdminVo 字段 + sellerUsername、sellerAvatar、sellerRealName、sellerPhone
     *
     * @param id 商品ID
     * @return 管理员端商品管理详情
     */
    @GetMapping("/manage-detail/admin/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<SpuAdminDetailVo> getManageDetailForAdmin(@PathVariable Long id) {
        Spu spu = spuService.getById(id);
        if (spu == null) {
            return Result.error("商品不存在");
        }

        SpuAdminDetailVo vo = spuService.getSpuAdminDetailById(id);
        if (vo == null) {
            return Result.error("商品不存在");
        }
        return Result.success(vo);
    }

    /**
     * 获取商品列表  ---------可删
     * 公开接口，仅返回上架状态（status=1）的商品
     */
    //@GetMapping("/list")
    //public Result<List<Spu>> getList(Spu spu) {
    //    // 公开接口只展示上架商品
    //    if (spu == null) {
    //        spu = new Spu();
    //    }
    //    spu.setStatus(1);
    //    List<Spu> list = spuService.getList(spu);
    //    return Result.success(list);
    //}

    /**
     * 根据商家ID获取商品列表（不分页）---------可删
     * 仅限商家及以上角色访问，用于商家管理自己的商品
     *
     * @param sellerId 商家ID
     * @return 商品列表
     */
    //@GetMapping("/list-by-seller/{sellerId}")
    //@PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    //public Result<List<Spu>> getListBySellerId(@PathVariable Long sellerId) {
    //    List<Spu> list = spuService.getListBySellerId(sellerId);
    //    return Result.success(list);
    //}

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
    @PreAuthorize("hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> getPageBySellerId(@PathVariable Long sellerId,
                                                         @RequestParam(required = false) Integer status,
                                                         @RequestParam(required = false) String keyword,
                                                         @RequestParam(defaultValue = "1") Integer page,
                                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        List<SpuSellerVo> list = spuService.getPageBySellerId(sellerId, status, keyword, page, pageSize);
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
     * 分页获取全部商品列表（含上架和下架）
     * 不限商家，用于超级管理员查看全平台商品，支持按状态筛选和商品名称搜索
     * 返回格式与 page-by-seller 一致，但不包含 sellerId 字段
     *
     * @param status   状态筛选（可选，1-上架 0-下架，不传则查询全部）
     * @param keyword  搜索关键字（可选，按商品名称模糊搜索）
     * @param page     页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 商品分页列表（含 categoryName、brandName）
     */
    @GetMapping("/page-all")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Map<String, Object>> getPageAll(@RequestParam(required = false) Integer status,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        List<SpuAdminVo> list = spuService.getPageAll(status, keyword, page, pageSize);
        int total = spuService.countAll(status, keyword);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("total", total);
        return Result.success(data);
    }

    /**
     * 分页获取商品列表（支持分类及其子分类、多字段模糊搜索、品牌筛选）
     * 公开接口，仅返回上架状态（status=1）的商品
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
        List<SpuVo> list;
        int total = 0;
        // 获取分类ID列表（如果传入了categoryId，包含子分类）
        List<Long> categoryIds = null;
        if (categoryId != null) {
            categoryIds = categoryService.getCategoryIdWithChildren(categoryId);
        }

        // 如果有搜索关键字或品牌ID或分类ID，使用综合搜索
        boolean hasAdvancedSearch = !StringUtils.isEmpty(keyword) || brandId != null || categoryIds != null;
        if (hasAdvancedSearch) {
            // 公开接口只展示上架商品，status=1过滤由mapper层SQL处理
            list = spuService.searchByKeyword(categoryIds, keyword, brandId, page, pageSize);
            total = spuService.countSearchByKeyword(categoryIds, keyword, brandId);
        } else {
            // 普通分页查询：公开接口只展示上架商品
            if (spu == null) {
                spu = new Spu();
            }
            spu.setStatus(1);
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
     * 根据店铺ID分页查询 SPU 列表（公开，无需登录）
     * 仅返回上架商品（status=1），支持按名称关键字、分类、价格范围筛选和排序
     *
     * @param storeId   店铺ID（路径参数）
     * @param keyword   商品名称关键字（模糊匹配，可选）
     * @param categoryId 分类ID（精确匹配，可选）
     * @param minPrice  最低售价下限（可选）
     * @param maxPrice  最低售价上限（可选）
     * @param sortBy    排序字段：sales（销量）/ price（价格）/ created_at（创建时间），默认 created_at
     * @param sortOrder 排序方向：asc（升序）/ desc（降序），默认 desc
     * @param page      页码，默认第1页
     * @param pageSize  每页数量，默认10条
     * @return 分页数据：{ list, page, pageSize, total, storeId, ...filters }
     */
    @GetMapping("/by-store/{storeId}/page")
    public Result<Map<String, Object>> getPageByStoreId(@PathVariable Long storeId,
                                                        @RequestParam(required = false) String keyword,
                                                        @RequestParam(required = false) Long categoryId,
                                                        @RequestParam(required = false) BigDecimal minPrice,
                                                        @RequestParam(required = false) BigDecimal maxPrice,
                                                        @RequestParam(defaultValue = "created_at") String sortBy,
                                                        @RequestParam(defaultValue = "desc") String sortOrder,
                                                        @RequestParam(defaultValue = "1") Integer page,
                                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        List<SpuVo> list = spuService.getPageByStoreId(storeId, keyword, categoryId,
                minPrice, maxPrice, sortBy, sortOrder, page, pageSize);
        int total = spuService.countByStoreId(storeId, keyword, categoryId, minPrice, maxPrice);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("total", total);
        data.put("storeId", storeId);
        if (keyword != null) data.put("keyword", keyword);
        if (categoryId != null) data.put("categoryId", categoryId);
        return Result.success(data);
    }

    /**
     * ES 商品搜索（公开）
     * <p>
     * 替代原有的 MySQL LIKE 模糊搜索，提供关键词分词匹配、分类/品牌/价格筛选、
     * 销量/价格/时间排序功能。仅返回上架商品。
     *
     * @param keyword    搜索关键词（可选）
     * @param categoryId 分类 ID（可选）
     * @param brandId    品牌 ID（可选）
     * @param minPrice   最低售价下限（可选）
     * @param maxPrice   最低售价上限（可选）
     * @param sortBy     排序字段：sales / price / created_at，默认 created_at
     * @param sortOrder  排序方向：asc / desc，默认 desc
     * @param page       页码，默认 1
     * @param pageSize   每页条数，默认 10
     * @return 分页数据：{ list, total, page, pageSize }
     */
    @GetMapping("/search")
    public Result<Map<String, Object>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "created_at") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) throws IOException {
        Map<String, Object> result = spuSearchService.search(keyword, categoryId, brandId, null,
                minPrice, maxPrice, sortBy, sortOrder, page, pageSize);
        return Result.success(result);
    }

    /**
     * 搜索建议（公开）
     * <p>
     * 基于商品名称的 Completion Suggester，用户在搜索框输入时自动补全。
     *
     * @param keyword 用户输入的前缀
     * @param size    返回的建议数量，默认 5
     * @return 建议词列表
     */
    @GetMapping("/suggest")
    public Result<List<String>> suggest(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "5") Integer size) throws IOException {
        List<String> suggestions = spuSearchService.suggest(keyword, size);
        return Result.success(suggestions);
    }

    /**
     * 根据SPU ID获取基本属性及属性值（公开）
     * <p>
     * 从 spu_basic_attr_values 表查询 SPU 已选的基本属性，
     * 关联 Attribute 表获取属性名称，关联 AttributeValue 表获取属性值名称。
     * 手动输入值直接返回 manual_value 字段内容。
     *
     * @param spuId SPU ID
     * @return 基本属性列表（含属性名称和值）
     */
    @GetMapping("/{spuId}/basic-attributes")
    public Result<List<SpuBasicAttrVo>> getBasicAttributes(@PathVariable Long spuId) {
        Spu spu = spuService.getById(spuId);
        if (spu == null) {
            return Result.error("商品不存在");
        }

        // 获取 SPU 已选的基本属性原始记录
        List<SpuBasicAttrValue> basicAttrs = spuAttrService.getBasicAttrsBySpuId(spuId);
        List<SpuBasicAttrVo> result = basicAttrs.stream()
                .map(ba -> {
                    SpuBasicAttrVo vo = new SpuBasicAttrVo();
                    vo.setAttrId(ba.getAttrId());

                    // 查询属性名称
                    Attribute attr = attributeMapper.selectById(ba.getAttrId());
                    vo.setAttrName(attr != null ? attr.getName() : null);

                    // 处理属性值：手动输入值优先，无手动输入则使用预定义值
                    if (ba.getManualValue() != null && !ba.getManualValue().isEmpty()) {
                        vo.setValue(ba.getManualValue());
                        vo.setValueId(null);
                    } else if (ba.getAttrValueId() != null) {
                        AttributeValue attrValue = attributeValueMapper.selectById(ba.getAttrValueId());
                        vo.setValueId(ba.getAttrValueId());
                        vo.setValue(attrValue != null ? attrValue.getValue() : null);
                        vo.setImageUrl(attrValue != null ? attrValue.getImageUrl() : null);
                    }
                    return vo;
                })
                .toList();
        return Result.success(result);
    }

    /**
     * 上传图片（使用公共FileService）
     */
    private Map<String, String> uploadImage(MultipartFile file) {
        return fileService.uploadFile(file, FileConstants.SPU);
    }

    private void deleteImageFile(String imagePath) {
        fileService.deleteFile(imagePath, FileConstants.SPU);
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
}