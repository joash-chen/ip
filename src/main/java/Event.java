/** A task that starts and ends at specified dates or times. */
public class Event extends Task {

    /** The date or time at which this event starts. */
    protected String from;

    /** The date or time at which this event ends. */
    protected String to;

    /** Creates an incomplete event. */
    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
