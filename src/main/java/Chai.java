import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class Chai {
    public static void main(String[] args) {
        String banner = " ▄████▄   ██░ ██  ▄▄▄      ██▓\n" +
                        "▒██▀ ▀█  ▓██░ ██▒▒████▄    ▒░░ \n" +
                        "▒▓█    ▄ ▒██▀▀██░▒██  ▀█▄  ▒██░\n" +
                        "▒▓▓▄ ▄██▒░▓█ ░██ ░██▄▄▄▄██ ▒██░\n" +
                        "▒ ▓███▀ ░░▓█▒░██▓ ▓█   ▓██▒░██░\n" +
                        "░ ░▒ ▒  ░ ▒ ░░▒░▒ ▒▒   ▓▒█░░ ▒░\n"; // used codex to make the ASCII art
        String separator = "____________________________________________________________";
        String introduction = separator + '\n' + banner + '\n' + "Hey I'm Chai :)\n" + "What do you need?\n" + separator;
        String goodbye = "See you soon!\n" + separator;

        Scanner scanner = new Scanner(System.in);
        ArrayList<Task> tasks;
        try {
            tasks = Storage.load();
        } catch (ChaiException e) {
            System.out.println("OOPS!!! " + e.getMessage());
            tasks = new ArrayList<>();
        }

        System.out.println(introduction);
        boolean isRunning = true;
        while (isRunning) {
            String command = scanner.nextLine();

            System.out.println(separator);

            String keyword = command.split("\\s+", 2)[0];
            CommandType commandType = CommandType.fromKeyword(keyword);

            switch (commandType) {
            case BYE:
                isRunning = false;
                break;
            case LIST:
                for (int i = 0; i < tasks.size(); i++) {
                    System.out.println((i + 1) + ". " + tasks.get(i));
                }
                break;
            case MARK: {
                String[] parts = command.split("\\s+"); // used gemini for regex
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
                System.out.println(output);
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
                System.out.println(output);
                break;
            }
            case TODO:
                try {
                    String description = command.substring("todo".length()).trim();
                    if (description.isEmpty()) {
                        throw new ChaiException("A todo needs a description. Try: todo <description>");
                    }
                    addTask(tasks, new Todo(description));
                } catch (ChaiException e) {
                    System.out.println("OOPS!!! " + e.getMessage());
                }
                break;
            case DEADLINE:
                try {
                    if (!command.startsWith("deadline ")) {
                        throw new ChaiException("Use: deadline <description> /by <yyyy-MM-dd>");
                    }
                    addDeadline(tasks, command);
                } catch (ChaiException e) {
                    System.out.println("OOPS!!! " + e.getMessage());
                }
                break;
            case EVENT:
                try {
                    if (!command.startsWith("event ")) {
                        throw new ChaiException(
                                "Use: event <description> /from <yyyy-MM-dd> /to <yyyy-MM-dd>");
                    }
                    addEvent(tasks, command);
                } catch (ChaiException e) {
                    System.out.println("OOPS!!! " + e.getMessage());
                }
                break;
            case DELETE:
                try {
                    deleteTask(tasks, command);
                } catch (ChaiException e) {
                    System.out.println("OOPS!!! " + e.getMessage());
                }
                break;
            case UNKNOWN:
            default:
                try {
                    throw new ChaiException("I don't know how to handle that command. Try: todo <description>");
                } catch (ChaiException e) {
                    System.out.println("OOPS!!! " + e.getMessage());
                }
                break;
            }

            if (isRunning) {
                System.out.println(separator);
            }
        }
        System.out.println(goodbye);

    }

    /** Adds a task and prints the standard confirmation message. */
    private static void addTask(ArrayList<Task> tasks, Task task) throws ChaiException {
        tasks.add(task);
        Storage.save(tasks);
        System.out.println("Got it. I've added this task:\n  " + task);
        System.out.println("Now you have " + tasks.size() + " tasks in the list.");
    }

    /** Parses and adds a deadline command in the form {@code deadline <description> /by <yyyy-MM-dd>}. */
    private static void addDeadline(ArrayList<Task> tasks, String command) throws ChaiException {
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
        addTask(tasks, new Deadline(description, by));
    }

    /** Parses an event command containing ISO start and end dates, then adds the event. */
    private static void addEvent(ArrayList<Task> tasks, String command) throws ChaiException {
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
        addTask(tasks, new Event(description, from, to));
    }

    /** Parses and removes a task in the form {@code delete <task number>}. */
    private static void deleteTask(ArrayList<Task> tasks, String command) throws ChaiException {
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
        System.out.println("Noted. I've removed this task:\n  " + removed);
        System.out.println("Now you have " + tasks.size() + " tasks in the list.");
    }
}
