package com.example.demo.service;

import com.example.demo.entity.FinanceRecord;
import java.util.List;
import java.util.Optional;

/**
 * 财务流水业务逻辑接口
 */
public interface FinanceRecordService {

    /**
     * 新增财务记录（手动录入）
     */
    FinanceRecord save(FinanceRecord record);

    /**
     * 查询所有财务记录
     */
    List<FinanceRecord> findAll();

    /**
     * 根据ID查找记录
     */
    Optional<FinanceRecord> findById(Long id);

    /**
     * 删除财务记录（需校验是否为自动同步）
     */
    void deleteById(Long id);
}
