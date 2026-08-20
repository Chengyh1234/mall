package com.cyh.mallstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallstore.entity.Banner;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 轮播图 Mapper 接口
 * 提供查询启用的轮播图列表
 */
@Mapper
public interface BannerMapper extends BaseMapper<Banner> {

    /**
     * 查询所有启用状态的轮播图，按排序和 ID 降序排列
     */
    List<Banner> selectActive();
}