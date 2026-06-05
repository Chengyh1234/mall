package com.cyh.mallportal.service;

import com.cyh.mallportal.dto.CategoryAttributeBindDto;
import com.cyh.mallportal.dto.CategoryAttributeSortDto;
import com.cyh.mallportal.entity.Attribute;
import com.cyh.mallportal.vo.CategoryAttributeBoundVo;

import java.util.List;

/**
 * 分类-属性绑定管理 Service 接口
 *
 * 提供运营管理员和超级管理员对分类与属性绑定关系的管理功能
 */
public interface CategoryAttributeManageService {

    /**
     * 获取指定分类已绑定的属性列表（含 SPU 引用状态）
     *
     * @param categoryId 分类ID
     * @return 已绑定的属性列表
     */
    List<CategoryAttributeBoundVo> getBoundAttributes(Long categoryId);

    /**
     * 获取指定分类尚未绑定的可用属性列表
     *
     * @param categoryId 分类ID
     * @return 未绑定的属性列表
     */
    List<Attribute> getAvailableAttributes(Long categoryId);

    /**
     * 绑定属性到分类
     *
     * @param dto 绑定信息（分类ID、属性ID、排序）
     * @return 绑定记录ID
     */
    Long bindAttribute(CategoryAttributeBindDto dto);

    /**
     * 修改绑定记录的排序
     *
     * @param id  绑定记录ID
     * @param dto 新的排序信息
     * @return 是否修改成功
     */
    boolean updateSort(Long id, CategoryAttributeSortDto dto);

    /**
     * 解绑属性
     * 如果该分类下的 SPU 已使用了该属性，则禁止解绑
     *
     * @param id 绑定记录ID
     * @return 是否解绑成功
     */
    boolean unbindAttribute(Long id);
}