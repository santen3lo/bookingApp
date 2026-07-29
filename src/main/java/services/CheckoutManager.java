package services;

import auth.SessionContext;
import domain.Booking;
import domain.Instrument;
import enums.ReturnCondition;
import exceptions.NotOpenException;
import exceptions.SecurityException;
import exceptions.StartAfterEndException;
import exceptions.UnderOneException;
import storage.DbErrorHandler;
import storage.JdbcStorage;
import validators.FlagValidator;
import validators.IdValidator;
import domain.Checkout;
import utils.IdGen;
import validators.NameValidator;
import validators.TimeValidator;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CheckoutManager {
    private final ArrayList<Checkout> checkouts = new ArrayList<>();
    private final IdGen id = new IdGen();
    private final JdbcStorage jdbc = new JdbcStorage();


    public ArrayList<Checkout> getCheckouts(){
        return checkouts;
    }

    public void loadFromDb() {
        try {
            checkouts.clear();
            checkouts.addAll(jdbc.loadAllCheckouts());
            System.out.println("Загружено чекаутов из БД: " + checkouts.size());
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки броней: " + e.getMessage());
        }
    }
    //==============================================================================================
    public void checkoutReturn(String[] com, Scanner sc) {
        try {
            if (!SessionContext.isAuthenticated()) {
                throw new SecurityException("Для создания брони необходимо войти в систему (login)");
            }
            long checkId = Integer.parseInt(com[1]);
            IdValidator.checkId((checkId));
            System.out.print("Состояние (OK | DAMAGED): ");
            ReturnCondition ret = ReturnCondition.valueOf(sc.nextLine());
            for (Checkout checkout : checkouts) {
                if (checkId == checkout.getId()){
                    if (SessionContext.getCurrentUserId() == checkout.getUserId()){
                        checkout.setReturnCondition(ret);
                        checkout.setReturnedAt(Instant.now());
                        System.out.println(ret + " returned");
                        break;
                    } else {
                        System.err.println("У вас нет прав, чтобы вернуть этот инструмент");
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Вы не ввели id чекаута");
        } catch (UnderOneException | StartAfterEndException | SecurityException e) {
            System.err.println(e.getMessage());
        } catch (NumberFormatException exception) {
            System.err.println("Вы ввели недействительный instrument id");
        } catch (IllegalArgumentException e) {
            System.err.println("Вы ввели неверное состояние");
        }
    }

    //==============================================================================================
    public void checkoutList(String[] com) {
        try {
            String flag = com[1];
            FlagValidator.checkOpenFlag(flag);
            System.out.println("ID  Instrument Id   User    TakenAt");
            for (Checkout checkout: checkouts){
                if (checkout.getReturnedAt() == null || checkout.getReturnedAt().isAfter(Instant.now())){
                    System.out.println(checkout.getId() + "   " + checkout.getInstrumentId()
                            + "              " + checkout.getUserId() + "        " + TimeValidator.parseBack(checkout.getTakenAt()));
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Вы не ввели id чекаута");
        } catch (NotOpenException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Произошла ошибка");
        }
    }//==============================================================================================
    public void checkoutShow(String[] com) {
        try {
            long checkoutId = Integer.parseInt(com[1]);
            IdValidator.checkId(checkoutId);

            for (Checkout checkout : checkouts) {
                if (checkout.getId() == checkoutId) {
                    System.out.println("#" + checkoutId);
                    System.out.println("instrument_id:" + checkout.getInstrumentId());
                    System.out.println("user:" + checkout.getUserId());
                    System.out.println("takenAt:" + TimeValidator.parseBack(checkout.getTakenAt()));
                    System.out.println("returnedAt:" + TimeValidator.parseBack(checkout.getReturnedAt()));
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Вы не ввели id чекаута");
        } catch (UnderOneException e){
            System.err.println(e.getMessage());
        } catch (NumberFormatException e){
            System.err.println("Вы ввели недействительный instrument id");
        }
    }
    //==============================================================================================
    public void checkoutTake(String[] com) {
        try {
            if (!SessionContext.isAuthenticated()) {
                throw new SecurityException("Для создания брони необходимо войти в систему (login)");
            }
            long instrId;
            instrId = Integer.parseInt(com[1]);
            IdValidator.checkId(instrId);
            for (Checkout checkout : checkouts) {
                if (instrId == checkout.getInstrumentId()) {
                    checkout.setReturnCondition(ReturnCondition.OK);
                    System.out.println("Кто берет:" + checkout.getOwnerUsername());
                    if (!checkout.getComment().isEmpty()){
                        System.out.println(checkout.getComment());
                    }
                    System.out.println(checkout.getReturnCondition());
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Вы не ввели id чекаута");
        } catch (NumberFormatException ex){
            System.err.println("Вы ввели недействительный instrument id");
        } catch (UnderOneException | SecurityException e){
            System.err.println(e.getMessage());
        }
    }






//==============================================================================================
//    public void checkoutTake(long instrId,  String com) throws NumberFormatException, UnderOneException {
//        IdValidator.checkId(instrId);
//        checkouts.add(new Checkout(id.createId(), instrId, SessionContext.getCurrentUserId(), com,
//                Instant.now(), null, ReturnCondition.OK, "system", Instant.now()));
//    }
//
//    //==============================================================================================
//    public void returnCheckout(long checkId, ReturnCondition r)
//            throws UnderOneException, IllegalArgumentException, StartAfterEndException{
//        IdValidator.checkId((checkId));
//        for (Checkout checkout : checkouts) {
//            if (checkId == checkout.getId()){
//                checkout.setReturnCondition(r);
//                checkout.setReturnedAt(Instant.now());
//                break;
//            }
//        }
//    }
//==============================================================================================
public void replaceAll(List<Checkout> newCheckouts) {
    checkouts.clear();
    checkouts.addAll(newCheckouts);
    id.setId(newCheckouts.stream()
            .mapToLong(Checkout::getId)
            .max()
            .orElse(0L) + 1);
}
    //==============================================================================================
    //==============================================================================================
    //==============================================================================================

    public void checkoutTake(long instrumentId, String comment)
            throws UnderOneException, RuntimeException {
        if (!SessionContext.isAuthenticated()) {
            throw new SecurityException("Для оформления выдачи необходимо войти в систему");
        }
        IdValidator.checkId(instrumentId);


        boolean alreadyOut = checkouts.stream()
                .anyMatch(c -> c.getInstrumentId() == instrumentId && c.getReturnedAt() == null);
        if (alreadyOut) {
            throw new IllegalStateException("Прибор уже выдан и не возвращён");
        }


        Checkout newC = new Checkout(
                0, instrumentId, SessionContext.getCurrentUserId(),
                comment != null ? comment.trim() : "",
                Instant.now(), null, null,
                SessionContext.getCurrentUser().getLogin(),
                Instant.now()
        );

        try {
            long generatedId = jdbc.insertCheckout(newC);
            newC.setId(generatedId);
            checkouts.add(newC);
            System.out.println("Успешно выдано");
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public void returnCheckout(long checkId, ReturnCondition r)
            throws UnderOneException, RuntimeException{
        if (!SessionContext.isAuthenticated()) {
            throw new SecurityException("Для оформления выдачи необходимо войти в систему");
        }
        IdValidator.checkId((checkId));
        for (Checkout checkout : checkouts) {
            if (checkId == checkout.getId()){
                if (SessionContext.getCurrentUserId() != checkout.getUserId()){
                    throw new RuntimeException("Нельзя вернуть не свой чекаут");
                }
                Instant oldReturn = checkout.getReturnedAt();
                ReturnCondition oldCond = checkout.getReturnCondition();

                checkout.setReturnedAt(Instant.now());
                checkout.setReturnCondition(r);

                try {
                    jdbc.updateCheckout(checkout);
                } catch (SQLException e) {
                    checkout.setReturnedAt(oldReturn);
                    checkout.setReturnCondition(oldCond);
                    throw dbError(e);
                }
                break;
            }
        }
    }

    private RuntimeException dbError(SQLException e) {
        return new RuntimeException(DbErrorHandler.translate(e), e);
    }

}