package com.libratrack.command;

import com.libratrack.model.BorrowRecord;
import com.libratrack.service.BorrowService;

import java.util.Scanner;

public class IssueBookCommand implements Command {
    private final BorrowService borrowService;
    private final Scanner scanner;

    public IssueBookCommand(BorrowService borrowService, Scanner scanner) {
        this.borrowService = borrowService;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        System.out.println("\n--- Issue Book ---");
        System.out.print("Member ID: ");
        int memberId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Book ISBN: ");
        String isbn = scanner.nextLine().trim();

        BorrowRecord record = borrowService.issueBook(memberId, isbn);
        System.out.printf("Book issued successfully!%n  Issue Date: %s%n  Due Date: %s%n",
                record.getIssueDate(), record.getDueDate());
    }
}
