package validators;

import domain.Booking;
import exceptions.PastTimeException;
import exceptions.StartAfterEndException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TimeValidator {
    public static String timeFormat (String time) throws PastTimeException {
        LocalDateTime localDateTime = LocalDateTime.parse(
                time,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        );
        Instant instant = localDateTime.atZone(ZoneId.of("Europe/Moscow")).toInstant();
        if (instant.isBefore(Instant.now())) {
            throw new PastTimeException();
        }
        return instant.toString();
    }
    public static String parseBack (Instant time) {
        if (time == null) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(
                time,
                ZoneId.systemDefault()
        );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return localDateTime.format(formatter);
    }
    public static void StartEndCheck(Instant start, Instant end) throws StartAfterEndException {
        if (start.isAfter(end)) {
            throw new StartAfterEndException("Начало должно быть раньше конца");// переиспользование, тут можно менять описание -- ГОТОВО
        }
    }


}
