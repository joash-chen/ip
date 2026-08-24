/** Represents an input error that Chai can explain to the user. */
public class ChaiException extends Exception {

    /** Creates an input error with the given explanation. */
    public ChaiException(String message) {
        super(message);
    }
}
