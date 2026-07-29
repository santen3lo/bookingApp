package command;

import auth.PasswordHasher;
import auth.SessionContext;
import domain.User;
import services.UserManager;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class LoginCommand implements Command{
    Scanner sc;
    UserManager userManager;

    public LoginCommand(Scanner sc, UserManager userManager) {
        this.sc = sc;
        this.userManager = userManager;
    }


    @Override
    public void execute(String[] args) {
        System.out.print("Логин: ");
        String login = sc.nextLine().trim();
        System.out.print("Пароль: ");
        String password = sc.nextLine().trim();

        User found = userManager.login(login, password).get();

        SessionContext.login(found);
    }

    @Override
    public String description() {
        return "";
    }
}
