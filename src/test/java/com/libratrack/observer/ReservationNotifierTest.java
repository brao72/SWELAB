package com.libratrack.observer;

import com.libratrack.model.Reservation;
import com.libratrack.repository.ReservationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationNotifierTest {

    @Mock
    private ReservationRepository reservationRepo;

    private ReservationNotifier notifier;

    @BeforeEach
    void setUp() {
        notifier = new ReservationNotifier(reservationRepo);
    }

    @Test
    void notifiesObserverWhenBookBecomesAvailable() {
        Reservation pending = new Reservation(1, 10);
        pending.setId(1);
        when(reservationRepo.findPendingByBookId(1)).thenReturn(List.of(pending));

        List<String> notifications = new ArrayList<>();
        notifier.addObserver((bookId, title) -> notifications.add(title));

        notifier.notifyBookAvailable(1, "Clean Code");

        assertEquals(1, notifications.size());
        assertEquals("Clean Code", notifications.get(0));
        verify(reservationRepo).update(pending);
        assertTrue(pending.isNotified());
    }

    @Test
    void doesNotNotifyWhenNoPendingReservations() {
        when(reservationRepo.findPendingByBookId(1)).thenReturn(List.of());

        List<String> notifications = new ArrayList<>();
        notifier.addObserver((bookId, title) -> notifications.add(title));

        notifier.notifyBookAvailable(1, "Clean Code");

        assertTrue(notifications.isEmpty());
    }

    @Test
    void removedObserverIsNotNotified() {
        Reservation pending = new Reservation(1, 10);
        pending.setId(1);
        when(reservationRepo.findPendingByBookId(1)).thenReturn(List.of(pending));

        List<String> notifications = new ArrayList<>();
        BookAvailabilityObserver observer = (bookId, title) -> notifications.add(title);
        notifier.addObserver(observer);
        notifier.removeObserver(observer);

        notifier.notifyBookAvailable(1, "Clean Code");

        assertTrue(notifications.isEmpty());
    }

    @Test
    void multipleObserversAllNotified() {
        Reservation pending = new Reservation(1, 10);
        pending.setId(1);
        when(reservationRepo.findPendingByBookId(1)).thenReturn(List.of(pending));

        List<Integer> notified = new ArrayList<>();
        notifier.addObserver((bookId, title) -> notified.add(1));
        notifier.addObserver((bookId, title) -> notified.add(2));

        notifier.notifyBookAvailable(1, "Clean Code");

        assertEquals(2, notified.size());
    }
}
