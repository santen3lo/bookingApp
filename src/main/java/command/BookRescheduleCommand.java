package command;

import domain.Booking;
import exceptions.NotAvailableException;
import exceptions.NotFoundException;
import exceptions.PastTimeException;
import exceptions.SecurityException;
import exceptions.StartAfterEndException;
import exceptions.UnderOneException;
import parsers.CliInputParser;
import services.BookingManager;

import java.time.Instant;

public class BookRescheduleCommand implements Command {
    private final BookingManager bookingManager;

    public BookRescheduleCommand(BookingManager bookingManager) {
        this.bookingManager = bookingManager;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (args.length < 6) {
                System.err.println("Использование: book_reschedule <booking_id> <YYYY-MM-DD> <HH:MM> <YYYY-MM-DD> <HH:MM>");
                return;
            }

            long bookingId = CliInputParser.parseId(args[1], "booking");
            Instant start = CliInputParser.parseFutureDateTime(args[2] + " " + args[3]);
            Instant end = CliInputParser.parseFutureDateTime(args[4] + " " + args[5]);
            CliInputParser.validateStartEnd(start, end);

            Booking b = bookingManager.rescheduleBooking(bookingId, start, end);
            System.out.println("Бронь #" + b.getId() + " успешно перенесена на " + args[2] + " " + args[3] + " - " + args[4] + " " + args[5]);

        } catch (NotAvailableException e) {
            System.err.println("Нельзя перенести: в это время инструмент занят");
        } catch (UnderOneException | PastTimeException | StartAfterEndException | SecurityException | NotFoundException | IllegalArgumentException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка переноса бронирования: " + e.getMessage());
        }
    }

    @Override
    public String description() {
        return "Перенести бронирование: book_reschedule <booking_id> <YYYY-MM-DD> <HH:MM> <YYYY-MM-DD> <HH:MM>";
    }
}
