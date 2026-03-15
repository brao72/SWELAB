package com.libratrack.command;

import com.libratrack.service.BorrowService;

import java.util.Scanner;

public class ReturnBookCommand implements Command {
    private final BorrowService borrowService;
    private final Scanner scanner;

    public ReturnBookCommand(BorrowService borrowService, Scanner scanner) {
        this.borrowService = borrowService;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        System.out.println("\n--- Return Book ---");
        System.out.print("Member ID: ");
        int memberId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Book ISBN: ");
        String isbn = scanner.nextLine().trim();

        BorrowService.ReturnResult result = borrowService.returnBook(memberId, isbn);
        System.out.println("Book returned successfully!");
        if (result.daysOverdue() > 0) {
            System.out.printf("  Overdue by %d days. Fine: ₹%.2f%n", result.daysOverdue(), result.fineAmount());
        } else {
            System.out.println("  Returned on time. No fine.");
        }
    }
}
