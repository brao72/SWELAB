package com.libratrack.service;

import com.libratrack.model.Fine;
import com.libratrack.repository.FineRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FineServiceTest {

    @Mock
    private FineRepository fineRepo;

    private FineService fineService;

    @BeforeEach
    void setUp() {
        fineService = new FineService(fineRepo);
    }

    @Test
    void getUnpaidFinesReturnsResults() {
        Fine fine = new Fine(1, 1, 20.0);
        fine.setId(1);
        when(fineRepo.findUnpaidByMemberId(1)).thenReturn(List.of(fine));

        List<Fine> result = fineService.getUnpaidFines(1);
        assertEquals(1, result.size());
        assertEquals(20.0, result.get(0).getAmount());
    }

    @Test
    void getTotalUnpaidDelegatesToRepo() {
        when(fineRepo.getTotalUnpaidByMemberId(1)).thenReturn(45.0);
        assertEquals(45.0, fineService.getTotalUnpaid(1));
    }

    @Test
    void payFineMarksFineAsPaid() {
        Fine fine = new Fine(1, 1, 20.0);
        fine.setId(1);
        List<Fine> unpaid = List.of(fine);

        fineService.payFine(1, unpaid);

        assertTrue(fine.isPaid());
        verify(fineRepo).update(fine);
    }

    @Test
    void payFineThrowsForNonExistentFine() {
        Fine fine = new Fine(1, 1, 20.0);
        fine.setId(1);
        List<Fine> unpaid = List.of(fine);

        assertThrows(IllegalArgumentException.class,
                () -> fineService.payFine(99, unpaid));
    }
}
