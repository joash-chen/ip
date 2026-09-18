package chai;

import chai.storage.Storage;
import chai.task.Deadline;
import chai.task.Event;
import chai.task.Task;
import chai.task.Todo;
import chai.ui.Ui;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/**
 * Runs the Chai task manager and coordinates user input, task operations,
 * and persistent storage.
 */
public class Chai {
    /** Prevents instantiation of the command-line entry-point class. */
    private Chai() {
    }

    /**
     * Starts Chai's command loop.
     *
     * @param args Command-line arguments, which Chai does not use.
     */
    public static void main(String[] args) {
        Ui ui = new Ui();
        ArrayList<Task> tasks;
        try {
            tasks = Storage.load();
        } catch (ChaiException e) {
            ui.showError(e.getMessage());
            tasks = new ArrayList<>();
        }

        ui.showWelcome();
        boolean isRunning = true;
        while (isRunning) {
            String command = ui.readCommand();

            ui.showLine();

            String keyword = command.split("\\s+", 2)[0];
            CommandType commandType = CommandType.fromKeyword(keyword);

            switch (commandType) {
                case BYE:
                    isRunning = false;
                    break;
                case LIST:
                    ui.showTaskList(tasks);
                    break;
                case MARK: {
                    String[] parts = command.split("\\s+");
                    String output;

                    if (parts.length != 2) {
                        output = "Please use the format: mark <task number>";
                    } else {
                        try {
                            int taskNumber = Integer.parseInt(parts[1]);

                            if (taskNumber < 1 || taskNumber > tasks.size()) {
                                output = "That task number does not exist. Please choose a number from 1 to "
                                        + tasks.size() + ".";
                            } else {
                                Task task = tasks.get(taskNumber - 1);
                                task.markAsDone();
                                Storage.save(tasks);
                                output = "Marked task " + taskNumber + " as done:\n  " + task;
                            }
                        } catch (NumberFormatException e) {
                            output = "The task number must be a whole number.";
                        } catch (ChaiException e) {
                            output = "OOPS!!! " + e.getMessage();
                        }
                    }
                    ui.showMessage(output);
                    break;
                }
                case UNMARK: {
                    String[] parts = command.split("\\s+");
                    String output;

                    if (parts.length != 2) {
                        output = "Please use the format: unmark <task number>";
                    } else {
                        try {
                            int taskNumber = Integer.parseInt(parts[1]);

                            if (taskNumber < 1 || taskNumber > tasks.size()) {
                                output = "That task number does not exist. Please choose a number from 1 to "
                                        + tasks.size() + ".";
                            } else {
                                Task task = tasks.get(taskNumber - 1);
                                task.markAsUndone();
                                Storage.save(tasks);
                                output = "Marked task " + taskNumber + " as not done:\n  " + task;
                            }
                        } catch (NumberFormatException e) {
                            output = "The task number must be a whole number.";
                        } catch (ChaiException e) {
                            output = "OOPS!!! " + e.getMessage();
                        }
                    }
                    ui.showMessage(output);
                    break;
                }
                case TODO:
                    try {
                        String description = command.substring("todo".length()).trim();
                        if (description.isEmpty()) {
                            throw new ChaiException("A todo needs a description. Try: todo <description>");
                        }
                        addTask(tasks, new Todo(description), ui);
                    } catch (ChaiException e) {
                        ui.showError(e.getMessage());
                    }
                    break;
                case DEADLINE:
                    try {
                        if (!command.startsWith("deadline ")) {
                            throw new ChaiException("Use: deadline <description> /by <yyyy-MM-dd>");
                        }
                        addDeadline(tasks, command, ui);
                    } catch (ChaiException e) {
                        ui.showError(e.getMessage());
                    }
                    break;
                case EVENT:
                    try {
                        if (!command.startsWith("event ")) {
                            throw new ChaiException(
                                    "Use: event <description> /from <yyyy-MM-dd> /to <yyyy-MM-dd>");
                        }
                        addEvent(tasks, command, ui);
                    } catch (ChaiException e) {
                        ui.showError(e.getMessage());
                    }
                    break;
                case DELETE:
                    try {
                        deleteTask(tasks, command, ui);
                    } catch (ChaiException e) {
                        ui.showError(e.getMessage());
                    }
                    break;
                case FIND:
                    try {
                        findTasks(tasks, command, ui);
                    } catch (ChaiException e) {
                        ui.showError(e.getMessage());
                    }
                    break;
                case UNKNOWN:
                default:
                    try {
                        throw new ChaiException(
                                "I don't know how to handle that command. Try: todo <description>");
                    } catch (ChaiException e) {
                        ui.showError(e.getMessage());
                    }
                    break;
            }

            if (isRunning) {
                ui.showLine();
            }
        }
        ui.showGoodbye();
    }

    /** Adds a task and prints the standard confirmation message. */
    private static void addTask(ArrayList<Task> tasks, Task task, Ui ui) throws ChaiException {
        tasks.add(task);
        Storage.save(tasks);
        ui.showTaskAdded(task, tasks.size());
    }

    /** Parses and adds a deadline command in the form {@code deadline <description> /by <yyyy-MM-dd>}. */
    private static void addDeadline(ArrayList<Task> tasks, String command, Ui ui) throws ChaiException {
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
        addTask(tasks, new Deadline(description, by), ui);
    }

    /** Parses an event command containing ISO start and end dates, then adds the event. */
    private static void addEvent(ArrayList<Task> tasks, String command, Ui ui) throws ChaiException {
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
        addTask(tasks, new Event(description, from, to), ui);
    }

    /** Parses and removes a task in the form {@code delete <task number>}. */
    private static void deleteTask(ArrayList<Task> tasks, String command, Ui ui) throws ChaiException {
        String[] parts = command.split("\\s+");
        if (parts.length != 2) {
            throw new ChaiException("Please use the format: delete <task number>");
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

        Task removed = tasks.remove(taskNumber - 1);
        Storage.save(tasks);
        ui.showTaskDeleted(removed, tasks.size());
    }

    /** Finds and displays tasks whose descriptions contain the command's keyword. */
    private static void findTasks(ArrayList<Task> tasks, String command, Ui ui) throws ChaiException {
        String keyword = command.substring("find".length()).trim();
        if (keyword.isEmpty()) {
            throw new ChaiException("Please use the format: find <keyword>");
        }

        ArrayList<Task> matchingTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (task.containsKeyword(keyword)) {
                matchingTasks.add(task);
            }
        }
        ui.showMatchingTasks(matchingTasks);
    }
}
