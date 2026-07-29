package exceptions;

public class NotAvailableException extends RuntimeException {
    public NotAvailableException() {
        super("Прибор занят");
    }
}
