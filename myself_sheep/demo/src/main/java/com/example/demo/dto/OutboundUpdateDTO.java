package com.example.demo.dto;

import java.math.BigDecimal;

public class OutboundUpdateDTO {

    private BigDecimal num; // 新出库数量
    private String outPurposes; // 新出库用途
    private String outStaff; // 新增：操作员字段

    public OutboundUpdateDTO() {
    }

    // 新增的 Getter 和 Setter
    public String getOutStaff() {
        return outStaff;
    }

    public void setOutStaff(String outStaff) {
        this.outStaff = outStaff;
    }

    // 原有的 Getter 和 Setter 保持不变
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
}
