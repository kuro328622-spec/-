package com.example.demo.service;

import com.example.demo.dto.OutboundDTO;
import com.example.demo.dto.OutboundUpdateDTO;
import com.example.demo.dto.StockOutQueryDTO;
import com.example.demo.entity.FeedStockOut;
import com.example.demo.entity.SysUser;
import java.util.List;

public interface FeedStockOutService {

    FeedStockOut createStockOut(OutboundDTO dto, SysUser currentUser) throws Exception;

    // 新增：适配 Vue 前端的创建
    FeedStockOut createStockOutDirect(FeedStockOut feedStockOut, SysUser currentUser) throws Exception;

    // 新增：适配 Vue 前端的更新（处理库存对冲）
    FeedStockOut updateStockOutDirect(Long stockOutId, FeedStockOut feedStockOut, SysUser currentUser) throws Exception;

    FeedStockOut updateStockOut(Long stockOutId, OutboundUpdateDTO dto, SysUser currentUser) throws Exception;

    void deleteStockOut(Long stockOutId, SysUser currentUser) throws Exception;

    List<FeedStockOut> getAllStockOut();

    List<FeedStockOut> getStockOutByCondition(StockOutQueryDTO queryDTO);

    FeedStockOut getById(Long stockOutId);
}
