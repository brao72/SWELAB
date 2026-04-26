package com.libratrack.service;

import com.libratrack.model.*;
import com.libratrack.observer.ReservationNotifier;
import com.libratrack.repository.*;
import com.libratrack.strategy.FineCalculator;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BorrowService {
    private static final double MAX_UNPAID_FINE = 50.0;

    private final BookRepository bookRepo;
    private final MemberRepository memberRepo;
    private final BorrowRecordRepository borrowRepo;
    private final ReservationRepository reservationRepo;
    private final FineRepository fineRepo;
    private final ReservationNotifier notifier;

    public BorrowService(BookRepository bookRepo, MemberRepository memberRepo,
                         BorrowRecordRepository borrowRepo, ReservationRepository reservationRepo,
                         FineRepository fineRepo, ReservationNotifier notifier) {
        this.bookRepo = bookRepo;
        this.memberRepo = memberRepo;
        this.borrowRepo = borrowRepo;
        this.reservationRepo = reservationRepo;
        this.fineRepo = fineRepo;
        this.notifier = notifier;
    }

    public BorrowRecord issueBook(int memberId, String isbn) {
        return issueBook(memberId, isbn, null);
    }

    public BorrowRecord issueBook(int memberId, String isbn, LocalDate customDueDate) {
        Member member = memberRepo.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

        if (!member.isActive()) {
            throw new IllegalStateException("Member account is inactive.");
        }

        double unpaidFines = fineRepo.getTotalUnpaidByMemberId(memberId);
        if (unpaidFines > MAX_UNPAID_FINE) {
            throw new IllegalStateException(
                    String.format("Member has ₹%.2f in unpaid fines (max allowed: ₹%.2f). Please clear fines first.", unpaidFines, MAX_UNPAID_FINE));
        }

        Book book = bookRepo.findByIsbn(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ISBN: " + isbn));

        List<BorrowRecord> activeLoans = borrowRepo.findActiveByMemberId(memberId);
        if (activeLoans.size() >= member.getBorrowLimit()) {
            throw new IllegalStateException(
                    String.format("Borrowing limit reached (%d/%d). Return a book first.", activeLoans.size(), member.getBorrowLimit()));
        }

        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No copies available. Consider reserving the book.");
        }

        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = customDueDate != null ? customDueDate : issueDate.plusDays(member.getLoanPeriodDays());
        BorrowRecord record = new BorrowRecord(book.getId(), memberId, issueDate, dueDate);
        borrowRepo.save(record);

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepo.update(book);

        return record;
    }

    public ReturnResult returnBook(int memberId, String isbn) {
        Book book = bookRepo.findByIsbn(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ISBN: " + isbn));

        BorrowRecord record = borrowRepo.findActiveByBookAndMember(book.getId(), memberId)
                .orElseThrow(() -> new IllegalArgumentException("No active borrow record found for this book and member."));

        record.setReturnDate(LocalDate.now());
        record.setReturned(true);
        borrowRepo.update(record);

        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepo.update(book);

        double fineAmount = 0;
        long daysOverdue = ChronoUnit.DAYS.between(record.getDueDate(), LocalDate.now());
        if (daysOverdue > 0) {
            Member member = memberRepo.findById(memberId).orElseThrow();
            FineCalculator calculator = new FineCalculator(member.getMemberType());
            fineAmount = calculator.calculate(daysOverdue);
            Fine fine = new Fine(record.getId(), memberId, fineAmount);
            fineRepo.save(fine);
        }

        notifier.notifyBookAvailable(book.getId(), book.getTitle());

        return new ReturnResult(record, fineAmount, daysOverdue);
    }

    public Reservation reserveBook(int memberId, String isbn) {
        memberRepo.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

        Book book = bookRepo.findByIsbn(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ISBN: " + isbn));

        if (book.getAvailableCopies() > 0) {
            throw new IllegalStateException("Book is available — issue it directly instead of reserving.");
        }

        Reservation reservation = new Reservation(book.getId(), memberId);
        return reservationRepo.save(reservation);
    }

    public List<BorrowRecord> getMemberHistory(int memberId) {
        return borrowRepo.findByMemberId(memberId);
    }

    public List<Reservation> getMemberReservations(int memberId) {
        return reservationRepo.findByMemberId(memberId);
    }

    public record ReturnResult(BorrowRecord record, double fineAmount, long daysOverdue) {}
}
