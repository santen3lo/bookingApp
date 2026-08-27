package services;

import command.*;
import storage.JdbcStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandManager {
    private final Scanner sc;
    private final String[] args;
    private final Map<String, Command> commandsMap = new HashMap<>();

    public CommandManager(Scanner scanner, String[] args) {
        this.sc = scanner;
        this.args = args;

        JdbcStorage jdbc = new JdbcStorage();
        UserManager userManager = new UserManager(jdbc);
        BookingManager bookingManager = new BookingManager(jdbc);
        CheckoutManager checkoutManager = new CheckoutManager(jdbc);
        InstrumentManager instrumentManager = new InstrumentManager(jdbc);

        try {
            userManager.loadUsers();
            bookingManager.loadFromDb();
            checkoutManager.loadFromDb();
            instrumentManager.loadFromDb();
            System.out.println("Данные успешно загружены из хранилищ.");
        } catch (Exception e) {
            System.err.println("Не удалось загрузить данные из БД: " + e.getMessage());
        }

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
        System.out.println("Консольный интерфейс запущен. Введите 'help' для списка команд.");
        boolean running = true;
        while (running && sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] in = line.split("\\s+");
            String commandName = in[0].toLowerCase();

            Command command = commandsMap.get(commandName);
            if (command instanceof ExitCommand) {
                command.execute(in);
                running = false;
            } else if (command != null) {
                command.execute(in);
                System.out.println("------------------");
            } else {
                System.out.println("Неизвестная команда: '" + commandName + "'. Введите 'help' для списка команд.\n");
            }
        }
    }
}
