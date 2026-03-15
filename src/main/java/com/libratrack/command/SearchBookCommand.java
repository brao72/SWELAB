package com.libratrack.command;

import com.libratrack.model.Book;
import com.libratrack.service.BookService;

import java.util.List;
import java.util.Scanner;

public class SearchBookCommand implements Command {
    private final BookService bookService;
    private final Scanner scanner;

    public SearchBookCommand(BookService bookService, Scanner scanner) {
        this.bookService = bookService;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        System.out.println("\n--- Search Books ---");
        System.out.print("Enter search keyword (title/author/isbn/genre): ");
        String keyword = scanner.nextLine().trim();

        List<Book> results = bookService.searchBooks(keyword);
        if (results.isEmpty()) {
            System.out.println("No books found matching '" + keyword + "'.");
        } else {
            System.out.printf("%-5s %-30s %-20s %-15s %-10s %s%n", "ID", "Title", "Author", "ISBN", "Genre", "Available");
            System.out.println("-".repeat(95));
            results.forEach(System.out::println);
        }
    }
}
