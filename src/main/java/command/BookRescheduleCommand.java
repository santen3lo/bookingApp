package command;

import services.BookingManager;
import services.CheckoutManager;

public class BookRescheduleCommand implements Command{
    BookingManager bookingManager;

    public BookRescheduleCommand (BookingManager bookingManager){
        this.bookingManager = bookingManager;
    }
    @Override
    public void execute(String[] args) {
        bookingManager.bookReschedule(args);
    }

    @Override
    public String description() {
        return "";
    }
}
