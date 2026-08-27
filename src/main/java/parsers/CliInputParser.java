package parsers;

import enums.InstrumentType;
import enums.ReturnCondition;
import exceptions.NotFromException;
import exceptions.NotOpenException;
import exceptions.PastTimeException;
import exceptions.StartAfterEndException;
import exceptions.UnderOneException;
import validators.IdValidator;
import validators.TimeValidator;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class CliInputParser {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private CliInputParser() {}

    public static long parseId(String token, String entityName) throws UnderOneException {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("ID для " + entityName + " не может быть пустым");
        }
        try {
            long id = Long.parseLong(token.trim());
            IdValidator.checkId(id);
            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Недействительный ID (" + token + ") для " + entityName);
        }
    }

    public static Instant parseFutureDateTime(String dateTimeStr) throws PastTimeException {
        Instant instant = parseDateTime(dateTimeStr);
        if (instant.isBefore(Instant.now())) {
            throw new PastTimeException();
        }
        return instant;
    }

    public static Instant parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            throw new IllegalArgumentException("Дата и время не могут быть пустыми");
        }
        try {
            LocalDateTime ldt = LocalDateTime.parse(dateTimeStr.trim(), DATE_TIME_FORMATTER);
            return ldt.atZone(ZoneId.systemDefault()).toInstant();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Неверный формат даты/времени: '" + dateTimeStr + "'. Ожидается 'YYYY-MM-DD HH:MM'");
        }
    }

    public static Instant parseFromDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            throw new IllegalArgumentException("Дата не может быть пустой");
        }
        try {
            LocalDate ld = LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
            return ld.atStartOfDay(ZoneId.systemDefault()).toInstant();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Неверный формат даты: '" + dateStr + "'. Ожидается 'YYYY-MM-DD'");
        }
    }

    public static InstrumentType parseInstrumentType(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Тип прибора не может быть пустым");
        }
        try {
            return InstrumentType.valueOf(token.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неизвестный тип прибора: '" + token + "'");
        }
    }

    public static ReturnCondition parseReturnCondition(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Состояние прибора не может быть пустым");
        }
        try {
            return ReturnCondition.valueOf(token.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неверное состояние: '" + token + "'. Ожидается OK или DAMAGED");
        }
    }

    public static void validateFromFlag(String flag) throws NotFromException {
        if (!"--from".equals(flag)) {
            throw new NotFromException(flag);
        }
    }

    public static void validateOpenOnlyFlag(String flag) throws NotOpenException {
        if (!"--open-only".equals(flag)) {
            throw new NotOpenException(flag);
        }
    }

    public static void validateStartEnd(Instant start, Instant end) throws StartAfterEndException {
        TimeValidator.StartEndCheck(start, end);
    }
}
