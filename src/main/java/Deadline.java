/** A task that must be completed before a specified date or time. */
public class Deadline extends Task {

    /** The date or time by which this task should be completed. */
    protected String by;

    /** Creates an incomplete deadline. */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}
