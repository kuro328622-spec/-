package com.example.demo.controller;

import com.example.demo.dto.StockOutQueryDTO;
import com.example.demo.entity.FeedStockOut;
import com.example.demo.entity.SysUser;
import com.example.demo.service.FeedStockOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feed/stock-out")
public class FeedStockOutController {

    @Autowired
    private FeedStockOutService stockOutService;

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
    public List<FeedStockOut> listStockOut(@RequestBody(required = false) StockOutQueryDTO queryDTO) {
        return stockOutService.getStockOutByCondition(queryDTO);
    }

    @PostMapping("/create")
    public FeedStockOut createStockOut(@RequestBody FeedStockOut feedStockOut) throws Exception {
        feedStockOut.setId(null);
        SysUser currentUser = getCurrentUser(feedStockOut.getOutStaff());
        return stockOutService.createStockOutDirect(feedStockOut, currentUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{id}")
    public FeedStockOut updateStockOut(@PathVariable("id") Long stockOutId,
            @RequestBody FeedStockOut feedStockOut) throws Exception {
        // 获取当前用户用于权限或审计
        SysUser currentUser = getCurrentUser(null);

        // 核心修复：调用专用的 update 方法，而不是 createStockOutDirect
        return stockOutService.updateStockOutDirect(stockOutId, feedStockOut, currentUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public String deleteStockOut(@PathVariable("id") Long stockOutId) {
        try {
            SysUser currentUser = getCurrentUser(null);
            stockOutService.deleteStockOut(stockOutId, currentUser);
            return "删除成功";
        } catch (Exception e) {
            e.printStackTrace();
            return "删除失败：" + e.getMessage();
        }
    }
}
