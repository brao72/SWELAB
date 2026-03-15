package com.libratrack.service;

import com.libratrack.model.Fine;
import com.libratrack.repository.FineRepository;

import java.util.List;

public class FineService {
    private final FineRepository fineRepo;

    public FineService(FineRepository fineRepo) {
        this.fineRepo = fineRepo;
    }

    public List<Fine> getUnpaidFines(int memberId) {
        return fineRepo.findUnpaidByMemberId(memberId);
    }

    public double getTotalUnpaid(int memberId) {
        return fineRepo.getTotalUnpaidByMemberId(memberId);
    }

    public void payFine(int fineId, List<Fine> unpaidFines) {
        Fine fine = unpaidFines.stream()
                .filter(f -> f.getId() == fineId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Fine not found: " + fineId));
        fine.setPaid(true);
        fineRepo.update(fine);
    }
}
