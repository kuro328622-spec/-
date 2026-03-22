package com.example.demo.controller;

import com.example.demo.entity.HStoreInventoryDrugVaccine;
import com.example.demo.service.HStoreInventoryDrugVaccineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/inventory/medicine")
public class HStoreInventoryDrugVaccineController {

    @Autowired
    private HStoreInventoryDrugVaccineService inventoryService;

    /**
     * 获取总库存列表 前端请求: GET /api/inventory/medicine
     */
    @GetMapping
    public List<HStoreInventoryDrugVaccine> getAllInventory() {
        return inventoryService.findAll();
    }

    /**
     * 修改库存记录的警戒数量 前端请求: PUT /api/inventory/medicine/{id}/alert?alertQuantity=值
     */
    @PutMapping("/{id}/alert")
    public HStoreInventoryDrugVaccine updateAlertQuantity(
            @PathVariable Long id,
            @RequestParam BigDecimal alertQuantity
    ) {
        return inventoryService.updateAlertQuantity(id, alertQuantity);
    }
}
