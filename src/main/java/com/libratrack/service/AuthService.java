package com.libratrack.service;

import com.libratrack.model.*;
import com.libratrack.repository.LibrarianRepository;
import com.libratrack.repository.MemberRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class AuthService {
    private final LibrarianRepository librarianRepo;
    private final MemberRepository memberRepo;

    public AuthService(LibrarianRepository librarianRepo, MemberRepository memberRepo) {
        this.librarianRepo = librarianRepo;
        this.memberRepo = memberRepo;
    }

    public Session loginLibrarian(String username, String password) {
        Librarian lib = librarianRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username."));
        String hash = hashPassword(password);
        if (!hash.equals(lib.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password.");
        }
        return new Session(Role.LIBRARIAN, lib.getId(), lib.getName());
    }

    public Session loginMember(int memberId) {
        Member member = memberRepo.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));
        if (!member.isActive()) {
            throw new IllegalArgumentException("Member account is inactive.");
        }
        return new Session(Role.MEMBER, member.getId(), member.getName());
    }

    public Librarian registerLibrarian(String username, String password, String name) {
        Librarian lib = new Librarian(username, hashPassword(password), name);
        return librarianRepo.save(lib);
    }

    public boolean hasAnyLibrarian() {
        return librarianRepo.count() > 0;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
