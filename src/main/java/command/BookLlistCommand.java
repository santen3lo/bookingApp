package command;

import domain.Booking;
import exceptions.NotFoundException;
import exceptions.NotFromException;
import exceptions.UnderOneException;
import parsers.CliInputParser;
import services.BookingManager;
import validators.TimeValidator;

import java.time.Instant;
import java.util.List;

public class BookLlistCommand implements Command {
    private final BookingManager bookingManager;

    public BookLlistCommand(BookingManager bookingManager) {
        this.bookingManager = bookingManager;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (args.length < 2) {
                System.err.println("Использование: book_list <instrument_id> [--from YYYY-MM-DD]");
                return;
            }
            long instId = CliInputParser.parseId(args[1], "instrument");

            Instant from = null;
            if (args.length >= 4) {
                CliInputParser.validateFromFlag(args[2]);
                from = CliInputParser.parseFromDate(args[3]);
            } else if (args.length == 3) {
                System.err.println("Не указана дата после флага --from");
                return;
            }

            List<Booking> list = bookingManager.getBookingsForInstrument(instId, from);
            if (list.isEmpty()) {
                System.out.println("Нет активных бронирований для инструмента #" + instId);
                return;
            }

            System.out.println(String.format("%-6s %-18s %-18s %-10s", "ID", "START", "END", "STATUS"));
            System.out.println("-------------------------------------------------------");
            for (Booking book : list) {
                System.out.println(String.format("%-6d %-18s %-18s %-10s",
                        book.getId(),
                        TimeValidator.parseBack(book.getStartAt()),
                        TimeValidator.parseBack(book.getEndAt()),
                        book.getStatus()));
            }

        } catch (UnderOneException | NotFromException | NotFoundException | IllegalArgumentException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка отображения списка броней: " + e.getMessage());
        }
    }

    @Override
    public String description() {
        return "Список бронирований инструмента: book_list <instrument_id> [--from YYYY-MM-DD]";
    }
}
