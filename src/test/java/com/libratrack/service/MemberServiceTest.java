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
    void registerStudent_success() {
        when(memberRepo.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        Member member = memberService.registerMember(MemberType.STUDENT, "Alice", "alice@u.edu", "123", "hash");

        assertInstanceOf(Student.class, member);
        assertEquals("Alice", member.getName());
        verify(memberRepo).save(any(Student.class));
    }

    @Test
    void registerFaculty_success() {
        when(memberRepo.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        Member member = memberService.registerMember(MemberType.FACULTY, "Dr. X", "x@u.edu", "456", "hash");

        assertInstanceOf(Faculty.class, member);
        verify(memberRepo).save(any(Faculty.class));
    }

    @Test
    void findById_found() {
        Student student = new Student("A", "a@b.com", "123", "hash");
        when(memberRepo.findById(1)).thenReturn(Optional.of(student));

        assertTrue(memberService.findById(1).isPresent());
    }

    @Test
    void findById_notFound() {
        when(memberRepo.findById(99)).thenReturn(Optional.empty());

        assertTrue(memberService.findById(99).isEmpty());
    }

    @Test
    void listAllMembers_delegatesToRepo() {
        when(memberRepo.findAll()).thenReturn(List.of(new Student("A", "a@b.com", "1", "hash")));

        assertEquals(1, memberService.listAllMembers().size());
    }

    @Test
    void deactivateMember_success() {
        Student student = new Student("A", "a@b.com", "1", "hash");
        assertTrue(student.isActive());
        when(memberRepo.findById(1)).thenReturn(Optional.of(student));

        memberService.deactivateMember(1);

        assertFalse(student.isActive());
        verify(memberRepo).update(student);
    }

    @Test
    void deactivateMember_notFound_throws() {
        when(memberRepo.findById(99)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> memberService.deactivateMember(99));
    }
}
