package domain;

import java.time.Instant;

public final class User {
    public void setId(long id) {
        this.id = id;
    }

    private long id;
    private final String login;
    private final String password;
    private final Instant createdAt;

    public User(long id, String login, String password, Instant createdAt) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public String getLogin() { return login; }
    public String getPassword() { return password; }
    public Instant getCreatedAt() { return createdAt; }
}