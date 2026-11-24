package com.attendance.worker.entity;

import com.attendance.common.entity.BaseTenantEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Worker entity representing an employee / laborer under an organization.
 */
@Entity
@Table(name = "workers")
public class Worker extends BaseTenantEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone", length = 32)
    private String phone;

    @Column(name = "photo_key")
    private String photoKey;

    @Column(name = "skill", length = 64)
    private String skill;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_type", nullable = false, length = 16)
    private PayType payType;

    @Column(name = "rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal rate;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public Worker() {
    }

    public Worker(String name, String phone, String skill, PayType payType, BigDecimal rate) {
        this.name = name;
        this.phone = phone;
        this.skill = skill;
        this.payType = payType;
        this.rate = rate;
        this.active = true;
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

    public String getPhotoKey() {
        return photoKey;
    }

    public void setPhotoKey(String photoKey) {
        this.photoKey = photoKey;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public PayType getPayType() {
        return payType;
    }

    public void setPayType(PayType payType) {
        this.payType = payType;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
