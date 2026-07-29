package services;

import auth.SessionContext;
import domain.Booking;
import domain.Checkout;
import domain.Instrument;
import enums.BookingStatus;
import enums.InstrumentType;
import exceptions.PastTimeException;
import exceptions.SecurityException;
import exceptions.StartAfterEndException;
import storage.JdbcStorage;
import storage.DbErrorHandler;
import utils.IdGen;
import validators.TimeValidator;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class InstrumentManager {
    private final List<Instrument> instruments = new ArrayList<>();
    private final JdbcStorage jdbc = new JdbcStorage();
    private final IdGen idGen = new IdGen();

    public void loadFromDb() {
        try {
            instruments.clear();
            instruments.addAll(jdbc.loadAllInstruments());
            System.out.println("Приборы загружены из БД: " + instruments.size());
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки приборов: " + DbErrorHandler.translate(e));
        }
    }

    public List<Instrument> getInstruments() {
        return new ArrayList<>(instruments);
    }

    public void replaceAll(List<Instrument> newInstruments) {
        instruments.clear();
        instruments.addAll(newInstruments);
        long maxId = newInstruments.stream().mapToLong(Instrument::getId).max().orElse(0L);
        idGen.setId(maxId + 1);
    }
    public void addInstrument(InstrumentType type) {
        if (!SessionContext.isAuthenticated()) {
            throw new SecurityException("Для добавления прибора необходима авторизация");
        }

        Instrument newI = new Instrument(0, type);

        try {
            long generatedId = jdbc.insertInstrument(newI);
            newI.setId(generatedId);
            instruments.add(newI);
        } catch (SQLException e) {
            throw new RuntimeException(DbErrorHandler.translate(e), e);
        }
    }
    public List<Instrument> instAvailable(CheckoutManager checkoutManager, BookingManager bookingManager, InstrumentType type,
                                          Instant start, Instant end) throws IllegalArgumentException, StartAfterEndException, PastTimeException {
        List<Instrument> instr = new ArrayList<>();
        TimeValidator.StartEndCheck(start, end);

        for (Instrument instrument : instruments) {
            if (instrument.getType() == type) {
                boolean flag = true;
                for (Booking book : bookingManager.getBooks()) {
                    if (book.getInstrumentId() == instrument.getId()) {
                        if (!(book.getStartAt().isAfter(end) || book.getEndAt().isBefore(start)) && book.getStatus() == BookingStatus.ACTIVE) {
                            flag = false;
                            break;
                        }
                    }
                }
                for (Checkout check : checkoutManager.getCheckouts()) {
                    if (check.getInstrumentId() == instrument.getId()) {
                        if (check.getReturnedAt() != null && !(check.getTakenAt().isAfter(end) || check.getReturnedAt().isBefore(start))) {
                            flag = false;
                            break;
                        }
                    }
                }
                if (flag){
                    instr.add(instrument);
                }
            }
        }
        return instr;
    }

}
