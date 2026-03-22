package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.FeedStockIn;

/**
 * 饲料入库记录仓库接口 继承 JpaRepository 以获得标准 CRUD 操作能力
 */
@Repository
public interface FeedStockInRepository extends JpaRepository<FeedStockIn, Long> {
    // 后续如果需要根据厂家或名称进行自定义查询，可以在这里添加方法
    // 例如：List<FeedStockIn> findByName(String name);
}
