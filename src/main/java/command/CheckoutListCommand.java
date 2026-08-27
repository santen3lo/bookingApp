package command;

import domain.Checkout;
import exceptions.NotOpenException;
import parsers.CliInputParser;
import services.CheckoutManager;
import validators.TimeValidator;

import java.util.List;

public class CheckoutListCommand implements Command {
    private final CheckoutManager checkoutManager;

    public CheckoutListCommand(CheckoutManager checkoutManager) {
        this.checkoutManager = checkoutManager;
    }

    @Override
    public void execute(String[] args) {
        try {
            boolean openOnly = false;
            if (args.length >= 2) {
                CliInputParser.validateOpenOnlyFlag(args[1]);
                openOnly = true;
            }

            List<Checkout> list = openOnly
                    ? checkoutManager.getOpenCheckouts()
                    : checkoutManager.getCheckouts();

            if (list.isEmpty()) {
                System.out.println("Список выдач пуст.");
                return;
            }

            System.out.println(String.format("%-6s %-15s %-12s %-18s %-18s %-10s",
                    "ID", "INSTRUMENT_ID", "USER", "TAKEN_AT", "RETURNED_AT", "CONDITION"));
            System.out.println("----------------------------------------------------------------------------------");
            for (Checkout c : list) {
                System.out.println(String.format("%-6d %-15d %-12s %-18s %-18s %-10s",
                        c.getId(),
                        c.getInstrumentId(),
                        c.getOwnerUsername() != null ? c.getOwnerUsername() : ("user#" + c.getUserId()),
                        TimeValidator.parseBack(c.getTakenAt()),
                        c.getReturnedAt() != null ? TimeValidator.parseBack(c.getReturnedAt()) : "-",
                        c.getReturnCondition() != null ? c.getReturnCondition().name() : "-"));
            }

        } catch (NotOpenException | IllegalArgumentException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка отображения списка выдач: " + e.getMessage());
        }
    }

    @Override
    public String description() {
        return "Список выдач: checkout_list [--open-only]";
    }
}
