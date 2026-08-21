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
            } else {
                if (taskCount < tasks.length) {
                    tasks[taskCount++] = new Task(command);

                    System.out.println("added: " + command);
                } else {
                    System.out.println("The task list is full");
                }
            }

            System.out.println(separator);
        }
        System.out.println(goodbye);

    }
}
