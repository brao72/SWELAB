package com.libratrack.repository.impl;

import com.libratrack.db.DatabaseConnection;
import com.libratrack.model.Librarian;
import com.libratrack.repository.LibrarianRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

public class PgLibrarianRepository implements LibrarianRepository {

    @Override
    public Librarian save(Librarian librarian) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(librarian);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error saving librarian: " + e.getMessage(), e);
        } finally {
            em.close();
        }
        return librarian;
    }

    @Override
    public Optional<Librarian> findByUsername(String username) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        try {
            List<Librarian> results = em.createQuery(
                    "SELECT l FROM Librarian l WHERE l.username = :username", Librarian.class)
                    .setParameter("username", username)
                    .getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    @Override
    public long count() {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(l) FROM Librarian l", Long.class)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }
}
