package chai.task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** A task that starts and ends on specified dates. */
public class Event extends Task {
    /** The human-friendly format used when displaying event dates. */
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH);

    /** The date on which this event starts. */
    protected LocalDate from;

    /** The date on which this event ends. */
    protected LocalDate to;

    /** Creates an incomplete event. */
    public Event(String description, LocalDate from, LocalDate to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /** Returns the event's start date. */
    public LocalDate getFrom() {
        return from;
    }

    /** Returns the event's end date. */
    public LocalDate getTo() {
        return to;
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from.format(DISPLAY_FORMAT)
                + " to: " + to.format(DISPLAY_FORMAT) + ")";
    }
}
