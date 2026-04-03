package Managers;

import java.util.Scanner;

public class CommandManager {
    Scanner sc;
    public  CommandManager(Scanner scanner){
        sc = scanner;
    }
    Order order = new Order();
    boolean stop = true;

    public void manage(){
        while(stop) {
            String[] in = sc.next().split(" ");
            String command = in[0];
            switch (command) {
                case "book_cancel":
                    order.bookingManager.bookCancel(in);
                    break;
                case "book_create":
                    order.bookingManager.createNewBooking(sc);
                    break;
                case "book_list":
                    order.bookingManager.bookList(in);
                    break;
                case "checkout_take":
                    order.checkoutManager.checkoutTake(in);
                    break;
                case "checkout_return":
                    order.checkoutManager.checkoutReturn(in);
                    break;
                case "checkout_list":
                    order.checkoutManager.checkoutList(in);
                    break;
                case "inst_available":
                    order.instAvailable(in);
                    break;
                case "book_show":
                    order.bookingManager.bookShow(in);
                    break;
                case "checkout_show":
                    order.checkoutManager.checkoutShow(in);
                    break;
                case "book_reschedule":
                    order.bookingManager.bookReschedule(in);
                    break;
                case "exit":
                    stop = false;
                    break;
                case "help":
                    System.out.println("1) book_create <instrument_id> \n" +
                            "2) book_list <instrument_id> [--from YYYY-MM-DD] \n" +
                            "3) book_cancel <booking_id>\n" +
                            "4) checkout_take <instrument_id>\n" +
                            "5) checkout_return <checkout_id>\n" +
                            "6) checkout_list [--open-only]\n" +
                            "7) inst_available <type> <start> <end>\n" +
                            "8) book_show <booking_id>\n" +
                            "9) checkout_show <checkout_id>\n" +
                            "10) book_reschedule <booking_id> <start> <end>\n" +
                            "11) exit\n");
                default:
                    System.out.println("Неизвестная комманда " + command);
            }
        }
    }

}
