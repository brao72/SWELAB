package com.libratrack.command;

import com.libratrack.model.BorrowRecord;
import com.libratrack.model.Role;
import com.libratrack.model.Session;
import com.libratrack.service.BorrowService;

import java.util.List;
import java.util.Scanner;

public class MemberHistoryCommand implements Command {
    private final BorrowService borrowService;
    private final Scanner scanner;
    private final Session session;

    public MemberHistoryCommand(BorrowService borrowService, Scanner scanner) {
        this(borrowService, scanner, null);
    }

    public MemberHistoryCommand(BorrowService borrowService, Scanner scanner, Session session) {
        this.borrowService = borrowService;
        this.scanner = scanner;
        this.session = session;
    }

    @Override
    public void execute() {
        System.out.println("\n--- Borrowing History ---");
        int memberId;
        if (session != null && session.getRole() == Role.MEMBER) {
            memberId = session.getUserId();
        } else {
            System.out.print("Member ID: ");
            memberId = Integer.parseInt(scanner.nextLine().trim());
        }

        List<BorrowRecord> records = borrowService.getMemberHistory(memberId);
        if (records.isEmpty()) {
            System.out.println("No borrowing history found.");
        } else {
            records.forEach(System.out::println);
        }
    }
}
