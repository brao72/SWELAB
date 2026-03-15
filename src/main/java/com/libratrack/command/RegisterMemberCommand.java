package com.libratrack.command;

import com.libratrack.model.Member;
import com.libratrack.model.MemberType;
import com.libratrack.service.MemberService;

import java.util.Scanner;

public class RegisterMemberCommand implements Command {
    private final MemberService memberService;
    private final Scanner scanner;

    public RegisterMemberCommand(MemberService memberService, Scanner scanner) {
        this.memberService = memberService;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        System.out.println("\n--- Register New Member ---");
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Phone: ");
        String phone = scanner.nextLine().trim();
        System.out.print("Type (STUDENT/FACULTY): ");
        String typeStr = scanner.nextLine().trim().toUpperCase();

        MemberType type;
        try {
            type = MemberType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid member type. Use STUDENT or FACULTY.");
            return;
        }

        Member member = memberService.registerMember(type, name, email, phone);
        System.out.printf("Member registered! ID: %d, Type: %s, Borrow Limit: %d, Loan Period: %d days%n",
                member.getId(), type, member.getBorrowLimit(), member.getLoanPeriodDays());
    }
}
