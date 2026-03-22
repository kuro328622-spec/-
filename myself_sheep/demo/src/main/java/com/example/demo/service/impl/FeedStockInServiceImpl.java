package com.example.demo.service.impl;

import com.example.demo.entity.FeedStockIn;
import com.example.demo.entity.FinanceRecord;
import com.example.demo.repository.FeedStockInRepository;
import com.example.demo.repository.FinanceRecordRepository;
import com.example.demo.service.FeedStockInService;
import com.example.demo.service.HStoreInventoryFeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class FeedStockInServiceImpl implements FeedStockInService {

    @Autowired
    private FeedStockInRepository stockInRepository;

    @Autowired
    private HStoreInventoryFeedService inventoryService;

    @Autowired
    private FinanceRecordRepository financeRepository;

    @Override
    @Transactional
    public FeedStockIn save(FeedStockIn stockIn) {
        if (stockIn.getStockInQuantity() == null || stockIn.getStockInQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("入库数量必须大于0");
        }

        // 计算总价：(单价 * 数量) + 运费
        BigDecimal unitPrice = stockIn.getUnitPrice() != null ? stockIn.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal quantity = stockIn.getStockInQuantity();
        BigDecimal freight = stockIn.getFreightFee() != null ? stockIn.getFreightFee() : BigDecimal.ZERO;
        stockIn.setTotalPrice(unitPrice.multiply(quantity).add(freight));

        // 如果入库时间为空，设为当前系统时间
        if (stockIn.getStockInTime() == null) {
            stockIn.setStockInTime(new Date());
        }

        BigDecimal currentTotal = inventoryService.syncInventoryFromStockIn(stockIn);
        stockIn.setTotalInventory(currentTotal);

        // 1. 保存饲料入库记录
        FeedStockIn savedStock = stockInRepository.save(stockIn);

        // 2. 自动生成财务支出记录
        FinanceRecord finance = new FinanceRecord();
        /**
         * 【修复】财务记录日期应关联“入库日期”，而非“生产日期”
         */
        finance.setRecordDate(savedStock.getStockInTime());
        finance.setRecordType("支出");
        finance.setCategory("购入饲料");
        finance.setAmount(savedStock.getTotalPrice());
        finance.setIsAuto(1);
        finance.setSourceTable("feed_stock_in");
        finance.setSourceId(savedStock.getId());
        finance.setRemark("关联饲料入库单号：" + savedStock.getId());

        financeRepository.save(finance);

        return savedStock;
    }

    @Override
    @Transactional
    public FeedStockIn update(Long id, FeedStockIn newStockIn) {
        FeedStockIn oldStockIn = stockInRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("饲料入库记录不存在"));

        inventoryService.syncInventoryOnUpdate(oldStockIn, newStockIn);

        // 更新基础字段
        oldStockIn.setUsage(newStockIn.getUsage());
        oldStockIn.setProductionDate(newStockIn.getProductionDate());
        oldStockIn.setExpirationDate(newStockIn.getExpirationDate());
        oldStockIn.setUnitOfMeasure(newStockIn.getUnitOfMeasure());
        oldStockIn.setStockInQuantity(newStockIn.getStockInQuantity());
        oldStockIn.setUnitPrice(newStockIn.getUnitPrice());
        oldStockIn.setFreightFee(newStockIn.getFreightFee());

        // 重新计算总价
        BigDecimal up = newStockIn.getUnitPrice() != null ? newStockIn.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal qty = newStockIn.getStockInQuantity();
        BigDecimal ff = newStockIn.getFreightFee() != null ? newStockIn.getFreightFee() : BigDecimal.ZERO;
        oldStockIn.setTotalPrice(up.multiply(qty).add(ff));

        oldStockIn.setConvertedUnitPrice(newStockIn.getConvertedUnitPrice());
        oldStockIn.setNutrients(newStockIn.getNutrients());
        oldStockIn.setWaterContent(newStockIn.getWaterContent());
        oldStockIn.setMildew(newStockIn.getMildew());
        oldStockIn.setImpurityContent(newStockIn.getImpurityContent());

        /**
         * 【建议】保留原有入库日期，除非前端明确传了新的入库日期
         */
        if (newStockIn.getStockInTime() != null) {
            oldStockIn.setStockInTime(newStockIn.getStockInTime());
        }

        // 1. 保存更新后的入库记录
        FeedStockIn updatedStock = stockInRepository.save(oldStockIn);

        // 2. 同步更新财务记录
        financeRepository.findAll().stream()
                .filter(f -> "feed_stock_in".equals(f.getSourceTable()) && id.equals(f.getSourceId()))
                .forEach(f -> {
                    f.setAmount(updatedStock.getTotalPrice());
                    /**
                     * 【修复】同步更新财务记录日期为入库日期
                     */
                    f.setRecordDate(updatedStock.getStockInTime());
                    financeRepository.save(f);
                });

        return updatedStock;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        FeedStockIn stockIn = stockInRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("饲料入库记录不存在"));

        financeRepository.findAll().stream()
                .filter(f -> "feed_stock_in".equals(f.getSourceTable()) && id.equals(f.getSourceId()))
                .forEach(f -> financeRepository.delete(f));

        inventoryService.syncInventoryOnDelete(stockIn);
        stockInRepository.deleteById(id);
    }

    @Override
    public List<FeedStockIn> findAll() {
        return stockInRepository.findAll();
    }

    @Override
    public Optional<FeedStockIn> findById(Long id) {
        return stockInRepository.findById(id);
    }
}
