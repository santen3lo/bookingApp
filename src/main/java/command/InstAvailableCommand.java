package command;

import domain.Instrument;
import enums.InstrumentType;
import exceptions.PastTimeException;
import exceptions.StartAfterEndException;
import parsers.CliInputParser;
import services.BookingManager;
import services.CheckoutManager;
import services.InstrumentManager;

import java.time.Instant;
import java.util.List;

public class InstAvailableCommand implements Command {
    private final BookingManager bookingManager;
    private final InstrumentManager instrumentManager;
    private final CheckoutManager checkoutManager;

    public InstAvailableCommand(BookingManager bm, CheckoutManager cm, InstrumentManager im) {
        this.bookingManager = bm;
        this.checkoutManager = cm;
        this.instrumentManager = im;
    }

    @Override
    public void execute(String[] args) {
        try {
            if (args.length < 6) {
                System.err.println("Использование: inst_available <TYPE> <YYYY-MM-DD> <HH:MM> <YYYY-MM-DD> <HH:MM>");
                return;
            }

            InstrumentType type = CliInputParser.parseInstrumentType(args[1]);
            Instant start = CliInputParser.parseFutureDateTime(args[2] + " " + args[3]);
            Instant end = CliInputParser.parseFutureDateTime(args[4] + " " + args[5]);
            CliInputParser.validateStartEnd(start, end);

            List<Instrument> available = instrumentManager.instAvailable(checkoutManager, bookingManager, type, start, end);

            if (available.isEmpty()) {
                System.out.println("Нет доступных приборов типа " + type + " на выбранный интервал.");
            } else {
                StringBuilder sb = new StringBuilder("Available instruments: ");
                for (int i = 0; i < available.size(); i++) {
                    sb.append(available.get(i).getId());
                    if (i < available.size() - 1) {
                        sb.append(", ");
                    }
                }
                System.out.println(sb);
            }

        } catch (StartAfterEndException | PastTimeException | IllegalArgumentException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка проверки доступности: " + e.getMessage());
        }
    }

    @Override
    public String description() {
        return "Проверить доступность приборов: inst_available <TYPE> <YYYY-MM-DD> <HH:MM> <YYYY-MM-DD> <HH:MM>";
    }
}
