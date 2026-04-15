package com.example.demo.service;

import com.example.demo.entity.FeedStockIn;
import java.util.List;
import java.util.Optional;

/**
 * 饲料入库业务逻辑接口
 */
public interface FeedStockInService {

    /**
     * 新增饲料入库记录
     */
    FeedStockIn save(FeedStockIn stockIn);

    /**
     * 查询所有饲料入库记录
     */
    List<FeedStockIn> findAll();

    /**
     * 根据ID查找记录
     */
    Optional<FeedStockIn> findById(Long id);

    /**
     * 删除入库记录（同步扣减库存）
     */
    void deleteById(Long id);

    /**
     * 更新入库记录（同步调整库存）
     */
    FeedStockIn update(Long id, FeedStockIn newStockIn);
}
