package com.libratrack.repository.impl;

import com.libratrack.db.DatabaseConnection;
import com.libratrack.model.Fine;
import com.libratrack.repository.FineRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PgFineRepository implements FineRepository {

    @Override
    public Fine save(Fine fine) {
        String sql = "INSERT INTO fines (borrow_record_id, member_id, amount, is_paid, created_at) VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, fine.getBorrowRecordId());
            stmt.setInt(2, fine.getMemberId());
            stmt.setDouble(3, fine.getAmount());
            stmt.setBoolean(4, fine.isPaid());
            stmt.setTimestamp(5, Timestamp.valueOf(fine.getCreatedAt()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                fine.setId(rs.getInt("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving fine: " + e.getMessage(), e);
        }
        return fine;
    }

    @Override
    public List<Fine> findUnpaidByMemberId(int memberId) {
        String sql = "SELECT * FROM fines WHERE member_id = ? AND is_paid = false ORDER BY created_at";
        List<Fine> fines = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                fines.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding fines: " + e.getMessage(), e);
        }
        return fines;
    }

    @Override
    public void update(Fine fine) {
        String sql = "UPDATE fines SET is_paid=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, fine.isPaid());
            stmt.setInt(2, fine.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating fine: " + e.getMessage(), e);
        }
    }

    @Override
    public double getTotalUnpaidByMemberId(int memberId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM fines WHERE member_id = ? AND is_paid = false";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error calculating fines: " + e.getMessage(), e);
        }
        return 0;
    }

    private Fine mapRow(ResultSet rs) throws SQLException {
        Fine fine = new Fine();
        fine.setId(rs.getInt("id"));
        fine.setBorrowRecordId(rs.getInt("borrow_record_id"));
        fine.setMemberId(rs.getInt("member_id"));
        fine.setAmount(rs.getDouble("amount"));
        fine.setPaid(rs.getBoolean("is_paid"));
        fine.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return fine;
    }
}
