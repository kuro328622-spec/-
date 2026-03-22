package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

// 总库存实体类：无Lombok，纯手动getter/setter，避免失效问题
@Entity
@Table(name = "h_store_inventory_drug_vaccine")
public class HStoreInventoryDrugVaccine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "manufacturer", nullable = false, length = 100)
    private String manufacturer; // 生产厂家

    @Column(name = "name", nullable = false, length = 100)
    private String name; // 药品名称

    @Column(name = "category", nullable = false)
    private String category; // 类别

    @Column(name = "component_type", length = 50)
    private String componentType; // 成分类型

    @Column(name = "unit_of_measure", nullable = false, length = 20)
    private String unitOfMeasure; // 计量单位

    @Column(name = "quantity", nullable = false, precision = 38, scale = 2)
    private BigDecimal quantity; // 库存数量

    @Column(name = "unit_price", nullable = false, precision = 38, scale = 2)
    private BigDecimal unitPrice; // 单价

    @Column(name = "alert", precision = 38, scale = 2)
    private BigDecimal alert; // 警戒数量

    @Column(name = "f_date", nullable = false)
    private Date fDate; // 创建时间

    @Column(name = "out_time", nullable = false)
    private Date outTime; // 更新时间

    // 无参构造器（JPA必须要有）
    public HStoreInventoryDrugVaccine() {
    }

    // 手动getter/setter - 所有字段
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

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getAlert() {
        return alert;
    }

    public void setAlert(BigDecimal alert) {
        this.alert = alert;
    }

    public Date getFDate() {
        return fDate;
    }

    public void setFDate(Date fDate) {
        this.fDate = fDate;
    }

    public Date getOutTime() {
        return outTime;
    }

    public void setOutTime(Date outTime) {
        this.outTime = outTime;
    }
}
