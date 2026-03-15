package com.libratrack.command;

import com.libratrack.model.Book;
import com.libratrack.service.BookService;

import java.util.List;

public class ListBooksCommand implements Command {
    private final BookService bookService;

    public ListBooksCommand(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public void execute() {
        System.out.println("\n--- All Books ---");
        List<Book> books = bookService.listAllBooks();
        if (books.isEmpty()) {
            System.out.println("No books in catalog.");
        } else {
            System.out.printf("%-5s %-30s %-20s %-15s %-10s %s%n", "ID", "Title", "Author", "ISBN", "Genre", "Available");
            System.out.println("-".repeat(95));
            books.forEach(System.out::println);
        }
    }
}
