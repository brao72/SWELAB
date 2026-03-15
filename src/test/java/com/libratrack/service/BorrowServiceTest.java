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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock private BookRepository bookRepo;
    @Mock private MemberRepository memberRepo;
    @Mock private BorrowRecordRepository borrowRepo;
    @Mock private ReservationRepository reservationRepo;
    @Mock private FineRepository fineRepo;

    private ReservationNotifier notifier;
    private BorrowService borrowService;

    @BeforeEach
    void setUp() {
        notifier = new ReservationNotifier(reservationRepo);
        borrowService = new BorrowService(bookRepo, memberRepo, borrowRepo, reservationRepo, fineRepo, notifier);
    }

    @Test
    void issueBookSuccessfully() {
        Student student = new Student("Alice", "alice@uni.edu", "123");
        student.setId(1);
        Book book = new Book("Clean Code", "Martin", "978-0132350884", "Software", 3);
        book.setId(1);

        when(memberRepo.findById(1)).thenReturn(Optional.of(student));
        when(fineRepo.getTotalUnpaidByMemberId(1)).thenReturn(0.0);
        when(bookRepo.findByIsbn("978-0132350884")).thenReturn(Optional.of(book));
        when(borrowRepo.findActiveByMemberId(1)).thenReturn(List.of());
        when(borrowRepo.save(any(BorrowRecord.class))).thenAnswer(inv -> {
            BorrowRecord r = inv.getArgument(0);
            r.setId(1);
            return r;
        });

        BorrowRecord result = borrowService.issueBook(1, "978-0132350884");

        assertEquals(1, result.getId());
        assertEquals(1, result.getBookId());
        assertEquals(1, result.getMemberId());
        assertEquals(LocalDate.now(), result.getIssueDate());
        assertEquals(LocalDate.now().plusDays(14), result.getDueDate());
        verify(bookRepo).update(book);
        assertEquals(2, book.getAvailableCopies());
    }

    @Test
    void issueBookFailsForNonExistentMember() {
        when(memberRepo.findById(99)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> borrowService.issueBook(99, "978-0132350884"));
    }

    @Test
    void issueBookFailsForInactiveMember() {
        Student student = new Student("Alice", "alice@uni.edu", "123");
        student.setId(1);
        student.setActive(false);
        when(memberRepo.findById(1)).thenReturn(Optional.of(student));

        assertThrows(IllegalStateException.class,
                () -> borrowService.issueBook(1, "978-0132350884"));
    }

    @Test
    void issueBookFailsForExcessiveFines() {
        Student student = new Student("Alice", "alice@uni.edu", "123");
        student.setId(1);
        when(memberRepo.findById(1)).thenReturn(Optional.of(student));
        when(fineRepo.getTotalUnpaidByMemberId(1)).thenReturn(60.0);

        assertThrows(IllegalStateException.class,
                () -> borrowService.issueBook(1, "978-0132350884"));
    }

    @Test
    void issueBookFailsWhenBorrowLimitReached() {
        Student student = new Student("Alice", "alice@uni.edu", "123");
        student.setId(1);
        Book book = new Book("Clean Code", "Martin", "978-0132350884", "Software", 3);
        book.setId(1);

        when(memberRepo.findById(1)).thenReturn(Optional.of(student));
        when(fineRepo.getTotalUnpaidByMemberId(1)).thenReturn(0.0);
        when(bookRepo.findByIsbn("978-0132350884")).thenReturn(Optional.of(book));
        when(borrowRepo.findActiveByMemberId(1)).thenReturn(List.of(
                new BorrowRecord(2, 1, LocalDate.now(), LocalDate.now().plusDays(14)),
                new BorrowRecord(3, 1, LocalDate.now(), LocalDate.now().plusDays(14)),
                new BorrowRecord(4, 1, LocalDate.now(), LocalDate.now().plusDays(14))
        ));

        assertThrows(IllegalStateException.class,
                () -> borrowService.issueBook(1, "978-0132350884"));
    }

    @Test
    void issueBookFailsWhenNoCopiesAvailable() {
        Student student = new Student("Alice", "alice@uni.edu", "123");
        student.setId(1);
        Book book = new Book("Clean Code", "Martin", "978-0132350884", "Software", 3);
        book.setId(1);
        book.setAvailableCopies(0);

        when(memberRepo.findById(1)).thenReturn(Optional.of(student));
        when(fineRepo.getTotalUnpaidByMemberId(1)).thenReturn(0.0);
        when(bookRepo.findByIsbn("978-0132350884")).thenReturn(Optional.of(book));
        when(borrowRepo.findActiveByMemberId(1)).thenReturn(List.of());

        assertThrows(IllegalStateException.class,
                () -> borrowService.issueBook(1, "978-0132350884"));
    }

    @Test
    void returnBookWithNoFine() {
        Book book = new Book("Clean Code", "Martin", "978-0132350884", "Software", 3);
        book.setId(1);
        book.setAvailableCopies(2);
        BorrowRecord record = new BorrowRecord(1, 1, LocalDate.now().minusDays(5), LocalDate.now().plusDays(9));
        record.setId(1);

        when(bookRepo.findByIsbn("978-0132350884")).thenReturn(Optional.of(book));
        when(borrowRepo.findActiveByBookAndMember(1, 1)).thenReturn(Optional.of(record));
        when(reservationRepo.findPendingByBookId(1)).thenReturn(List.of());

        BorrowService.ReturnResult result = borrowService.returnBook(1, "978-0132350884");

        assertTrue(record.isReturned());
        assertEquals(0.0, result.fineAmount());
        assertEquals(3, book.getAvailableCopies());
    }

    @Test
    void returnBookWithFineForOverdue() {
        Book book = new Book("Clean Code", "Martin", "978-0132350884", "Software", 3);
        book.setId(1);
        book.setAvailableCopies(2);
        BorrowRecord record = new BorrowRecord(1, 1, LocalDate.now().minusDays(20), LocalDate.now().minusDays(5));
        record.setId(1);

        Student student = new Student("Alice", "alice@uni.edu", "123");
        student.setId(1);

        when(bookRepo.findByIsbn("978-0132350884")).thenReturn(Optional.of(book));
        when(borrowRepo.findActiveByBookAndMember(1, 1)).thenReturn(Optional.of(record));
        when(memberRepo.findById(1)).thenReturn(Optional.of(student));
        when(fineRepo.save(any(Fine.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reservationRepo.findPendingByBookId(1)).thenReturn(List.of());

        BorrowService.ReturnResult result = borrowService.returnBook(1, "978-0132350884");

        assertEquals(5, result.daysOverdue());
        assertEquals(10.0, result.fineAmount()); // Student: 5 days * ₹2/day
        verify(fineRepo).save(any(Fine.class));
    }

    @Test
    void returnBookFailsForMissingBook() {
        when(bookRepo.findByIsbn("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> borrowService.returnBook(1, "nonexistent"));
    }

    @Test
    void reserveBookSuccessfully() {
        Student student = new Student("Alice", "alice@uni.edu", "123");
        student.setId(1);
        Book book = new Book("Clean Code", "Martin", "978-0132350884", "Software", 3);
        book.setId(1);
        book.setAvailableCopies(0);

        when(memberRepo.findById(1)).thenReturn(Optional.of(student));
        when(bookRepo.findByIsbn("978-0132350884")).thenReturn(Optional.of(book));
        when(reservationRepo.save(any(Reservation.class))).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            r.setId(1);
            return r;
        });

        Reservation result = borrowService.reserveBook(1, "978-0132350884");

        assertEquals(1, result.getId());
        assertEquals(1, result.getBookId());
    }

    @Test
    void reserveBookFailsWhenCopiesAvailable() {
        Student student = new Student("Alice", "alice@uni.edu", "123");
        student.setId(1);
        Book book = new Book("Clean Code", "Martin", "978-0132350884", "Software", 3);
        book.setId(1);

        when(memberRepo.findById(1)).thenReturn(Optional.of(student));
        when(bookRepo.findByIsbn("978-0132350884")).thenReturn(Optional.of(book));

        assertThrows(IllegalStateException.class,
                () -> borrowService.reserveBook(1, "978-0132350884"));
    }

    @Test
    void getMemberHistoryDelegatesToRepo() {
        when(borrowRepo.findByMemberId(1)).thenReturn(List.of(
                new BorrowRecord(1, 1, LocalDate.now(), LocalDate.now().plusDays(14))
        ));

        assertEquals(1, borrowService.getMemberHistory(1).size());
    }
}
