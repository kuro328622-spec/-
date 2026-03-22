package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.Date;

public class OutboundDTO {

    private Long inventoryId; // 对应总库存 ID
    private BigDecimal num; // 出库数量
    private String outPurposes; // 出库用途
    private Date deliveryTime; // 出库时间
    private String notes; // 备注
    private String outStaff; // 新增：操作员字段

    public OutboundDTO() {
    }

    // 新增的 Getter 和 Setter
    public String getOutStaff() {
        return outStaff;
    }

    public void setOutStaff(String outStaff) {
        this.outStaff = outStaff;
    }

    // 原有的 Getter 和 Setter 保持不变
    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    public BigDecimal getNum() {
        return num;
    }

    public void setNum(BigDecimal num) {
        this.num = num;
    }

    public String getOutPurposes() {
        return outPurposes;
    }

    public void setOutPurposes(String outPurposes) {
        this.outPurposes = outPurposes;
    }

    public Date getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(Date deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
