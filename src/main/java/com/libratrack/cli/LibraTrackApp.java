package com.libratrack.cli;

import com.libratrack.command.*;
import com.libratrack.db.DatabaseConnection;
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
            initCommands();
            mainLoop();
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnection.getInstance().close();
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
                """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(schema);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize schema: " + e.getMessage(), e);
        }
    }

    private void initCommands() {
        BookRepository bookRepo = new PgBookRepository();
        MemberRepository memberRepo = new PgMemberRepository();
        BorrowRecordRepository borrowRepo = new PgBorrowRecordRepository();
        ReservationRepository reservationRepo = new PgReservationRepository();
        FineRepository fineRepo = new PgFineRepository();

        ReservationNotifier notifier = new ReservationNotifier(reservationRepo);

        BookService bookService = new BookService(bookRepo);
        MemberService memberService = new MemberService(memberRepo);
        BorrowService borrowService = new BorrowService(bookRepo, memberRepo, borrowRepo, reservationRepo, fineRepo, notifier);
        FineService fineService = new FineService(fineRepo);

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
    }

    private void mainLoop() {
        while (true) {
            printMenu();
            System.out.print("\nChoose an option: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("0")) {
                System.out.println("Goodbye!");
                break;
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

    private void printMenu() {
        System.out.println("\n--- Main Menu ---");
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
        System.out.println(" 0. Exit");
    }
}
