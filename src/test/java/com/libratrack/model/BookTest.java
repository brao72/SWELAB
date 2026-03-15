package com.libratrack.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookTest {

    @Test
    void constructorSetsFieldsCorrectly() {
        Book book = new Book("Clean Code", "Robert Martin", "978-0132350884", "Software", 5);

        assertEquals("Clean Code", book.getTitle());
        assertEquals("Robert Martin", book.getAuthor());
        assertEquals("978-0132350884", book.getIsbn());
        assertEquals("Software", book.getGenre());
        assertEquals(5, book.getTotalCopies());
        assertEquals(5, book.getAvailableCopies());
    }

    @Test
    void availableCopiesEqualsTotalOnCreation() {
        Book book = new Book("Test", "Author", "123", "Genre", 3);
        assertEquals(book.getTotalCopies(), book.getAvailableCopies());
    }

    @Test
    void settersUpdateFields() {
        Book book = new Book();
        book.setId(1);
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setIsbn("ISBN");
        book.setGenre("Genre");
        book.setTotalCopies(10);
        book.setAvailableCopies(7);

        assertEquals(1, book.getId());
        assertEquals("Title", book.getTitle());
        assertEquals("Author", book.getAuthor());
        assertEquals("ISBN", book.getIsbn());
        assertEquals("Genre", book.getGenre());
        assertEquals(10, book.getTotalCopies());
        assertEquals(7, book.getAvailableCopies());
    }

    @Test
    void toStringContainsKeyFields() {
        Book book = new Book("Clean Code", "Robert Martin", "978-0132350884", "Software", 5);
        book.setId(1);
        String str = book.toString();
        assertTrue(str.contains("Clean Code"));
        assertTrue(str.contains("Robert Martin"));
        assertTrue(str.contains("5/5"));
    }
}
