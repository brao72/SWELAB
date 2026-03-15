package com.libratrack.repository;

import com.libratrack.model.Librarian;

import java.util.Optional;

public interface LibrarianRepository {
    Librarian save(Librarian librarian);
    Optional<Librarian> findByUsername(String username);
    long count();
}
