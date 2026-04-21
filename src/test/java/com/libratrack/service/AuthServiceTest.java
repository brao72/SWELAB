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
    void loginLibrarian_success() {
        String hash = AuthService.hashPassword("admin123");
        Librarian lib = new Librarian("admin", hash, "Admin");
        lib.setId(1);
        when(librarianRepo.findByUsername("admin")).thenReturn(Optional.of(lib));

        Session session = authService.loginLibrarian("admin", "admin123");

        assertEquals(Role.LIBRARIAN, session.getRole());
        assertEquals("Admin", session.getDisplayName());
    }

    @Test
    void loginLibrarian_wrongUsername_throws() {
        when(librarianRepo.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> authService.loginLibrarian("nobody", "pass"));
    }

    @Test
    void loginLibrarian_wrongPassword_throws() {
        Librarian lib = new Librarian("admin", AuthService.hashPassword("correct"), "Admin");
        when(librarianRepo.findByUsername("admin")).thenReturn(Optional.of(lib));

        assertThrows(IllegalArgumentException.class,
                () -> authService.loginLibrarian("admin", "wrong"));
    }

    @Test
    void loginMember_success() {
        Student student = new Student("Alice", "a@b.com", "123");
        student.setId(5);
        when(memberRepo.findById(5)).thenReturn(Optional.of(student));

        Session session = authService.loginMember(5);

        assertEquals(Role.MEMBER, session.getRole());
        assertEquals(5, session.getUserId());
        assertEquals("Alice", session.getDisplayName());
    }

    @Test
    void loginMember_notFound_throws() {
        when(memberRepo.findById(99)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> authService.loginMember(99));
    }

    @Test
    void loginMember_inactive_throws() {
        Student student = new Student("A", "a@b.com", "1");
        student.setActive(false);
        when(memberRepo.findById(1)).thenReturn(Optional.of(student));

        assertThrows(IllegalArgumentException.class,
                () -> authService.loginMember(1));
    }

    @Test
    void registerLibrarian_savesToRepo() {
        when(librarianRepo.save(any(Librarian.class))).thenAnswer(inv -> inv.getArgument(0));

        Librarian lib = authService.registerLibrarian("newadmin", "pass", "New Admin");

        assertEquals("newadmin", lib.getUsername());
        assertNotEquals("pass", lib.getPasswordHash()); // should be hashed
        verify(librarianRepo).save(any(Librarian.class));
    }

    @Test
    void hashPassword_isSHA256_deterministic() {
        String hash1 = AuthService.hashPassword("test");
        String hash2 = AuthService.hashPassword("test");
        assertEquals(hash1, hash2);
        assertEquals(64, hash1.length()); // SHA-256 = 64 hex chars
    }

    @Test
    void hashPassword_differentInputs_differentHashes() {
        assertNotEquals(
                AuthService.hashPassword("password1"),
                AuthService.hashPassword("password2"));
    }

    @Test
    void hasAnyLibrarian_true() {
        when(librarianRepo.count()).thenReturn(1L);
        assertTrue(authService.hasAnyLibrarian());
    }

    @Test
    void hasAnyLibrarian_false() {
        when(librarianRepo.count()).thenReturn(0L);
        assertFalse(authService.hasAnyLibrarian());
    }
}
