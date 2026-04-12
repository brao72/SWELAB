package com.libratrack.api;

import com.libratrack.model.Session;
import com.libratrack.service.AuthService;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public void registerRoutes(Javalin app) {
        app.post("/api/auth/login", this::login);
    }

    private void login(Context ctx) {
        LoginRequest req = ctx.bodyAsClass(LoginRequest.class);

        Session session;
        if ("librarian".equalsIgnoreCase(req.type())) {
            session = authService.loginLibrarian(req.username(), req.password());
        } else if ("member".equalsIgnoreCase(req.type())) {
            session = authService.loginMember(req.memberId());
        } else {
            ctx.status(400).json(new ApiServer.ErrorResponse("Invalid login type. Use 'librarian' or 'member'."));
            return;
        }

        ctx.json(new LoginResponse(
                session.getRole().name(),
                session.getUserId(),
                session.getDisplayName()
        ));
    }

    public record LoginRequest(String type, String username, String password, int memberId) {}
    public record LoginResponse(String role, int userId, String displayName) {}
}
