package command;

import services.CheckoutManager;

public class CheckoutListCommand implements Command{
    CheckoutManager checkoutManager;
    public CheckoutListCommand (CheckoutManager checkoutManager){
        this.checkoutManager = checkoutManager;
    }
    @Override
    public void execute(String[] args) {
        checkoutManager.checkoutList(args);
    }

    @Override
    public String description() {
        return "";
    }
}
