package Managers;

import Enums.InstrumentType;
import Exceptions.PastTimeException;
import Exceptions.StartAfterEndException;
import Validators.TimeValidator;
import domain.Booking;
import domain.Checkout;
import domain.Instrument;

import java.time.Instant;

public class Order {
    private Instrument instrument;
    private Booking booking;
    private Checkout checkout;

    BookingManager bookingManager = new BookingManager();
    InstrumentManager instrumentManager = new InstrumentManager();
    CheckoutManager checkoutManager = new CheckoutManager();

    public void instAvailable(String[] com) {
        //Enums.InstrumentType type, Instant start, Instant end
        try {
            InstrumentType type = InstrumentType.valueOf(com[1]);
            Instant start = Instant.parse(TimeValidator.timeFormat(com[2]));
            Instant end = Instant.parse(TimeValidator.timeFormat(com[3]));
            TimeValidator.StartEndCheck(start, end);

            System.out.print("Available instruments: ");
            for (Instrument instrument : instrumentManager.getInstruments()) {
                if (instrument.getType() == type) {
                    for (Booking book : bookingManager.getBooks()) {
                        if (book.getStartAt().isAfter(end) || book.getEndAt().isBefore(start)) {
                            System.out.print(book.getInstrumentId()+", ");
                        }
                    }
                    for (Checkout check : checkoutManager.getCheckouts()) {
                        if (check.getTakenAt().isAfter(end) || check.getReturnedAt().isBefore(start)) {
                            System.out.print(check.getInstrumentId()+", ");
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Такого инструмента не существует");
        } catch (StartAfterEndException | PastTimeException e) {
            System.out.println(e.getMessage());
        }
    }
}
