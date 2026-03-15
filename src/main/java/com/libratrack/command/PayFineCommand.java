package com.libratrack.command;

import com.libratrack.model.Fine;
import com.libratrack.model.Role;
import com.libratrack.model.Session;
import com.libratrack.service.FineService;

import java.util.List;
import java.util.Scanner;

public class PayFineCommand implements Command {
    private final FineService fineService;
    private final Scanner scanner;
    private final Session session;

    public PayFineCommand(FineService fineService, Scanner scanner) {
        this(fineService, scanner, null);
    }

    public PayFineCommand(FineService fineService, Scanner scanner, Session session) {
        this.fineService = fineService;
        this.scanner = scanner;
        this.session = session;
    }

    @Override
    public void execute() {
        System.out.println("\n--- Pay Fine ---");
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
            return;
        }

        fines.forEach(System.out::println);
        System.out.print("Enter Fine ID to pay: ");
        int fineId = Integer.parseInt(scanner.nextLine().trim());

        fineService.payFine(fineId, fines);
        System.out.println("Fine paid successfully!");
    }
}
