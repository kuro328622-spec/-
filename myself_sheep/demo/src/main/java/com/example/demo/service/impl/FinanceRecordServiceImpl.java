package com.example.demo.service.impl;

import com.example.demo.entity.FinanceRecord;
import com.example.demo.repository.FinanceRecordRepository;
import com.example.demo.service.FinanceRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class FinanceRecordServiceImpl implements FinanceRecordService {

    @Autowired
    private FinanceRecordRepository financeRepository;

    @Override
    @Transactional
    public FinanceRecord save(FinanceRecord record) {
        // 手动录入时，强制标记为非自动同步
        if (record.getIsAuto() == null) {
            record.setIsAuto(0);
        }
        return financeRepository.save(record);
    }

    @Override
    public List<FinanceRecord> findAll() {
        // 按日期倒序排列，方便前端查看最新账单
        return financeRepository.findAll();
    }

    @Override
    public Optional<FinanceRecord> findById(Long id) {
        return financeRepository.findById(id);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        FinanceRecord record = financeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("财务记录不存在"));

        // 核心安全校验：自动生成的记录（入库关联）禁止在财务模块直接删除
        if (record.getIsAuto() != null && record.getIsAuto() == 1) {
            throw new RuntimeException("系统自动生成的入库支出记录，请在对应入库模块处理，此处禁止删除！");
        }

        financeRepository.deleteById(id);
    }
}
