package com.libratrack.repository.impl;

import com.libratrack.db.DatabaseConnection;
import com.libratrack.factory.MemberFactory;
import com.libratrack.model.Member;
import com.libratrack.model.MemberType;
import com.libratrack.repository.MemberRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PgMemberRepository implements MemberRepository {

    @Override
    public Member save(Member member) {
        String sql = "INSERT INTO members (name, email, phone, member_type, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, member.getName());
            stmt.setString(2, member.getEmail());
            stmt.setString(3, member.getPhone());
            stmt.setString(4, member.getMemberType().name());
            stmt.setBoolean(5, member.isActive());
            stmt.setTimestamp(6, Timestamp.valueOf(member.getCreatedAt()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                member.setId(rs.getInt("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving member: " + e.getMessage(), e);
        }
        return member;
    }

    @Override
    public Optional<Member> findById(int id) {
        String sql = "SELECT * FROM members WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding member: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Member> findAll() {
        String sql = "SELECT * FROM members ORDER BY id";
        List<Member> members = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                members.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing members: " + e.getMessage(), e);
        }
        return members;
    }

    @Override
    public void update(Member member) {
        String sql = "UPDATE members SET name=?, email=?, phone=?, member_type=?, is_active=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, member.getName());
            stmt.setString(2, member.getEmail());
            stmt.setString(3, member.getPhone());
            stmt.setString(4, member.getMemberType().name());
            stmt.setBoolean(5, member.isActive());
            stmt.setInt(6, member.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating member: " + e.getMessage(), e);
        }
    }

    private Member mapRow(ResultSet rs) throws SQLException {
        MemberType type = MemberType.valueOf(rs.getString("member_type"));
        Member member = MemberFactory.createMember(type,
                rs.getString("name"), rs.getString("email"), rs.getString("phone"));
        member.setId(rs.getInt("id"));
        member.setActive(rs.getBoolean("is_active"));
        member.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return member;
    }
}
