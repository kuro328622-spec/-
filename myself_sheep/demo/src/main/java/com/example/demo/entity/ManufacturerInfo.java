package com.example.demo.entity; // 请确保与你项目包名一致

import jakarta.persistence.*;
import java.util.Date;

// 生产厂家/供应商信息实体类：手动Getter/Setter，保持与库存表风格一致
@Entity
@Table(name = "manufacturer_info")
public class ManufacturerInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "manufacturer_name", nullable = false, unique = true, length = 100)
    private String manufacturerName; // 生产厂家名称

    @Column(name = "man_category", length = 50)
    private String manCategory; // 供应类别

    @Column(name = "man_contact_person", length = 50)
    private String manContactPerson; // 联系人姓名

    @Column(name = "man_contact_phone", length = 20)
    private String manContactPhone; // 联系电话

    @Column(name = "man_contact_email", length = 100)
    private String manContactEmail; // 联系邮箱

    @Column(name = "man_contact_address", length = 255)
    private String manContactAddress; // 联系地址

    @Column(name = "man_remark")
    private String manRemark; // 备注信息

    @Column(name = "create_time", nullable = false, updatable = false)
    private Date createTime; // 档案建立时间

    // JPA 必须的无参构造器
    public ManufacturerInfo() {
    }

    // 手动生成 Getter 和 Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getManCategory() {
        return manCategory;
    }

    public void setManCategory(String manCategory) {
        this.manCategory = manCategory;
    }

    public String getManContactPerson() {
        return manContactPerson;
    }

    public void setManContactPerson(String manContactPerson) {
        this.manContactPerson = manContactPerson;
    }

    public String getManContactPhone() {
        return manContactPhone;
    }

    public void setManContactPhone(String manContactPhone) {
        this.manContactPhone = manContactPhone;
    }

    public String getManContactEmail() {
        return manContactEmail;
    }

    public void setManContactEmail(String manContactEmail) {
        this.manContactEmail = manContactEmail;
    }

    public String getManContactAddress() {
        return manContactAddress;
    }

    public void setManContactAddress(String manContactAddress) {
        this.manContactAddress = manContactAddress;
    }

    public String getManRemark() {
        return manRemark;
    }

    public void setManRemark(String manRemark) {
        this.manRemark = manRemark;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
