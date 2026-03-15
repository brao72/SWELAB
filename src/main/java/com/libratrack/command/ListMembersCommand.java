package com.libratrack.command;

import com.libratrack.model.Member;
import com.libratrack.service.MemberService;

import java.util.List;

public class ListMembersCommand implements Command {
    private final MemberService memberService;

    public ListMembersCommand(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public void execute() {
        System.out.println("\n--- All Members ---");
        List<Member> members = memberService.listAllMembers();
        if (members.isEmpty()) {
            System.out.println("No members registered.");
        } else {
            System.out.printf("%-5s %-20s %-25s %-12s %-8s %s%n", "ID", "Name", "Email", "Phone", "Type", "Status");
            System.out.println("-".repeat(90));
            members.forEach(System.out::println);
        }
    }
}
