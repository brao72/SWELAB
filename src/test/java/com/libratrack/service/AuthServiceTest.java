package com.libratrack.service;

import com.libratrack.model.*;
import com.libratrack.repository.LibrarianRepository;
import com.libratrack.repository.MemberRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private LibrarianRepository librarianRepo;
    @Mock private MemberRepository memberRepo;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(librarianRepo, memberRepo);
    }

    @Test
    void loginLibrarianSuccessfully() {
        Librarian lib = new Librarian("admin", AuthService.hashPassword("admin123"), "System Admin");
        lib.setId(1);
        when(librarianRepo.findByUsername("admin")).thenReturn(Optional.of(lib));

        Session session = authService.loginLibrarian("admin", "admin123");

        assertEquals(Role.LIBRARIAN, session.getRole());
        assertEquals(1, session.getUserId());
        assertEquals("System Admin", session.getDisplayName());
    }

    @Test
    void loginLibrarianFailsForWrongPassword() {
        Librarian lib = new Librarian("admin", AuthService.hashPassword("admin123"), "System Admin");
        lib.setId(1);
        when(librarianRepo.findByUsername("admin")).thenReturn(Optional.of(lib));

        assertThrows(IllegalArgumentException.class,
                () -> authService.loginLibrarian("admin", "wrongpassword"));
    }

    @Test
    void loginLibrarianFailsForUnknownUsername() {
        when(librarianRepo.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> authService.loginLibrarian("unknown", "pass"));
    }

    @Test
    void loginMemberSuccessfully() {
        Student student = new Student("Alice", "alice@uni.edu", "123");
        student.setId(1);
        when(memberRepo.findById(1)).thenReturn(Optional.of(student));

        Session session = authService.loginMember(1);

        assertEquals(Role.MEMBER, session.getRole());
        assertEquals(1, session.getUserId());
        assertEquals("Alice", session.getDisplayName());
    }

    @Test
    void loginMemberFailsForInactive() {
        Student student = new Student("Alice", "alice@uni.edu", "123");
        student.setId(1);
        student.setActive(false);
        when(memberRepo.findById(1)).thenReturn(Optional.of(student));

        assertThrows(IllegalArgumentException.class,
                () -> authService.loginMember(1));
    }

    @Test
    void loginMemberFailsForNonExistent() {
        when(memberRepo.findById(99)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> authService.loginMember(99));
    }

    @Test
    void registerLibrarianSavesToRepo() {
        when(librarianRepo.save(any(Librarian.class))).thenAnswer(inv -> {
            Librarian l = inv.getArgument(0);
            l.setId(1);
            return l;
        });

        Librarian lib = authService.registerLibrarian("admin", "pass", "Admin");

        assertEquals(1, lib.getId());
        assertEquals("admin", lib.getUsername());
        assertNotEquals("pass", lib.getPasswordHash()); // should be hashed
        verify(librarianRepo).save(any(Librarian.class));
    }

    @Test
    void hasAnyLibrarianDelegatesToRepo() {
        when(librarianRepo.count()).thenReturn(0L);
        assertFalse(authService.hasAnyLibrarian());

        when(librarianRepo.count()).thenReturn(1L);
        assertTrue(authService.hasAnyLibrarian());
    }

    @Test
    void hashPasswordIsConsistent() {
        String hash1 = AuthService.hashPassword("test");
        String hash2 = AuthService.hashPassword("test");
        assertEquals(hash1, hash2);
    }

    @Test
    void hashPasswordDiffersForDifferentInputs() {
        String hash1 = AuthService.hashPassword("password1");
        String hash2 = AuthService.hashPassword("password2");
        assertNotEquals(hash1, hash2);
    }
}
