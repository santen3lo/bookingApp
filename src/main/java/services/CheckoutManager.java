package services;

import auth.SessionContext;
import domain.Checkout;
import enums.ReturnCondition;
import exceptions.NotFoundException;
import exceptions.SecurityException;
import exceptions.UnderOneException;
import storage.DbErrorHandler;
import storage.JdbcStorage;
import utils.IdGen;
import validators.IdValidator;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CheckoutManager {
    private final List<Checkout> checkouts = new ArrayList<>();
    private final IdGen id = new IdGen();
    private final JdbcStorage jdbc;

    public CheckoutManager() {
        this(new JdbcStorage());
    }

    public CheckoutManager(JdbcStorage jdbc) {
        this.jdbc = jdbc;
    }

    public List<Checkout> getCheckouts() {
        return new ArrayList<>(checkouts);
    }

    public void loadFromDb() {
        try {
            checkouts.clear();
            checkouts.addAll(jdbc.loadAllCheckouts());
            System.out.println("Загружено чекаутов из БД: " + checkouts.size());
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки чекаутов: " + DbErrorHandler.translate(e));
        }
    }

    public Checkout getCheckoutById(long checkoutId) throws UnderOneException {
        IdValidator.checkId(checkoutId);
        return checkouts.stream()
                .filter(c -> c.getId() == checkoutId)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Выдача с ID " + checkoutId + " не найдена"));
    }

    public List<Checkout> getOpenCheckouts() {
        return checkouts.stream()
                .filter(c -> c.getReturnedAt() == null)
                .collect(Collectors.toList());
    }

    public Checkout takeCheckout(long instrumentId, String comment) throws UnderOneException {
        if (!SessionContext.isAuthenticated()) {
            throw new SecurityException("Для оформления выдачи необходимо войти в систему");
        }
        IdValidator.checkId(instrumentId);

        boolean alreadyOut = checkouts.stream()
                .anyMatch(c -> c.getInstrumentId() == instrumentId && c.getReturnedAt() == null);
        if (alreadyOut) {
            throw new IllegalStateException("Прибор уже выдан и ещё не возвращён");
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
            return newC;
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public Checkout returnCheckout(long checkoutId, ReturnCondition condition) throws UnderOneException {
        if (!SessionContext.isAuthenticated()) {
            throw new SecurityException("Для возврата прибора необходимо войти в систему");
        }
        IdValidator.checkId(checkoutId);
        if (condition == null) {
            throw new IllegalArgumentException("Состояние возврата (OK или DAMAGED) обязательно");
        }

        Checkout checkout = getCheckoutById(checkoutId);

        if (checkout.getReturnedAt() != null) {
            throw new IllegalStateException("Этот чекаут уже закрыт (прибор уже возвращён ранее)");
        }

        if (SessionContext.getCurrentUserId() != checkout.getUserId()) {
            throw new SecurityException("Вы не можете вернуть чужую выдачу");
        }

        Instant oldReturn = checkout.getReturnedAt();
        ReturnCondition oldCond = checkout.getReturnCondition();

        checkout.setReturnedAt(Instant.now());
        checkout.setReturnCondition(condition);

        try {
            jdbc.updateCheckout(checkout);
            return checkout;
        } catch (SQLException e) {
            checkout.setReturnedAt(oldReturn);
            checkout.setReturnCondition(oldCond);
            throw dbError(e);
        }
    }

    public void replaceAll(List<Checkout> newCheckouts) {
        checkouts.clear();
        checkouts.addAll(newCheckouts);
        id.setId(newCheckouts.stream()
                .mapToLong(Checkout::getId)
                .max()
                .orElse(0L) + 1);
    }

    private RuntimeException dbError(SQLException e) {
        return new RuntimeException(DbErrorHandler.translate(e), e);
    }
}