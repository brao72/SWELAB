package com.libratrack.factory;

import com.libratrack.model.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemberFactoryTest {

    @Test
    void createsStudentForStudentType() {
        Member member = MemberFactory.createMember(MemberType.STUDENT, "Alice", "alice@uni.edu", "1234567890");
        assertInstanceOf(Student.class, member);
        assertEquals("Alice", member.getName());
        assertEquals("alice@uni.edu", member.getEmail());
        assertEquals("1234567890", member.getPhone());
        assertEquals(MemberType.STUDENT, member.getMemberType());
    }

    @Test
    void createsFacultyForFacultyType() {
        Member member = MemberFactory.createMember(MemberType.FACULTY, "Dr. Bob", "bob@uni.edu", "9876543210");
        assertInstanceOf(Faculty.class, member);
        assertEquals("Dr. Bob", member.getName());
        assertEquals(MemberType.FACULTY, member.getMemberType());
    }

    @Test
    void studentFromFactoryHasCorrectLimits() {
        Member member = MemberFactory.createMember(MemberType.STUDENT, "Alice", "a@b.c", "123");
        assertEquals(3, member.getBorrowLimit());
        assertEquals(14, member.getLoanPeriodDays());
    }

    @Test
    void facultyFromFactoryHasCorrectLimits() {
        Member member = MemberFactory.createMember(MemberType.FACULTY, "Dr. Bob", "b@c.d", "456");
        assertEquals(5, member.getBorrowLimit());
        assertEquals(30, member.getLoanPeriodDays());
    }
}
