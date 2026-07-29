package command;

import services.CheckoutManager;

import java.util.Scanner;

public class CheckoutReturnCommand implements Command{
    CheckoutManager checkoutManager;
    Scanner sc;

    public CheckoutReturnCommand (CheckoutManager checkoutManager, Scanner sc){
        this.checkoutManager = checkoutManager;
        this.sc = sc;
    }
    @Override
    public void execute(String[] args) {
        checkoutManager.checkoutReturn(args, sc);
    }

    @Override
    public String description() {
        return "";
    }
}
