package app.cli;

import java.util.*;

import app.IApp;

/**
 * A command line interface to this database application
 */
public class CommandLineInterface {

    /** Constant prompt for user input */
    private final static String PROMPT = "pdm320_11 interface> ";

    /** Error message for unrecognized command */
    private final static String ERR_MESSAGE = "Unrecognized command, please try again";

    /** Exit message for when exit command is called */
    private final static String EXIT_MESSAGE = "Exiting command line interface";

    /** Command to exit the application */
    private final static String EXIT = "EXIT";

    /** List of all possible commands */
    private final static List<String> commands = new ArrayList<>(List.of(EXIT));

    /** The app this interface communicates with */
    private IApp app;
    public CommandLineInterface(String username, String password){
        this.app = IApp.createApp(username, password);
    }

    /**
     * Primary method to launch this CLI, takes no arguments and returns nothing.
     * When the exit command is given it closes this class's resources and calls
     * .exit() on the database app.
     */
    public void launch(){
        Scanner in = new Scanner(System.in);
        boolean exit = false;

        while(!exit){
            System.out.print(PROMPT);
            String input = in.nextLine();
            String[] tokens = input.strip().split("\\s+");

            switch(tokens[0].toUpperCase()){
                case EXIT -> {
                    System.out.println(EXIT_MESSAGE);
                    exit = true;
                }//add more cases
                default -> {
                    System.out.println(ERR_MESSAGE);
                }
            }
        }

        in.close();
        app.exit();
    }
}
