package command;

import services.UserManager;

import java.util.Scanner;

public class RegisterCommand implements Command {
    private final Scanner sc;
    private final UserManager userManager;

    public RegisterCommand(Scanner sc, UserManager userManager) {
        this.sc = sc;
        this.userManager = userManager;
    }

    @Override
    public void execute(String[] args) {
        System.out.print("Логин: ");
        String login = sc.nextLine().trim();
        if (login.isBlank()) {
            System.err.println("Ошибка: логин не может быть пустым");
            return;
        }

        System.out.print("Пароль: ");
        String password = sc.nextLine().trim();
        if (password.isBlank()) {
            System.err.println("Ошибка: пароль не может быть пустым");
            return;
        }

        try {
            userManager.register(login, password);
            System.out.println("Регистрация успешна. Теперь войдите через login");
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка регистрации: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Не удалось зарегистрировать пользователя: " + e.getMessage());
        }
    }

    @Override
    public String description() {
        return "Регистрация нового пользователя: register";
    }
}
