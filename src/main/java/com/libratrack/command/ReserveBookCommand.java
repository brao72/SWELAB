package com.libratrack.command;

import com.libratrack.model.Reservation;
import com.libratrack.service.BorrowService;

import java.util.Scanner;

public class ReserveBookCommand implements Command {
    private final BorrowService borrowService;
    private final Scanner scanner;

    public ReserveBookCommand(BorrowService borrowService, Scanner scanner) {
        this.borrowService = borrowService;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        System.out.println("\n--- Reserve Book ---");
        System.out.print("Member ID: ");
        int memberId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Book ISBN: ");
        String isbn = scanner.nextLine().trim();

        Reservation reservation = borrowService.reserveBook(memberId, isbn);
        System.out.printf("Reservation created! ID: %d. You'll be notified when the book is available.%n",
                reservation.getId());
    }
}
