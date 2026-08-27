package services;

import auth.PasswordHasher;
import domain.User;
import storage.DbErrorHandler;
import storage.JdbcStorage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class UserManager {
    private final JdbcStorage jdbc;
    private final List<User> users = new ArrayList<>(); // Кэш для быстрого поиска

    public UserManager() {
        this(new JdbcStorage());
    }

    public UserManager(JdbcStorage jdbc) {
        this.jdbc = jdbc;
    }

    /** Загружает пользователей из БД в локальный кэш */
    public void loadUsers() {
        try {
            users.clear();
            users.addAll(jdbc.loadAllUsers());
            System.out.println("Пользователи загружены из БД: " + users.size());
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки пользователей: " + DbErrorHandler.translate(e));
        }
    }

    /** Регистрация: запись в БД + добавление в кэш */
    public long register(String login, String password) {
        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Логин и пароль не могут быть пустыми");
        }
        String cleanLogin = login.trim();
        if (users.stream().anyMatch(u -> u.getLogin().equalsIgnoreCase(cleanLogin))) {
            throw new IllegalArgumentException("Логин '" + cleanLogin + "' уже занят");
        }

        String hash = PasswordHasher.hash(password);
        User newUser = new User(0, cleanLogin, hash, java.time.Instant.now());

        try {
            long generatedId = jdbc.insertUser(newUser);
            newUser.setId(generatedId);
            users.add(newUser);
            return generatedId;
        } catch (SQLException e) {
            throw new RuntimeException(DbErrorHandler.translate(e), e);
        }
    }

    public Optional<User> login(String login, String password) {
        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            return Optional.empty();
        }
        String cleanLogin = login.trim();
        return users.stream()
                .filter(u -> u.getLogin().equalsIgnoreCase(cleanLogin))
                .findFirst()
                .filter(u -> PasswordHasher.verify(password, u.getPassword()));
    }

    /** Получение логина по ID (для отображения владельца) */
    public String getLoginById(long userId) {
        return users.stream()
                .filter(u -> u.getId() == userId)
                .findFirst()
                .map(User::getLogin)
                .orElse("user#" + userId);
    }

    public void refresh() {
        loadUsers();
    }
}