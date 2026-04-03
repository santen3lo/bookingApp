package Managers;

import Enums.ReturnCondition;
import Exceptions.NotOpenException;
import Exceptions.UnderOneException;
import Validators.FlagValidator;
import Validators.IdValidator;
import domain.Checkout;

import java.util.ArrayList;

public class CheckoutManager {
    private ArrayList<Checkout> checkouts;
    private InstrumentManager instrumentManager;
    private IdGen id;

    public ArrayList<Checkout> getCheckouts(){
        return checkouts;
    }

    public void checkoutReturn(String[] com) {
        //long k = 0;
        try {
            long instId = Integer.parseInt(com[1]);
            IdValidator.checkId((instId));
            for (Checkout checkout : checkouts) {
                if (instId == checkout.getId()) {
                    checkout.setReturnCondition(ReturnCondition.OK);
                    System.out.println("Кто берет:" + checkout.getOwnerUsername());
                    System.out.println("Комметнтарий");
                    System.out.println(checkout.getReturnCondition());
                }
            }
        } catch (UnderOneException e) {
            System.out.println(e.getMessage());
        } catch (NumberFormatException exception) {
            System.out.println("Вы ввели недействительный instrument id");
        }
    }
    public void checkoutList(String[] com) {
        try {
            String flag = com[1];
            FlagValidator.checkOpenFlag(flag);
            System.out.println("ID  domain.Instrument   User    TakenAt");
            for (Checkout checkout: checkouts){
                System.out.println(checkout.getId() + "  " + checkout.getInstrumentId() + "  " + checkout.getUsername() + "        " + checkout.getTakenAt());
            }
        } catch (NotOpenException e) {
            System.out.println(e.getMessage());
        }
    }
    public void checkoutShow(String[] com) {
        try {
            long checkoutId = Integer.parseInt(com[1]);
            IdValidator.checkId(checkoutId);

            for (Checkout checkout : checkouts) {
                if (checkout.getId() == checkoutId) {
                    System.out.println("#" + id);
                    System.out.println("instrument_id:" + checkout.getInstrumentId());
                    System.out.println("user:" + checkout.getUsername());
                    System.out.println("takenAt:" + checkout.getTakenAt());
                    System.out.println("returnedAt:" + checkout.getReturnedAt());
                }
            }
        } catch (UnderOneException e){
            System.out.println(e.getMessage());
        } catch (NumberFormatException e){
            System.out.println("Вы ввели недействительный instrument id");
        }
    }
    public void checkoutTake(String[] com) {
        try {
            long instrId;
            instrId = Integer.parseInt(com[1]);
            IdValidator.checkId(instrId);
            for (Checkout checkout : checkouts) {
                if (instrId == checkout.getInstrumentId()) {
                    checkout.setReturnCondition(ReturnCondition.OK);
                    System.out.println("Кто берет:" + checkout.getOwnerUsername());
                    System.out.println("Комметнтарий");
                    System.out.println(checkout.getReturnCondition());
                }
            }
        } catch (NumberFormatException ex){
            System.out.println("Вы ввели недействительный instrument id");
        } catch (UnderOneException e){
            System.out.println(e.getMessage());
        }

    }
}
