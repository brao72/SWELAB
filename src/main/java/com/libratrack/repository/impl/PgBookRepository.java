package com.libratrack.repository.impl;

import com.libratrack.db.DatabaseConnection;
import com.libratrack.model.Book;
import com.libratrack.repository.BookRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PgBookRepository implements BookRepository {

    @Override
    public Book save(Book book) {
        String sql = "INSERT INTO books (title, author, isbn, genre, total_copies, available_copies) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getIsbn());
            stmt.setString(4, book.getGenre());
            stmt.setInt(5, book.getTotalCopies());
            stmt.setInt(6, book.getAvailableCopies());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                book.setId(rs.getInt("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving book: " + e.getMessage(), e);
        }
        return book;
    }

    @Override
    public Optional<Book> findById(int id) {
        String sql = "SELECT * FROM books WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding book: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        String sql = "SELECT * FROM books WHERE isbn = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding book: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Book> search(String keyword) {
        String sql = "SELECT * FROM books WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ? OR LOWER(isbn) LIKE ? OR LOWER(genre) LIKE ?";
        List<Book> books = new ArrayList<>();
        String pattern = "%" + keyword.toLowerCase() + "%";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            stmt.setString(4, pattern);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                books.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching books: " + e.getMessage(), e);
        }
        return books;
    }

    @Override
    public List<Book> findAll() {
        String sql = "SELECT * FROM books ORDER BY id";
        List<Book> books = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                books.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing books: " + e.getMessage(), e);
        }
        return books;
    }

    @Override
    public void update(Book book) {
        String sql = "UPDATE books SET title=?, author=?, isbn=?, genre=?, total_copies=?, available_copies=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getIsbn());
            stmt.setString(4, book.getGenre());
            stmt.setInt(5, book.getTotalCopies());
            stmt.setInt(6, book.getAvailableCopies());
            stmt.setInt(7, book.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating book: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting book: " + e.getMessage(), e);
        }
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setIsbn(rs.getString("isbn"));
        book.setGenre(rs.getString("genre"));
        book.setTotalCopies(rs.getInt("total_copies"));
        book.setAvailableCopies(rs.getInt("available_copies"));
        return book;
    }
}
