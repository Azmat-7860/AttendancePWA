package com.attendance.site;

import com.attendance.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Site entity representing a work site or construction location under an organization.
 */
@Entity
@Table(name = "sites")
public class Site extends BaseTenantEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public Site() {
    }

    public Site(String name, String address) {
        this.name = name;
        this.address = address;
        this.active = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
