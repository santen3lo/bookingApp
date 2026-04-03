package Exceptions;

public class PastTimeException extends RuntimeException {
    public PastTimeException() {
        super("Время должно быть в будущем");
    }
}
