/** The kinds of commands Chai understands. */
public enum CommandType {
    BYE,
    LIST,
    MARK,
    UNMARK,
    TODO,
    DEADLINE,
    EVENT,
    DELETE,
    UNKNOWN;

    /** Maps the first word of user input to a {@code CommandType}, defaulting to {@code UNKNOWN}. */
    public static CommandType fromKeyword(String keyword) {
        try {
            return CommandType.valueOf(keyword.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
