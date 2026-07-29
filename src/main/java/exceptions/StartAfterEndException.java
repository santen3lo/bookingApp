package exceptions;

public class StartAfterEndException extends RuntimeException {
    public StartAfterEndException(String description) {
        super("Начало после конца");
    }
}
