package com.example.demo.controller;

import com.example.demo.entity.HStoreInventoryFeed;
import com.example.demo.service.HStoreInventoryFeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 饲料总库存管理 Controller 路径规范对齐：/api/inventory/feed
 */
@CrossOrigin
@RestController
@RequestMapping("/api/inventory/feed")
public class HStoreInventoryFeedController {

    @Autowired
    private HStoreInventoryFeedService inventoryService;

    /**
     * 获取饲料总库存列表 前端请求: GET /api/inventory/feed
     */
    @GetMapping
    public List<HStoreInventoryFeed> getAllInventory() {
        return inventoryService.findAll();
    }

    /**
     * 修改饲料库存记录的警戒数量 前端请求: PUT /api/inventory/feed/{id}/alert?alertQuantity=值
     */
    @PutMapping("/{id}/alert")
    public HStoreInventoryFeed updateAlertQuantity(
            @PathVariable Long id,
            @RequestParam BigDecimal alertQuantity
    ) {
        return inventoryService.updateAlertQuantity(id, alertQuantity);
    }
}
