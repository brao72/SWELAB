package com.libratrack.command;

import com.libratrack.model.Book;
import com.libratrack.service.BookService;

import java.util.Scanner;

public class AddBookCommand implements Command {
    private final BookService bookService;
    private final Scanner scanner;

    public AddBookCommand(BookService bookService, Scanner scanner) {
        this.bookService = bookService;
        this.scanner = scanner;
    }

    @Override
    public void execute() {
        System.out.println("\n--- Add New Book ---");
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Author: ");
        String author = scanner.nextLine().trim();
        System.out.print("ISBN: ");
        String isbn = scanner.nextLine().trim();
        System.out.print("Genre: ");
        String genre = scanner.nextLine().trim();
        System.out.print("Number of copies: ");
        int copies = Integer.parseInt(scanner.nextLine().trim());

        Book book = bookService.addBook(title, author, isbn, genre, copies);
        System.out.printf("Book added successfully! ID: %d%n", book.getId());
    }
}
