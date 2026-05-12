package com.cyh.mallportal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallportal.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类Mapper接口
 * 提供分类数据访问操作
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 根据父分类ID查询子分类
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<Category> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 根据分类级别查询
     *
     * @param level 分类级别（1-一级分类，2-二级分类，3-三级分类）
     * @return 分类列表
     */
    List<Category> selectByLevel(@Param("level") Integer level);

    /**
     * 根据状态查询分类
     *
     * @param status 状态（1-启用，0-禁用）
     * @return 分类列表
     */
    List<Category> selectByStatus(@Param("status") Integer status);
}
