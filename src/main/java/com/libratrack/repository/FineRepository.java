package com.libratrack.repository;

import com.libratrack.model.Fine;
import java.util.List;

public interface FineRepository {
    Fine save(Fine fine);
    List<Fine> findUnpaidByMemberId(int memberId);
    void update(Fine fine);
    double getTotalUnpaidByMemberId(int memberId);
}
