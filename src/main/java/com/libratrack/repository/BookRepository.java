package com.libratrack.repository;

import com.libratrack.model.Book;
import java.util.List;
import java.util.Optional;

public interface BookRepository {
    Book save(Book book);
    Optional<Book> findById(int id);
    Optional<Book> findByIsbn(String isbn);
    List<Book> search(String keyword);
    List<Book> findAll();
    void update(Book book);
    void delete(int id);
}
