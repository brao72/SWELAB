package com.libratrack.repository;

import com.libratrack.model.Reservation;
import java.util.List;

public interface ReservationRepository {
    Reservation save(Reservation reservation);
    List<Reservation> findPendingByBookId(int bookId);
    List<Reservation> findByMemberId(int memberId);
    void update(Reservation reservation);
}
