package com.libratrack.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BorrowRecordTest {

    @Test
    void constructorSetsFieldsCorrectly() {
        LocalDate issue = LocalDate.of(2026, 3, 1);
        LocalDate due = LocalDate.of(2026, 3, 15);
        BorrowRecord record = new BorrowRecord(1, 2, issue, due);

        assertEquals(1, record.getBookId());
        assertEquals(2, record.getMemberId());
        assertEquals(issue, record.getIssueDate());
        assertEquals(due, record.getDueDate());
        assertFalse(record.isReturned());
        assertNull(record.getReturnDate());
    }

    @Test
    void markAsReturned() {
        BorrowRecord record = new BorrowRecord(1, 2, LocalDate.now(), LocalDate.now().plusDays(14));
        record.setReturned(true);
        record.setReturnDate(LocalDate.now());

        assertTrue(record.isReturned());
        assertNotNull(record.getReturnDate());
    }
}
