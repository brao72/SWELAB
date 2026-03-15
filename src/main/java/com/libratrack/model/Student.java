package com.libratrack.model;

public class Student extends Member {
    private static final int BORROW_LIMIT = 3;
    private static final int LOAN_PERIOD_DAYS = 14;

    public Student(String name, String email, String phone) {
        super(name, email, phone, MemberType.STUDENT);
    }

    public Student() {}

    @Override
    public int getBorrowLimit() { return BORROW_LIMIT; }

    @Override
    public int getLoanPeriodDays() { return LOAN_PERIOD_DAYS; }
}
