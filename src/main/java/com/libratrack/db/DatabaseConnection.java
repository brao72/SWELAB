package com.libratrack.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private final EntityManagerFactory emf;

    private DatabaseConnection(String url, String user, String password) {
        Map<String, String> props = new HashMap<>();
        props.put("jakarta.persistence.jdbc.url", url);
        props.put("jakarta.persistence.jdbc.user", user);
        props.put("jakarta.persistence.jdbc.password", password);
        this.emf = Persistence.createEntityManagerFactory("libratrack", props);
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

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
