package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyh.mallportal.entity.Brand;
import com.cyh.mallportal.mapper.BrandMapper;
import com.cyh.mallportal.service.BrandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 品牌服务实现类
 * 实现品牌的新增、删除、修改、查询等功能
 */
@Slf4j
@Service
public class BrandServiceImpl implements BrandService {

    /**
     * 品牌Mapper接口
     */
    @Autowired
    private BrandMapper brandMapper;

    /**
     * 新增品牌
     *
     * @param brand 品牌信息
     * @return 新增的品牌ID，失败返回null
     */
    @Override
    public Long add(Brand brand) {
        try {
            // 设置创建时间和更新时间
            brand.setCreatedAt(LocalDateTime.now());
            brand.setUpdatedAt(LocalDateTime.now());
            // 默认启用状态
            if (brand.getStatus() == null) {
                brand.setStatus(1);
            }
            // 默认排序号为0
            if (brand.getSort() == null) {
                brand.setSort(0);
            }
            // 执行插入操作
            int result = brandMapper.insert(brand);
            if (result > 0) {
                log.info("新增品牌成功，品牌ID: {}, 品牌名称: {}", brand.getId(), brand.getName());
                return brand.getId();
            }
        } catch (Exception e) {
            log.error("新增品牌失败，品牌名称: {}", brand.getName(), e);
        }
        return null;
    }

    /**
     * 根据ID删除品牌（逻辑删除）
     *
     * @param id 品牌ID
     * @return 删除成功返回true，失败返回false
     */
    @Override
    public boolean delete(Long id) {
        try {
            // 使用deleteById方法，MyBatis-Plus会自动将逻辑删除字段is_deleted设置为1
            int result = brandMapper.deleteById(id);
            if (result > 0) {
                log.info("删除品牌成功，品牌ID: {}", id);
                return true;
            }
        } catch (Exception e) {
            log.error("删除品牌失败，品牌ID: {}", id, e);
        }
        return false;
    }

    /**
     * 更新品牌信息
     *
     * @param brand 品牌信息（需包含ID）
     * @return 更新成功返回true，失败返回false
     */
    @Override
    public boolean update(Brand brand) {
        try {
            // 更新时设置更新时间
            brand.setUpdatedAt(LocalDateTime.now());
            // 执行更新操作
            int result = brandMapper.updateById(brand);
            if (result > 0) {
                log.info("更新品牌成功，品牌ID: {}, 品牌名称: {}", brand.getId(), brand.getName());
                return true;
            }
        } catch (Exception e) {
            log.error("更新品牌失败，品牌ID: {}", brand.getId(), e);
        }
        return false;
    }

    /**
     * 根据ID获取品牌详情
     *
     * @param id 品牌ID
     * @return 品牌信息，不存在返回null
     */
    @Override
    public Brand getById(Long id) {
        try {
            return brandMapper.selectById(id);
        } catch (Exception e) {
            log.error("查询品牌详情失败，品牌ID: {}", id, e);
            return null;
        }
    }

    /**
     * 获取品牌列表（不分页）
     *
     * @param brand 查询条件（支持id、name、status）
     * @return 品牌列表
     */
    @Override
    public List<Brand> getList(Brand brand) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<Brand> queryWrapper = buildQueryWrapper(brand);
            // 按排序号升序、创建时间降序排列
            queryWrapper.orderByAsc(Brand::getSort)
                    .orderByDesc(Brand::getCreatedAt);
            return brandMapper.selectList(queryWrapper);
        } catch (Exception e) {
            log.error("查询品牌列表失败", e);
            return null;
        }
    }

    /**
     * 分页查询品牌
     *
     * @param brand 查询条件（支持id、name、status）
     * @param page 页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 品牌分页列表
     */
    @Override
    public IPage<Brand> getPage(Brand brand, Integer page, Integer pageSize) {
        try {
            // 创建分页参数
            Page<Brand> pageParam = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);
            // 构建查询条件
            LambdaQueryWrapper<Brand> queryWrapper = buildQueryWrapper(brand);
            // 按排序号升序、创建时间降序排列
            queryWrapper.orderByAsc(Brand::getSort)
                    .orderByDesc(Brand::getCreatedAt);
            // 执行分页查询
            return brandMapper.selectPage(pageParam, queryWrapper);
        } catch (Exception e) {
            log.error("分页查询品牌失败", e);
            return null;
        }
    }

    /**
     * 根据状态查询品牌列表
     *
     * @param status 状态（1-启用 0-禁用）
     * @return 品牌列表，按排序号升序、创建时间降序排列
     */
    @Override
    public List<Brand> getByStatus(Integer status) {
        try {
            return brandMapper.selectByStatus(status);
        } catch (Exception e) {
            log.error("根据状态查询品牌列表失败，状态: {}", status, e);
            return null;
        }
    }

    /**
     * 根据品牌名称模糊查询品牌列表
     *
     * @param name 品牌名称（支持模糊匹配）
     * @return 品牌列表，只返回启用状态的品牌
     */
    @Override
    public List<Brand> getByNameLike(String name) {
        try {
            return brandMapper.selectByNameLike(name);
        } catch (Exception e) {
            log.error("根据品牌名称模糊查询失败，品牌名称: {}", name, e);
            return null;
        }
    }

    /**
     * 按排序号查询品牌列表
     *
     * @return 品牌列表，只返回启用状态的品牌，按排序号升序排列
     */
    @Override
    public List<Brand> getBySort() {
        try {
            return brandMapper.selectBySort();
        } catch (Exception e) {
            log.error("按排序号查询品牌列表失败", e);
            return null;
        }
    }

    /**
     * 构建查询条件
     *
     * @param brand 查询条件对象
     * @return LambdaQueryWrapper查询构造器
     */
    private LambdaQueryWrapper<Brand> buildQueryWrapper(Brand brand) {
        LambdaQueryWrapper<Brand> queryWrapper = new LambdaQueryWrapper<>();
        // 如果查询条件为空，直接返回空条件
        if (brand == null) {
            return queryWrapper;
        }
        // 按ID精确查询
        if (brand.getId() != null) {
            queryWrapper.eq(Brand::getId, brand.getId());
        }
        // 按品牌名称模糊查询
        if (StringUtils.hasText(brand.getName())) {
            queryWrapper.like(Brand::getName, brand.getName());
        }
        // 按状态精确查询
        if (brand.getStatus() != null) {
            queryWrapper.eq(Brand::getStatus, brand.getStatus());
        }
        return queryWrapper;
    }
}
