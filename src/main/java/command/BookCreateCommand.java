package command;

import enums.BookingStatus;
import exceptions.NotAvailableException;
import exceptions.PastTimeException;
import exceptions.SecurityException;
import exceptions.StartAfterEndException;
import exceptions.UnderOneException;
import parsers.CliInputParser;
import services.BookingManager;
import domain.Booking;

import java.time.Instant;
import java.util.Scanner;

public class BookCreateCommand implements Command {
    private final BookingManager bookingManager;
    private final Scanner sc;

    public BookCreateCommand(BookingManager bookingManager, Scanner sc) {
        this.bookingManager = bookingManager;
        this.sc = sc;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (args.length < 2) {
                System.err.println("Вы не ввели ID инструмента (использование: book_create <instrument_id>)");
                return;
            }
            long instId = CliInputParser.parseId(args[1], "instrument");

            System.out.print("Начало (YYYY-MM-DD HH:MM): ");
            String startStr = sc.nextLine().trim();
            Instant startAt = CliInputParser.parseFutureDateTime(startStr);

            System.out.print("Конец (YYYY-MM-DD HH:MM): ");
            String endStr = sc.nextLine().trim();
            Instant endAt = CliInputParser.parseFutureDateTime(endStr);

            CliInputParser.validateStartEnd(startAt, endAt);

            Booking booking = bookingManager.createBooking(instId, startAt, endAt);
            System.out.println("OK booking_id = " + booking.getId());

        } catch (NotAvailableException e) {
            System.err.println("В это время инструмент занят");
        } catch (PastTimeException | StartAfterEndException | UnderOneException | SecurityException | IllegalArgumentException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка создания бронирования: " + e.getMessage());
        }
    }

    @Override
    public String description() {
        return "Создать новое бронирование: book_create <instrument_id>";
    }
}
