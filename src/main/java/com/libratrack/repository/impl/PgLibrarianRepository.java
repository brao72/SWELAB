package com.libratrack.repository.impl;

import com.libratrack.db.DatabaseConnection;
import com.libratrack.model.Librarian;
import com.libratrack.repository.LibrarianRepository;

import java.sql.*;
import java.util.Optional;

public class PgLibrarianRepository implements LibrarianRepository {

    @Override
    public Librarian save(Librarian librarian) {
        String sql = "INSERT INTO librarians (username, password_hash, name, created_at) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, librarian.getUsername());
            stmt.setString(2, librarian.getPasswordHash());
            stmt.setString(3, librarian.getName());
            stmt.setTimestamp(4, Timestamp.valueOf(librarian.getCreatedAt()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                librarian.setId(rs.getInt("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving librarian: " + e.getMessage(), e);
        }
        return librarian;
    }

    @Override
    public Optional<Librarian> findByUsername(String username) {
        String sql = "SELECT * FROM librarians WHERE username = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding librarian: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM librarians";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting librarians: " + e.getMessage(), e);
        }
        return 0;
    }

    private Librarian mapRow(ResultSet rs) throws SQLException {
        Librarian lib = new Librarian();
        lib.setId(rs.getInt("id"));
        lib.setUsername(rs.getString("username"));
        lib.setPasswordHash(rs.getString("password_hash"));
        lib.setName(rs.getString("name"));
        lib.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return lib;
    }
}
