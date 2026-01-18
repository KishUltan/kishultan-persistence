package com.kishultan.persistence.model;

import jakarta.persistence.*;
import java.util.List;

/**
 * 测试联系人实体类
 * 用于替代原项目中的Contact实体类
 * 与TestClinic关联
 */
@Entity
@Table(name = "his_contacts")
public class TestContact {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "created_time")
    private java.util.Date createTime;
    
    // ManyToMany反向关联
    @ManyToMany(mappedBy = "contacts")
    @Transient
    private List<TestClinic> clinics;
    
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
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public java.util.Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(java.util.Date createTime) {
        this.createTime = createTime;
    }
    
    public List<TestClinic> getClinics() {
        return clinics;
    }
    
    public void setClinics(List<TestClinic> clinics) {
        this.clinics = clinics;
    }
}

