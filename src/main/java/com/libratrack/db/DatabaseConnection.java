package com.libratrack.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private final HikariDataSource dataSource;

    private DatabaseConnection(String url, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        this.dataSource = new HikariDataSource(config);
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DatabaseConnection not initialized. Call initialize() first.");
        }
        return instance;
    }

    public static synchronized void initialize(String url, String user, String password) {
        if (instance == null) {
            instance = new DatabaseConnection(url, user, password);
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
