package com.libratrack.api;

import com.libratrack.model.Book;
import com.libratrack.model.BorrowRecord;
import com.libratrack.model.Reservation;
import com.libratrack.service.BookService;
import com.libratrack.service.BorrowService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.time.LocalDate;
import java.util.List;

public class BorrowController {
    private final BorrowService borrowService;
    private final BookService bookService;

    public BorrowController(BorrowService borrowService, BookService bookService) {
        this.borrowService = borrowService;
        this.bookService = bookService;
    }

    public void registerRoutes(Javalin app) {
        app.post("/api/borrow/issue", this::issueBook);
        app.post("/api/borrow/return", this::returnBook);
        app.post("/api/borrow/reserve", this::reserveBook);
        app.get("/api/borrow/history/{memberId}", this::getMemberHistory);
        app.get("/api/borrow/notifications/{memberId}", this::getNotifications);
    }

    private void issueBook(Context ctx) {
        BorrowRequest req = ctx.bodyAsClass(BorrowRequest.class);
        LocalDate dueDate = req.dueDate() != null ? LocalDate.parse(req.dueDate()) : null;
        BorrowRecord record = borrowService.issueBook(req.memberId(), req.isbn(), dueDate);
        ctx.status(201).json(record);
    }

    private void returnBook(Context ctx) {
        BorrowRequest req = ctx.bodyAsClass(BorrowRequest.class);
        BorrowService.ReturnResult result = borrowService.returnBook(req.memberId(), req.isbn());
        ctx.json(new ReturnResponse(
                result.record().getId(),
                result.fineAmount(),
                result.daysOverdue()
        ));
    }

    private void reserveBook(Context ctx) {
        BorrowRequest req = ctx.bodyAsClass(BorrowRequest.class);
        Reservation reservation = borrowService.reserveBook(req.memberId(), req.isbn());
        ctx.status(201).json(reservation);
    }

    private void getMemberHistory(Context ctx) {
        int memberId = Integer.parseInt(ctx.pathParam("memberId"));
        List<BorrowRecord> history = borrowService.getMemberHistory(memberId);
        ctx.json(history);
    }

    private void getNotifications(Context ctx) {
        int memberId = Integer.parseInt(ctx.pathParam("memberId"));
        List<Reservation> reservations = borrowService.getMemberReservations(memberId);
        List<NotificationResponse> notifications = reservations.stream()
                .filter(Reservation::isNotified)
                .map(r -> {
                    String bookTitle = bookService.findById(r.getBookId())
                            .map(Book::getTitle)
                            .orElse("Unknown Book");
                    return new NotificationResponse(r.getId(), bookTitle, r.getReservedAt().toString());
                })
                .toList();
        ctx.json(notifications);
    }

    public record BorrowRequest(int memberId, String isbn, String dueDate) {}
    public record NotificationResponse(int reservationId, String bookTitle, String reservedAt) {}
    public record ReturnResponse(int recordId, double fineAmount, long daysOverdue) {}
}
