package com.libratrack.model;

import java.time.LocalDateTime;

public abstract class Member {
    private int id;
    private String name;
    private String email;
    private String phone;
    private MemberType memberType;
    private boolean isActive;
    private LocalDateTime createdAt;

    public Member(String name, String email, String phone, MemberType memberType) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.memberType = memberType;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    public Member() {}

    public abstract int getBorrowLimit();
    public abstract int getLoanPeriodDays();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public MemberType getMemberType() { return memberType; }
    public void setMemberType(MemberType memberType) { this.memberType = memberType; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("%-5d %-20s %-25s %-12s %-8s %s",
                id, name, email, phone, memberType, isActive ? "Active" : "Inactive");
    }
}
