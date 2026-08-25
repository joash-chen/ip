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
        ArrayList<Task> tasks = new ArrayList<>();

        System.out.println(introduction);
        while (true) {
            String command = scanner.nextLine();

            System.out.println(separator);

            if (command.equals("bye")) {
                break;
            } else if (command.equals("list")) {
                for (int i = 0; i < tasks.size(); i++) {
                    System.out.println((i + 1) + ". " + tasks.get(i));
                }
            } else if (command.equals("mark") || command.startsWith("mark ")) {
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
                            output = "Marked task " + taskNumber + " as done:\n  " + task;
                        }
                    } catch (NumberFormatException e) {
                        output = "The task number must be a whole number.";
                    }
                }
                System.out.println(output);
            } else if (command.equals("unmark") || command.startsWith("unmark ")) {
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
                            output = "Marked task " + taskNumber + " as not done:\n  " + task;
                        }
                    } catch (NumberFormatException e) {
                        output = "The task number must be a whole number.";
                    }
                }
                System.out.println(output);
            } else if (command.equals("todo") || command.startsWith("todo ")) {
                try {
                    String description = command.substring("todo".length()).trim();
                    if (description.isEmpty()) {
                        throw new ChaiException("A todo needs a description. Try: todo <description>");
                    }
                    addTask(tasks, new Todo(description));
                } catch (ChaiException e) {
                    System.out.println("OOPS!!! " + e.getMessage());
                }
            } else if (command.startsWith("deadline ")) {
                try {
                    addDeadline(tasks, command);
                } catch (ChaiException e) {
                    System.out.println("OOPS!!! " + e.getMessage());
                }
            } else if (command.startsWith("event ")) {
                try {
                    addEvent(tasks, command);
                } catch (ChaiException e) {
                    System.out.println("OOPS!!! " + e.getMessage());
                }
            } else if (command.equals("delete") || command.startsWith("delete ")) {
                try {
                    deleteTask(tasks, command);
                } catch (ChaiException e) {
                    System.out.println("OOPS!!! " + e.getMessage());
                }
            } else {
                try {
                    throw new ChaiException("I don't know how to handle that command. Try: todo <description>");
                } catch (ChaiException e) {
                    System.out.println("OOPS!!! " + e.getMessage());
                }
            }

            System.out.println(separator);
        }
        System.out.println(goodbye);

    }

    /** Adds a task and prints the standard confirmation message. */
    private static void addTask(ArrayList<Task> tasks, Task task) {
        tasks.add(task);
        System.out.println("Got it. I've added this task:\n  " + task);
        System.out.println("Now you have " + tasks.size() + " tasks in the list.");
    }

    /** Parses and adds a deadline command in the form {@code deadline <description> /by <time>}. */
    private static void addDeadline(ArrayList<Task> tasks, String command) throws ChaiException {
        String body = command.substring("deadline ".length());
        int marker = body.indexOf(" /by ");
        if (marker < 0) {
            throw new ChaiException("Use: deadline <description> /by <date or time>");
        }

        String description = body.substring(0, marker).trim();
        String by = body.substring(marker + " /by ".length()).trim();
        if (description.isEmpty() || by.isEmpty()) {
            throw new ChaiException("A deadline needs both a description and a date or time.");
        }
        addTask(tasks, new Deadline(description, by));
    }

    /** Parses and adds an event command in the form {@code event <description> /from <start> /to <end>}. */
    private static void addEvent(ArrayList<Task> tasks, String command) throws ChaiException {
        String body = command.substring("event ".length());
        int fromMarker = body.indexOf(" /from ");
        int toMarker = body.indexOf(" /to ", fromMarker + 1);
        if (fromMarker < 0 || toMarker < 0) {
            throw new ChaiException("Use: event <description> /from <start> /to <end>");
        }

        String description = body.substring(0, fromMarker).trim();
        String from = body.substring(fromMarker + " /from ".length(), toMarker).trim();
        String to = body.substring(toMarker + " /to ".length()).trim();
        if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
            throw new ChaiException("An event needs a description, start, and end time.");
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
        System.out.println("Noted. I've removed this task:\n  " + removed);
        System.out.println("Now you have " + tasks.size() + " tasks in the list.");
    }
}
