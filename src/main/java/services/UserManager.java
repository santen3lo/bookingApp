package services;

import auth.PasswordHasher;
import auth.SessionContext;
import domain.User;
import storage.JdbcStorage;
import storage.DbErrorHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class UserManager {
    private final JdbcStorage jdbc = new JdbcStorage();
    private final List<User> users = new ArrayList<>(); // Кэш для быстрого поиска в UI

    /** Загружает пользователей из БД в локальный кэш (вызывается 1 раз при старте) */
    public void loadUsers() {
        try {
            users.clear();
            users.addAll(jdbc.loadAllUsers());
            System.out.println("✅ Пользователи загружены из БД: " + users.size());
        } catch (SQLException e) {
            System.err.println("⚠️ Ошибка загрузки пользователей: " + DbErrorHandler.translate(e));
        }
    }

    /** Регистрация: пишет в БД + обновляет кэш */
    public long register(String login, String password) {
        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Логин и пароль не могут быть пустыми");
        }
        if (users.stream().anyMatch(u -> u.getLogin().equalsIgnoreCase(login.trim()))) {
            throw new IllegalArgumentException("Логин '" + login + "' уже занят");
        }

        String hash = PasswordHasher.hash(password);
        User newUser = new User(0, login.trim(), hash, java.time.Instant.now()); // id=0, БД вернёт настоящий

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
        return users.stream()
                .filter(u -> u.getLogin().equalsIgnoreCase(login.trim()))
                .findFirst()
                .filter(u -> PasswordHasher.verify(password, u.getPassword()));
    }

    /** Получение логина по ID (для колонки Owner в таблицах) */
    public String getLoginById(long userId) {
        return users.stream()
                .filter(u -> u.getId() == userId)
                .findFirst()
                .map(User::getLogin)
                .orElse("user#" + userId);
    }

    /** Полная перезагрузка кэша (вызывается после refreshAction) */
    public void refresh() { loadUsers(); }

    // UserFileStorage больше НЕ нужен. Можно удалить файл.
}