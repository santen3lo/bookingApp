package Validators;

import Exceptions.PastTimeException;
import Exceptions.StartAfterEndException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeValidator {
    public static String timeFormat (String time) throws PastTimeException {
        LocalDateTime localDateTime = LocalDateTime.parse(
                time,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        );
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        if (instant.isBefore(Instant.now())) {
            throw new PastTimeException();
        }
        return instant.toString();
    }
    public static void StartEndCheck (Instant start, Instant end) throws StartAfterEndException {
        if (start.isAfter(end)) {
            throw new StartAfterEndException();
        }
    }
}
