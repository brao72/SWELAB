package com.libratrack.command;

import com.libratrack.model.Fine;
import com.libratrack.service.FineService;

import java.util.List;
import java.util.Scanner;

public class ViewFinesCommand implements Command {
    private final FineService fineService;
    private final Scanner scanner;

    public ViewFinesCommand(FineService fineService, Scanner scanner) {
        this.fineService = fineService;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        System.out.println("\n--- View Fines ---");
        System.out.print("Member ID: ");
        int memberId = Integer.parseInt(scanner.nextLine().trim());

        List<Fine> fines = fineService.getUnpaidFines(memberId);
        if (fines.isEmpty()) {
            System.out.println("No unpaid fines for this member.");
        } else {
            double total = fineService.getTotalUnpaid(memberId);
            fines.forEach(System.out::println);
            System.out.printf("Total unpaid: ₹%.2f%n", total);
        }
    }
}
