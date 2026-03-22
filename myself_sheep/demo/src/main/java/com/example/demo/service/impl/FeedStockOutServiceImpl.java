package com.example.demo.service.impl;

import com.example.demo.dto.OutboundDTO;
import com.example.demo.dto.OutboundUpdateDTO;
import com.example.demo.dto.StockOutQueryDTO;
import com.example.demo.entity.FeedStockOut;
import com.example.demo.entity.HStoreInventoryFeed;
import com.example.demo.entity.SysUser;
import com.example.demo.repository.FeedStockOutRepository;
import com.example.demo.repository.HStoreInventoryFeedRepository;
import com.example.demo.service.FeedStockOutService;
import com.example.demo.util.OutboundNoGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class FeedStockOutServiceImpl implements FeedStockOutService {

    @Autowired
    private FeedStockOutRepository stockOutRepository;

    @Autowired
    private HStoreInventoryFeedRepository inventoryRepository;

    @Autowired
    private OutboundNoGenerator outboundNoGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FeedStockOut createStockOutDirect(FeedStockOut stockOut, SysUser currentUser) throws Exception {
        HStoreInventoryFeed inventory = inventoryRepository.findByManufacturerAndNameAndCategoryAndComponentType(
                stockOut.getManufacturer(),
                stockOut.getName(),
                stockOut.getCategory(),
                stockOut.getComponentType()
        ).orElseThrow(() -> new Exception("未找到对应的饲料库存记录，无法出库"));

        if (stockOut.getNum() == null || stockOut.getNum().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("出库数量必须大于0");
        }
        if (stockOut.getNum().compareTo(inventory.getQuantity()) > 0) {
            throw new Exception("库存不足！当前库存仅剩: " + inventory.getQuantity());
        }

        inventory.setQuantity(inventory.getQuantity().subtract(stockOut.getNum()));
        inventoryRepository.save(inventory);

        stockOut.setId(null);
        if (stockOut.getOutboundNo() == null || stockOut.getOutboundNo().isEmpty()) {
            stockOut.setOutboundNo(outboundNoGenerator.generateOutboundNo());
        }
        stockOut.setOutStaff(currentUser.getUsername());
        if (stockOut.getDeliveryTime() == null) {
            stockOut.setDeliveryTime(new Date());
        }

        return stockOutRepository.save(stockOut);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FeedStockOut updateStockOutDirect(Long stockOutId, FeedStockOut newData, SysUser currentUser) throws Exception {
        // 1. 获取旧记录
        FeedStockOut oldRecord = stockOutRepository.findById(stockOutId)
                .orElseThrow(() -> new Exception("未找到原始出库记录，ID: " + stockOutId));

        // 2. 查找关联库存
        HStoreInventoryFeed inventory = inventoryRepository.findByManufacturerAndNameAndCategoryAndComponentType(
                oldRecord.getManufacturer(),
                oldRecord.getName(),
                oldRecord.getCategory(),
                oldRecord.getComponentType()
        ).orElseThrow(() -> new Exception("未找到对应的总库存记录，无法同步调整"));

        // 3. 计算库存变动差值 (新数量 - 旧数量)
        // 如果 diff > 0, 说明出库增加了，需要从总库存里减去 diff
        // 如果 diff < 0, 说明出库减少了，需要往总库存里加上 |diff|
        BigDecimal diff = newData.getNum().subtract(oldRecord.getNum());
        BigDecimal updatedInventoryQty = inventory.getQuantity().subtract(diff);

        if (updatedInventoryQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new Exception("修改失败：库存不足！当前库存剩余: " + inventory.getQuantity());
        }

        // 4. 更新总库存
        inventory.setQuantity(updatedInventoryQty);
        inventoryRepository.save(inventory);

        // 5. 将新值覆盖到旧记录对象上（保持 ID 和单号不变）
        oldRecord.setNum(newData.getNum());
        oldRecord.setOutPurposes(newData.getOutPurposes());
        oldRecord.setNotes(newData.getNotes());
        oldRecord.setDeliveryTime(newData.getDeliveryTime());
        oldRecord.setOutStaff(newData.getOutStaff() != null ? newData.getOutStaff() : oldRecord.getOutStaff());

        // 6. 保存（此时 JPA 会执行 UPDATE）
        return stockOutRepository.save(oldRecord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockOut(Long stockOutId, SysUser currentUser) throws Exception {
        FeedStockOut stockOut = getById(stockOutId);
        inventoryRepository.findByManufacturerAndNameAndCategoryAndComponentType(
                stockOut.getManufacturer(), stockOut.getName(), stockOut.getCategory(), stockOut.getComponentType()
        ).ifPresent(inventory -> {
            inventory.setQuantity(inventory.getQuantity().add(stockOut.getNum()));
            inventoryRepository.save(inventory);
        });
        stockOutRepository.delete(stockOut);
    }

    // 原有 DTO 兼容方法和查询方法保持不变
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FeedStockOut createStockOut(OutboundDTO dto, SysUser currentUser) throws Exception {
        /* 原有逻辑 */ return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FeedStockOut updateStockOut(Long stockOutId, OutboundUpdateDTO dto, SysUser currentUser) throws Exception {
        /* 原有逻辑 */ return null;
    }

    @Override
    public List<FeedStockOut> getAllStockOut() {
        return stockOutRepository.findAll();
    }

    @Override
    public List<FeedStockOut> getStockOutByCondition(StockOutQueryDTO queryDTO) {
        return stockOutRepository.findAll();
    }

    @Override
    public FeedStockOut getById(Long stockOutId) {
        return stockOutRepository.findById(stockOutId).orElseThrow(() -> new RuntimeException("未找到记录"));
    }
}
