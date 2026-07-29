package command;

import services.UserManager;

import java.util.Scanner;

public class RegisterCommand implements Command {
    Scanner sc;
    UserManager userManager;

    public RegisterCommand(Scanner sc, UserManager u) {
        this.sc = sc;
        this.userManager = u;
    }

    @Override
    public void execute(String[] args) {
        System.out.print("Логин: ");
        String login = sc.nextLine().trim();
        if (login.isBlank()) { System.out.println("Ошибка: логин не может быть пустым"); return; }

        System.out.print("Пароль: ");
        String password = sc.nextLine().trim();
        if (password.isBlank()) { System.out.println("Ошибка: пароль не может быть пустым"); return; }


        userManager.register(login, password);
        System.out.println("Регистрация успешна. Теперь войдите через login");
    }

    @Override
    public String description() {
        return "";
    }
}
