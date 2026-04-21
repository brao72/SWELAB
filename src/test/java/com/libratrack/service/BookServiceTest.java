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
    void addBook_success() {
        when(bookRepo.findByIsbn("978-1234")).thenReturn(Optional.empty());
        when(bookRepo.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book book = bookService.addBook("Clean Code", "Robert Martin", "978-1234", "Software", 3);

        assertEquals("Clean Code", book.getTitle());
        assertEquals(3, book.getAvailableCopies());
        verify(bookRepo).save(any(Book.class));
    }

    @Test
    void addBook_duplicateIsbn_throws() {
        when(bookRepo.findByIsbn("978-1234")).thenReturn(Optional.of(new Book()));

        assertThrows(IllegalArgumentException.class,
                () -> bookService.addBook("Title", "Author", "978-1234", "Genre", 1));

        verify(bookRepo, never()).save(any());
    }

    @Test
    void searchBooks_delegatesToRepo() {
        List<Book> expected = List.of(new Book("A", "B", "123", "G", 1));
        when(bookRepo.search("java")).thenReturn(expected);

        List<Book> result = bookService.searchBooks("java");

        assertEquals(1, result.size());
        verify(bookRepo).search("java");
    }

    @Test
    void listAllBooks_delegatesToRepo() {
        when(bookRepo.findAll()).thenReturn(List.of(new Book(), new Book()));

        List<Book> result = bookService.listAllBooks();

        assertEquals(2, result.size());
    }

    @Test
    void findByIsbn_found() {
        Book book = new Book("Test", "Auth", "111", "G", 1);
        when(bookRepo.findByIsbn("111")).thenReturn(Optional.of(book));

        Optional<Book> result = bookService.findByIsbn("111");

        assertTrue(result.isPresent());
        assertEquals("Test", result.get().getTitle());
    }

    @Test
    void findByIsbn_notFound() {
        when(bookRepo.findByIsbn("999")).thenReturn(Optional.empty());

        assertTrue(bookService.findByIsbn("999").isEmpty());
    }

    @Test
    void removeBook_delegatesToRepo() {
        bookService.removeBook(1);
        verify(bookRepo).delete(1);
    }
}
