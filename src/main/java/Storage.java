import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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

        List<String> lines;
        try {
            lines = Files.readAllLines(DATA_FILE);
        } catch (IOException e) {
            throw new ChaiException("I could not load your saved tasks: " + e.getMessage());
        }

        ArrayList<Task> tasks = new ArrayList<>();
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            if (line.isBlank()) {
                continue;
            }
            try {
                tasks.add(fromFileLine(line));
            } catch (ChaiException e) {
                throw new ChaiException("Saved data is invalid on line " + (index + 1) + ": " + e.getMessage());
            }
        }
        return tasks;
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
    private static Task fromFileLine(String line) throws ChaiException {
        String[] parts = line.split(" \\| ", -1);
        if (parts.length < 2 || (!parts[1].equals("0") && !parts[1].equals("1"))) {
            throw new ChaiException("the completion status must be 0 or 1.");
        }

        Task task;
        switch (parts[0]) {
        case "T":
            requireFieldCount(parts, 3, "todo");
            task = new Todo(parts[2]);
            break;
        case "D":
            requireFieldCount(parts, 4, "deadline");
            task = new Deadline(parts[2], parseDate(parts[3]));
            break;
        case "E":
            requireFieldCount(parts, 5, "event");
            LocalDate from = parseDate(parts[3]);
            LocalDate to = parseDate(parts[4]);
            if (to.isBefore(from)) {
                throw new ChaiException("the event end date cannot be before its start date.");
            }
            task = new Event(parts[2], from, to);
            break;
        default:
            throw new ChaiException("the task type must be T, D, or E.");
        }

        if (parts[1].equals("1")) {
            task.markAsDone();
        }
        return task;
    }

    /** Checks that a stored task line contains the expected number of fields. */
    private static void requireFieldCount(String[] parts, int expectedCount, String taskType) throws ChaiException {
        if (parts.length != expectedCount) {
            throw new ChaiException("the " + taskType + " entry has the wrong number of fields.");
        }
    }

    /** Parses an ISO date stored in the human-editable data file. */
    private static LocalDate parseDate(String value) throws ChaiException {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new ChaiException("the date must use yyyy-MM-dd.");
        }
    }
}
