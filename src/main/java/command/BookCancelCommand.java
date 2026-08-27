package command;

import domain.Booking;
import exceptions.NotFoundException;
import exceptions.SecurityException;
import exceptions.UnderOneException;
import parsers.CliInputParser;
import services.BookingManager;

public class BookCancelCommand implements Command {
    private final BookingManager bookingManager;

    public BookCancelCommand(BookingManager bookingManager) {
        this.bookingManager = bookingManager;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (args.length < 2) {
                System.err.println("Вы не ввели ID бронирования (использование: book_cancel <booking_id>)");
                return;
            }
            long bookingId = CliInputParser.parseId(args[1], "booking");
            Booking b = bookingManager.cancelBooking(bookingId);
            System.out.println("Бронь #" + b.getId() + " успешно отменена");

        } catch (UnderOneException | SecurityException | NotFoundException | IllegalArgumentException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка отмены брони: " + e.getMessage());
        }
    }

    @Override
    public String description() {
        return "Отменить бронирование: book_cancel <booking_id>";
    }
}
