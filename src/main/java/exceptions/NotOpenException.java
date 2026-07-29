package exceptions;

public class NotOpenException extends RuntimeException {
    public NotOpenException(String message) {
        super(message+" не поддерживается. Введите --open-only");
    }
}
