package com.example.demo.service;

import com.example.demo.dto.OutboundDTO;
import com.example.demo.dto.OutboundUpdateDTO;
import com.example.demo.dto.StockOutQueryDTO;
import com.example.demo.entity.DrugVaccineStockOut;
import com.example.demo.entity.SysUser;

import java.util.List;

public interface DrugVaccineStockOutService {

    DrugVaccineStockOut createStockOut(OutboundDTO dto, SysUser currentUser) throws Exception;

    DrugVaccineStockOut updateStockOut(Long stockOutId, OutboundUpdateDTO dto, SysUser currentUser) throws Exception;

    void deleteStockOut(Long stockOutId, SysUser currentUser) throws Exception;

    List<DrugVaccineStockOut> getAllStockOut();

    List<DrugVaccineStockOut> getStockOutByCondition(StockOutQueryDTO queryDTO);

    DrugVaccineStockOut getById(Long stockOutId);
}
