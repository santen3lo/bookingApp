package Exceptions;

public class NotFromException extends RuntimeException {
    public NotFromException(String message) {
        super(message+" не поддерживается. Введите --from YYYY-MM-DD");
    }
}
