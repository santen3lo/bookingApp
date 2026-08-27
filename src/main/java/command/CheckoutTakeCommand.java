package command;

import domain.Checkout;
import exceptions.SecurityException;
import exceptions.UnderOneException;
import parsers.CliInputParser;
import services.CheckoutManager;

import java.util.Arrays;

public class CheckoutTakeCommand implements Command {
    private final CheckoutManager checkoutManager;

    public CheckoutTakeCommand(CheckoutManager checkoutManager) {
        this.checkoutManager = checkoutManager;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (args.length < 2) {
                System.err.println("Вы не ввели ID инструмента (использование: checkout_take <instrument_id> [comment])");
                return;
            }
            long instId = CliInputParser.parseId(args[1], "instrument");
            String comment = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "";

            Checkout c = checkoutManager.takeCheckout(instId, comment);
            System.out.println("OK checkout_id = " + c.getId() + ", прибор #" + instId + " выдан пользователю " + c.getOwnerUsername());

        } catch (IllegalStateException | UnderOneException | SecurityException | IllegalArgumentException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка оформления выдачи: " + e.getMessage());
        }
    }

    @Override
    public String description() {
        return "Оформить выдачу инструмента: checkout_take <instrument_id> [comment]";
    }
}
