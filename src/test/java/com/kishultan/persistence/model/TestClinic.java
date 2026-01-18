package com.kishultan.persistence.model;

import jakarta.persistence.*;
import java.util.List;

/**
 * 测试诊所实体类
 * 用于替代原项目中的Clinic实体类
 * 包含与Contact的关联关系（ManyToMany）
 */
@Entity
@Table(name = "his_clinics")
public class TestClinic {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "region_code")
    private String regionCode;
    
    @Column(name = "region_name")
    private String regionName;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "state")
    private Integer state;
    
    @Column(name = "createdby")
    private String createdBy;
    
    @Column(name = "ctime")
    private java.util.Date createTime;
    
    // 关联关系（ManyToMany，用于fetch测试）
    @ManyToMany
    @JoinTable(
        name = "his_clinic_contacts",
        joinColumns = @JoinColumn(name = "clinic_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "contact_id")
    )
    @Transient
    private List<TestContact> contacts;
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getRegionCode() {
        return regionCode;
    }
    
    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }
    
    public String getRegionName() {
        return regionName;
    }
    
    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public Integer getState() {
        return state;
    }
    
    public void setState(Integer state) {
        this.state = state;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public java.util.Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(java.util.Date createTime) {
        this.createTime = createTime;
    }
    
    public List<TestContact> getContacts() {
        return contacts;
    }
    
    public void setContacts(List<TestContact> contacts) {
        this.contacts = contacts;
    }
}

