package com.libratrack.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fines")
public class Fine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "borrow_record_id")
    private int borrowRecordId;

    @Column(name = "member_id")
    private int memberId;

    @Column(nullable = false)
    private double amount;

    @Column(name = "is_paid")
    private boolean isPaid;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Fine() {}

    public Fine(int borrowRecordId, int memberId, double amount) {
        this.borrowRecordId = borrowRecordId;
        this.memberId = memberId;
        this.amount = amount;
        this.isPaid = false;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBorrowRecordId() { return borrowRecordId; }
    public void setBorrowRecordId(int borrowRecordId) { this.borrowRecordId = borrowRecordId; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("Fine #%d - Member:%d Amount:₹%.2f %s",
                id, memberId, amount, isPaid ? "PAID" : "UNPAID");
    }
}
