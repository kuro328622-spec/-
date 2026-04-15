package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.entity.FinanceRecord;

/**
 * 财务收支记录仓库接口 继承 JpaRepository 以获得标准 CRUD 操作能力
 */
@Repository
public interface FinanceRecordRepository extends JpaRepository<FinanceRecord, Long> {
    // 这里继承了基础功能，后续根据前端需求可以添加按日期范围查询的方法
}
