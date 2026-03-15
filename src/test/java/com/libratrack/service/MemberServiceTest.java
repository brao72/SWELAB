package com.libratrack.service;

import com.libratrack.model.*;
import com.libratrack.repository.MemberRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepo;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepo);
    }

    @Test
    void registerStudentMember() {
        when(memberRepo.save(any(Member.class))).thenAnswer(inv -> {
            Member m = inv.getArgument(0);
            m.setId(1);
            return m;
        });

        Member result = memberService.registerMember(MemberType.STUDENT, "Alice", "alice@uni.edu", "123");

        assertInstanceOf(Student.class, result);
        assertEquals(1, result.getId());
        verify(memberRepo).save(any(Member.class));
    }

    @Test
    void registerFacultyMember() {
        when(memberRepo.save(any(Member.class))).thenAnswer(inv -> {
            Member m = inv.getArgument(0);
            m.setId(2);
            return m;
        });

        Member result = memberService.registerMember(MemberType.FACULTY, "Dr. Bob", "bob@uni.edu", "456");

        assertInstanceOf(Faculty.class, result);
        assertEquals(2, result.getId());
    }

    @Test
    void findByIdReturnsExistingMember() {
        Student student = new Student("Alice", "alice@uni.edu", "123");
        student.setId(1);
        when(memberRepo.findById(1)).thenReturn(Optional.of(student));

        assertTrue(memberService.findById(1).isPresent());
    }

    @Test
    void findByIdReturnsEmptyForMissing() {
        when(memberRepo.findById(99)).thenReturn(Optional.empty());
        assertTrue(memberService.findById(99).isEmpty());
    }

    @Test
    void listAllMembers() {
        when(memberRepo.findAll()).thenReturn(List.of(
                new Student("A", "a@b.c", "1"),
                new Faculty("B", "b@c.d", "2")
        ));

        assertEquals(2, memberService.listAllMembers().size());
    }

    @Test
    void deactivateMemberSetsInactive() {
        Student student = new Student("Alice", "alice@uni.edu", "123");
        student.setId(1);
        when(memberRepo.findById(1)).thenReturn(Optional.of(student));

        memberService.deactivateMember(1);

        assertFalse(student.isActive());
        verify(memberRepo).update(student);
    }

    @Test
    void deactivateNonExistentMemberThrows() {
        when(memberRepo.findById(99)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> memberService.deactivateMember(99));
    }
}
