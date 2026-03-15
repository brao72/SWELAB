package com.libratrack.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FineTest {

    @Test
    void constructorSetsDefaults() {
        Fine fine = new Fine(1, 2, 20.0);
        assertEquals(1, fine.getBorrowRecordId());
        assertEquals(2, fine.getMemberId());
        assertEquals(20.0, fine.getAmount());
        assertFalse(fine.isPaid());
        assertNotNull(fine.getCreatedAt());
    }

    @Test
    void canBeMarkedPaid() {
        Fine fine = new Fine(1, 2, 20.0);
        fine.setPaid(true);
        assertTrue(fine.isPaid());
    }

    @Test
    void toStringContainsAmount() {
        Fine fine = new Fine(1, 2, 20.0);
        fine.setId(1);
        String str = fine.toString();
        assertTrue(str.contains("20.00"));
        assertTrue(str.contains("UNPAID"));
    }
}
