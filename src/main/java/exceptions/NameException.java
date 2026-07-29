package exceptions;

public class NameException extends RuntimeException {
    public NameException(String desc) {
        super(desc);
    }
}
