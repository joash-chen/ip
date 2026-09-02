import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Saves Chai's tasks in a text file relative to the project root. */
public class Storage {
    /** The location used to persist Chai's task list. */
    private static final Path DATA_FILE = Path.of("data", "chai.txt");

    /** Writes every task in the list to the data file. */
    public static void save(List<Task> tasks) throws ChaiException {
        List<String> lines = new ArrayList<>();
        for (Task task : tasks) {
            lines.add(toFileLine(task));
        }

        try {
            Files.createDirectories(DATA_FILE.getParent());
            Files.write(DATA_FILE, lines);
        } catch (IOException e) {
            throw new ChaiException("I could not save your tasks: " + e.getMessage());
        }
    }

    /** Converts one task into a single line in the data file. */
    private static String toFileLine(Task task) {
        String done = task.isDone ? "1" : "0";
        if (task instanceof Todo) {
            return "T | " + done + " | " + task.description;
        }
        if (task instanceof Deadline deadline) {
            return "D | " + done + " | " + deadline.description + " | " + deadline.by;
        }
        Event event = (Event) task;
        return "E | " + done + " | " + event.description + " | " + event.from + " | " + event.to;
    }
}
