package com.example.demo.service;

import com.example.demo.entity.HStoreInventoryFeed;
import com.example.demo.entity.FeedStockIn;
import java.math.BigDecimal;
import java.util.List;

/**
 * 饲料总库存 Service 接口
 */
public interface HStoreInventoryFeedService {

    List<HStoreInventoryFeed> findAll();

    HStoreInventoryFeed updateAlertQuantity(Long id, BigDecimal alertQuantity);

    /**
     * 出库操作：减少库存
     */
    HStoreInventoryFeed reduceStock(String manufacturer, String name, String category, BigDecimal quantityChange);

    /**
     * 入库操作：同步库存，并返回最新总量
     */
    BigDecimal syncInventoryFromStockIn(FeedStockIn stockIn);

    /**
     * 删除入库记录时同步扣减库存
     */
    void syncInventoryOnDelete(FeedStockIn stockIn);

    /**
     * 修改入库记录时同步调整库存
     */
    void syncInventoryOnUpdate(FeedStockIn oldStockIn, FeedStockIn newStockIn);
}
