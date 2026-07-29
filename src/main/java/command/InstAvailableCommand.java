package command;

import domain.Booking;
import domain.Checkout;
import domain.Instrument;
import enums.InstrumentType;
import exceptions.PastTimeException;
import exceptions.StartAfterEndException;
import services.BookingManager;
import services.CheckoutManager;
import services.InstrumentManager;
import validators.TimeValidator;

import java.time.Instant;

public class InstAvailableCommand implements Command{

    BookingManager bookingManager;
    InstrumentManager instrumentManager;
    CheckoutManager checkoutManager;

    public InstAvailableCommand(BookingManager bm, CheckoutManager cm, InstrumentManager im){
        this.bookingManager = bm;
        this.checkoutManager = cm;
        this.instrumentManager = im;
    }

    @Override
    public void execute (String[] com) {
        //Enums.InstrumentType type, Instant start, Instant end
        try {
            InstrumentType type = InstrumentType.valueOf(com[1]);
            Instant start = Instant.parse(TimeValidator.timeFormat(com[2] +" "+ com[3]));
            Instant end = Instant.parse(TimeValidator.timeFormat(com[4] + " "+com[5]));
            TimeValidator.StartEndCheck(start, end);

            System.out.print("Available instruments: ");
            for (Instrument instrument : instrumentManager.getInstruments()) {
                if (instrument.getType() == type) {
                    boolean flag = true;
                    for (Booking book : bookingManager.getBooks()) {
                        if (book.getInstrumentId() == instrument.getId()) {
                            if (!(book.getStartAt().isAfter(end) || book.getEndAt().isBefore(start))) {
                                flag = false;
                                break;
                            }
                        }
                    }
                    for (Checkout check : checkoutManager.getCheckouts()) {
                        if (check.getInstrumentId() == instrument.getId()) {
                            if (!(check.getTakenAt().isAfter(end) || check.getReturnedAt().isBefore(start))) {
                                flag = false;
                                break;
                            }
                        }
                    }
                    if (flag){
                        System.out.print(instrument.getId() + ", ");

                    }
                }
            }
            System.out.println("\b\b");
        } catch (IllegalArgumentException e) {
            System.out.println("Такого инструмента не существует");
        } catch (StartAfterEndException | PastTimeException e) {
            System.out.println(e.getMessage());
        } catch (IndexOutOfBoundsException e){
            System.err.println("Вы ввели неправильное количество аргументов");
        }
    }

    @Override
    public String description() {
        return "";
    }
}
