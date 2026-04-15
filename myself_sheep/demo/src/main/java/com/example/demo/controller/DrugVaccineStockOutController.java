package com.example.demo.controller;

import com.example.demo.dto.OutboundDTO;
import com.example.demo.dto.OutboundUpdateDTO;
import com.example.demo.dto.StockOutQueryDTO;
import com.example.demo.entity.DrugVaccineStockOut;
import com.example.demo.entity.SysUser;
import com.example.demo.service.DrugVaccineStockOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock/out")
public class DrugVaccineStockOutController {

    @Autowired
    private DrugVaccineStockOutService stockOutService;

    private SysUser getCurrentUser(String fallbackStaffName) {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof SysUser) {
                return (SysUser) principal;
            }
        } catch (Exception e) {
            System.err.println("SecurityContext 获取用户失败: " + e.getMessage());
        }
        SysUser fallbackUser = new SysUser();
        fallbackUser.setUsername(fallbackStaffName != null ? fallbackStaffName : "系统管理员");
        return fallbackUser;
    }

    @PostMapping("/list")
    public List<DrugVaccineStockOut> listStockOut(@RequestBody(required = false) StockOutQueryDTO queryDTO) {
        return stockOutService.getStockOutByCondition(queryDTO);
    }

    @PostMapping("/create")
    public DrugVaccineStockOut createStockOut(@RequestBody OutboundDTO dto) throws Exception {
        return stockOutService.createStockOut(dto, getCurrentUser(dto.getOutStaff()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{id}")
    public DrugVaccineStockOut updateStockOut(@PathVariable("id") Long stockOutId,
            @RequestBody OutboundUpdateDTO dto) throws Exception {
        // 逻辑已下沉到 Service，这里不再手动计算 diff 和调用 inventoryService
        return stockOutService.updateStockOut(stockOutId, dto, getCurrentUser(null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public String deleteStockOut(@PathVariable("id") Long stockOutId) {
        try {
            // 只需要调用 Service，库存返还已经在 Service 的事务中处理了
            stockOutService.deleteStockOut(stockOutId, getCurrentUser(null));
            return "删除成功";
        } catch (Exception e) {
            e.printStackTrace();
            return "删除失败：" + e.getMessage();
        }
    }
}
