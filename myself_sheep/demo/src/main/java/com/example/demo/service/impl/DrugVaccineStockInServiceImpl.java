package com.example.demo.service.impl;

import com.example.demo.entity.DrugVaccineStockIn;
import com.example.demo.entity.FinanceRecord;
import com.example.demo.repository.DrugVaccineStockInRepository;
import com.example.demo.repository.FinanceRecordRepository;
import com.example.demo.service.DrugVaccineStockInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class DrugVaccineStockInServiceImpl implements DrugVaccineStockInService {

    @Autowired
    private DrugVaccineStockInRepository stockInRepository;

    @Autowired
    private HStoreInventoryDrugVaccineServiceImpl inventoryService;

    @Autowired
    private FinanceRecordRepository financeRepository;

    @Override
    @Transactional
    public DrugVaccineStockIn save(DrugVaccineStockIn stockIn) {
        // 1. 验证入库数量
        if (stockIn.getStockInQuantity() == null || stockIn.getStockInQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("入库数量必须大于0");
        }

        // 2. 核心计算逻辑：强制计算总价（单价 * 数量 + 运费）
        BigDecimal unitPrice = stockIn.getUnitPrice() != null ? stockIn.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal quantity = stockIn.getStockInQuantity();
        BigDecimal freight = stockIn.getFreightFee() != null ? stockIn.getFreightFee() : BigDecimal.ZERO;
        // 设置总价：确保财务记录金额包含运费
        stockIn.setTotalPrice(unitPrice.multiply(quantity).add(freight));

        if (stockIn.getStockInTime() == null) {
            stockIn.setStockInTime(new Date());
        }

        // 3. 同步库存
        BigDecimal currentTotal = inventoryService.syncInventoryFromStockIn(stockIn);
        stockIn.setTotalInventory(currentTotal);

        // 4. 保存入库记录
        DrugVaccineStockIn savedStock = stockInRepository.save(stockIn);

        // 5. 联动生成财务记录
        FinanceRecord finance = new FinanceRecord();
        finance.setRecordDate(savedStock.getStockInTime()); // 使用入库时间作为财务记录日期
        finance.setRecordType("支出");
        finance.setCategory("购入药品/疫苗");
        finance.setAmount(savedStock.getTotalPrice()); // 此时 totalPrice 已包含运费
        finance.setIsAuto(1);
        finance.setSourceTable("drug_vaccine_stock_in");
        finance.setSourceId(savedStock.getId());
        finance.setRemark("关联药品疫苗入库单号：" + savedStock.getId() + " (含运费：" + freight + ")");

        financeRepository.save(finance);

        return savedStock;
    }

    @Override
    @Transactional
    public DrugVaccineStockIn update(Long id, DrugVaccineStockIn newStockIn) {
        DrugVaccineStockIn oldStockIn = findById(id)
                .orElseThrow(() -> new RuntimeException("入库记录不存在，ID：" + id));

        if (newStockIn.getStockInQuantity() == null || newStockIn.getStockInQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("入库数量必须大于0");
        }

        // 1. 同步库存变更
        inventoryService.syncInventoryOnUpdate(oldStockIn, newStockIn);

        // 2. 更新基础属性
        oldStockIn.setName(newStockIn.getName());
        oldStockIn.setManufacturer(newStockIn.getManufacturer());
        oldStockIn.setCategory(newStockIn.getCategory());
        oldStockIn.setComponentType(newStockIn.getComponentType());
        oldStockIn.setUsage(newStockIn.getUsage());
        oldStockIn.setProductionDate(newStockIn.getProductionDate());
        oldStockIn.setExpirationDate(newStockIn.getExpirationDate());
        oldStockIn.setProductionBatch(newStockIn.getProductionBatch());
        oldStockIn.setUnitOfMeasure(newStockIn.getUnitOfMeasure());
        oldStockIn.setStockInQuantity(newStockIn.getStockInQuantity());
        oldStockIn.setUnitPrice(newStockIn.getUnitPrice());
        oldStockIn.setFreightFee(newStockIn.getFreightFee());
        oldStockIn.setConvertedUnitPrice(newStockIn.getConvertedUnitPrice());
        oldStockIn.setOperator(newStockIn.getOperator());
        oldStockIn.setStockInTime(new Date());

        // 3. 重新计算总价
        BigDecimal up = oldStockIn.getUnitPrice() != null ? oldStockIn.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal qty = oldStockIn.getStockInQuantity();
        BigDecimal ff = oldStockIn.getFreightFee() != null ? oldStockIn.getFreightFee() : BigDecimal.ZERO;
        oldStockIn.setTotalPrice(up.multiply(qty).add(ff));

        // 4. 保存更新
        DrugVaccineStockIn updatedStock = stockInRepository.save(oldStockIn);

        // 5. 同步更新关联的财务记录
        financeRepository.findAll().stream()
                .filter(f -> "drug_vaccine_stock_in".equals(f.getSourceTable()) && id.equals(f.getSourceId()))
                .forEach(f -> {
                    f.setAmount(updatedStock.getTotalPrice());
                    f.setRecordDate(updatedStock.getStockInTime());
                    f.setRemark("关联药品疫苗入库单号：" + updatedStock.getId() + " (已更新，含运费：" + ff + ")");
                    financeRepository.save(f);
                });

        return updatedStock;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        DrugVaccineStockIn stockIn = findById(id)
                .orElseThrow(() -> new RuntimeException("入库记录不存在，ID：" + id));

        // 1. 联动删除财务记录
        financeRepository.findAll().stream()
                .filter(f -> "drug_vaccine_stock_in".equals(f.getSourceTable()) && id.equals(f.getSourceId()))
                .forEach(f -> financeRepository.delete(f));

        // 2. 同步库存并删除入库记录
        inventoryService.syncInventoryOnDelete(stockIn);
        stockInRepository.deleteById(id);
    }

    @Override
    public List<DrugVaccineStockIn> findAll() {
        return stockInRepository.findAll();
    }

    @Override
    public Optional<DrugVaccineStockIn> findById(Long id) {
        return stockInRepository.findById(id);
    }
}
