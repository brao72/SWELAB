package com.libratrack.strategy;

import com.libratrack.model.MemberType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FineStrategyTest {

    @Test
    void studentFineIs2PerDay() {
        FineStrategy strategy = new StudentFineStrategy();
        assertEquals(10.0, strategy.calculate(5));
    }

    @Test
    void facultyFineIs5PerDay() {
        FineStrategy strategy = new FacultyFineStrategy();
        assertEquals(25.0, strategy.calculate(5));
    }

    @Test
    void zeroDaysOverdueReturnsZeroForStudent() {
        assertEquals(0.0, new StudentFineStrategy().calculate(0));
    }

    @Test
    void zeroDaysOverdueReturnsZeroForFaculty() {
        assertEquals(0.0, new FacultyFineStrategy().calculate(0));
    }

    @Test
    void negativeDaysReturnsZero() {
        assertEquals(0.0, new StudentFineStrategy().calculate(-3));
        assertEquals(0.0, new FacultyFineStrategy().calculate(-3));
    }

    @Test
    void fineCalculatorUsesStudentStrategy() {
        FineCalculator calculator = new FineCalculator(MemberType.STUDENT);
        assertEquals(20.0, calculator.calculate(10));
    }

    @Test
    void fineCalculatorUsesFacultyStrategy() {
        FineCalculator calculator = new FineCalculator(MemberType.FACULTY);
        assertEquals(50.0, calculator.calculate(10));
    }

    @Test
    void singleDayOverdue() {
        assertEquals(2.0, new StudentFineStrategy().calculate(1));
        assertEquals(5.0, new FacultyFineStrategy().calculate(1));
    }
}
