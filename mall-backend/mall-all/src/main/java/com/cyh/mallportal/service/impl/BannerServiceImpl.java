package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyh.mallcommon.constant.RedisConstants;
import com.cyh.mallportal.entity.Banner;
import com.cyh.mallportal.mapper.BannerMapper;
import com.cyh.mallportal.mq.event.CacheDomain;
import com.cyh.mallportal.mq.event.CacheInvalidateEvent;
import com.cyh.mallportal.mq.publisher.CacheEventPublisher;
import com.cyh.mallportal.service.BannerCacheService;
import com.cyh.mallportal.service.BannerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 轮播图服务实现类
 * 实现轮播图的新增、删除、修改、查询等功能
 * 首页展示的启用列表走Redis缓存优化
 */
@Slf4j
@Service
public class BannerServiceImpl implements BannerService {

    @Autowired
    private BannerMapper bannerMapper;

    @Autowired
    private BannerCacheService bannerCacheService;

    @Autowired
    private CacheEventPublisher cacheEventPublisher;

    @Override
    public Long add(Banner banner) {
        try {
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
                cacheEventPublisher.publishInvalidate(new CacheInvalidateEvent()
                        .setDomain(CacheDomain.BANNER)
                        .setExactKeys(List.of(RedisConstants.BANNER_ACTIVE_KEY)));
                log.info("新增轮播图成功，轮播图ID: {}, 标题: {}, 已发布缓存失效事件", banner.getId(), banner.getTitle());
                return banner.getId();
            }
        } catch (Exception e) {
            log.error("新增轮播图失败，标题: {}", banner.getTitle(), e);
        }
        return null;
    }

    @Override
    public boolean update(Banner banner) {
        try {
            banner.setUpdatedAt(LocalDateTime.now());
            int result = bannerMapper.updateById(banner);
            if (result > 0) {
                cacheEventPublisher.publishInvalidate(new CacheInvalidateEvent()
                        .setDomain(CacheDomain.BANNER)
                        .setExactKeys(List.of(RedisConstants.BANNER_ACTIVE_KEY)));
                log.info("更新轮播图成功，轮播图ID: {}, 标题: {}, 已发布缓存失效事件", banner.getId(), banner.getTitle());
                return true;
            }
        } catch (Exception e) {
            log.error("更新轮播图失败，轮播图ID: {}", banner.getId(), e);
        }
        return false;
    }

    @Override
    public boolean delete(Long id) {
        try {
            Banner existing = bannerMapper.selectById(id);
            if (existing == null) {
                log.warn("删除轮播图失败，轮播图不存在，ID: {}", id);
                return false;
            }
            int result = bannerMapper.deleteById(id);
            if (result > 0) {
                cacheEventPublisher.publishInvalidate(new CacheInvalidateEvent()
                        .setDomain(CacheDomain.BANNER)
                        .setExactKeys(List.of(RedisConstants.BANNER_ACTIVE_KEY)));
                log.info("删除轮播图成功，轮播图ID: {}, 标题: {}, 已发布缓存失效事件", id, existing.getTitle());
                return true;
            }
        } catch (Exception e) {
            log.error("删除轮播图失败，轮播图ID: {}", id, e);
        }
        return false;
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        try {
            Banner banner = bannerMapper.selectById(id);
            if (banner == null) {
                log.warn("更新轮播图状态失败，轮播图不存在，ID: {}", id);
                return false;
            }
            banner.setStatus(status);
            banner.setUpdatedAt(LocalDateTime.now());
            int result = bannerMapper.updateById(banner);
            if (result > 0) {
                cacheEventPublisher.publishInvalidate(new CacheInvalidateEvent()
                        .setDomain(CacheDomain.BANNER)
                        .setExactKeys(List.of(RedisConstants.BANNER_ACTIVE_KEY)));
                log.info("更新轮播图状态成功，轮播图ID: {}, 状态: {}, 已发布缓存失效事件", id, status);
                return true;
            }
        } catch (Exception e) {
            log.error("更新轮播图状态失败，轮播图ID: {}", id, e);
        }
        return false;
    }

    @Override
    public Banner getById(Long id) {
        try {
            return bannerMapper.selectById(id);
        } catch (Exception e) {
            log.error("查询轮播图详情失败，轮播图ID: {}", id, e);
            return null;
        }
    }

    @Override
    public List<Banner> getList(Integer status) {
        try {
            LambdaQueryWrapper<Banner> queryWrapper = new LambdaQueryWrapper<>();
            if (status != null) {
                queryWrapper.eq(Banner::getStatus, status);
            }
            queryWrapper.orderByAsc(Banner::getSort)
                    .orderByDesc(Banner::getId);
            return bannerMapper.selectList(queryWrapper);
        } catch (Exception e) {
            log.error("查询轮播图列表失败", e);
            return null;
        }
    }

    @Override
    public List<Banner> getActiveList() {
        List<Banner> cached = bannerCacheService.getActiveBanners();
        if (cached != null) {
            log.debug("从缓存获取轮播图列表成功");
            return cached;
        }

        try {
            List<Banner> list = bannerMapper.selectActive();
            bannerCacheService.setActiveBanners(list);
            log.debug("从数据库查询轮播图列表并缓存");
            return list;
        } catch (Exception e) {
            log.error("查询启用的轮播图列表失败", e);
            return null;
        }
    }
}