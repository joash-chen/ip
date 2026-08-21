import java.util.Scanner;

public class Chai {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String banner = " ▄████▄   ██░ ██  ▄▄▄      ██▓\n" +
                "▒██▀ ▀█  ▓██░ ██▒▒████▄        \n" +
                "▒▓█    ▄ ▒██▀▀██░▒██  ▀█▄  ▒██░\n" +
                "▒▓▓▄ ▄██▒░▓█ ░██ ░██▄▄▄▄██ ▒██░\n" +
                "▒ ▓███▀ ░░▓█▒░██▓ ▓█   ▓██▒░██░\n" +
                "░ ░▒ ▒  ░ ▒ ░░▒░▒ ▒▒   ▓▒█░░ ▒░";

        System.out.println("____________________________________________________________\n");
        System.out.println(banner + '\n');
        System.out.println("Hello! I'm Chai.");
        System.out.println("What can I do for you?");
        System.out.println("____________________________________________________________");

        while (true) {
            String command = scanner.nextLine();

            System.out.println("____________________________________________________________");

            if (command.equals("bye")) {
                break;
            }

            System.out.println(command);
            System.out.println("____________________________________________________________");
        }

        System.out.println("Bye. Hope to see you again soon!");
        System.out.println("____________________________________________________________");
    }
}