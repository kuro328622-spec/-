package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "h_store_inventory_feed")
public class HStoreInventoryFeed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "manufacturer", nullable = false, length = 100)
    private String manufacturer;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "component_type", length = 50)
    private String componentType;

    @Column(name = "ingredients_type")
    private Integer ingredientsType;

    @Column(name = "unit_of_measure", nullable = false, length = 20)
    private String unitOfMeasure;

    @Column(name = "quantity", nullable = false, precision = 38, scale = 2)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "unit_price", nullable = false, precision = 38, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    // 核心修复：设置默认警戒值为 10.00
    @Column(name = "alert", precision = 38, scale = 2)
    private BigDecimal alert = new BigDecimal("10.00");

    // 核心修复：设置默认创建时间
    @Column(name = "f_date", nullable = false)
    private Date fDate = new Date();

    // 核心修复：设置默认更新时间
    @Column(name = "out_time", nullable = false)
    private Date outTime = new Date();

    public HStoreInventoryFeed() {
    }

    // Getter & Setter (保持不变)
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

    public Integer getIngredientsType() {
        return ingredientsType;
    }

    public void setIngredientsType(Integer ingredientsType) {
        this.ingredientsType = ingredientsType;
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
