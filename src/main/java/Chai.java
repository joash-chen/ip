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

        Scanner scanner = new Scanner(System.in);

        System.out.println(introduction);
        while (true) {
            String command = scanner.nextLine();

            System.out.println(separator);

            if (command.equals("bye")) {
                break;
            }

            System.out.println(command);
            System.out.println(separator);
        }

        System.out.println("See you soon!");
        System.out.println(separator);
    }
}