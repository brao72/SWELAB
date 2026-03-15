package com.libratrack.model;

public class Session {
    private final Role role;
    private final int userId;
    private final String displayName;

    public Session(Role role, int userId, String displayName) {
        this.role = role;
        this.userId = userId;
        this.displayName = displayName;
    }

    public Role getRole() { return role; }
    public int getUserId() { return userId; }
    public String getDisplayName() { return displayName; }
}
