package Validators;

import Exceptions.UnderOneException;

public class IdValidator {
    public static void checkId(long id) throws UnderOneException {
        if (id < 1) {
            throw new UnderOneException();
        }
    }
}
