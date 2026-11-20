package com.attendance.user.entity;

import com.attendance.common.entity.BaseTenantEntity;
import jakarta.persistence.*;

/**
 * User entity representing an invited supervisor or owner.
 */
@Entity
@Table(name = "users")
public class User extends BaseTenantEntity {

    @Column(name = "google_sub")
    private String googleSub;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public User() {
    }

    public User(String email, String name, Role role) {
        this.email = email;
        this.name = name;
        this.role = role;
        this.active = true;
    }

    public String getGoogleSub() {
        return googleSub;
    }

    public void setGoogleSub(String googleSub) {
        this.googleSub = googleSub;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
