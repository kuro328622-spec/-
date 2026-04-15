package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "feed_stock_in")
public class FeedStockIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 生产厂家 (对齐原有字段名)
    private String manufacturer;

    // 饲料名称 (对齐原有字段名)
    private String name;

    // 类别
    private String category;

    // 成分类型
    private String componentType;

    @Column(name = "`usage`", columnDefinition = "TEXT")
    private String usage;

    @Column(name = "production_batch", nullable = false)
    private String productionBatch;

    // --- 日期格式化配置 (保留原有习惯) ---
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date productionDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date expirationDate;

    private String unitOfMeasure;

    private BigDecimal stockInQuantity;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;

    private BigDecimal freightFee;

    private BigDecimal convertedUnitPrice;

    @Column(name = "operator")
    private String operator;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "stock_in_time", nullable = false, updatable = false)
    private Date stockInTime;

    // --- 饲料特有验收字段 ---
    @Column(columnDefinition = "TEXT")
    private String nutrients; // 营养成分

    private BigDecimal waterContent; // 原材料含水量

    private String mildew; // 原材料霉变

    private BigDecimal impurityContent; // 原材料杂质含量

    // --- 核心功能字段：入库后的当前总库存快照 (置于末尾) ---
    private BigDecimal totalInventory;

    // --- JPA 回调：自动记录入库时间 ---
    @PrePersist
    protected void onCreate() {
        this.stockInTime = new Date();
    }

    // --- Getter 和 Setter 方法 ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
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

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getProductionBatch() {
        return productionBatch;
    }

    public void setProductionBatch(String productionBatch) {
        this.productionBatch = productionBatch;
    }

    public Date getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(Date productionDate) {
        this.productionDate = productionDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public BigDecimal getStockInQuantity() {
        return stockInQuantity;
    }

    public void setStockInQuantity(BigDecimal stockInQuantity) {
        this.stockInQuantity = stockInQuantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getFreightFee() {
        return freightFee;
    }

    public void setFreightFee(BigDecimal freightFee) {
        this.freightFee = freightFee;
    }

    public BigDecimal getConvertedUnitPrice() {
        return convertedUnitPrice;
    }

    public void setConvertedUnitPrice(BigDecimal convertedUnitPrice) {
        this.convertedUnitPrice = convertedUnitPrice;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Date getStockInTime() {
        return stockInTime;
    }

    public void setStockInTime(Date stockInTime) {
        this.stockInTime = stockInTime;
    }

    public String getNutrients() {
        return nutrients;
    }

    public void setNutrients(String nutrients) {
        this.nutrients = nutrients;
    }

    public BigDecimal getWaterContent() {
        return waterContent;
    }

    public void setWaterContent(BigDecimal waterContent) {
        this.waterContent = waterContent;
    }

    public String getMildew() {
        return mildew;
    }

    public void setMildew(String mildew) {
        this.mildew = mildew;
    }

    public BigDecimal getImpurityContent() {
        return impurityContent;
    }

    public void setImpurityContent(BigDecimal impurityContent) {
        this.impurityContent = impurityContent;
    }

    public BigDecimal getTotalInventory() {
        return totalInventory;
    }

    public void setTotalInventory(BigDecimal totalInventory) {
        this.totalInventory = totalInventory;
    }
}
