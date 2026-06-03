package com.cyh.mallportal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallportal.entity.Banner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 轮播图Mapper接口
 */
@Mapper
public interface BannerMapper extends BaseMapper<Banner> {

    /**
     * 获取启用的轮播图列表
     *
     * @return 启用的轮播图列表，按排序号升序、创建时间降序排列
     */
    List<Banner> selectActive();
}