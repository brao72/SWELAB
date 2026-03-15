package com.libratrack.service;

import com.libratrack.model.Book;
import com.libratrack.repository.BookRepository;

import java.util.List;
import java.util.Optional;

public class BookService {
    private final BookRepository bookRepo;

    public BookService(BookRepository bookRepo) {
        this.bookRepo = bookRepo;
    }

    public Book addBook(String title, String author, String isbn, String genre, int copies) {
        Optional<Book> existing = bookRepo.findByIsbn(isbn);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("A book with ISBN " + isbn + " already exists.");
        }
        Book book = new Book(title, author, isbn, genre, copies);
        return bookRepo.save(book);
    }

    public List<Book> searchBooks(String keyword) {
        return bookRepo.search(keyword);
    }

    public List<Book> listAllBooks() {
        return bookRepo.findAll();
    }

    public Optional<Book> findByIsbn(String isbn) {
        return bookRepo.findByIsbn(isbn);
    }

    public void removeBook(int id) {
        bookRepo.delete(id);
    }
}
