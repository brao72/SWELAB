package com.libratrack.factory;

import com.libratrack.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemberFactoryTest {

    @Test
    void createStudent_returnsStudentInstance() {
        Member member = MemberFactory.createMember(MemberType.STUDENT, "Alice", "alice@test.com", "1234567890");
        assertInstanceOf(Student.class, member);
        assertEquals("Alice", member.getName());
        assertEquals("alice@test.com", member.getEmail());
        assertEquals(MemberType.STUDENT, member.getMemberType());
    }

    @Test
    void createFaculty_returnsFacultyInstance() {
        Member member = MemberFactory.createMember(MemberType.FACULTY, "Dr. Bob", "bob@test.com", "9876543210");
        assertInstanceOf(Faculty.class, member);
        assertEquals("Dr. Bob", member.getName());
        assertEquals(MemberType.FACULTY, member.getMemberType());
    }

    @Test
    void student_hasCorrectLimits() {
        Member student = MemberFactory.createMember(MemberType.STUDENT, "A", "a@b.com", "123");
        assertEquals(3, student.getBorrowLimit());
        assertEquals(14, student.getLoanPeriodDays());
    }

    @Test
    void faculty_hasCorrectLimits() {
        Member faculty = MemberFactory.createMember(MemberType.FACULTY, "B", "b@b.com", "456");
        assertEquals(5, faculty.getBorrowLimit());
        assertEquals(30, faculty.getLoanPeriodDays());
    }

    @Test
    void newMember_isActiveByDefault() {
        Member member = MemberFactory.createMember(MemberType.STUDENT, "X", "x@y.com", "000");
        assertTrue(member.isActive());
    }
}
