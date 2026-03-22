package com.example.demo.controller;

import com.example.demo.entity.ManufacturerInfo;
import com.example.demo.service.ManufacturerInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 生产厂家/供应商管理 Controller
 */
@RestController
@RequestMapping("/api/manufacturer")
public class ManufacturerInfoController {

    @Autowired
    private ManufacturerInfoService manufacturerService;

    /**
     * 获取所有厂家名录 所有人登录后均可查看
     */
    @GetMapping("")
    public List<ManufacturerInfo> getAllManufacturers() {
        return manufacturerService.findAll();
    }

    /**
     * 根据名称查找厂家（用于前端入库页面的模糊匹配或自动填充）
     */
    @GetMapping("/search")
    public ResponseEntity<ManufacturerInfo> getByName(@RequestParam String name) {
        return manufacturerService.findByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 新增厂家记录 权限锁定：仅限管理员(ROLE_ADMIN)操作
     */
    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ManufacturerInfo> addManufacturer(@RequestBody ManufacturerInfo manufacturer) {
        // Service层内部已包含唯一性校验逻辑
        ManufacturerInfo savedMan = manufacturerService.save(manufacturer);
        return ResponseEntity.ok(savedMan);
    }

    /**
     * 更新厂家信息 权限锁定：仅限管理员(ROLE_ADMIN)操作
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ManufacturerInfo> updateManufacturer(
            @PathVariable Long id,
            @RequestBody ManufacturerInfo manufacturerDetails) {

        Optional<ManufacturerInfo> existingManOpt = manufacturerService.findById(id);
        if (existingManOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ManufacturerInfo updatedMan = manufacturerService.update(id, manufacturerDetails);
        return ResponseEntity.ok(updatedMan);
    }

    /**
     * 删除厂家记录 权限锁定：仅限管理员(ROLE_ADMIN)操作
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteManufacturer(@PathVariable Long id) {
        Optional<ManufacturerInfo> existingManOpt = manufacturerService.findById(id);
        if (existingManOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        manufacturerService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
