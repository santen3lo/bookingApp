package Exceptions;

public class StartAfterEndException extends RuntimeException {
    public StartAfterEndException() {
        super("Начало должно быть раньше конца");
    }
}
