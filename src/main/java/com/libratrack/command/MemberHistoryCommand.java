package com.libratrack.command;

import com.libratrack.model.BorrowRecord;
import com.libratrack.service.BorrowService;

import java.util.List;
import java.util.Scanner;

public class MemberHistoryCommand implements Command {
    private final BorrowService borrowService;
    private final Scanner scanner;

    public MemberHistoryCommand(BorrowService borrowService, Scanner scanner) {
        this.borrowService = borrowService;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        System.out.println("\n--- Member Borrowing History ---");
        System.out.print("Member ID: ");
        int memberId = Integer.parseInt(scanner.nextLine().trim());

        List<BorrowRecord> records = borrowService.getMemberHistory(memberId);
        if (records.isEmpty()) {
            System.out.println("No borrowing history for this member.");
        } else {
            records.forEach(System.out::println);
        }
    }
}
