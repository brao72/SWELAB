package com.libratrack.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.libratrack.db.DatabaseConnection;
import com.libratrack.observer.ReservationNotifier;
import com.libratrack.repository.*;
import com.libratrack.repository.impl.*;
import com.libratrack.service.*;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApiServer {
    private final Javalin app;

    public static void main(String[] args) {
        ApiServer server = new ApiServer();
        server.start();
    }

    public ApiServer() {
        initDatabase();

        BookRepository bookRepo = new PgBookRepository();
        MemberRepository memberRepo = new PgMemberRepository();
        BorrowRecordRepository borrowRepo = new PgBorrowRecordRepository();
        ReservationRepository reservationRepo = new PgReservationRepository();
        FineRepository fineRepo = new PgFineRepository();
        LibrarianRepository librarianRepo = new PgLibrarianRepository();
        ReservationNotifier notifier = new ReservationNotifier(reservationRepo);

        BookService bookService = new BookService(bookRepo);
        MemberService memberService = new MemberService(memberRepo);
        BorrowService borrowService = new BorrowService(bookRepo, memberRepo, borrowRepo, reservationRepo, fineRepo, notifier);
        FineService fineService = new FineService(fineRepo);
        AuthService authService = new AuthService(librarianRepo, memberRepo);

        if (!authService.hasAnyLibrarian()) {
            authService.registerLibrarian("admin", "admin123", "System Admin");
            System.out.println("Default librarian created — username: admin, password: admin123");
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(mapper));
            config.plugins.enableCors(cors -> cors.add(it -> {
                it.anyHost();
            }));
        });

        AuthController authController = new AuthController(authService);
        BookController bookController = new BookController(bookService);
        MemberController memberController = new MemberController(memberService);
        BorrowController borrowController = new BorrowController(borrowService);
        FineController fineController = new FineController(fineService);

        authController.registerRoutes(app);
        bookController.registerRoutes(app);
        memberController.registerRoutes(app);
        borrowController.registerRoutes(app);
        fineController.registerRoutes(app);

        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(400).json(new ErrorResponse(e.getMessage()));
        });
        app.exception(IllegalStateException.class, (e, ctx) -> {
            ctx.status(409).json(new ErrorResponse(e.getMessage()));
        });
        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(500).json(new ErrorResponse("Internal server error: " + e.getMessage()));
        });
    }

    public void start() {
        int port = 7070;
        app.start(port);
        System.out.println("LibraTrack API running on http://localhost:" + port);
    }

    private void initDatabase() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (is == null) {
                throw new RuntimeException("application.properties not found");
            }
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
        String url = System.getenv().getOrDefault("DB_URL", props.getProperty("db.url"));
        String user = System.getenv().getOrDefault("DB_USER", props.getProperty("db.user"));
        String password = System.getenv().getOrDefault("DB_PASSWORD", props.getProperty("db.password"));
        DatabaseConnection.initialize(url, user, password);
    }

    public record ErrorResponse(String error) {}
}
