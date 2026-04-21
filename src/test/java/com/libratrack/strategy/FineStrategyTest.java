package com.libratrack.strategy;

import com.libratrack.model.MemberType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FineStrategyTest {

    @Test
    void studentFineRate_isRs2PerDay() {
        FineStrategy strategy = new StudentFineStrategy();
        assertEquals(10.0, strategy.calculate(5));   // 5 days * Rs.2
    }

    @Test
    void facultyFineRate_isRs5PerDay() {
        FineStrategy strategy = new FacultyFineStrategy();
        assertEquals(25.0, strategy.calculate(5));   // 5 days * Rs.5
    }

    @Test
    void zeroDaysOverdue_returnsZero() {
        assertEquals(0.0, new StudentFineStrategy().calculate(0));
        assertEquals(0.0, new FacultyFineStrategy().calculate(0));
    }

    @Test
    void negativeDaysOverdue_returnsZero() {
        assertEquals(0.0, new StudentFineStrategy().calculate(-3));
        assertEquals(0.0, new FacultyFineStrategy().calculate(-3));
    }

    @Test
    void singleDayOverdue() {
        assertEquals(2.0, new StudentFineStrategy().calculate(1));
        assertEquals(5.0, new FacultyFineStrategy().calculate(1));
    }

    @Test
    void fineCalculator_picksStudentStrategy() {
        FineCalculator calc = new FineCalculator(MemberType.STUDENT);
        assertEquals(20.0, calc.calculate(10));  // 10 * Rs.2
    }

    @Test
    void fineCalculator_picksFacultyStrategy() {
        FineCalculator calc = new FineCalculator(MemberType.FACULTY);
        assertEquals(50.0, calc.calculate(10));  // 10 * Rs.5
    }
}
