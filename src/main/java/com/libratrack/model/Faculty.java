package com.libratrack.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("FACULTY")
public class Faculty extends Member {
    private static final int BORROW_LIMIT = 5;
    private static final int LOAN_PERIOD_DAYS = 30;

    public Faculty(String name, String email, String phone) {
        super(name, email, phone, MemberType.FACULTY);
    }

    public Faculty() {}

    @Override
    public int getBorrowLimit() { return BORROW_LIMIT; }

    @Override
    public int getLoanPeriodDays() { return LOAN_PERIOD_DAYS; }
}
