package com.libratrack.cli;

import com.libratrack.command.*;
import com.libratrack.db.DatabaseConnection;
import com.libratrack.model.Role;
import com.libratrack.model.Session;
import com.libratrack.observer.ReservationNotifier;
import com.libratrack.repository.*;
import com.libratrack.repository.impl.*;
import com.libratrack.service.*;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

public class LibraTrackApp {
    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final Scanner scanner = new Scanner(System.in);

    private BookService bookService;
    private MemberService memberService;
    private BorrowService borrowService;
    private FineService fineService;
    private AuthService authService;

    public static void main(String[] args) {
        LibraTrackApp app = new LibraTrackApp();
        app.run();
    }

    public void run() {
        System.out.println("========================================");
        System.out.println("   LibraTrack - Library Management System");
        System.out.println("========================================");

        try {
            initDatabase();
            initSchema();
            initServices();
            seedDefaultLibrarian();
            loginLoop();
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                DatabaseConnection.getInstance().close();
            } catch (IllegalStateException ignored) {
                // Database was never initialized, nothing to close
            }
        }
    }

    private void initDatabase() throws IOException {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (is == null) {
                throw new IOException("application.properties not found in classpath");
            }
            props.load(is);
        }
        DatabaseConnection.initialize(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password")
        );
        System.out.println("Connected to database.");
    }

    private void initSchema() {
        String schema = """
                CREATE TABLE IF NOT EXISTS books (
                    id SERIAL PRIMARY KEY,
                    title VARCHAR(255) NOT NULL,
                    author VARCHAR(255) NOT NULL,
                    isbn VARCHAR(20) UNIQUE NOT NULL,
                    genre VARCHAR(100),
                    total_copies INT NOT NULL DEFAULT 1,
                    available_copies INT NOT NULL DEFAULT 1
                );

                CREATE TABLE IF NOT EXISTS members (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    email VARCHAR(255),
                    phone VARCHAR(20),
                    member_type VARCHAR(20) NOT NULL,
                    is_active BOOLEAN DEFAULT true,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );

                CREATE TABLE IF NOT EXISTS borrow_records (
                    id SERIAL PRIMARY KEY,
                    book_id INT REFERENCES books(id),
                    member_id INT REFERENCES members(id),
                    issue_date DATE NOT NULL,
                    due_date DATE NOT NULL,
                    return_date DATE,
                    is_returned BOOLEAN DEFAULT false
                );

                CREATE TABLE IF NOT EXISTS reservations (
                    id SERIAL PRIMARY KEY,
                    book_id INT REFERENCES books(id),
                    member_id INT REFERENCES members(id),
                    reserved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    notified BOOLEAN DEFAULT false,
                    fulfilled BOOLEAN DEFAULT false
                );

                CREATE TABLE IF NOT EXISTS fines (
                    id SERIAL PRIMARY KEY,
                    borrow_record_id INT REFERENCES borrow_records(id),
                    member_id INT REFERENCES members(id),
                    amount DECIMAL(10,2) NOT NULL,
                    is_paid BOOLEAN DEFAULT false,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );

                CREATE TABLE IF NOT EXISTS librarians (
                    id SERIAL PRIMARY KEY,
                    username VARCHAR(100) UNIQUE NOT NULL,
                    password_hash VARCHAR(64) NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(schema);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize schema: " + e.getMessage(), e);
        }
    }

    private void initServices() {
        BookRepository bookRepo = new PgBookRepository();
        MemberRepository memberRepo = new PgMemberRepository();
        BorrowRecordRepository borrowRepo = new PgBorrowRecordRepository();
        ReservationRepository reservationRepo = new PgReservationRepository();
        FineRepository fineRepo = new PgFineRepository();
        LibrarianRepository librarianRepo = new PgLibrarianRepository();

        ReservationNotifier notifier = new ReservationNotifier(reservationRepo);

        bookService = new BookService(bookRepo);
        memberService = new MemberService(memberRepo);
        borrowService = new BorrowService(bookRepo, memberRepo, borrowRepo, reservationRepo, fineRepo, notifier);
        fineService = new FineService(fineRepo);
        authService = new AuthService(librarianRepo, memberRepo);
    }

    private void seedDefaultLibrarian() {
        if (!authService.hasAnyLibrarian()) {
            authService.registerLibrarian("admin", "admin123", "System Admin");
            System.out.println("Default librarian created — username: admin, password: admin123");
        }
    }

    private void loginLoop() {
        while (true) {
            System.out.println("\n--- Login ---");
            System.out.println(" 1. Login as Librarian");
            System.out.println(" 2. Login as Member");
            System.out.println(" 0. Exit");
            System.out.print("\nChoose an option: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("0")) {
                System.out.println("Goodbye!");
                return;
            }

            Session session = null;
            try {
                if (choice.equals("1")) {
                    System.out.print("Username: ");
                    String username = scanner.nextLine().trim();
                    System.out.print("Password: ");
                    String password = scanner.nextLine().trim();
                    session = authService.loginLibrarian(username, password);
                } else if (choice.equals("2")) {
                    System.out.print("Member ID: ");
                    int memberId = Integer.parseInt(scanner.nextLine().trim());
                    session = authService.loginMember(memberId);
                } else {
                    System.out.println("Invalid option.");
                    continue;
                }
            } catch (Exception e) {
                System.out.println("Login failed: " + e.getMessage());
                continue;
            }

            System.out.printf("Welcome, %s!%n", session.getDisplayName());
            initCommands(session);
            mainLoop(session);
            commands.clear();
        }
    }

    private void initCommands(Session session) {
        if (session.getRole() == Role.LIBRARIAN) {
            commands.put("1", new AddBookCommand(bookService, scanner));
            commands.put("2", new SearchBookCommand(bookService, scanner));
            commands.put("3", new ListBooksCommand(bookService));
            commands.put("4", new RegisterMemberCommand(memberService, scanner));
            commands.put("5", new ListMembersCommand(memberService));
            commands.put("6", new IssueBookCommand(borrowService, scanner));
            commands.put("7", new ReturnBookCommand(borrowService, scanner));
            commands.put("8", new ReserveBookCommand(borrowService, scanner));
            commands.put("9", new MemberHistoryCommand(borrowService, scanner));
            commands.put("10", new ViewFinesCommand(fineService, scanner));
            commands.put("11", new PayFineCommand(fineService, scanner));
        } else {
            commands.put("1", new SearchBookCommand(bookService, scanner));
            commands.put("2", new ListBooksCommand(bookService));
            commands.put("3", new MemberHistoryCommand(borrowService, scanner, session));
            commands.put("4", new ViewFinesCommand(fineService, scanner, session));
            commands.put("5", new ReserveBookCommand(borrowService, scanner, session));
            commands.put("6", new PayFineCommand(fineService, scanner, session));
        }
    }

    private void mainLoop(Session session) {
        while (true) {
            printMenu(session);
            System.out.print("\nChoose an option: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("0")) {
                System.out.println("Logged out.");
                return;
            }

            Command command = commands.get(choice);
            if (command != null) {
                try {
                    command.execute();
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else {
                System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void printMenu(Session session) {
        System.out.printf("\n--- %s Menu [%s] ---%n",
                session.getRole() == Role.LIBRARIAN ? "Librarian" : "Member",
                session.getDisplayName());

        if (session.getRole() == Role.LIBRARIAN) {
            System.out.println(" 1. Add Book");
            System.out.println(" 2. Search Books");
            System.out.println(" 3. List All Books");
            System.out.println(" 4. Register Member");
            System.out.println(" 5. List All Members");
            System.out.println(" 6. Issue Book");
            System.out.println(" 7. Return Book");
            System.out.println(" 8. Reserve Book");
            System.out.println(" 9. Member History");
            System.out.println("10. View Fines");
            System.out.println("11. Pay Fine");
        } else {
            System.out.println(" 1. Search Books");
            System.out.println(" 2. List All Books");
            System.out.println(" 3. My Borrowing History");
            System.out.println(" 4. My Fines");
            System.out.println(" 5. Reserve Book");
            System.out.println(" 6. Pay Fine");
        }
        System.out.println(" 0. Logout");
    }
}
