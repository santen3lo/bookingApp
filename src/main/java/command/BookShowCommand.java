package command;

import services.BookingManager;

import java.util.Scanner;

public class BookShowCommand implements Command{
    BookingManager bookingManager;

    public BookShowCommand (BookingManager bookingManager) {
        this.bookingManager = bookingManager;
    }
    @Override
    public void execute(String[] args) {
        bookingManager.bookShow(args);
    }

    @Override
    public String description() {
        return "";
    }
}
