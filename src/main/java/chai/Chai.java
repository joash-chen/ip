package chai;

import java.util.ArrayList;
import java.util.List;

import chai.storage.Storage;
import chai.task.Task;
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
        CommandType commandType = Parser.parseCommandType(normalizedCommand);

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
                    return addTask(Parser.parseTodo(normalizedCommand));
                case DEADLINE:
                    return addTask(Parser.parseDeadline(normalizedCommand));
                case EVENT:
                    return addTask(Parser.parseEvent(normalizedCommand));
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

    /** Marks or unmarks the numbered task. */
    private String setTaskCompletion(String command, boolean isDone) throws ChaiException {
        int taskIndex = Parser.parseTaskIndex(command, isDone ? "mark" : "unmark", tasks.size());
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
        int taskIndex = Parser.parseTaskIndex(command, "delete", tasks.size());
        Task removed = tasks.remove(taskIndex);
        Storage.save(tasks);
        return "Noted. I've removed this task:\n  " + removed
                + "\nNow you have " + tasks.size() + " tasks in the list.";
    }

    /** Finds tasks whose descriptions contain the command's keyword. */
    private String findTasks(String command) throws ChaiException {
        String keyword = Parser.parseFindKeyword(command);

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
