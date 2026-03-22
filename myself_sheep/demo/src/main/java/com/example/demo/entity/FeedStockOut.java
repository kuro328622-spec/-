package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "feed_stock_out")
public class FeedStockOut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "outbound_no", nullable = false, length = 30)
    private String outboundNo; // 出库单号

    @Column(name = "name", nullable = false, length = 100)
    private String name; // 饲料名称

    @Column(name = "category", length = 255)
    private String category; // 类别

    @Column(name = "component_type", length = 50)
    private String componentType; // 成分类型

    @Column(name = "manufacturer", nullable = false, length = 100)
    private String manufacturer; // 生产厂家

    @Column(name = "num", nullable = false, precision = 38, scale = 2)
    private BigDecimal num; // 出库数量

    @Column(name = "out_staff", nullable = false, length = 30)
    private String outStaff; // 出库人员

    @Column(name = "out_purposes", nullable = false, length = 100)
    private String outPurposes; // 出库用途

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "delivery_time", nullable = false)
    private Date deliveryTime; // 出库时间

    @Column(name = "notes", length = 255)
    private String notes; // 备注信息

    @Column(name = "production_batch", length = 50)
    private String productionBatch; // 生产批号

    public FeedStockOut() {
    }

    // Getter and Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOutboundNo() {
        return outboundNo;
    }

    public void setOutboundNo(String outboundNo) {
        this.outboundNo = outboundNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public BigDecimal getNum() {
        return num;
    }

    public void setNum(BigDecimal num) {
        this.num = num;
    }

    public String getOutStaff() {
        return outStaff;
    }

    public void setOutStaff(String outStaff) {
        this.outStaff = outStaff;
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

    public String getProductionBatch() {
        return productionBatch;
    }

    public void setProductionBatch(String productionBatch) {
        this.productionBatch = productionBatch;
    }
}
