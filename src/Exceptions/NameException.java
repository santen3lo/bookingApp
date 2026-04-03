package Exceptions;

public class NameException extends RuntimeException {
    public NameException() {
        super("Пустая строка не может быть именем");
    }
}
