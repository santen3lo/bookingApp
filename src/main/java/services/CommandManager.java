package services;
import command.*;
import javafx.scene.control.Alert;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandManager {
    Scanner sc;
    String[] args;
    Map<String, Command> commandsMap = new HashMap<>();
    public  CommandManager(Scanner scanner, String[] args){
        BookingManager bookingManager = new BookingManager();
        CheckoutManager checkoutManager = new CheckoutManager();
        InstrumentManager instrumentManager = new InstrumentManager();
        UserManager userManager = new UserManager();
        try {
            userManager.loadUsers();
            bookingManager.loadFromDb();
            checkoutManager.loadFromDb();
            instrumentManager.loadFromDb();
            System.out.println("Данные успешно загружены из хранилищ.");
        } catch (Exception e) {
            System.err.println("Не удалось загрузить");
        }

        this.args = args;

        sc = scanner;
        commandsMap.put("help", new HelpCommand());
        commandsMap.put("book_create", new BookCreateCommand(bookingManager, sc));
        commandsMap.put("book_list", new BookLlistCommand(bookingManager));
        commandsMap.put("book_reschedule", new BookRescheduleCommand(bookingManager));
        commandsMap.put("book_cancel", new BookCancelCommand(bookingManager));
        commandsMap.put("book_show", new BookShowCommand(bookingManager));
        commandsMap.put("checkout_list", new CheckoutListCommand(checkoutManager));
        commandsMap.put("checkout_return", new CheckoutReturnCommand(checkoutManager, sc));
        commandsMap.put("checkout_show", new CheckoutShowCommand(checkoutManager));
        commandsMap.put("inst_available", new InstAvailableCommand(bookingManager, checkoutManager, instrumentManager));
        commandsMap.put("checkout_take", new CheckoutTakeCommand(checkoutManager));
        commandsMap.put("exit", new ExitCommand());
        commandsMap.put("login", new LoginCommand(sc, userManager));
        commandsMap.put("register", new RegisterCommand(sc, userManager));
    }

    public void manage() {
        boolean stop = true;
        while (stop) {
            String[] in = sc.nextLine().split(" ");
            String command = in[0];

            if (commandsMap.get(command) instanceof ExitCommand) {
                commandsMap.get(command).execute(in);
                stop = false;
            } else if (commandsMap.containsKey(command)) {
                commandsMap.get(command).execute(in);
                System.out.println("------------------");
            } else {
                System.out.println("Неизвестная команда " + command +"\n");
            }
        }

    }

}
