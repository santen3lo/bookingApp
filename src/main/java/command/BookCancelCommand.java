package command;

import services.BookingManager;

import java.util.Scanner;

public class BookCancelCommand implements Command{
    BookingManager bookingManager;

    public BookCancelCommand (BookingManager bookingManager) {
        this.bookingManager = bookingManager;
    }
    @Override
    public void execute(String[] args) {
        bookingManager.bookCancel(args);
    }

    @Override
    public String description() {
        return "";
    }
}
