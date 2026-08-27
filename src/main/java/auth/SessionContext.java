package auth;

import domain.User;
import exceptions.SecurityException;

public final class SessionContext {
    private static User currentUser;

    private SessionContext() {}

    public static void login(User user) {
        currentUser = user;
        System.out.println("Вход выполнен: " + user.getLogin());
    }

    public static void logout() {
        currentUser = null;
        System.out.println("Вы вышли из системы");
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isAuthenticated() {
        return currentUser != null;
    }

    public static long getCurrentUserId() {
        if (!isAuthenticated()) {
            throw new SecurityException("Пользователь не авторизован");
        }
        return currentUser.getId();
    }
}