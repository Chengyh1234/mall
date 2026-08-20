package com.cyh.mallstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyh.mallstore.entity.Banner;
import com.cyh.mallstore.mapper.BannerMapper;
import com.cyh.mallstore.service.BannerCacheService;
import com.cyh.mallstore.service.BannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 轮播图服务实现类
 * 实现轮播图 CRUD 操作，数据变更时主动清除缓存
 * 活跃列表优先从缓存读取，缓存未命中时查库并回填
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerMapper bannerMapper;
    private final BannerCacheService bannerCacheService;

    /**
     * 新增轮播图，设置默认状态和排序，成功后清除缓存
     */
    @Override
    public Long add(Banner banner) {
        banner.setCreatedAt(LocalDateTime.now());
        banner.setUpdatedAt(LocalDateTime.now());
        if (banner.getStatus() == null) {
            banner.setStatus(1);
        }
        if (banner.getSort() == null) {
            banner.setSort(0);
        }
        int result = bannerMapper.insert(banner);
        if (result > 0) {
            bannerCacheService.clearAllBannerCache();
            log.info("新增轮播图成功，轮播图ID: {}, 标题: {}", banner.getId(), banner.getTitle());
            return banner.getId();
        }
        return null;
    }

    /**
     * 更新轮播图信息，成功后清除缓存
     */
    @Override
    public boolean update(Banner banner) {
        banner.setUpdatedAt(LocalDateTime.now());
        int result = bannerMapper.updateById(banner);
        if (result > 0) {
            bannerCacheService.clearAllBannerCache();
            log.info("更新轮播图成功，轮播图ID: {}, 标题: {}", banner.getId(), banner.getTitle());
            return true;
        }
        return false;
    }

    /**
     * 删除轮播图，删除前校验是否存在，成功后清除缓存
     */
    @Override
    public boolean delete(Long id) {
        Banner existing = bannerMapper.selectById(id);
        if (existing == null) {
            log.warn("删除轮播图失败，轮播图不存在，ID: {}", id);
            return false;
        }
        int result = bannerMapper.deleteById(id);
        if (result > 0) {
            bannerCacheService.clearAllBannerCache();
            log.info("删除轮播图成功，轮播图ID: {}, 标题: {}", id, existing.getTitle());
            return true;
        }
        return false;
    }

    /**
     * 更新轮播图启用/禁用状态，成功后清除缓存
     */
    @Override
    public boolean updateStatus(Long id, Integer status) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null) {
            log.warn("更新轮播图状态失败，轮播图不存在，ID: {}", id);
            return false;
        }
        banner.setStatus(status);
        banner.setUpdatedAt(LocalDateTime.now());
        int result = bannerMapper.updateById(banner);
        if (result > 0) {
            bannerCacheService.clearAllBannerCache();
            log.info("更新轮播图状态成功，轮播图ID: {}, 状态: {}", id, status);
            return true;
        }
        return false;
    }

    /**
     * 按 ID 查询轮播图详情
     */
    @Override
    public Banner getById(Long id) {
        return bannerMapper.selectById(id);
    }

    /**
     * 查询轮播图列表，可选按状态筛选，按排序和 ID 降序排列
     */
    @Override
    public List<Banner> getList(Integer status) {
        LambdaQueryWrapper<Banner> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            queryWrapper.eq(Banner::getStatus, status);
        }
        queryWrapper.orderByAsc(Banner::getSort)
                .orderByDesc(Banner::getId);
        return bannerMapper.selectList(queryWrapper);
    }

    /**
     * 获取启用状态的轮播图列表
     * 缓存穿透防护：优先从缓存读取，缓存未命中时查库并回填
     */
    @Override
    public List<Banner> getActiveList() {
        List<Banner> cached = bannerCacheService.getActiveBanners();
        if (cached != null) {
            log.debug("从缓存获取轮播图列表成功");
            return cached;
        }
        List<Banner> list = bannerMapper.selectActive();
        bannerCacheService.setActiveBanners(list);
        log.debug("从数据库查询轮播图列表并缓存");
        return list;
    }
}