package command;

import services.BookingManager;

import java.util.Scanner;

public class BookCreateCommand implements Command{
    BookingManager bookingManager;
    Scanner sc;

    public BookCreateCommand(BookingManager bookingManager, Scanner sc) {
        this.bookingManager = bookingManager;
        this.sc = sc;
    }
    @Override
    public void execute(String[] args) {
        try{
            bookingManager.createNewBooking(args[1], sc);//sc
        } catch (Exception e){
            System.err.println("Вы не ввели ID инструмента");
        }
    }

    @Override
    public String description() {
        return "";
    }
}
