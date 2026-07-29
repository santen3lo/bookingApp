package command;

import services.BookingManager;

public class BookLlistCommand implements Command{
    BookingManager bookingManager;
    String[] command;
    public BookLlistCommand (BookingManager bookingManager){
        this.bookingManager = bookingManager;
    }
    @Override
    public void execute(String[] args) {
        bookingManager.bookList(args);
    }

    @Override
    public String description() {
        return "";
    }
}
