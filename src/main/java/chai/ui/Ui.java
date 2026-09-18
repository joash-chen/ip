package chai.ui;

import chai.task.Task;

import java.util.List;
import java.util.Scanner;

/** Handles all console input and output for Chai. */
public class Ui {
    /** Divider used to separate Chai's responses. */
    private static final String SEPARATOR = "____________________________________________________________";

    /** Reads commands entered by the user. */
    private final Scanner scanner;

    /** Creates a console UI that reads from standard input. */
    public Ui() {
        this.scanner = new Scanner(System.in);
    }

    /** Displays Chai's greeting and banner. */
    public void showWelcome() {
        String banner = " ▄████▄   ██░ ██  ▄▄▄      ██▓\n"
                + "▒██▀ ▀█  ▓██░ ██▒▒████▄    ▒░░ \n"
                + "▒▓█    ▄ ▒██▀▀██░▒██  ▀█▄  ▒██░\n"
                + "▒▓▓▄ ▄██▒░▓█ ░██ ░██▄▄▄▄██ ▒██░\n"
                + "▒ ▓███▀ ░░▓█▒░██▓ ▓█   ▓██▒░██░\n"
                + "░ ░▒ ▒  ░ ▒ ░░▒░▒ ▒▒   ▓▒█░░ ▒░\n";
        System.out.println(SEPARATOR + '\n' + banner + '\n'
                + "Hey I'm Chai :)\nWhat do you need?\n" + SEPARATOR);
    }

    /**
     * Reads and returns the next command.
     *
     * @return Command entered by the user.
     */
    public String readCommand() {
        return scanner.nextLine();
    }

    /** Displays the response divider. */
    public void showLine() {
        System.out.println(SEPARATOR);
    }

    /** Displays Chai's farewell. */
    public void showGoodbye() {
        System.out.println("See you soon!\n" + SEPARATOR);
    }

    /**
     * Displays a normal response message.
     *
     * @param message Message to display.
     */
    public void showMessage(String message) {
        System.out.println(message);
    }

    /**
     * Displays an error in Chai's standard error style.
     *
     * @param message Error explanation to display.
     */
    public void showError(String message) {
        System.out.println("OOPS!!! " + message);
    }

    /**
     * Displays all tasks with one-based list numbers.
     *
     * @param tasks Tasks to display.
     */
    public void showTaskList(List<Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i + 1) + ". " + tasks.get(i));
        }
    }

    /**
     * Confirms that a task was added and displays the new list size.
     *
     * @param task Task that was added.
     * @param taskCount Number of tasks after the addition.
     */
    public void showTaskAdded(Task task, int taskCount) {
        System.out.println("Got it. I've added this task:\n  " + task);
        System.out.println("Now you have " + taskCount + " tasks in the list.");
    }

    /**
     * Confirms that a task was deleted and displays the new list size.
     *
     * @param task Task that was deleted.
     * @param taskCount Number of tasks after the deletion.
     */
    public void showTaskDeleted(Task task, int taskCount) {
        System.out.println("Noted. I've removed this task:\n  " + task);
        System.out.println("Now you have " + taskCount + " tasks in the list.");
    }
}
