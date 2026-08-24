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
        Task[] tasks = new Task[100];
        int taskCount = 0;

        System.out.println(introduction);
        while (true) {
            String command = scanner.nextLine();

            System.out.println(separator);

            if (command.equals("bye")) {
                break;
            } else if (command.equals("list")) {
                for (int i = 0; i < taskCount; i++) {
                    System.out.println((i + 1) + ". " + tasks[i]);
                }
            } else if (command.equals("mark") || command.startsWith("mark ")) {
                String[] parts = command.split("\\s+"); // used gemini for regex
                String output;

                if (parts.length != 2) {
                    output = "Please use the format: mark <task number>";
                } else {
                    try {
                        int taskNumber = Integer.parseInt(parts[1]);

                        if (taskNumber < 1 || taskNumber > taskCount) {
                            output = "That task number does not exist. Please choose a number from 1 to "
                                    + taskCount + ".";
                        } else {
                            tasks[taskNumber - 1].markAsDone();
                            output = "Marked task " + taskNumber + " as done:\n  " +
                                    tasks[taskNumber - 1];
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

                        if (taskNumber < 1 || taskNumber > taskCount) {
                            output = "That task number does not exist. Please choose a number from 1 to "
                                    + taskCount + ".";
                        } else {
                            tasks[taskNumber - 1].markAsUndone();
                            output = "Marked task " + taskNumber + " as not done:\n  "
                                    + tasks[taskNumber - 1];
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
                    taskCount = addTask(tasks, taskCount, new Todo(description));
                } catch (ChaiException e) {
                    System.out.println("OOPS!!! " + e.getMessage());
                }
            } else if (command.startsWith("deadline ")) {
                taskCount = addDeadline(tasks, taskCount, command);
            } else if (command.startsWith("event ")) {
                taskCount = addEvent(tasks, taskCount, command);
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
    private static int addTask(Task[] tasks, int taskCount, Task task) {
        if (taskCount >= tasks.length) {
            System.out.println("The task list is full");
            return taskCount;
        }

        tasks[taskCount] = task;
        taskCount++;
        System.out.println("Got it. I've added this task:\n  " + task);
        System.out.println("Now you have " + taskCount + " tasks in the list.");
        return taskCount;
    }

    /** Parses and adds a deadline command in the form {@code deadline <description> /by <time>}. */
    private static int addDeadline(Task[] tasks, int taskCount, String command) {
        String body = command.substring("deadline ".length());
        int marker = body.indexOf(" /by ");
        if (marker < 0) {
            System.out.println("Please use the format: deadline <description> /by <date or time>");
            return taskCount;
        }

        String description = body.substring(0, marker).trim();
        String by = body.substring(marker + " /by ".length()).trim();
        if (description.isEmpty() || by.isEmpty()) {
            System.out.println("Please provide both a description and a deadline.");
            return taskCount;
        }
        return addTask(tasks, taskCount, new Deadline(description, by));
    }

    /** Parses and adds an event command in the form {@code event <description> /from <start> /to <end>}. */
    private static int addEvent(Task[] tasks, int taskCount, String command) {
        String body = command.substring("event ".length());
        int fromMarker = body.indexOf(" /from ");
        int toMarker = body.indexOf(" /to ", fromMarker + 1);
        if (fromMarker < 0 || toMarker < 0) {
            System.out.println("Please use the format: event <description> /from <start> /to <end>");
            return taskCount;
        }

        String description = body.substring(0, fromMarker).trim();
        String from = body.substring(fromMarker + " /from ".length(), toMarker).trim();
        String to = body.substring(toMarker + " /to ".length()).trim();
        if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
            System.out.println("Please provide a description, start, and end time.");
            return taskCount;
        }
        return addTask(tasks, taskCount, new Event(description, from, to));
    }
}
