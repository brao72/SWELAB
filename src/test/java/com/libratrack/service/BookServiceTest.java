package com.libratrack.service;

import com.libratrack.model.Book;
import com.libratrack.repository.BookRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepo;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepo);
    }

    @Test
    void addBookSuccessfully() {
        when(bookRepo.findByIsbn("978-0132350884")).thenReturn(Optional.empty());
        when(bookRepo.save(any(Book.class))).thenAnswer(inv -> {
            Book b = inv.getArgument(0);
            b.setId(1);
            return b;
        });

        Book result = bookService.addBook("Clean Code", "Robert Martin", "978-0132350884", "Software", 3);

        assertEquals(1, result.getId());
        assertEquals("Clean Code", result.getTitle());
        verify(bookRepo).save(any(Book.class));
    }

    @Test
    void addBookThrowsForDuplicateIsbn() {
        Book existing = new Book("Old", "Author", "978-0132350884", "Genre", 1);
        when(bookRepo.findByIsbn("978-0132350884")).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class,
                () -> bookService.addBook("New", "Author", "978-0132350884", "Genre", 1));
        verify(bookRepo, never()).save(any());
    }

    @Test
    void searchBooksReturnsResults() {
        Book book = new Book("Clean Code", "Robert Martin", "978-0132350884", "Software", 3);
        when(bookRepo.search("clean")).thenReturn(List.of(book));

        List<Book> results = bookService.searchBooks("clean");

        assertEquals(1, results.size());
        assertEquals("Clean Code", results.get(0).getTitle());
    }

    @Test
    void listAllBooksReturnsAll() {
        when(bookRepo.findAll()).thenReturn(List.of(
                new Book("A", "X", "1", "G", 1),
                new Book("B", "Y", "2", "G", 2)
        ));

        assertEquals(2, bookService.listAllBooks().size());
    }

    @Test
    void findByIsbnDelegatesToRepo() {
        Book book = new Book("Clean Code", "Robert Martin", "978-0132350884", "Software", 3);
        when(bookRepo.findByIsbn("978-0132350884")).thenReturn(Optional.of(book));

        assertTrue(bookService.findByIsbn("978-0132350884").isPresent());
    }

    @Test
    void removeBookDelegatesToRepo() {
        bookService.removeBook(1);
        verify(bookRepo).delete(1);
    }
}
