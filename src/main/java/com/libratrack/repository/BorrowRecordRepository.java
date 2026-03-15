package com.libratrack.repository;

import com.libratrack.model.BorrowRecord;
import java.util.List;
import java.util.Optional;

public interface BorrowRecordRepository {
    BorrowRecord save(BorrowRecord record);
    Optional<BorrowRecord> findActiveByBookAndMember(int bookId, int memberId);
    List<BorrowRecord> findActiveByMemberId(int memberId);
    List<BorrowRecord> findByMemberId(int memberId);
    void update(BorrowRecord record);
}
