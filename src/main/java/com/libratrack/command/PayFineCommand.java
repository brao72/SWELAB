package com.libratrack.command;

import com.libratrack.model.Fine;
import com.libratrack.service.FineService;

import java.util.List;
import java.util.Scanner;

public class PayFineCommand implements Command {
    private final FineService fineService;
    private final Scanner scanner;

    public PayFineCommand(FineService fineService, Scanner scanner) {
        this.fineService = fineService;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        System.out.println("\n--- Pay Fine ---");
        System.out.print("Member ID: ");
        int memberId = Integer.parseInt(scanner.nextLine().trim());

        List<Fine> fines = fineService.getUnpaidFines(memberId);
        if (fines.isEmpty()) {
            System.out.println("No unpaid fines for this member.");
            return;
        }

        fines.forEach(System.out::println);
        System.out.print("Enter Fine ID to pay: ");
        int fineId = Integer.parseInt(scanner.nextLine().trim());

        fineService.payFine(fineId, fines);
        System.out.println("Fine paid successfully!");
    }
}
