package com.libratrack.repository.impl;

import com.libratrack.db.DatabaseConnection;
import com.libratrack.model.Reservation;
import com.libratrack.repository.ReservationRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PgReservationRepository implements ReservationRepository {

    @Override
    public Reservation save(Reservation reservation) {
        String sql = "INSERT INTO reservations (book_id, member_id, reserved_at, notified, fulfilled) VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reservation.getBookId());
            stmt.setInt(2, reservation.getMemberId());
            stmt.setTimestamp(3, Timestamp.valueOf(reservation.getReservedAt()));
            stmt.setBoolean(4, reservation.isNotified());
            stmt.setBoolean(5, reservation.isFulfilled());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                reservation.setId(rs.getInt("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving reservation: " + e.getMessage(), e);
        }
        return reservation;
    }

    @Override
    public List<Reservation> findPendingByBookId(int bookId) {
        String sql = "SELECT * FROM reservations WHERE book_id = ? AND fulfilled = false ORDER BY reserved_at ASC";
        List<Reservation> reservations = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                reservations.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reservations: " + e.getMessage(), e);
        }
        return reservations;
    }

    @Override
    public List<Reservation> findByMemberId(int memberId) {
        String sql = "SELECT * FROM reservations WHERE member_id = ? ORDER BY reserved_at DESC";
        List<Reservation> reservations = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                reservations.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reservations: " + e.getMessage(), e);
        }
        return reservations;
    }

    @Override
    public void update(Reservation reservation) {
        String sql = "UPDATE reservations SET notified=?, fulfilled=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, reservation.isNotified());
            stmt.setBoolean(2, reservation.isFulfilled());
            stmt.setInt(3, reservation.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating reservation: " + e.getMessage(), e);
        }
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        r.setBookId(rs.getInt("book_id"));
        r.setMemberId(rs.getInt("member_id"));
        r.setReservedAt(rs.getTimestamp("reserved_at").toLocalDateTime());
        r.setNotified(rs.getBoolean("notified"));
        r.setFulfilled(rs.getBoolean("fulfilled"));
        return r;
    }
}
