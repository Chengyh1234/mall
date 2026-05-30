package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyh.mallportal.entity.LogisticsCompany;
import com.cyh.mallportal.mapper.LogisticsCompanyMapper;
import com.cyh.mallportal.service.LogisticsCompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流公司服务实现类
 * 提供物流公司业务逻辑的具体实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsCompanyServiceImpl implements LogisticsCompanyService {

    private final LogisticsCompanyMapper logisticsCompanyMapper;

    /**
     * 获取所有启用的物流公司列表
     *
     * @return 物流公司列表
     */
    @Override
    public List<LogisticsCompany> getEnabledList() {
        return logisticsCompanyMapper.selectEnabledList();
    }

    /**
     * 根据ID获取物流公司
     *
     * @param id 物流公司ID
     * @return 物流公司
     */
    @Override
    public LogisticsCompany getById(Long id) {
        return logisticsCompanyMapper.selectById(id);
    }

    /**
     * 根据代码获取物流公司
     *
     * @param code 物流公司代码
     * @return 物流公司
     */
    @Override
    public LogisticsCompany getByCode(String code) {
        return logisticsCompanyMapper.selectByCode(code);
    }

    /**
     * 获取物流公司列表（分页）
     *
     * @param status 状态（可选）
     * @param page   页码
     * @param size   每页条数
     * @return 物流公司列表
     */
    @Override
    public List<LogisticsCompany> getPage(Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<LogisticsCompany> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(LogisticsCompany::getStatus, status);
        }
        wrapper.orderByAsc(LogisticsCompany::getSort);

        int offset = (page - 1) * size;
        wrapper.last("LIMIT " + offset + ", " + size);

        return logisticsCompanyMapper.selectList(wrapper);
    }

    /**
     * 获取物流公司总数
     *
     * @param status 状态（可选）
     * @return 总数
     */
    @Override
    public int count(Integer status) {
        LambdaQueryWrapper<LogisticsCompany> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(LogisticsCompany::getStatus, status);
        }
        long total = logisticsCompanyMapper.selectCount(wrapper);
        return (int) total;
    }

    /**
     * 新增物流公司
     *
     * @param company 物流公司实体
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(LogisticsCompany company) {
        log.info("新增物流公司: {}", company.getName());

        // 检查code是否重复
        LogisticsCompany exist = logisticsCompanyMapper.selectByCode(company.getCode());
        if (exist != null) {
            log.warn("物流公司代码已存在: {}", company.getCode());
            return false;
        }

        if (company.getSort() == null) {
            company.setSort(0);
        }
        if (company.getStatus() == null) {
            company.setStatus(1);
        }
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());

        int rows = logisticsCompanyMapper.insert(company);
        log.info("新增物流公司成功, ID: {}", company.getId());
        return rows > 0;
    }

    /**
     * 更新物流公司
     *
     * @param company 物流公司实体
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(LogisticsCompany company) {
        log.info("更新物流公司: {}", company.getId());

        LogisticsCompany exist = logisticsCompanyMapper.selectById(company.getId());
        if (exist == null) {
            log.warn("物流公司不存在: {}", company.getId());
            return false;
        }

        // 如果修改了code，检查是否重复
        if (company.getCode() != null && !company.getCode().equals(exist.getCode())) {
            LogisticsCompany byCode = logisticsCompanyMapper.selectByCode(company.getCode());
            if (byCode != null) {
                log.warn("物流公司代码已存在: {}", company.getCode());
                return false;
            }
        }

        company.setUpdatedAt(LocalDateTime.now());
        int rows = logisticsCompanyMapper.updateById(company);

        if (rows > 0) {
            log.info("更新物流公司成功: {}", company.getId());
        }
        return rows > 0;
    }

    /**
     * 删除物流公司
     *
     * @param id 物流公司ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        log.info("删除物流公司: {}", id);

        LogisticsCompany exist = logisticsCompanyMapper.selectById(id);
        if (exist == null) {
            log.warn("物流公司不存在: {}", id);
            return false;
        }

        int rows = logisticsCompanyMapper.deleteById(id);
        if (rows > 0) {
            log.info("删除物流公司成功: {}", id);
        }
        return rows > 0;
    }

    /**
     * 更新状态
     *
     * @param id     物流公司ID
     * @param status 状态
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        log.info("更新物流公司状态: {}, 状态: {}", id, status);

        LogisticsCompany company = logisticsCompanyMapper.selectById(id);
        if (company == null) {
            log.warn("物流公司不存在: {}", id);
            return false;
        }

        company.setStatus(status);
        company.setUpdatedAt(LocalDateTime.now());

        int rows = logisticsCompanyMapper.updateById(company);
        return rows > 0;
    }
}