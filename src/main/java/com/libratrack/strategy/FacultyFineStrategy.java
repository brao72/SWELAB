package com.libratrack.strategy;

public class FacultyFineStrategy implements FineStrategy {
    private static final double RATE_PER_DAY = 5.0;

    @Override
    public double calculate(long daysOverdue) {
        return daysOverdue > 0 ? daysOverdue * RATE_PER_DAY : 0;
    }
}
