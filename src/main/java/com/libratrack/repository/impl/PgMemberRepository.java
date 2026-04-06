package com.libratrack.repository.impl;

import com.libratrack.db.DatabaseConnection;
import com.libratrack.model.Member;
import com.libratrack.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

public class PgMemberRepository implements MemberRepository {

    @Override
    public Member save(Member member) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(member);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error saving member: " + e.getMessage(), e);
        } finally {
            em.close();
        }
        return member;
    }

    @Override
    public Optional<Member> findById(int id) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        try {
            Member member = em.find(Member.class, id);
            return Optional.ofNullable(member);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Member> findAll() {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        try {
            return em.createQuery("SELECT m FROM Member m ORDER BY m.id", Member.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void update(Member member) {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(member);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error updating member: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}
