package validators;

import exceptions.UnderOneException;

public class IdValidator {
    public static void checkId(long id) throws UnderOneException {
        if (id < 1) {
            throw new UnderOneException("Id должен быть больше 0");
        }
    }
}
