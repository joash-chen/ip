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

    /** Loads saved tasks, or returns an empty list when Chai has not been run before. */
    public static ArrayList<Task> load() throws ChaiException {
        if (Files.notExists(DATA_FILE)) {
            return new ArrayList<>();
        }

        try {
            ArrayList<Task> tasks = new ArrayList<>();
            for (String line : Files.readAllLines(DATA_FILE)) {
                tasks.add(fromFileLine(line));
            }
            return tasks;
        } catch (IOException e) {
            throw new ChaiException("I could not load your saved tasks: " + e.getMessage());
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

    /** Recreates one task from a line previously written by {@link #toFileLine(Task)}. */
    private static Task fromFileLine(String line) {
        String[] parts = line.split(" \\| ", -1);
        Task task;
        switch (parts[0]) {
        case "T":
            task = new Todo(parts[2]);
            break;
        case "D":
            task = new Deadline(parts[2], parts[3]);
            break;
        case "E":
            task = new Event(parts[2], parts[3], parts[4]);
            break;
        default:
            throw new IllegalArgumentException("Unknown task type: " + parts[0]);
        }

        if (parts[1].equals("1")) {
            task.markAsDone();
        }
        return task;
    }
}
