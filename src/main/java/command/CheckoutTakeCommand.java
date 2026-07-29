package command;

import services.CheckoutManager;

public class CheckoutTakeCommand implements Command{
    CheckoutManager checkoutManager;
    public CheckoutTakeCommand (CheckoutManager checkoutManager){
        this.checkoutManager = checkoutManager;
    }

    @Override
    public void execute(String[] args) {
        checkoutManager.checkoutTake(args);
    }

    @Override
    public String description() {
        return "";
    }
}
