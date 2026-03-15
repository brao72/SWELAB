package com.libratrack.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReservationTest {

    @Test
    void constructorSetsDefaults() {
        Reservation r = new Reservation(1, 2);
        assertEquals(1, r.getBookId());
        assertEquals(2, r.getMemberId());
        assertNotNull(r.getReservedAt());
        assertFalse(r.isNotified());
        assertFalse(r.isFulfilled());
    }

    @Test
    void canBeMarkedNotified() {
        Reservation r = new Reservation(1, 2);
        r.setNotified(true);
        assertTrue(r.isNotified());
    }

    @Test
    void canBeMarkedFulfilled() {
        Reservation r = new Reservation(1, 2);
        r.setFulfilled(true);
        assertTrue(r.isFulfilled());
    }
}
