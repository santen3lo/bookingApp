package command;

public class HelpCommand implements Command{
//    private final Service service;
//
//    HelpCommand(Service service) {
//        this.service = service;
//    }
    @Override
    public void execute(String[] arg) {
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
    }

    @Override
    public String description() {
        return "Команда выводящая ...";
    }
}
