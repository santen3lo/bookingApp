package validators;

import exceptions.NotFromException;
import exceptions.NotOpenException;

public class FlagValidator {
    public static void checkFromFlag(String flag) throws NotFromException {
        if (!flag.equals("--from")){
            throw new NotFromException(flag);
        }
    }
    public static void checkOpenFlag(String flag) throws NotOpenException {
        if (!flag.equals("--open-only")){
            throw new NotOpenException(flag);
        }
    }
}
