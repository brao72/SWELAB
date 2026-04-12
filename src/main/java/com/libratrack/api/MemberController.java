package com.libratrack.api;

import com.libratrack.model.Member;
import com.libratrack.model.MemberType;
import com.libratrack.service.MemberService;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    public void registerRoutes(Javalin app) {
        app.get("/api/members", this::listMembers);
        app.get("/api/members/{id}", this::getMember);
        app.post("/api/members", this::registerMember);
        app.patch("/api/members/{id}/deactivate", this::deactivateMember);
    }

    private void listMembers(Context ctx) {
        ctx.json(memberService.listAllMembers());
    }

    private void getMember(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Member member = memberService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + id));
        ctx.json(member);
    }

    private void registerMember(Context ctx) {
        RegisterRequest req = ctx.bodyAsClass(RegisterRequest.class);
        MemberType type = MemberType.valueOf(req.type().toUpperCase());
        Member member = memberService.registerMember(type, req.name(), req.email(), req.phone());
        ctx.status(201).json(member);
    }

    private void deactivateMember(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        memberService.deactivateMember(id);
        ctx.status(204);
    }

    public record RegisterRequest(String type, String name, String email, String phone) {}
}
