package com.example.demo.service.impl;

import com.example.demo.entity.HStoreInventoryFeed;
import com.example.demo.entity.FeedStockIn;
import com.example.demo.repository.HStoreInventoryFeedRepository;
import com.example.demo.service.HStoreInventoryFeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class HStoreInventoryFeedServiceImpl implements HStoreInventoryFeedService {

    @Autowired
    private HStoreInventoryFeedRepository inventoryRepository;

    @Override
    public List<HStoreInventoryFeed> findAll() {
        return inventoryRepository.findAll();
    }

    @Override
    @Transactional
    public HStoreInventoryFeed updateAlertQuantity(Long id, BigDecimal alertQuantity) {
        HStoreInventoryFeed inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("饲料库存记录不存在"));
        inventory.setAlert(alertQuantity);
        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public BigDecimal syncInventoryFromStockIn(FeedStockIn stockIn) {
        // 1. 获取清洗后的四个匹配条件
        String mfg = stockIn.getManufacturer().trim();
        String name = stockIn.getName().trim();
        String cat = stockIn.getCategory().trim();
        String comp = stockIn.getComponentType().trim();

        // 使用四维度查询
        Optional<HStoreInventoryFeed> inventoryOpt = inventoryRepository
                .findByManufacturerAndNameAndCategoryAndComponentType(mfg, name, cat, comp);

        HStoreInventoryFeed inventory;
        if (inventoryOpt.isEmpty()) {
            inventory = new HStoreInventoryFeed();
            inventory.setManufacturer(mfg);
            inventory.setName(name);
            inventory.setCategory(cat);
            inventory.setComponentType(comp);
            inventory.setUnitOfMeasure(stockIn.getUnitOfMeasure());
            inventory.setQuantity(stockIn.getStockInQuantity());
            inventory.setUnitPrice(stockIn.getUnitPrice());
            inventory.setFDate(new Date());
            inventory.setAlert(new BigDecimal("10.00"));
        } else {
            inventory = inventoryOpt.get();
            inventory.setQuantity(inventory.getQuantity().add(stockIn.getStockInQuantity()));
        }

        inventory.setOutTime(new Date());
        return inventoryRepository.save(inventory).getQuantity();
    }

    @Override
    @Transactional
    public void syncInventoryOnDelete(FeedStockIn stockIn) {
        // 使用四维度查询
        HStoreInventoryFeed inventory = inventoryRepository
                .findByManufacturerAndNameAndCategoryAndComponentType(
                        stockIn.getManufacturer().trim(),
                        stockIn.getName().trim(),
                        stockIn.getCategory().trim(),
                        stockIn.getComponentType().trim()
                )
                .orElseThrow(() -> new RuntimeException("联动失败：未找到匹配的库存记录"));

        BigDecimal finalQty = inventory.getQuantity().subtract(stockIn.getStockInQuantity());
        if (finalQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("删除失败：当前库存不足以扣减此数量");
        }

        inventory.setQuantity(finalQty);
        inventory.setOutTime(new Date());
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void syncInventoryOnUpdate(FeedStockIn oldStockIn, FeedStockIn newStockIn) {
        // 基于旧的四维度信息定位库存记录
        HStoreInventoryFeed inventory = inventoryRepository
                .findByManufacturerAndNameAndCategoryAndComponentType(
                        oldStockIn.getManufacturer().trim(),
                        oldStockIn.getName().trim(),
                        oldStockIn.getCategory().trim(),
                        oldStockIn.getComponentType().trim()
                )
                .orElseThrow(() -> new RuntimeException("修改失败：找不到对应的库存记录"));

        BigDecimal diff = newStockIn.getStockInQuantity().subtract(oldStockIn.getStockInQuantity());
        BigDecimal finalQty = inventory.getQuantity().add(diff);

        if (finalQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("修改失败：调整后的库存不能小于0");
        }

        inventory.setQuantity(finalQty);
        inventory.setOutTime(new Date());
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public HStoreInventoryFeed reduceStock(String m, String n, String c, BigDecimal q) {
        return null;
    }
}
