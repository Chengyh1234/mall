package com.cyh.mallorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyh.mallorder.entity.LogisticsCompany;
import com.cyh.mallorder.mapper.LogisticsCompanyMapper;
import com.cyh.mallorder.service.LogisticsCompanyService;
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
     * 获取启用的物流公司列表（按排序号升序）
     */
    @Override
    public List<LogisticsCompany> getEnabledList() {
        return logisticsCompanyMapper.selectEnabledList();
    }

    /**
     * 根据 ID 查询物流公司
     */
    @Override
    public LogisticsCompany getById(Long id) {
        return logisticsCompanyMapper.selectById(id);
    }

    /**
     * 根据编码查询物流公司
     */
    @Override
    public LogisticsCompany getByCode(String code) {
        return logisticsCompanyMapper.selectByCode(code);
    }

    /**
     * 分页查询物流公司列表（支持按状态筛选）
     * 按排序号升序排列。
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
     * 统计物流公司数量（支持按状态筛选）
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
     * 新增物流公司（管理后台）
     * 校验编码唯一性，自动填充排序和状态默认值。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(LogisticsCompany company) {
        log.info("新增物流公司: {}", company.getName());

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
     * 更新物流公司信息（管理后台）
     * 修改编码时校验新编码的唯一性。
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
     * 删除物流公司（管理后台）
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
     * 更新物流公司启用/禁用状态（管理后台）
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