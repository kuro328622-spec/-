// src/main/java/com/example/demo/controller/DrugVaccineStockInController.java
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

import com.example.demo.entity.DrugVaccineStockIn;
import com.example.demo.service.DrugVaccineStockInService;

/**
 * 药品/疫苗入库管理 Controller
 */
@RestController
@RequestMapping("/api/drug-vaccine/stock-in")
public class DrugVaccineStockInController {

    @Autowired
    private DrugVaccineStockInService stockInService;

    /**
     * 新增入库记录 权限修复：允许管理员和普通用户执行入库操作
     */
    @PostMapping("")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<DrugVaccineStockIn> addStockIn(@RequestBody DrugVaccineStockIn stockIn) {
        if (stockIn.getProductionBatch() == null || stockIn.getProductionBatch().trim().isEmpty()) {
            String uniqueBatchNumber = UUID.randomUUID().toString().replaceAll("-", "");
            stockIn.setProductionBatch(uniqueBatchNumber);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String currentUsername = authentication.getName();
            stockIn.setOperator(currentUsername);
        }

        DrugVaccineStockIn savedStockIn = stockInService.save(stockIn);
        return ResponseEntity.ok(savedStockIn);
    }

    /**
     * 获取所有入库记录 所有人登录后均可查看列表
     */
    @GetMapping("")
    public List<DrugVaccineStockIn> getAllStockIn() {
        return stockInService.findAll();
    }

    /**
     * 更新入库记录 权限锁定：仅限管理员(ROLE_ADMIN)操作，普通用户录错了必须找管理员
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<DrugVaccineStockIn> updateStockIn(
            @PathVariable Long id,
            @RequestBody DrugVaccineStockIn stockInDetails) {

        Optional<DrugVaccineStockIn> existingStockInOpt = stockInService.findById(id);
        if (existingStockInOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        DrugVaccineStockIn updatedStockIn = stockInService.update(id, stockInDetails);
        return ResponseEntity.ok(updatedStockIn);
    }

    /**
     * 删除入库记录 权限锁定：仅限管理员(ROLE_ADMIN)操作
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteStockIn(@PathVariable Long id) {
        Optional<DrugVaccineStockIn> existingStockInOpt = stockInService.findById(id);
        if (existingStockInOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        stockInService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
