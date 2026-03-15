package com.libratrack.strategy;

import com.libratrack.model.MemberType;

public class FineCalculator {
    private FineStrategy strategy;

    public FineCalculator(MemberType memberType) {
        this.strategy = switch (memberType) {
            case STUDENT -> new StudentFineStrategy();
            case FACULTY -> new FacultyFineStrategy();
        };
    }

    public double calculate(long daysOverdue) {
        return strategy.calculate(daysOverdue);
    }
}
