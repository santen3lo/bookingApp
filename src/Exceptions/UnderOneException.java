package Exceptions;

public class UnderOneException extends Exception {
    public UnderOneException() {
        super("Id должен быть больше 0");
    }
}
