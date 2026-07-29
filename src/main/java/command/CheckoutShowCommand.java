package command;

import services.CheckoutManager;

public class CheckoutShowCommand implements Command{
    CheckoutManager checkoutManager;

    public CheckoutShowCommand (CheckoutManager checkoutManager){
        this.checkoutManager = checkoutManager;
    }
    @Override
    public void execute(String[] args) {
        checkoutManager.checkoutShow(args);
    }

    @Override
    public String description() {
        return "";
    }
}
