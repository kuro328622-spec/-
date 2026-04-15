package com.example.demo.controller;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.FeedStockIn;
import com.example.demo.service.FeedStockInService;

/**
 * 饲料入库管理 Controller 参考 DrugVaccineStockInController 逻辑，保持权限与操作流程一致
 */
@RestController
@RequestMapping("/api/feed/stock-in")
public class FeedStockInController {

    @Autowired
    private FeedStockInService stockInService;

    /**
     * 新增饲料入库记录 权限：允许管理员(ROLE_ADMIN)和普通用户(ROLE_USER)执行
     */
    @PostMapping("")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<FeedStockIn> addStockIn(@RequestBody FeedStockIn stockIn) {
        // 1. 自动生成批次号（如果前端没传）
        if (stockIn.getProductionBatch() == null || stockIn.getProductionBatch().trim().isEmpty()) {
            String uniqueBatchNumber = UUID.randomUUID().toString().replaceAll("-", "");
            stockIn.setProductionBatch(uniqueBatchNumber);
        }

        // 2. 自动获取并设置当前操作员
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String currentUsername = authentication.getName();
            stockIn.setOperator(currentUsername);
        }

        // 3. 执行保存（Service 内部会自动联动更新总库存）
        FeedStockIn savedStockIn = stockInService.save(stockIn);
        return ResponseEntity.ok(savedStockIn);
    }

    /**
     * 获取所有饲料入库记录 权限：登录用户均可查看
     */
    @GetMapping("")
    public List<FeedStockIn> getAllStockIn() {
        return stockInService.findAll();
    }

    /**
     * 更新饲料入库记录 权限锁定：仅限管理员(ROLE_ADMIN)操作
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<FeedStockIn> updateStockIn(
            @PathVariable Long id,
            @RequestBody FeedStockIn stockInDetails) {

        Optional<FeedStockIn> existingStockInOpt = stockInService.findById(id);
        if (existingStockInOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        FeedStockIn updatedStockIn = stockInService.update(id, stockInDetails);
        return ResponseEntity.ok(updatedStockIn);
    }

    /**
     * 删除饲料入库记录 权限锁定：仅限管理员(ROLE_ADMIN)操作
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteStockIn(@PathVariable Long id) {
        Optional<FeedStockIn> existingStockInOpt = stockInService.findById(id);
        if (existingStockInOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        stockInService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
