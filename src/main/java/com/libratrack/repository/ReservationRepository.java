package com.libratrack.repository;

import com.libratrack.model.Reservation;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    Reservation save(Reservation reservation);
    Optional<Reservation> findById(int id);
    List<Reservation> findPendingByBookId(int bookId);
    List<Reservation> findByMemberId(int memberId);
    void update(Reservation reservation);
}
