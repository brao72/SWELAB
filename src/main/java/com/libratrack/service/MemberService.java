package com.libratrack.service;

import com.libratrack.factory.MemberFactory;
import com.libratrack.model.Member;
import com.libratrack.model.MemberType;
import com.libratrack.repository.MemberRepository;

import java.util.List;
import java.util.Optional;

public class MemberService {
    private final MemberRepository memberRepo;

    public MemberService(MemberRepository memberRepo) {
        this.memberRepo = memberRepo;
    }

    public Member registerMember(MemberType type, String name, String email, String phone, String passwordHash) {
        Member member = MemberFactory.createMember(type, name, email, phone, passwordHash);
        return memberRepo.save(member);
    }

    public Optional<Member> findById(int id) {
        return memberRepo.findById(id);
    }

    public List<Member> listAllMembers() {
        return memberRepo.findAll();
    }

    public void deactivateMember(int id) {
        Member member = memberRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + id));
        member.setActive(false);
        memberRepo.update(member);
    }
}
