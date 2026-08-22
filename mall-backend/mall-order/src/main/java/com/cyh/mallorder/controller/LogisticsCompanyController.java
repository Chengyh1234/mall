package com.cyh.mallorder.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallcommon.validation.group.Create;
import com.cyh.mallcommon.validation.group.Update;
import com.cyh.mallorder.dto.LogisticsCompanyDto;
import com.cyh.mallorder.entity.LogisticsCompany;
import com.cyh.mallorder.service.LogisticsCompanyService;
import com.cyh.mallorder.vo.LogisticsCompanyAdminVo;
import com.cyh.mallorder.vo.LogisticsCompanyVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 物流公司管理控制器
 * 提供物流公司的增删改查及启禁用功能
 */
@RestController
@RequestMapping("/logistics")
@RequiredArgsConstructor
public class LogisticsCompanyController {

    private final LogisticsCompanyService logisticsCompanyService;

    /**
     * 获取所有启用的物流公司列表（前端下单时使用）
     *
     * @return 物流公司列表
     */
    @GetMapping("/list")
    public Result<List<LogisticsCompanyVo>> getEnabledList() {
        List<LogisticsCompany> list = logisticsCompanyService.getEnabledList();
        List<LogisticsCompanyVo> voList = list.stream()
                .map(LogisticsCompanyVo::fromLogisticsCompany)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 获取物流公司详情
     *
     * @param id 物流公司ID
     * @return 物流公司详情
     */
    @GetMapping("/detail/{id}")
    public Result<LogisticsCompanyVo> getById(@PathVariable Long id) {
        LogisticsCompany company = logisticsCompanyService.getById(id);
        if (company != null) {
            return Result.success(LogisticsCompanyVo.fromLogisticsCompany(company));
        }
        return Result.error("物流公司不存在");
    }

    /**
     * 根据代码获取物流公司
     *
     * @param code 物流公司代码
     * @return 物流公司详情
     */
    @GetMapping("/code/{code}")
    public Result<LogisticsCompanyVo> getByCode(@PathVariable String code) {
        LogisticsCompany company = logisticsCompanyService.getByCode(code);
        if (company != null) {
            return Result.success(LogisticsCompanyVo.fromLogisticsCompany(company));
        }
        return Result.error("物流公司不存在");
    }

    /**
     * 分页获取物流公司列表（管理后台使用）
     *
     * @param status 状态（可选）
     * @param page   页码
     * @param size   每页条数
     * @return 分页结果
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Map<String, Object>> getPage(@RequestParam(required = false) Integer status,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size) {
        List<LogisticsCompany> list = logisticsCompanyService.getPage(status, page, size);
        int total = logisticsCompanyService.count(status);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list.stream()
                .map(LogisticsCompanyAdminVo::fromLogisticsCompany)
                .collect(Collectors.toList()));
        data.put("page", page);
        data.put("size", size);
        data.put("total", total);

        return Result.success(data);
    }

    /**
     * 新增物流公司
     *
     * @param dto 物流公司DTO
     * @return 新增结果
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Map<String, Object>> add(@RequestBody @Validated(Create.class) LogisticsCompanyDto dto) {
        LogisticsCompany company = new LogisticsCompany();
        company.setName(dto.getName());
        company.setCode(dto.getCode());
        company.setLogo(dto.getLogo());
        company.setWebsite(dto.getWebsite());
        company.setPhone(dto.getPhone());
        company.setSort(dto.getSort());
        company.setStatus(dto.getStatus());

        boolean success = logisticsCompanyService.add(company);
        if (success) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", company.getId());
            return Result.success("添加成功", data);
        }
        return Result.error("添加失败，物流公司代码可能已存在");
    }

    /**
     * 更新物流公司
     *
     * @param dto 物流公司DTO
     * @return 更新结果
     */
    @PutMapping("/update")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> update(@RequestBody @Validated(Update.class) LogisticsCompanyDto dto) {
        LogisticsCompany company = new LogisticsCompany();
        company.setId(dto.getId());
        company.setName(dto.getName());
        company.setCode(dto.getCode());
        company.setLogo(dto.getLogo());
        company.setWebsite(dto.getWebsite());
        company.setPhone(dto.getPhone());
        company.setSort(dto.getSort());
        company.setStatus(dto.getStatus());

        boolean success = logisticsCompanyService.update(company);
        if (success) {
            return Result.success("更新成功", null);
        }
        return Result.error("更新失败，物流公司不存在或代码已存在");
    }

    /**
     * 删除物流公司
     *
     * @param id 物流公司ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = logisticsCompanyService.delete(id);
        if (success) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败，物流公司不存在");
    }

    /**
     * 启用/禁用物流公司
     *
     * @param id     物流公司ID
     * @param status 状态（1-启用 0-禁用）
     * @return 更新结果
     */
    @PutMapping("/status/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status != 0 && status != 1) {
            return Result.error("状态值不正确");
        }

        boolean success = logisticsCompanyService.updateStatus(id, status);
        if (success) {
            return Result.success("状态更新成功", null);
        }
        return Result.error("更新失败，物流公司不存在");
    }
}