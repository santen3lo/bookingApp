package Validators;

import Exceptions.NameException;

public class NameValidator {
    public static void nameCheck (String name) throws NameException {
        if (!name.trim().isEmpty()) {
            throw new NameException();
        }
    }
}
