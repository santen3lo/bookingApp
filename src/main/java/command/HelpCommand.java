package command;

public class HelpCommand implements Command {
    @Override
    public void execute(String[] args) {
        System.out.println("""
                Доступные команды:
                1) register - зарегистрировать новую учетную запись
                2) login - войти в систему
                3) book_create <instrument_id> - забронировать инструмент
                4) book_list <instrument_id> [--from YYYY-MM-DD] - список броней инструмента
                5) book_cancel <booking_id> - отменить бронь
                6) book_reschedule <booking_id> <YYYY-MM-DD> <HH:MM> <YYYY-MM-DD> <HH:MM> - перенести бронь
                7) book_show <booking_id> - подробности брони
                8) checkout_take <instrument_id> [комментарий] - выдать инструмент
                9) checkout_return <checkout_id> - вернуть инструмент (состояние OK | DAMAGED)
                10) checkout_list [--open-only] - список выдач
                11) checkout_show <checkout_id> - подробности выдачи
                12) inst_available <TYPE> <YYYY-MM-DD> <HH:MM> <YYYY-MM-DD> <HH:MM> - проверить доступность
                13) help - показать эту справку
                14) exit - выйти из программы
                """);
    }

    @Override
    public String description() {
        return "Справка по доступным командам: help";
    }
}
