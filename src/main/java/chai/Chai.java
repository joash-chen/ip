package chai;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import chai.storage.Storage;
import chai.task.Deadline;
import chai.task.Event;
import chai.task.Task;
import chai.task.Todo;
import chai.ui.Ui;

/**
 * Runs the Chai task manager and coordinates commands, task operations,
 * and persistent storage.
 */
public class Chai {
    /** Chai's task list for the current session. */
    private final ArrayList<Task> tasks;

    /** An error encountered while loading saved tasks, or an empty string. */
    private final String startupError;

    /** Loads Chai's persisted tasks and prepares a new session. */
    public Chai() {
        ArrayList<Task> loadedTasks;
        String loadError = "";
        try {
            loadedTasks = Storage.load();
        } catch (ChaiException e) {
            loadedTasks = new ArrayList<>();
            loadError = formatError(e.getMessage());
        }
        tasks = loadedTasks;
        startupError = loadError;
    }

    /**
     * Starts Chai's command-line interface.
     *
     * @param args Command-line arguments, which Chai does not use.
     */
    public static void main(String[] args) {
        new Ui().run(new Chai());
    }

    /**
     * Returns the greeting shown when a user starts Chai.
     *
     * @return Greeting, preceded by any saved-data error encountered at startup.
     */
    public String getWelcomeMessage() {
        String greeting = "Hey, I'm Chai :)\nWhat do you need?";
        return startupError.isEmpty() ? greeting : startupError + "\n" + greeting;
    }

    /**
     * Processes one command and returns Chai's response.
     *
     * @param command User command to process.
     * @return Response suitable for either the console or graphical interface.
     */
    public String getResponse(String command) {
        String normalizedCommand = command.trim();
        String keyword = normalizedCommand.split("\\s+", 2)[0];
        CommandType commandType = CommandType.fromKeyword(keyword);

        try {
            switch (commandType) {
                case BYE:
                    return "See you soon!";
                case LIST:
                    return formatTaskList(tasks, "Here are the tasks in your list:");
                case MARK:
                    return setTaskCompletion(normalizedCommand, true);
                case UNMARK:
                    return setTaskCompletion(normalizedCommand, false);
                case TODO:
                    return addTodo(normalizedCommand);
                case DEADLINE:
                    return addDeadline(normalizedCommand);
                case EVENT:
                    return addEvent(normalizedCommand);
                case DELETE:
                    return deleteTask(normalizedCommand);
                case FIND:
                    return findTasks(normalizedCommand);
                case UNKNOWN:
                default:
                    throw new ChaiException(
                            "I don't know how to handle that command. Try: todo <description>");
            }
        } catch (ChaiException e) {
            return formatError(e.getMessage());
        }
    }

    /** Adds a todo parsed from its command text. */
    private String addTodo(String command) throws ChaiException {
        String description = command.substring("todo".length()).trim();
        if (description.isEmpty()) {
            throw new ChaiException("A todo needs a description. Try: todo <description>");
        }
        return addTask(new Todo(description));
    }

    /** Parses and adds a deadline command in the form {@code deadline <description> /by <yyyy-MM-dd>}. */
    private String addDeadline(String command) throws ChaiException {
        if (!command.startsWith("deadline ")) {
            throw new ChaiException("Use: deadline <description> /by <yyyy-MM-dd>");
        }

        String body = command.substring("deadline ".length());
        int marker = body.indexOf(" /by ");
        if (marker < 0) {
            throw new ChaiException("Use: deadline <description> /by <yyyy-MM-dd>");
        }

        String description = body.substring(0, marker).trim();
        String byText = body.substring(marker + " /by ".length()).trim();
        if (description.isEmpty() || byText.isEmpty()) {
            throw new ChaiException("A deadline needs both a description and a date.");
        }

        LocalDate by;
        try {
            by = LocalDate.parse(byText);
        } catch (DateTimeParseException e) {
            throw new ChaiException("The deadline date must use yyyy-MM-dd, for example 2019-12-02.");
        }
        return addTask(new Deadline(description, by));
    }

    /** Parses and adds an event command containing ISO start and end dates. */
    private String addEvent(String command) throws ChaiException {
        if (!command.startsWith("event ")) {
            throw new ChaiException("Use: event <description> /from <yyyy-MM-dd> /to <yyyy-MM-dd>");
        }

        String body = command.substring("event ".length());
        int fromMarker = body.indexOf(" /from ");
        int toMarker = body.indexOf(" /to ", fromMarker + 1);
        if (fromMarker < 0 || toMarker < 0) {
            throw new ChaiException("Use: event <description> /from <yyyy-MM-dd> /to <yyyy-MM-dd>");
        }

        String description = body.substring(0, fromMarker).trim();
        String fromText = body.substring(fromMarker + " /from ".length(), toMarker).trim();
        String toText = body.substring(toMarker + " /to ".length()).trim();
        if (description.isEmpty() || fromText.isEmpty() || toText.isEmpty()) {
            throw new ChaiException("An event needs a description, start date, and end date.");
        }

        LocalDate from;
        LocalDate to;
        try {
            from = LocalDate.parse(fromText);
            to = LocalDate.parse(toText);
        } catch (DateTimeParseException e) {
            throw new ChaiException("Event dates must use yyyy-MM-dd, for example 2019-12-02.");
        }
        if (to.isBefore(from)) {
            throw new ChaiException("The event end date cannot be before its start date.");
        }
        return addTask(new Event(description, from, to));
    }

    /** Marks or unmarks the numbered task. */
    private String setTaskCompletion(String command, boolean isDone) throws ChaiException {
        String action = isDone ? "mark" : "unmark";
        int taskIndex = parseTaskIndex(command, action);
        Task task = tasks.get(taskIndex);
        if (isDone) {
            task.markAsDone();
        } else {
            task.markAsUndone();
        }
        Storage.save(tasks);

        String state = isDone ? "done" : "not done";
        return "Marked task " + (taskIndex + 1) + " as " + state + ":\n  " + task;
    }

    /** Parses and removes a task in the form {@code delete <task number>}. */
    private String deleteTask(String command) throws ChaiException {
        int taskIndex = parseTaskIndex(command, "delete");
        Task removed = tasks.remove(taskIndex);
        Storage.save(tasks);
        return "Noted. I've removed this task:\n  " + removed
                + "\nNow you have " + tasks.size() + " tasks in the list.";
    }

    /** Finds tasks whose descriptions contain the command's keyword. */
    private String findTasks(String command) throws ChaiException {
        String keyword = command.substring("find".length()).trim();
        if (keyword.isEmpty()) {
            throw new ChaiException("Please use the format: find <keyword>");
        }

        List<Task> matchingTasks = tasks.stream().
                filter(task -> task.containsKeyword(keyword)).
                toList();
        return formatTaskList(matchingTasks, "Here are the matching tasks in your list:");
    }

    /** Adds and saves a task, then returns the standard confirmation. */
    private String addTask(Task task) throws ChaiException {
        tasks.add(task);
        Storage.save(tasks);
        return "Got it. I've added this task:\n  " + task
                + "\nNow you have " + tasks.size() + " tasks in the list.";
    }

    /** Parses a one-based task number and converts it to a valid list index. */
    private int parseTaskIndex(String command, String action) throws ChaiException {
        String[] parts = command.split("\\s+");
        if (parts.length != 2) {
            throw new ChaiException("Please use the format: " + action + " <task number>");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new ChaiException("The task number must be a whole number.");
        }

        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new ChaiException("That task number does not exist. Please choose a number from 1 to "
                    + tasks.size() + ".");
        }
        return taskNumber - 1;
    }

    /** Formats a numbered task list with the given heading. */
    private String formatTaskList(List<Task> taskList, String heading) {
        if (taskList.isEmpty()) {
            return heading + "\n  (none)";
        }

        StringBuilder output = new StringBuilder(heading);
        for (int index = 0; index < taskList.size(); index++) {
            output.append("\n").append(index + 1).append(". ").append(taskList.get(index));
        }
        return output.toString();
    }

    /** Adds Chai's standard prefix to an error explanation. */
    private static String formatError(String message) {
        return "OOPS!!! " + message;
    }
}
