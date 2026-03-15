package com.libratrack.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemberTest {

    @Test
    void studentHasCorrectBorrowLimit() {
        Student student = new Student("Alice", "alice@uni.edu", "1234567890");
        assertEquals(3, student.getBorrowLimit());
    }

    @Test
    void studentHasCorrectLoanPeriod() {
        Student student = new Student("Alice", "alice@uni.edu", "1234567890");
        assertEquals(14, student.getLoanPeriodDays());
    }

    @Test
    void facultyHasCorrectBorrowLimit() {
        Faculty faculty = new Faculty("Dr. Bob", "bob@uni.edu", "9876543210");
        assertEquals(5, faculty.getBorrowLimit());
    }

    @Test
    void facultyHasCorrectLoanPeriod() {
        Faculty faculty = new Faculty("Dr. Bob", "bob@uni.edu", "9876543210");
        assertEquals(30, faculty.getLoanPeriodDays());
    }

    @Test
    void studentMemberTypeIsStudent() {
        Student student = new Student("Alice", "alice@uni.edu", "1234567890");
        assertEquals(MemberType.STUDENT, student.getMemberType());
    }

    @Test
    void facultyMemberTypeIsFaculty() {
        Faculty faculty = new Faculty("Dr. Bob", "bob@uni.edu", "9876543210");
        assertEquals(MemberType.FACULTY, faculty.getMemberType());
    }

    @Test
    void memberIsActiveByDefault() {
        Student student = new Student("Alice", "alice@uni.edu", "1234567890");
        assertTrue(student.isActive());
    }

    @Test
    void memberCanBeDeactivated() {
        Student student = new Student("Alice", "alice@uni.edu", "1234567890");
        student.setActive(false);
        assertFalse(student.isActive());
    }

    @Test
    void memberCreatedAtIsNotNull() {
        Faculty faculty = new Faculty("Dr. Bob", "bob@uni.edu", "9876543210");
        assertNotNull(faculty.getCreatedAt());
    }

    @Test
    void toStringContainsMemberName() {
        Student student = new Student("Alice", "alice@uni.edu", "1234567890");
        student.setId(1);
        assertTrue(student.toString().contains("Alice"));
    }
}
