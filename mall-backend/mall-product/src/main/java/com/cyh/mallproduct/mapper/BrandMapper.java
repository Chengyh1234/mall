package com.cyh.mallproduct.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallproduct.entity.Brand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 品牌Mapper接口
 * 定义品牌数据的数据库操作方法
 */
@Mapper
public interface BrandMapper extends BaseMapper<Brand> {

    /**
     * 根据状态查询品牌列表
     *
     * @param status 状态（1-启用 0-禁用）
     * @return 品牌列表，按排序号升序、创建时间降序排列
     */
    List<Brand> selectByStatus(@Param("status") Integer status);

    /**
     * 根据品牌名称模糊查询品牌列表
     *
     * @param name 品牌名称（支持模糊匹配）
     * @return 品牌列表，只返回启用状态的品牌
     */
    List<Brand> selectByNameLike(@Param("name") String name);

    /**
     * 按排序号查询品牌列表
     *
     * @return 品牌列表，只返回启用状态的品牌，按排序号升序排列
     */
    List<Brand> selectBySort();
}