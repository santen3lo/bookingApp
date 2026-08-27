package parsers;

import enums.InstrumentType;
import enums.ReturnCondition;
import exceptions.PastTimeException;
import exceptions.StartAfterEndException;
import exceptions.UnderOneException;
import validators.IdValidator;
import validators.TimeValidator;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class GuiInputParser {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private GuiInputParser() {}

    public record BookingCreateParams(long instrumentId, Instant startAt, Instant endAt) {}
    public record BookingRescheduleParams(Instant startAt, Instant endAt) {}
    public record CheckoutTakeParams(long instrumentId, String comment) {}
    public record AvailabilityParams(InstrumentType type, Instant startAt, Instant endAt) {}

    public static BookingCreateParams parseBookingCreate(String input)
            throws UnderOneException, PastTimeException, StartAfterEndException {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Строка параметров не может быть пустой");
        }
        String[] parts = input.split(",", 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Ожидается 3 параметра через запятую: instrumentId, start, end");
        }
        long instrumentId = parseId(parts[0].trim(), "instrument");
        Instant startAt = parseFutureDateTime(parts[1].trim());
        Instant endAt = parseFutureDateTime(parts[2].trim());
        TimeValidator.StartEndCheck(startAt, endAt);
        return new BookingCreateParams(instrumentId, startAt, endAt);
    }

    public static BookingRescheduleParams parseBookingReschedule(String input)
            throws PastTimeException, StartAfterEndException {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Строка параметров не может быть пустой");
        }
        String[] parts = input.split(",", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Ожидается 2 параметра через запятую: start, end");
        }
        Instant startAt = parseFutureDateTime(parts[0].trim());
        Instant endAt = parseFutureDateTime(parts[1].trim());
        TimeValidator.StartEndCheck(startAt, endAt);
        return new BookingRescheduleParams(startAt, endAt);
    }

    public static CheckoutTakeParams parseCheckoutTake(String input) throws UnderOneException {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Строка параметров не может быть пустой");
        }
        String[] parts = input.split(",", 2);
        long instrumentId = parseId(parts[0].trim(), "instrument");
        String comment = parts.length > 1 ? parts[1].trim() : "";
        return new CheckoutTakeParams(instrumentId, comment);
    }

    public static AvailabilityParams parseAvailability(String input)
            throws PastTimeException, StartAfterEndException {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Строка параметров не может быть пустой");
        }
        String[] parts = input.split(",", 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Ожидается 3 параметра через запятую: TYPE, start, end");
        }
        InstrumentType type = parseInstrumentType(parts[0].trim());
        Instant start = parseFutureDateTime(parts[1].trim());
        Instant end = parseFutureDateTime(parts[2].trim());
        TimeValidator.StartEndCheck(start, end);
        return new AvailabilityParams(type, start, end);
    }

    public static InstrumentType parseInstrumentType(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Тип инструмента не может быть пустым");
        }
        try {
            return InstrumentType.valueOf(input.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неверный тип инструмента: '" + input + "'. Проверьте написание.");
        }
    }

    public static ReturnCondition parseReturnCondition(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Состояние не может быть пустым");
        }
        try {
            return ReturnCondition.valueOf(input.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неверное состояние: '" + input + "'. Ожидается OK или DAMAGED");
        }
    }

    private static long parseId(String token, String entityName) throws UnderOneException {
        try {
            long id = Long.parseLong(token);
            IdValidator.checkId(id);
            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Недействительный ID (" + token + ") для " + entityName);
        }
    }

    private static Instant parseFutureDateTime(String dateTimeStr) throws PastTimeException {
        try {
            LocalDateTime ldt = LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
            Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();
            if (instant.isBefore(Instant.now())) {
                throw new PastTimeException();
            }
            return instant;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Неверный формат даты/времени: '" + dateTimeStr + "'. Ожидается 'YYYY-MM-DD HH:MM'");
        }
    }
}
