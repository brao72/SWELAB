package com.libratrack.api;

import com.libratrack.model.Book;
import com.libratrack.service.BookService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;

public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    public void registerRoutes(Javalin app) {
        app.get("/api/books", this::listBooks);
        app.get("/api/books/search", this::searchBooks);
        app.get("/api/books/{isbn}", this::getByIsbn);
        app.post("/api/books", this::addBook);
        app.delete("/api/books/{id}", this::removeBook);
    }

    private void listBooks(Context ctx) {
        ctx.json(bookService.listAllBooks());
    }

    private void searchBooks(Context ctx) {
        String query = ctx.queryParam("q");
        if (query == null || query.isBlank()) {
            ctx.json(bookService.listAllBooks());
        } else {
            ctx.json(bookService.searchBooks(query));
        }
    }

    private void getByIsbn(Context ctx) {
        String isbn = ctx.pathParam("isbn");
        Book book = bookService.findByIsbn(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ISBN: " + isbn));
        ctx.json(book);
    }

    private void addBook(Context ctx) {
        AddBookRequest req = ctx.bodyAsClass(AddBookRequest.class);
        Book book = bookService.addBook(req.title(), req.author(), req.isbn(), req.genre(), req.copies());
        ctx.status(201).json(book);
    }

    private void removeBook(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        bookService.removeBook(id);
        ctx.status(204);
    }

    public record AddBookRequest(String title, String author, String isbn, String genre, int copies) {}
}
