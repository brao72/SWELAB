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
    void getUnpaidFines_delegatesToRepo() {
        Fine fine = new Fine(1, 10, 20.0);
        when(fineRepo.findUnpaidByMemberId(10)).thenReturn(List.of(fine));

        List<Fine> result = fineService.getUnpaidFines(10);

        assertEquals(1, result.size());
        assertEquals(20.0, result.get(0).getAmount());
    }

    @Test
    void getUnpaidFines_empty() {
        when(fineRepo.findUnpaidByMemberId(10)).thenReturn(List.of());

        assertTrue(fineService.getUnpaidFines(10).isEmpty());
    }

    @Test
    void getTotalUnpaid_delegatesToRepo() {
        when(fineRepo.getTotalUnpaidByMemberId(10)).thenReturn(45.0);

        assertEquals(45.0, fineService.getTotalUnpaid(10));
    }

    @Test
    void payFine_success() {
        Fine fine1 = new Fine(1, 10, 20.0);
        fine1.setId(1);
        Fine fine2 = new Fine(2, 10, 30.0);
        fine2.setId(2);
        List<Fine> unpaid = List.of(fine1, fine2);

        fineService.payFine(2, unpaid);

        assertTrue(fine2.isPaid());
        verify(fineRepo).update(fine2);
    }

    @Test
    void payFine_invalidId_throws() {
        Fine fine = new Fine(1, 10, 20.0);
        fine.setId(1);
        List<Fine> unpaid = List.of(fine);

        assertThrows(IllegalArgumentException.class,
                () -> fineService.payFine(99, unpaid));

        verify(fineRepo, never()).update(any());
    }

    @Test
    void payFine_marksOnlyTargetFine() {
        Fine fine1 = new Fine(1, 10, 20.0);
        fine1.setId(1);
        Fine fine2 = new Fine(2, 10, 30.0);
        fine2.setId(2);
        List<Fine> unpaid = List.of(fine1, fine2);

        fineService.payFine(1, unpaid);

        assertTrue(fine1.isPaid());
        assertFalse(fine2.isPaid());
    }
}
