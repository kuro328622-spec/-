package com.example.demo.service.impl;

import com.example.demo.dto.OutboundDTO;
import com.example.demo.dto.OutboundUpdateDTO;
import com.example.demo.dto.StockOutQueryDTO;
import com.example.demo.entity.DrugVaccineStockOut;
import com.example.demo.entity.HStoreInventoryDrugVaccine;
import com.example.demo.entity.SysUser;
import com.example.demo.repository.DrugVaccineStockOutRepository;
import com.example.demo.repository.HStoreInventoryDrugVaccineRepository;
import com.example.demo.service.DrugVaccineStockOutService;
import com.example.demo.util.OutboundNoGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class DrugVaccineStockOutServiceImpl implements DrugVaccineStockOutService {

    @Autowired
    private DrugVaccineStockOutRepository stockOutRepository;

    @Autowired
    private HStoreInventoryDrugVaccineRepository inventoryRepository;

    @Autowired
    private OutboundNoGenerator outboundNoGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugVaccineStockOut createStockOut(OutboundDTO dto, SysUser currentUser) throws Exception {
        // 创建出库单时，通常直接根据选中的库存 ID 操作，无需修改
        HStoreInventoryDrugVaccine inventory = inventoryRepository.findById(dto.getInventoryId())
                .orElseThrow(() -> new Exception("库存记录不存在"));

        if (dto.getNum() == null || dto.getNum().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("出库数量必须大于0");
        }
        if (dto.getNum().compareTo(inventory.getQuantity()) > 0) {
            throw new Exception("出库数量不能大于库存数量");
        }

        inventory.setQuantity(inventory.getQuantity().subtract(dto.getNum()));
        inventoryRepository.save(inventory);

        DrugVaccineStockOut stockOut = new DrugVaccineStockOut();
        stockOut.setManufacturer(inventory.getManufacturer());
        stockOut.setName(inventory.getName());
        stockOut.setCategory(inventory.getCategory());
        stockOut.setComponentType(inventory.getComponentType());
        stockOut.setOutboundNo(outboundNoGenerator.generateOutboundNo());
        // 这里的 Date 会被 application.properties 中的格式化拦截，显示精确时间
        stockOut.setDeliveryTime(dto.getDeliveryTime() != null ? dto.getDeliveryTime() : new Date());
        stockOut.setOutPurposes(dto.getOutPurposes());
        stockOut.setNum(dto.getNum());
        stockOut.setOutStaff(currentUser.getUsername());
        stockOut.setNotes(dto.getNotes());

        return stockOutRepository.save(stockOut);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugVaccineStockOut updateStockOut(Long stockOutId, OutboundUpdateDTO dto, SysUser currentUser) throws Exception {
        DrugVaccineStockOut stockOut = getById(stockOutId);

        boolean isAdmin = currentUser.getRole() != null
                && (currentUser.getRole().getId() == 1L || "ROLE_ADMIN".equals(currentUser.getRole().getRoleCode()));
        boolean isOwner = stockOut.getOutStaff() != null && stockOut.getOutStaff().equals(currentUser.getUsername());

        if (!isAdmin && !isOwner && !"系统管理员".equals(currentUser.getUsername())) {
            throw new Exception("权限不足，无法编辑此记录");
        }

        BigDecimal oldNum = stockOut.getNum();
        BigDecimal newNum = dto.getNum();
        if (newNum == null || newNum.compareTo(BigDecimal.ZERO) < 0) {
            throw new Exception("出库数量无效");
        }

        BigDecimal diff = newNum.subtract(oldNum);

        // --- 核心修复点：使用新的精确查询方法，增加 Category 条件 ---
        inventoryRepository.findByManufacturerAndNameAndCategory(
                stockOut.getManufacturer(),
                stockOut.getName(),
                stockOut.getCategory()
        )
                .ifPresentOrElse(inventory -> {
                    BigDecimal updatedQuantity = inventory.getQuantity().subtract(diff);
                    if (updatedQuantity.compareTo(BigDecimal.ZERO) < 0) {
                        throw new RuntimeException("库存不足，无法调整数量");
                    }
                    inventory.setQuantity(updatedQuantity);
                    inventoryRepository.save(inventory);
                }, () -> {
                    // 如果找不到，可能是因为类别或名称发生了变更，打印警告日志
                    System.err.println("警告：未找到唯一匹配库存记录 [" + stockOut.getName() + " | " + stockOut.getCategory() + "]");
                });

        stockOut.setNum(newNum);
        stockOut.setOutPurposes(dto.getOutPurposes());
        return stockOutRepository.save(stockOut);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockOut(Long stockOutId, SysUser currentUser) throws Exception {
        DrugVaccineStockOut stockOut = getById(stockOutId);

        boolean isAdmin = currentUser.getRole() != null
                && (currentUser.getRole().getId() == 1L || "ROLE_ADMIN".equals(currentUser.getRole().getRoleCode()));
        boolean isOwner = stockOut.getOutStaff() != null && stockOut.getOutStaff().equals(currentUser.getUsername());

        if (!isAdmin && !isOwner && !"系统管理员".equals(currentUser.getUsername())) {
            throw new Exception("权限不足，无法删除此记录");
        }

        // --- 核心修复点：使用新的精确查询方法，增加 Category 条件 ---
        inventoryRepository.findByManufacturerAndNameAndCategory(
                stockOut.getManufacturer(),
                stockOut.getName(),
                stockOut.getCategory()
        )
                .ifPresent(inventory -> {
                    BigDecimal currentQty = inventory.getQuantity() != null ? inventory.getQuantity() : BigDecimal.ZERO;
                    inventory.setQuantity(currentQty.add(stockOut.getNum()));
                    inventoryRepository.save(inventory);
                });

        stockOutRepository.delete(stockOut);
        stockOutRepository.flush();
    }

    @Override
    public List<DrugVaccineStockOut> getAllStockOut() {
        return stockOutRepository.findAll();
    }

    @Override
    public List<DrugVaccineStockOut> getStockOutByCondition(StockOutQueryDTO queryDTO) {
        return stockOutRepository.findAll();
    }

    @Override
    public DrugVaccineStockOut getById(Long stockOutId) {
        return stockOutRepository.findById(stockOutId)
                .orElseThrow(() -> new RuntimeException("记录不存在，ID：" + stockOutId));
    }
}
