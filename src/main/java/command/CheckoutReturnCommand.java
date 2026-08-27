package command;

import domain.Checkout;
import enums.ReturnCondition;
import exceptions.NotFoundException;
import exceptions.SecurityException;
import exceptions.UnderOneException;
import parsers.CliInputParser;
import services.CheckoutManager;

import java.util.Scanner;

public class CheckoutReturnCommand implements Command {
    private final CheckoutManager checkoutManager;
    private final Scanner sc;

    public CheckoutReturnCommand(CheckoutManager checkoutManager, Scanner sc) {
        this.checkoutManager = checkoutManager;
        this.sc = sc;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (args.length < 2) {
                System.err.println("Вы не ввели ID чекаута (использование: checkout_return <checkout_id>)");
                return;
            }
            long checkId = CliInputParser.parseId(args[1], "checkout");

            System.out.print("Состояние (OK | DAMAGED): ");
            String condStr = sc.nextLine().trim();
            ReturnCondition condition = CliInputParser.parseReturnCondition(condStr);

            Checkout c = checkoutManager.returnCheckout(checkId, condition);
            System.out.println(condition + " returned (checkout #" + c.getId() + ")");

        } catch (UnderOneException | SecurityException | NotFoundException | IllegalStateException | IllegalArgumentException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка возврата прибора: " + e.getMessage());
        }
    }

    @Override
    public String description() {
        return "Оформить возврат прибора: checkout_return <checkout_id>";
    }
}
