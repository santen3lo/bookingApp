package command;

import auth.SessionContext;
import domain.User;
import services.UserManager;

import java.util.Optional;
import java.util.Scanner;

public class LoginCommand implements Command {
    private final Scanner sc;
    private final UserManager userManager;

    public LoginCommand(Scanner sc, UserManager userManager) {
        this.sc = sc;
        this.userManager = userManager;
    }

    @Override
    public void execute(String[] args) {
        System.out.print("Логин: ");
        String login = sc.nextLine().trim();
        if (login.isBlank()) {
            System.err.println("Логин не может быть пустым");
            return;
        }

        System.out.print("Пароль: ");
        String password = sc.nextLine().trim();
        if (password.isBlank()) {
            System.err.println("Пароль не может быть пустым");
            return;
        }

        Optional<User> found = userManager.login(login, password);
        if (found.isPresent()) {
            SessionContext.login(found.get());
        } else {
            System.err.println("Ошибка: неверный логин или пароль");
        }
    }

    @Override
    public String description() {
        return "Вход в систему: login";
    }
}
