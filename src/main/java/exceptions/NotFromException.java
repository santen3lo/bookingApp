package exceptions;

public class NotFromException extends RuntimeException {
    public NotFromException(String message) {
        super("Флаг "+message + " не поддерживается");
    }
}
