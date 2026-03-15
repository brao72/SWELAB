package com.libratrack.observer;

import com.libratrack.model.Reservation;
import com.libratrack.repository.ReservationRepository;

import java.util.ArrayList;
import java.util.List;

public class ReservationNotifier {
    private final List<BookAvailabilityObserver> observers = new ArrayList<>();
    private final ReservationRepository reservationRepo;

    public ReservationNotifier(ReservationRepository reservationRepo) {
        this.reservationRepo = reservationRepo;
    }

    public void addObserver(BookAvailabilityObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(BookAvailabilityObserver observer) {
        observers.remove(observer);
    }

    public void notifyBookAvailable(int bookId, String bookTitle) {
        List<Reservation> pending = reservationRepo.findPendingByBookId(bookId);
        if (!pending.isEmpty()) {
            Reservation first = pending.get(0);
            first.setNotified(true);
            reservationRepo.update(first);

            for (BookAvailabilityObserver observer : observers) {
                observer.onBookAvailable(bookId, bookTitle);
            }

            System.out.printf("  [Notification] Member #%d notified: '%s' is now available!%n",
                    first.getMemberId(), bookTitle);
        }
    }
}
