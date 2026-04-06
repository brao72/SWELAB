package com.libratrack.repository.impl;

import com.libratrack.db.DatabaseConnection;
import com.libratrack.model.Reservation;
import com.libratrack.repository.ReservationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class PgReservationRepository implements ReservationRepository {

    @Override
    public Reservation save(Reservation reservation) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(reservation);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error saving reservation: " + e.getMessage(), e);
        } finally {
            em.close();
        }
        return reservation;
    }

    @Override
    public List<Reservation> findPendingByBookId(int bookId) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        try {
            return em.createQuery(
                    "SELECT r FROM Reservation r WHERE r.bookId = :bookId AND r.fulfilled = false ORDER BY r.reservedAt ASC", Reservation.class)
                    .setParameter("bookId", bookId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Reservation> findByMemberId(int memberId) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        try {
            return em.createQuery(
                    "SELECT r FROM Reservation r WHERE r.memberId = :memberId ORDER BY r.reservedAt DESC", Reservation.class)
                    .setParameter("memberId", memberId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void update(Reservation reservation) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(reservation);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error updating reservation: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}
