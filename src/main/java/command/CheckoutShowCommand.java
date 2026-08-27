package command;

import domain.Checkout;
import exceptions.NotFoundException;
import exceptions.UnderOneException;
import parsers.CliInputParser;
import services.CheckoutManager;
import validators.TimeValidator;

public class CheckoutShowCommand implements Command {
    private final CheckoutManager checkoutManager;

    public CheckoutShowCommand(CheckoutManager checkoutManager) {
        this.checkoutManager = checkoutManager;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (args.length < 2) {
                System.err.println("Вы не ввели ID чекаута (использование: checkout_show <checkout_id>)");
                return;
            }
            long checkoutId = CliInputParser.parseId(args[1], "checkout");
            Checkout c = checkoutManager.getCheckoutById(checkoutId);

            System.out.println("#" + c.getId());
            System.out.println("instrument_id: " + c.getInstrumentId());
            System.out.println("user: " + (c.getOwnerUsername() != null ? c.getOwnerUsername() : c.getUserId()));
            System.out.println("takenAt: " + TimeValidator.parseBack(c.getTakenAt()));
            System.out.println("returnedAt: " + (c.getReturnedAt() != null ? TimeValidator.parseBack(c.getReturnedAt()) : "-"));
            System.out.println("condition: " + (c.getReturnCondition() != null ? c.getReturnCondition().name() : "-"));
            if (c.getComment() != null && !c.getComment().isBlank()) {
                System.out.println("comment: " + c.getComment());
            }

        } catch (UnderOneException | NotFoundException | IllegalArgumentException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка отображения чекаута: " + e.getMessage());
        }
    }

    @Override
    public String description() {
        return "Показать детали выдачи: checkout_show <checkout_id>";
    }
}
