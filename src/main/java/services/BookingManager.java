package services;

import auth.SessionContext;
import domain.Booking;
import enums.BookingStatus;
import exceptions.NotAvailableException;
import exceptions.NotFoundException;
import exceptions.SecurityException;
import exceptions.StartAfterEndException;
import exceptions.UnderOneException;
import storage.DbErrorHandler;
import storage.JdbcStorage;
import utils.IdGen;
import validators.IdValidator;
import validators.TimeValidator;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BookingManager {
    private final List<Booking> books = new ArrayList<>();
    private final IdGen id = new IdGen();
    private final JdbcStorage jdbc;

    public BookingManager() {
        this(new JdbcStorage());
    }

    public BookingManager(JdbcStorage jdbc) {
        this.jdbc = jdbc;
    }

    public void loadFromDb() {
        try {
            books.clear();
            books.addAll(jdbc.loadAllBookings());
            System.out.println("Загружено броней из БД: " + books.size());
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки броней: " + DbErrorHandler.translate(e));
        }
    }

    public List<Booking> getBooks() {
        return new ArrayList<>(books);
    }

    public Booking getBookingById(long bookingId) throws UnderOneException {
        IdValidator.checkId(bookingId);
        return books.stream()
                .filter(b -> b.getId() == bookingId)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Booking " + bookingId + " не найден"));
    }

    public List<Booking> getBookingsForInstrument(long instrumentId, Instant from) throws UnderOneException {
        IdValidator.checkId(instrumentId);
        boolean any = books.stream().anyMatch(b -> b.getInstrumentId() == instrumentId);
        if (!any) {
            throw new NotFoundException("Booking с instrument_id " + instrumentId + " не найден");
        }

        List<Booking> result = new ArrayList<>();
        for (Booking book : books) {
            if (book.getInstrumentId() == instrumentId
                    && book.getStatus() != BookingStatus.CANCELLED
                    && (from == null || book.getStartAt().isAfter(from))) {
                result.add(book);
            }
        }
        return result;
    }

    public Booking createBooking(long instrumentId, Instant startAt, Instant endAt)
            throws UnderOneException, StartAfterEndException {
        if (!SessionContext.isAuthenticated()) {
            throw new SecurityException("Для создания брони необходимо войти в систему (login)");
        }
        IdValidator.checkId(instrumentId);
        TimeValidator.StartEndCheck(startAt, endAt);

        // Проверяем занятость прибора
        for (Booking booking : books) {
            if (booking.getInstrumentId() == instrumentId && booking.getStatus() == BookingStatus.ACTIVE) {
                if (!(booking.getEndAt().isBefore(startAt) || booking.getStartAt().isAfter(endAt))) {
                    throw new NotAvailableException();
                }
            }
        }

        Booking newB = new Booking(0, instrumentId, startAt, endAt, SessionContext.getCurrentUserId(),
                BookingStatus.ACTIVE, Instant.now(), Instant.now());
        try {
            long generatedId = jdbc.insertBooking(newB);
            newB.setId(generatedId);
            books.add(newB);
            return newB;
        } catch (SQLException e) {
            throw dbError(e);
        }
    }

    public Booking cancelBooking(long bookingId) throws UnderOneException {
        if (!SessionContext.isAuthenticated()) {
            throw new SecurityException("Для отмены брони необходимо войти в систему (login)");
        }
        IdValidator.checkId(bookingId);

        Booking book = getBookingById(bookingId);
        if (SessionContext.getCurrentUserId() != book.getOwnerUserId()) {
            throw new SecurityException("Вы не можете отменить чужую бронь");
        }

        BookingStatus oldStatus = book.getStatus();
        Instant oldUpdated = book.getUpdatedAt();
        book.setStatus(BookingStatus.CANCELLED);
        book.setUpdatedAt(Instant.now());

        try {
            jdbc.updateBooking(book);
            return book;
        } catch (SQLException e) {
            book.setStatus(oldStatus);
            book.setUpdatedAt(oldUpdated);
            throw dbError(e);
        }
    }

    public Booking rescheduleBooking(long bookingId, Instant newStart, Instant newEnd)
            throws UnderOneException, StartAfterEndException {
        if (!SessionContext.isAuthenticated()) {
            throw new SecurityException("Для переноса брони необходимо войти в систему (login)");
        }
        IdValidator.checkId(bookingId);
        TimeValidator.StartEndCheck(newStart, newEnd);

        Booking book = getBookingById(bookingId);
        if (SessionContext.getCurrentUserId() != book.getOwnerUserId()) {
            throw new SecurityException("У вас нет прав на изменение чужой брони");
        }

        // Проверяем пересечение с другими активными бронями того же инструмента
        for (Booking other : books) {
            if (other.getInstrumentId() == book.getInstrumentId() && other.getId() != bookingId
                    && other.getStatus() == BookingStatus.ACTIVE) {
                if (!(other.getEndAt().isBefore(newStart) || other.getStartAt().isAfter(newEnd))) {
                    throw new NotAvailableException();
                }
            }
        }

        Instant oldStart = book.getStartAt();
        Instant oldEnd = book.getEndAt();
        Instant oldUpdated = book.getUpdatedAt();

        book.setStartAt(newStart);
        book.setEndAt(newEnd);
        book.setUpdatedAt(Instant.now());

        try {
            jdbc.updateBooking(book);
            return book;
        } catch (SQLException e) {
            book.setStartAt(oldStart);
            book.setEndAt(oldEnd);
            book.setUpdatedAt(oldUpdated);
            throw dbError(e);
        }
    }

    public void replaceAll(List<Booking> newBookings) {
        books.clear();
        books.addAll(newBookings);
        long maxId = newBookings.stream().mapToLong(Booking::getId).max().orElse(0L);
        id.setId(maxId + 1);
    }

    private RuntimeException dbError(SQLException e) {
        return new RuntimeException(DbErrorHandler.translate(e), e);
    }
}