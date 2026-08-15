package com.cyh.mallportal.service;

import com.cyh.mallportal.dto.AttributeDto;
import com.cyh.mallportal.dto.AttributeValueDto;
import com.cyh.mallportal.vo.AttrValueVo;
import com.cyh.mallportal.vo.AttributeVo;

import java.util.List;

/**
 * 属性管理 Service 接口
 * 提供运营管理员和超级管理员对属性及属性值的增删改查功能
 */
public interface AttributeManageService {

    // ==================== 属性管理 ====================

    /**
     * 新增属性
     *
     * @param dto 属性信息
     * @return 新增的属性ID
     */
    Long addAttribute(AttributeDto dto);

    /**
     * 修改属性
     * 如果该属性下已有属性值，则禁止修改属性类型（attrType），但可以修改名称和排序
     *
     * @param id  属性ID
     * @param dto 新的属性信息
     * @return 是否修改成功
     */
    boolean updateAttribute(Long id, AttributeDto dto);

    /**
     * 删除属性
     * 如果该属性下存在属性值，则禁止删除
     *
     * @param id 属性ID
     * @return 是否删除成功
     */
    boolean deleteAttribute(Long id);

    /**
     * 查询全部属性列表（含属性值）
     *
     * @return 属性列表（每个属性含其下的属性值）
     */
    List<AttributeVo> listAllAttributes();

    /**
     * 查询单个属性详情（含属性值）
     *
     * @param id 属性ID
     * @return 属性详情（含属性值列表）
     */
    AttributeVo getAttributeDetail(Long id);

    // ==================== 属性值管理 ====================

    /**
     * 在指定属性下新增属性值
     *
     * @param dto 属性值信息
     * @return 新增的属性值ID
     */
    Long addAttributeValue(AttributeValueDto dto);

    /**
     * 修改属性值
     * 如果该属性值已被 SPU 或 SKU 引用，则禁止修改
     *
     * @param id  属性值ID
     * @param dto 新的属性值信息
     * @return 是否修改成功
     */
    boolean updateAttributeValue(Long id, AttributeValueDto dto);

    /**
     * 删除属性值
     * 如果该属性值已被 SPU 或 SKU 引用，则禁止删除
     *
     * @param id 属性值ID
     * @return 是否删除成功
     */
    boolean deleteAttributeValue(Long id);

    /**
     * 根据属性ID查询其下的所有属性值
     *
     * @param attrId 属性ID
     * @return 属性值列表
     */
    List<AttrValueVo> listAttributeValues(Long attrId);
}