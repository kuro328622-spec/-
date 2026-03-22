package com.example.demo.controller;

import com.example.demo.entity.FinanceRecord;
import com.example.demo.service.FinanceRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 财务明细管理 Controller 已修复路径以匹配前端 financialApi.js 定义
 */
@RestController
@RequestMapping("/api/financial") // 修复点 1：对齐 API 模块基础路径
public class FinanceRecordController {

    @Autowired
    private FinanceRecordService financeService;

    /**
     * 新增财务记录 匹配前端 addFinancialRecord -> POST /api/financial/add
     */
    @PostMapping("/add") // 修复点 2：增加 /add 子路径
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<FinanceRecord> addRecord(@RequestBody FinanceRecord record) {
        // 执行保存
        FinanceRecord savedRecord = financeService.save(record);
        return ResponseEntity.ok(savedRecord);
    }

    /**
     * 获取所有财务记录 匹配前端 getFinancialList -> GET /api/financial/list
     */
    @GetMapping("/list") // 修复点 3：增加 /list 子路径
    public List<FinanceRecord> getAllRecords() {
        return financeService.findAll();
    }

    /**
     * 删除财务记录 匹配前端 deleteFinancialRecord -> DELETE /api/financial/delete/{id}
     */
    @DeleteMapping("/delete/{id}") // 修复点 4：增加 /delete 前缀
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> deleteRecord(@PathVariable Long id) {
        Optional<FinanceRecord> existingRecord = financeService.findById(id);
        if (existingRecord.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            financeService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            // 返回业务异常信息（如：禁止删除自动同步记录）
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
