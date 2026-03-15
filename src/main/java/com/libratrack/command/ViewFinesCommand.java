package com.libratrack.command;

import com.libratrack.model.Fine;
import com.libratrack.model.Role;
import com.libratrack.model.Session;
import com.libratrack.service.FineService;

import java.util.List;
import java.util.Scanner;

public class ViewFinesCommand implements Command {
    private final FineService fineService;
    private final Scanner scanner;
    private final Session session;

    public ViewFinesCommand(FineService fineService, Scanner scanner) {
        this(fineService, scanner, null);
    }

    public ViewFinesCommand(FineService fineService, Scanner scanner, Session session) {
        this.fineService = fineService;
        this.scanner = scanner;
        this.session = session;
    }

    @Override
    public void execute() {
        System.out.println("\n--- View Fines ---");
        int memberId;
        if (session != null && session.getRole() == Role.MEMBER) {
            memberId = session.getUserId();
        } else {
            System.out.print("Member ID: ");
            memberId = Integer.parseInt(scanner.nextLine().trim());
        }

        List<Fine> fines = fineService.getUnpaidFines(memberId);
        if (fines.isEmpty()) {
            System.out.println("No unpaid fines.");
        } else {
            double total = fineService.getTotalUnpaid(memberId);
            fines.forEach(System.out::println);
            System.out.printf("Total unpaid: ₹%.2f%n", total);
        }
    }
}
