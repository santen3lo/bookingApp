package command;

import domain.Booking;
import exceptions.NotFoundException;
import exceptions.UnderOneException;
import parsers.CliInputParser;
import services.BookingManager;
import validators.TimeValidator;

public class BookShowCommand implements Command {
    private final BookingManager bookingManager;

    public BookShowCommand(BookingManager bookingManager) {
        this.bookingManager = bookingManager;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (args.length < 2) {
                System.err.println("Вы не ввели ID бронирования (использование: book_show <booking_id>)");
                return;
            }
            long bookingId = CliInputParser.parseId(args[1], "booking");
            Booking book = bookingManager.getBookingById(bookingId);

            System.out.println("#" + book.getId());
            System.out.println("instrument_id: " + book.getInstrumentId());
            System.out.println("start: " + TimeValidator.parseBack(book.getStartAt()));
            System.out.println("end: " + TimeValidator.parseBack(book.getEndAt()));
            System.out.println("status: " + book.getStatus());
            System.out.println("User: " + book.getOwnerUserId());

        } catch (UnderOneException | NotFoundException | IllegalArgumentException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка отображения брони: " + e.getMessage());
        }
    }

    @Override
    public String description() {
        return "Показать детали бронирования: book_show <booking_id>";
    }
}
