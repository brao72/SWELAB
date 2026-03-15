package com.libratrack.model;

import java.time.LocalDateTime;

public class Reservation {
    private int id;
    private int bookId;
    private int memberId;
    private LocalDateTime reservedAt;
    private boolean notified;
    private boolean fulfilled;

    public Reservation() {}

    public Reservation(int bookId, int memberId) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.reservedAt = LocalDateTime.now();
        this.notified = false;
        this.fulfilled = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public LocalDateTime getReservedAt() { return reservedAt; }
    public void setReservedAt(LocalDateTime reservedAt) { this.reservedAt = reservedAt; }

    public boolean isNotified() { return notified; }
    public void setNotified(boolean notified) { this.notified = notified; }

    public boolean isFulfilled() { return fulfilled; }
    public void setFulfilled(boolean fulfilled) { this.fulfilled = fulfilled; }
}
