package com.example.demo.service.impl;

import com.example.demo.entity.HStoreInventoryDrugVaccine;
import com.example.demo.entity.DrugVaccineStockIn;
import com.example.demo.repository.HStoreInventoryDrugVaccineRepository;
import com.example.demo.service.HStoreInventoryDrugVaccineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 总库存Service实现类 修复点： 1. 所有查询采用 厂家 + 名称 + 类别 的精确匹配 2. syncInventoryFromStockIn
 * 修改返回类型为 BigDecimal，解决调用处的类型不兼容报错
 */
@Service
public class HStoreInventoryDrugVaccineServiceImpl implements HStoreInventoryDrugVaccineService {

    @Autowired
    private HStoreInventoryDrugVaccineRepository inventoryRepository;

    // ==================== 查询和警戒量 ====================
    @Override
    public List<HStoreInventoryDrugVaccine> findAll() {
        return inventoryRepository.findAll();
    }

    @Override
    @Transactional
    public HStoreInventoryDrugVaccine updateAlertQuantity(Long id, BigDecimal alertQuantity) {
        HStoreInventoryDrugVaccine inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("库存记录不存在"));
        inventory.setAlert(alertQuantity);
        return inventoryRepository.save(inventory);
    }

    // ==================== 出库操作 ====================
    @Override
    @Transactional
    public HStoreInventoryDrugVaccine reduceStock(String manufacturer, String name, String category, BigDecimal quantityChange) {
        if (quantityChange.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("出库数量必须大于0");
        }

        Optional<HStoreInventoryDrugVaccine> inventoryOpt = inventoryRepository
                .findByManufacturerAndNameAndCategory(manufacturer, name, category);

        if (inventoryOpt.isEmpty()) {
            throw new RuntimeException("未找到对应库存记录（厂家、名称及类别不匹配）");
        }

        HStoreInventoryDrugVaccine inventory = inventoryOpt.get();
        BigDecimal finalQty = inventory.getQuantity().subtract(quantityChange);

        if (finalQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("库存不足，当前库存: " + inventory.getQuantity());
        }

        inventory.setQuantity(finalQty);
        inventory.setOutTime(new Date());
        return inventoryRepository.save(inventory);
    }

    // ==================== 入库同步操作 ====================
    /**
     * 【核心修复】修改返回值为 BigDecimal，以便上层 Service 获取最新库存总量
     */
    @Override
    @Transactional
    public BigDecimal syncInventoryFromStockIn(DrugVaccineStockIn stockIn) {
        Optional<HStoreInventoryDrugVaccine> inventoryOpt = inventoryRepository
                .findByManufacturerAndNameAndCategory(
                        stockIn.getManufacturer(),
                        stockIn.getName(),
                        stockIn.getCategory()
                );

        HStoreInventoryDrugVaccine inventory;
        BigDecimal stockQty = stockIn.getStockInQuantity();
        BigDecimal stockPrice = stockIn.getUnitPrice();
        BigDecimal stockFreight = stockIn.getFreightFee() != null ? stockIn.getFreightFee() : BigDecimal.ZERO;

        if (inventoryOpt.isEmpty()) {
            inventory = new HStoreInventoryDrugVaccine();
            inventory.setManufacturer(stockIn.getManufacturer());
            inventory.setName(stockIn.getName());
            inventory.setCategory(stockIn.getCategory());
            inventory.setComponentType(stockIn.getComponentType());
            inventory.setUnitOfMeasure(stockIn.getUnitOfMeasure());
            inventory.setQuantity(stockQty);

            // 计算包含运费的单价
            BigDecimal totalPrice = stockQty.multiply(stockPrice).add(stockFreight);
            inventory.setUnitPrice(totalPrice.divide(stockQty, 2, RoundingMode.HALF_UP));
            inventory.setFDate(new Date());
            inventory.setOutTime(new Date());
            inventory.setAlert(new BigDecimal("10.00")); // 设置默认警戒线
        } else {
            inventory = inventoryOpt.get();
            BigDecimal oldQty = inventory.getQuantity();
            BigDecimal oldTotal = oldQty.multiply(inventory.getUnitPrice());
            BigDecimal newTotal = stockQty.multiply(stockPrice).add(stockFreight);
            BigDecimal finalQty = oldQty.add(stockQty);

            inventory.setQuantity(finalQty);
            // 更新加权平均单价
            inventory.setUnitPrice(oldTotal.add(newTotal).divide(finalQty, 2, RoundingMode.HALF_UP));
            inventory.setOutTime(new Date());
        }

        HStoreInventoryDrugVaccine saved = inventoryRepository.save(inventory);
        return saved.getQuantity(); // 返回最新库存总量
    }

    // ==================== 更新入库记录同步操作 ====================
    @Override
    @Transactional
    public void syncInventoryOnUpdate(DrugVaccineStockIn oldStockIn, DrugVaccineStockIn newStockIn) {
        Optional<HStoreInventoryDrugVaccine> inventoryOpt = inventoryRepository
                .findByManufacturerAndNameAndCategory(
                        oldStockIn.getManufacturer(),
                        oldStockIn.getName(),
                        oldStockIn.getCategory()
                );

        if (inventoryOpt.isEmpty()) {
            throw new RuntimeException("未找到对应库存记录，无法同步更新");
        }

        HStoreInventoryDrugVaccine inventory = inventoryOpt.get();

        BigDecimal oldQty = oldStockIn.getStockInQuantity();
        BigDecimal newQty = newStockIn.getStockInQuantity();
        BigDecimal diff = newQty.subtract(oldQty);

        BigDecimal finalQty = inventory.getQuantity().add(diff);
        if (finalQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("调整后的库存数量不能小于0");
        }

        inventory.setQuantity(finalQty);
        inventory.setOutTime(new Date());
        inventory.setUnitPrice(newStockIn.getUnitPrice()); // 更新为最新的单价
        inventoryRepository.save(inventory);
    }

    // ==================== 删除入库记录同步操作 ====================
    @Override
    @Transactional
    public void syncInventoryOnDelete(DrugVaccineStockIn stockIn) {
        Optional<HStoreInventoryDrugVaccine> inventoryOpt = inventoryRepository
                .findByManufacturerAndNameAndCategory(
                        stockIn.getManufacturer(),
                        stockIn.getName(),
                        stockIn.getCategory()
                );

        if (inventoryOpt.isEmpty()) {
            return;
        }

        HStoreInventoryDrugVaccine inventory = inventoryOpt.get();
        BigDecimal finalQty = inventory.getQuantity().subtract(stockIn.getStockInQuantity());

        if (finalQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("库存扣减后将小于0，无法删除该入库记录");
        }

        inventory.setQuantity(finalQty);
        inventory.setOutTime(new Date());
        inventoryRepository.save(inventory);
    }
}
