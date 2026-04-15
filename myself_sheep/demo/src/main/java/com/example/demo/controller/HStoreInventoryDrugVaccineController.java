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
     * 获取总库存列表
     */
    @GetMapping
    public List<HStoreInventoryDrugVaccine> getAllInventory() {
        return inventoryService.findAll();
    }

    /**
     * 修改库存记录的警戒数量
     */
    @PutMapping("/{id}/alert")
    public HStoreInventoryDrugVaccine updateAlertQuantity(
            @PathVariable Long id,
            @RequestParam BigDecimal alertQuantity
    ) {
        return inventoryService.updateAlertQuantity(id, alertQuantity);
    }

    /**
     * ✨ 新增：删除药品库存档案接口 对应前端：await deleteMedicine(Number(row.id));
     */
    @DeleteMapping("/{id}")
    public void deleteMedicine(@PathVariable Long id) {
        inventoryService.deleteById(id);
    }
}
