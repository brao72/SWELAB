package com.libratrack.repository.impl;

import com.libratrack.db.DatabaseConnection;
import com.libratrack.model.Fine;
import com.libratrack.repository.FineRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class PgFineRepository implements FineRepository {

    @Override
    public Fine save(Fine fine) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(fine);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error saving fine: " + e.getMessage(), e);
        } finally {
            em.close();
        }
        return fine;
    }

    @Override
    public List<Fine> findUnpaidByMemberId(int memberId) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        try {
            return em.createQuery(
                    "SELECT f FROM Fine f WHERE f.memberId = :memberId AND f.isPaid = false ORDER BY f.createdAt", Fine.class)
                    .setParameter("memberId", memberId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void update(Fine fine) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(fine);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error updating fine: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    @Override
    public double getTotalUnpaidByMemberId(int memberId) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        try {
            Double total = em.createQuery(
                    "SELECT COALESCE(SUM(f.amount), 0.0) FROM Fine f WHERE f.memberId = :memberId AND f.isPaid = false", Double.class)
                    .setParameter("memberId", memberId)
                    .getSingleResult();
            return total != null ? total : 0.0;
        } finally {
            em.close();
        }
    }
}
