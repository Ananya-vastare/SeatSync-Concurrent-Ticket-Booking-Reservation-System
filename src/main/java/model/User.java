package model;

public class User {
    private final String userId;
    private final String name;

    public User(String userId, String name) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be empty.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("User name cannot be empty.");
        }
        this.userId = userId.trim();
        this.name = name.trim();
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return userId + " - " + name;
    }
}
