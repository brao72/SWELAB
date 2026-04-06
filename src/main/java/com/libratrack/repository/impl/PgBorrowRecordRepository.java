package com.libratrack.repository.impl;

import com.libratrack.db.DatabaseConnection;
import com.libratrack.model.BorrowRecord;
import com.libratrack.repository.BorrowRecordRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

public class PgBorrowRecordRepository implements BorrowRecordRepository {

    @Override
    public BorrowRecord save(BorrowRecord record) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(record);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error saving borrow record: " + e.getMessage(), e);
        } finally {
            em.close();
        }
        return record;
    }

    @Override
    public Optional<BorrowRecord> findActiveByBookAndMember(int bookId, int memberId) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        try {
            List<BorrowRecord> results = em.createQuery(
                    "SELECT br FROM BorrowRecord br WHERE br.bookId = :bookId AND br.memberId = :memberId AND br.isReturned = false", BorrowRecord.class)
                    .setParameter("bookId", bookId)
                    .setParameter("memberId", memberId)
                    .getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    @Override
    public List<BorrowRecord> findActiveByMemberId(int memberId) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        try {
            return em.createQuery(
                    "SELECT br FROM BorrowRecord br WHERE br.memberId = :memberId AND br.isReturned = false", BorrowRecord.class)
                    .setParameter("memberId", memberId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<BorrowRecord> findByMemberId(int memberId) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        try {
            return em.createQuery(
                    "SELECT br FROM BorrowRecord br WHERE br.memberId = :memberId ORDER BY br.issueDate DESC", BorrowRecord.class)
                    .setParameter("memberId", memberId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void update(BorrowRecord record) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(record);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error updating borrow record: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}
