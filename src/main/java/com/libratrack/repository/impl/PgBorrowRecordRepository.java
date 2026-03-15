package com.libratrack.repository.impl;

import com.libratrack.db.DatabaseConnection;
import com.libratrack.model.BorrowRecord;
import com.libratrack.repository.BorrowRecordRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PgBorrowRecordRepository implements BorrowRecordRepository {

    @Override
    public BorrowRecord save(BorrowRecord record) {
        String sql = "INSERT INTO borrow_records (book_id, member_id, issue_date, due_date, is_returned) VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, record.getBookId());
            stmt.setInt(2, record.getMemberId());
            stmt.setDate(3, Date.valueOf(record.getIssueDate()));
            stmt.setDate(4, Date.valueOf(record.getDueDate()));
            stmt.setBoolean(5, record.isReturned());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                record.setId(rs.getInt("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving borrow record: " + e.getMessage(), e);
        }
        return record;
    }

    @Override
    public Optional<BorrowRecord> findActiveByBookAndMember(int bookId, int memberId) {
        String sql = "SELECT * FROM borrow_records WHERE book_id = ? AND member_id = ? AND is_returned = false";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, memberId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding borrow record: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<BorrowRecord> findActiveByMemberId(int memberId) {
        String sql = "SELECT * FROM borrow_records WHERE member_id = ? AND is_returned = false";
        return findByQuery(sql, memberId);
    }

    @Override
    public List<BorrowRecord> findByMemberId(int memberId) {
        String sql = "SELECT * FROM borrow_records WHERE member_id = ? ORDER BY issue_date DESC";
        return findByQuery(sql, memberId);
    }

    @Override
    public void update(BorrowRecord record) {
        String sql = "UPDATE borrow_records SET return_date=?, is_returned=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, record.getReturnDate() != null ? Date.valueOf(record.getReturnDate()) : null);
            stmt.setBoolean(2, record.isReturned());
            stmt.setInt(3, record.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating borrow record: " + e.getMessage(), e);
        }
    }

    private List<BorrowRecord> findByQuery(String sql, int memberId) {
        List<BorrowRecord> records = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                records.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding borrow records: " + e.getMessage(), e);
        }
        return records;
    }

    private BorrowRecord mapRow(ResultSet rs) throws SQLException {
        BorrowRecord record = new BorrowRecord();
        record.setId(rs.getInt("id"));
        record.setBookId(rs.getInt("book_id"));
        record.setMemberId(rs.getInt("member_id"));
        record.setIssueDate(rs.getDate("issue_date").toLocalDate());
        record.setDueDate(rs.getDate("due_date").toLocalDate());
        Date returnDate = rs.getDate("return_date");
        if (returnDate != null) {
            record.setReturnDate(returnDate.toLocalDate());
        }
        record.setReturned(rs.getBoolean("is_returned"));
        return record;
    }
}
