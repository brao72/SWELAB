package com.libratrack.repository.impl;

import com.libratrack.db.DatabaseConnection;
import com.libratrack.model.Book;
import com.libratrack.repository.BookRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

public class PgBookRepository implements BookRepository {

    @Override
    public Book save(Book book) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(book);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error saving book: " + e.getMessage(), e);
        } finally {
            em.close();
        }
        return book;
    }

    @Override
    public Optional<Book> findById(int id) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        try {
            Book book = em.find(Book.class, id);
            return Optional.ofNullable(book);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        try {
            List<Book> results = em.createQuery("SELECT b FROM Book b WHERE b.isbn = :isbn", Book.class)
                    .setParameter("isbn", isbn)
                    .getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Book> search(String keyword) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        try {
            String pattern = "%" + keyword.toLowerCase() + "%";
            return em.createQuery(
                    "SELECT b FROM Book b WHERE LOWER(b.title) LIKE :kw OR LOWER(b.author) LIKE :kw OR LOWER(b.isbn) LIKE :kw OR LOWER(b.genre) LIKE :kw", Book.class)
                    .setParameter("kw", pattern)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Book> findAll() {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        try {
            return em.createQuery("SELECT b FROM Book b ORDER BY b.id", Book.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void update(Book book) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(book);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error updating book: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(int id) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Book book = em.find(Book.class, id);
            if (book != null) {
                em.remove(book);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error deleting book: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}
