package com.libratrack.strategy;

public class StudentFineStrategy implements FineStrategy {
    private static final double RATE_PER_DAY = 2.0;

    @Override
    public double calculate(long daysOverdue) {
        return daysOverdue > 0 ? daysOverdue * RATE_PER_DAY : 0;
    }
}
