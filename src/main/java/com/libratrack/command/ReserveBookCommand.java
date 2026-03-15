package com.libratrack.command;

import com.libratrack.model.Reservation;
import com.libratrack.model.Role;
import com.libratrack.model.Session;
import com.libratrack.service.BorrowService;

import java.util.Scanner;

public class ReserveBookCommand implements Command {
    private final BorrowService borrowService;
    private final Scanner scanner;
    private final Session session;

    public ReserveBookCommand(BorrowService borrowService, Scanner scanner) {
        this(borrowService, scanner, null);
    }

    public ReserveBookCommand(BorrowService borrowService, Scanner scanner, Session session) {
        this.borrowService = borrowService;
        this.scanner = scanner;
        this.session = session;
    }

    @Override
    public void execute() {
        System.out.println("\n--- Reserve Book ---");
        int memberId;
        if (session != null && session.getRole() == Role.MEMBER) {
            memberId = session.getUserId();
        } else {
            System.out.print("Member ID: ");
            memberId = Integer.parseInt(scanner.nextLine().trim());
        }
        System.out.print("Book ISBN: ");
        String isbn = scanner.nextLine().trim();

        Reservation reservation = borrowService.reserveBook(memberId, isbn);
        System.out.printf("Reservation created! ID: %d. You'll be notified when the book is available.%n",
                reservation.getId());
    }
}
