package com.libratrack.service;

import com.libratrack.model.*;
import com.libratrack.observer.ReservationNotifier;
import com.libratrack.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock private BookRepository bookRepo;
    @Mock private MemberRepository memberRepo;
    @Mock private BorrowRecordRepository borrowRepo;
    @Mock private ReservationRepository reservationRepo;
    @Mock private FineRepository fineRepo;
    @Mock private ReservationNotifier notifier;

    private BorrowService borrowService;

    @BeforeEach
    void setUp() {
        borrowService = new BorrowService(bookRepo, memberRepo, borrowRepo, reservationRepo, fineRepo, notifier);
    }

    // --- issueBook tests ---

    @Test
    void issueBook_success() {
        Student student = new Student("Alice", "a@b.com", "123", "hash");
        student.setId(1);
        Book book = new Book("Test", "Auth", "978-111", "Genre", 3);
        book.setId(10);

        when(memberRepo.findById(1)).thenReturn(Optional.of(student));
        when(fineRepo.getTotalUnpaidByMemberId(1)).thenReturn(0.0);
        when(bookRepo.findByIsbn("978-111")).thenReturn(Optional.of(book));
        when(borrowRepo.findActiveByMemberId(1)).thenReturn(List.of());
        when(borrowRepo.save(any(BorrowRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        BorrowRecord record = borrowService.issueBook(1, "978-111");

        assertEquals(10, record.getBookId());
        assertEquals(1, record.getMemberId());
        assertEquals(LocalDate.now(), record.getIssueDate());
        assertEquals(LocalDate.now().plusDays(14), record.getDueDate());
        assertEquals(2, book.getAvailableCopies()); // 3 - 1
        verify(bookRepo).update(book);
    }

    @Test
    void issueBook_memberNotFound_throws() {
        when(memberRepo.findById(99)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> borrowService.issueBook(99, "978-111"));
    }

    @Test
    void issueBook_inactiveMember_throws() {
        Student student = new Student("A", "a@b.com", "1", "hash");
        student.setActive(false);
        when(memberRepo.findById(1)).thenReturn(Optional.of(student));

        assertThrows(IllegalStateException.class,
                () -> borrowService.issueBook(1, "978-111"));
    }

    @Test
    void issueBook_tooManyUnpaidFines_throws() {
        Student student = new Student("A", "a@b.com", "1", "hash");
        when(memberRepo.findById(1)).thenReturn(Optional.of(student));
        when(fineRepo.getTotalUnpaidByMemberId(1)).thenReturn(60.0); // > 50

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> borrowService.issueBook(1, "978-111"));

        assertTrue(ex.getMessage().contains("unpaid fines"));
    }

    @Test
    void issueBook_borrowLimitReached_throws() {
        Student student = new Student("A", "a@b.com", "1", "hash");
        student.setId(1);
        Book book = new Book("T", "A", "978-111", "G", 5);

        when(memberRepo.findById(1)).thenReturn(Optional.of(student));
        when(fineRepo.getTotalUnpaidByMemberId(1)).thenReturn(0.0);
        when(bookRepo.findByIsbn("978-111")).thenReturn(Optional.of(book));
        // Student limit is 3 - return 3 active loans
        when(borrowRepo.findActiveByMemberId(1)).thenReturn(
                List.of(new BorrowRecord(), new BorrowRecord(), new BorrowRecord()));

        assertThrows(IllegalStateException.class,
                () -> borrowService.issueBook(1, "978-111"));
    }

    @Test
    void issueBook_noCopiesAvailable_throws() {
        Student student = new Student("A", "a@b.com", "1", "hash");
        student.setId(1);
        Book book = new Book("T", "A", "978-111", "G", 1);
        book.setAvailableCopies(0);

        when(memberRepo.findById(1)).thenReturn(Optional.of(student));
        when(fineRepo.getTotalUnpaidByMemberId(1)).thenReturn(0.0);
        when(bookRepo.findByIsbn("978-111")).thenReturn(Optional.of(book));
        when(borrowRepo.findActiveByMemberId(1)).thenReturn(List.of());

        assertThrows(IllegalStateException.class,
                () -> borrowService.issueBook(1, "978-111"));
    }

    // --- returnBook tests ---

    @Test
    void returnBook_onTime_noFine() {
        Book book = new Book("T", "A", "978-111", "G", 3);
        book.setId(10);
        book.setAvailableCopies(2);
        BorrowRecord record = new BorrowRecord(10, 1, LocalDate.now().minusDays(5), LocalDate.now().plusDays(9));

        when(bookRepo.findByIsbn("978-111")).thenReturn(Optional.of(book));
        when(borrowRepo.findActiveByBookAndMember(10, 1)).thenReturn(Optional.of(record));

        BorrowService.ReturnResult result = borrowService.returnBook(1, "978-111");

        assertTrue(record.isReturned());
        assertEquals(LocalDate.now(), record.getReturnDate());
        assertEquals(3, book.getAvailableCopies()); // 2 + 1
        assertEquals(0.0, result.fineAmount());
        verify(fineRepo, never()).save(any());
    }

    @Test
    void returnBook_overdue_createsFine() {
        Book book = new Book("T", "A", "978-111", "G", 3);
        book.setId(10);
        book.setAvailableCopies(2);
        // Due date was 5 days ago
        BorrowRecord record = new BorrowRecord(10, 1, LocalDate.now().minusDays(19), LocalDate.now().minusDays(5));
        Student student = new Student("A", "a@b.com", "1", "hash");

        when(bookRepo.findByIsbn("978-111")).thenReturn(Optional.of(book));
        when(borrowRepo.findActiveByBookAndMember(10, 1)).thenReturn(Optional.of(record));
        when(memberRepo.findById(1)).thenReturn(Optional.of(student));

        BorrowService.ReturnResult result = borrowService.returnBook(1, "978-111");

        assertEquals(5, result.daysOverdue());
        assertEquals(10.0, result.fineAmount());  // 5 days * Rs.2 (student rate)
        verify(fineRepo).save(any(Fine.class));
    }

    @Test
    void returnBook_notifiesObserver() {
        Book book = new Book("T", "A", "978-111", "G", 3);
        book.setId(10);
        book.setAvailableCopies(2);
        BorrowRecord record = new BorrowRecord(10, 1, LocalDate.now().minusDays(5), LocalDate.now().plusDays(9));

        when(bookRepo.findByIsbn("978-111")).thenReturn(Optional.of(book));
        when(borrowRepo.findActiveByBookAndMember(10, 1)).thenReturn(Optional.of(record));

        borrowService.returnBook(1, "978-111");

        verify(notifier).notifyBookAvailable(10, "T");
    }

    // --- reserveBook tests ---

    @Test
    void reserveBook_success() {
        Student student = new Student("A", "a@b.com", "1", "hash");
        Book book = new Book("T", "A", "978-111", "G", 1);
        book.setId(10);
        book.setAvailableCopies(0);

        when(memberRepo.findById(1)).thenReturn(Optional.of(student));
        when(bookRepo.findByIsbn("978-111")).thenReturn(Optional.of(book));
        when(reservationRepo.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        Reservation res = borrowService.reserveBook(1, "978-111");

        assertEquals(10, res.getBookId());
        assertEquals(1, res.getMemberId());
    }

    @Test
    void reserveBook_bookAvailable_throws() {
        Student student = new Student("A", "a@b.com", "1", "hash");
        Book book = new Book("T", "A", "978-111", "G", 1);
        book.setAvailableCopies(1); // available - should issue, not reserve

        when(memberRepo.findById(1)).thenReturn(Optional.of(student));
        when(bookRepo.findByIsbn("978-111")).thenReturn(Optional.of(book));

        assertThrows(IllegalStateException.class,
                () -> borrowService.reserveBook(1, "978-111"));
    }

    // --- getMemberHistory ---

    @Test
    void getMemberHistory_delegatesToRepo() {
        when(borrowRepo.findByMemberId(1)).thenReturn(List.of(new BorrowRecord(), new BorrowRecord()));

        List<BorrowRecord> history = borrowService.getMemberHistory(1);

        assertEquals(2, history.size());
    }
}
