package services;

import auth.SessionContext;
import domain.Booking;
import domain.Checkout;
import domain.Instrument;
import enums.BookingStatus;
import enums.InstrumentType;
import exceptions.NotFoundException;
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
    private final JdbcStorage jdbc;
    private final IdGen idGen = new IdGen();

    public InstrumentManager() {
        this(new JdbcStorage());
    }

    public InstrumentManager(JdbcStorage jdbc) {
        this.jdbc = jdbc;
    }

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

    public Instrument getInstrumentById(long id) {
        return instruments.stream()
                .filter(i -> i.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Прибор с ID " + id + " не найден"));
    }

    public void replaceAll(List<Instrument> newInstruments) {
        instruments.clear();
        instruments.addAll(newInstruments);
        long maxId = newInstruments.stream().mapToLong(Instrument::getId).max().orElse(0L);
        idGen.setId(maxId + 1);
    }

    public Instrument addInstrument(InstrumentType type) {
        if (!SessionContext.isAuthenticated()) {
            throw new SecurityException("Для добавления прибора необходима авторизация");
        }
        if (type == null) {
            throw new IllegalArgumentException("Тип прибора не может быть null");
        }

        Instrument newI = new Instrument(0, type);
        try {
            long generatedId = jdbc.insertInstrument(newI);
            newI.setId(generatedId);
            instruments.add(newI);
            return newI;
        } catch (SQLException e) {
            throw new RuntimeException(DbErrorHandler.translate(e), e);
        }
    }

    public List<Instrument> instAvailable(CheckoutManager checkoutManager, BookingManager bookingManager, InstrumentType type,
                                          Instant start, Instant end) throws IllegalArgumentException, StartAfterEndException, PastTimeException {
        List<Instrument> available = new ArrayList<>();
        TimeValidator.StartEndCheck(start, end);

        for (Instrument instrument : instruments) {
            if (instrument.getType() != type) {
                continue;
            }

            boolean isFree = true;

            // 1. Проверка активных бронирований
            for (Booking book : bookingManager.getBooks()) {
                if (book.getInstrumentId() == instrument.getId() && book.getStatus() == BookingStatus.ACTIVE) {
                    // Пересечение интервалов: НЕ (end <= book.start ИЛИ start >= book.end)
                    if (!(book.getEndAt().isBefore(start) || book.getStartAt().isAfter(end))) {
                        isFree = false;
                        break;
                    }
                }
            }

            if (!isFree) {
                continue;
            }

            // 2. Проверка фактических выдач (чекаутов)
            for (Checkout check : checkoutManager.getCheckouts()) {
                if (check.getInstrumentId() == instrument.getId()) {
                    if (check.getReturnedAt() == null) {
                        // Прибор прямо сейчас на руках: занят с момента takenAt до возврата
                        if (!end.isBefore(check.getTakenAt())) {
                            isFree = false;
                            break;
                        }
                    } else {
                        // Прибор был возвращен: проверяем пересечение с интервалом [takenAt, returnedAt]
                        if (!(check.getReturnedAt().isBefore(start) || check.getTakenAt().isAfter(end))) {
                            isFree = false;
                            break;
                        }
                    }
                }
            }

            if (isFree) {
                available.add(instrument);
            }
        }
        return available;
    }
}
