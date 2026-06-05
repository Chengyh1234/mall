package com.cyh.mallportal.service;

import com.cyh.mallportal.entity.Banner;
import com.cyh.mallportal.vo.BannerVO;

import java.util.List;

/**
 * 轮播图服务接口
 * 定义轮播图的新增、删除、修改、查询等业务方法
 */
public interface BannerService {

    /**
     * 新增轮播图
     *
     * @param banner 轮播图信息
     * @return 新增的轮播图ID，失败返回null
     */
    Long add(Banner banner);

    /**
     * 编辑轮播图
     *
     * @param banner 轮播图信息（需包含ID）
     * @return 更新成功返回true，失败返回false
     */
    boolean update(Banner banner);

    /**
     * 删除轮播图（物理删除）
     *
     * @param id 轮播图ID
     * @return 删除成功返回true，失败返回false
     */
    boolean delete(Long id);

    /**
     * 更新轮播图状态
     *
     * @param id     轮播图ID
     * @param status 状态（1-启用 0-禁用）
     * @return 更新成功返回true，失败返回false
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 根据ID获取轮播图详情
     *
     * @param id 轮播图ID
     * @return 轮播图信息，不存在返回null
     */
    Banner getById(Long id);

    /**
     * 获取轮播图列表（管理后台用，支持按状态筛选）
     *
     * @param status 状态（可选，传null查全部）
     * @return 轮播图列表
     */
    List<Banner> getList(Integer status);

    /**
     * 获取启用的轮播图列表（首页展示用，走Redis缓存）
     *
     * @return 启用的轮播图列表，按排序号升序排列
     */
    List<Banner> getActiveList();
}