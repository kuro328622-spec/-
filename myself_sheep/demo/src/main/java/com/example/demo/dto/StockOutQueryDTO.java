package com.example.demo.dto;

import java.util.Date;

/**
 * 出库记录查询条件 DTO 用于前端请求查询出库列表时传递参数
 */
public class StockOutQueryDTO {

    private String name;           // 药品名称（可选）
    private String manufacturer;   // 生产厂家（可选）
    private Date startDate;        // 出库开始时间（可选）
    private Date endDate;          // 出库结束时间（可选）

    // --- Getter / Setter ---
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
