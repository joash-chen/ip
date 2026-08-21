import java.util.Scanner;

public class Chai {
    public static void main(String[] args) {
        String banner = " ▄████▄   ██░ ██  ▄▄▄      ██▓\n" +
                        "▒██▀ ▀█  ▓██░ ██▒▒████▄        \n" +
                        "▒▓█    ▄ ▒██▀▀██░▒██  ▀█▄  ▒██░\n" +
                        "▒▓▓▄ ▄██▒░▓█ ░██ ░██▄▄▄▄██ ▒██░\n" +
                        "▒ ▓███▀ ░░▓█▒░██▓ ▓█   ▓██▒░██░\n" +
                        "░ ░▒ ▒  ░ ▒ ░░▒░▒ ▒▒   ▓▒█░░ ▒░\n";
        String separator = "____________________________________________________________";
        String introduction = separator + '\n' + banner + '\n' + "Hey I'm Chai :)\n" + "What do you need?\n" + separator;
        String goodbye = "See you soon!\n" + separator;

        Scanner scanner = new Scanner(System.in);
        String[] tasks = new String[100];
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
            } else {
                if (taskCount < tasks.length) {
                    tasks[taskCount++] = command;

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