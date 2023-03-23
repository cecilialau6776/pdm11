package app.cli;

import java.sql.Array;
import java.sql.Time;
import java.util.*;

import app.IApp;
import app.model.Collection;
import app.model.Game;
import app.model.Platform;
import app.model.User;

/**
 * A command line interface to this database application
 */
public class CommandLineInterface{

    /** Constant prompt for user input */
    private final static String PROMPT = "pdm320_11 interface> ";

    /** Error message for unrecognized command */
    private final static String ERR_MESSAGE = "Unrecognized command, please try again";

    /** Exit message for when exit command is called */
    private final static String EXIT_MESSAGE = "Exiting command line interface";

    /** Command to exit the application */
    private final static String EXIT = "EXIT";

    /** Command to log in as a user */
    private final static String LOGIN = "LOGIN";

    /** Command to log out as a user */
    private final static String LOGOUT = "LOGOUT";

    /** Command to sign up as a user */
    private final static String SIGNUP = "SIGNUP";

    /** Command to list all of user's collections */
    private final static String GET_COLLECTIONS = "GET_COLLECTIONS";

    /** Command to add a game to a user's collection */
    private final static String COLLECTION_ADD = "COLLECTION_ADD";

    /** Command to remove a game from a user's collection */
    private final static String COLLECTION_REMOVE = "COLLECTION_REMOVE";

    /** Command to delete a user's collection */
    private final static String COLLECTION_DELETE = "COLLECTION_DELETE";

    /** Command to rename a user's collection */
    private final static String COLLECTION_RENAME = "COLLECTION_RENAME";

    /** Command to create a new collection */
    private final static String COLLECTION_CREATE = "COLLECTION_CREATE";

    /** Declares current user */
    private User user;

    /** Command to rate a game */
    private final static String RATE = "RATE";

    /** The app this CLI communicates with */
    private IApp app;

    private void printCollection(Collection collection) {
        System.out.println("Name:\t" + collection.coll_name());
        System.out.println("# of games\t" + collection.games().length);
        System.out.println("Play time:\t" + app.total_playtime_collection(collection));
    }

    private void get_collections() {
        Collection[] collections = app.get_collections();
        if(collections.length == 0) {
            System.out.println("You do not have any collections.");
            return;
        }
        for(int i = 0; i < collections.length; ++i){
            for(int j = i+1; j < collections.length; ++j){
                Collection first = collections[i];
                Collection second = collections[j];
                if(first.coll_name().compareTo(second.coll_name()) > 0) {
                    collections[i] = second;
                    collections[j] = first;
                }
            }
        }
        for (Collection collection : collections) {
            printCollection(collection);
        }
        System.out.println();
    }

    private void collection_add(String coll_name, String game_name) {
        Scanner in = new Scanner(System.in);
        String input;

        Collection[] collections_list = app.get_collection_name(coll_name);

        if(collections_list.length == 0) {
            System.out.println("You do not have a collection with that name.");
            return;
        }
        Collection selected_coll = collections_list[0];

        if(collections_list.length > 1) {
            System.out.println("Which collection would you like to add the game to (enter the number)?");
            for(int i = 0; i < collections_list.length; ++i) {
                Collection curr_coll = collections_list[i];
                System.out.println(i+1);
                printCollection(curr_coll);
            }
            int input_to_int;
            do {
                input = in.nextLine();
                input_to_int = Integer.parseInt(input);
                if(input_to_int > 0 && input_to_int < collections_list.length) {
                    break;
                }
                System.out.println("Invalid collection number. Try again.");
            } while (true);

            selected_coll = collections_list[input_to_int-1];
        }
        Game[] game_list = app.search_game_name(game_name);

        if(game_list.length == 0) {
            System.out.println("There is not a game with that name.");
            return;
        }

        Game selected_game = game_list[0];
        if(game_list.length > 1) {
            System.out.println("Which game would you like to add (enter the number)?");
            for(int i = 0; i < game_list.length; ++i) {
                Game curr_game = game_list[i];
                System.out.println(i+1 + ".\tName: " + curr_game.title() + "\n\tPlay time: " +
                        curr_game.playtime());
            }

            int input_to_int;
            do {
                input = in.nextLine();
                input_to_int = Integer.parseInt(input);
                if(input_to_int > 0 && input_to_int < game_list.length) {
                    break;
                }
                System.out.println("Invalid game number. Try again.");
            } while (true);

            selected_game = game_list[input_to_int-1];
        }

        Platform[] user_platforms = app.get_platforms();

        Platform[] game_platforms = app.get_game_platforms(selected_game);

        boolean platform_match = false;

        for (Platform user_platform : user_platforms) {
            for (Platform game_platform : game_platforms) {
                if (user_platform.name().equals(game_platform.name())) {
                    platform_match = true;
                    break;
                }
            }
        }

        if(!platform_match) {
            System.out.println("*WARNING*\nYou do not own the platform this game is on. Proceed (y/n)?");
            input = in.nextLine();

            if(input.toLowerCase().charAt(0) == 'n') {
                return;
            }
        }

        Collection updated_coll  = app.collection_add(selected_coll, selected_game);
        System.out.println("Updated collection:");
        printCollection(updated_coll);
    }

    private void collection_remove(String coll_name, String game_name) {
        Scanner in = new Scanner(System.in);
        String input;
        Collection[] collections_list_init = app.get_collections();
        Collection[] collections_list = app.get_collection_name(coll_name);

        if(collections_list.length == 0) {
            System.out.println("You do not have a collection with that name.");
            return;
        }
        Collection selected_coll = collections_list[0];

        if(collections_list.length > 1) {
            System.out.println("Which collection would you like to remove the game from (enter the number)?");
            for(int i = 0; i < collections_list.length; ++i) {
                Collection curr_coll = collections_list[i];
                System.out.println(i+1);
                printCollection(curr_coll);
            }

            int input_to_int;
            do {
                input = in.nextLine();
                input_to_int = Integer.parseInt(input);
                if(input_to_int > 0 && input_to_int < collections_list.length) {
                    break;
                }
                System.out.println("Invalid collection number. Try again.");
            } while (true);

            selected_coll = collections_list[input_to_int-1];
        }

        Game[] game_list_init = selected_coll.games();

        ArrayList<Game> game_list = new ArrayList<>();

        for (Game game : game_list_init) {
            if (game.title().equals(game_name)) {
                game_list.add(game);
            }
        }

        if(game_list.size() == 0) {
            System.out.println("There is not a game with that name in your collection.");
            return;
        }

        Game selected_game = game_list.get(0);
        if(game_list.size() > 1) {
            System.out.println("Which game would you like to remove (enter the number)?");
            for(int i = 0; i < game_list.size(); ++i) {
                Game curr_game = game_list.get(i);
                System.out.println(i+1 + ".\tName: " + curr_game.title() + "\n\tPlay time: " +
                        curr_game.playtime());
            }

            int input_to_int;
            do {
                input = in.nextLine();
                input_to_int = Integer.parseInt(input);
                if(input_to_int > 0 && input_to_int < game_list.size()) {
                    break;
                }
                System.out.println("Invalid game number. Try again.");
            } while (true);

            selected_game = game_list.get(input_to_int-1);
        }

        Collection updated_coll  = app.collection_remove(selected_coll, selected_game);
        System.out.println("Updated collection:");
        printCollection(updated_coll);
    }

    private void collection_delete(String coll_name) {
        Scanner in = new Scanner(System.in);
        String input;
        Collection[] collections_list = app.get_collection_name(coll_name);

        if(collections_list.length == 0) {
            System.out.println("You do not have a collection with that name.");
            return;
        }
        Collection selected_coll = collections_list[0];

        if(collections_list.length > 1) {
            System.out.println("Which collection would you like to delete (enter the number)?");
            for(int i = 0; i < collections_list.length; ++i) {
                Collection curr_coll = collections_list[i];
                System.out.println(i+1);
                printCollection(curr_coll);
            }

            int input_to_int;
            do {
                input = in.nextLine();
                input_to_int = Integer.parseInt(input);
                if(input_to_int > 0 && input_to_int < collections_list.length) {
                    break;
                }
                System.out.println("Invalid collection number. Try again.");
            } while (true);

            selected_coll = collections_list[input_to_int-1];
        }

        app.collection_delete(selected_coll);
        System.out.println("Collection deleted.");
    }

    private void collection_rename(String old_coll_name, String new_coll_name) {
        Scanner in = new Scanner(System.in);
        String input;
        Collection[] collections_list = app.get_collection_name(old_coll_name);

        if(collections_list.length == 0) {
            System.out.println("You do not have a collection with that name.");
            return;
        }
        Collection selected_coll = collections_list[0];

        if(collections_list.length > 1) {
            System.out.println("Which collection would you like to rename (enter the number)?");
            for(int i = 0; i < collections_list.length; ++i) {
                Collection curr_coll = collections_list[i];
                System.out.println(i+1);
                printCollection(curr_coll);
            }

            int input_to_int;
            do {
                input = in.nextLine();
                input_to_int = Integer.parseInt(input);
                if(input_to_int > 0 && input_to_int < collections_list.length) {
                    break;
                }
                System.out.println("Invalid collection number. Try again.");
            } while (true);

            selected_coll = collections_list[input_to_int-1];
        }

        Collection renamed_coll = app.collection_rename(selected_coll, new_coll_name);

        System.out.println("Renamed collection:");
        printCollection(renamed_coll);
    }

    private void collection_create(String coll_name) {
        Collection new_coll = app.collection_create(coll_name);

        System.out.println("New collection:");
        printCollection(new_coll);
    }

    private void rate(String coll_name, String rating) {
        Scanner in = new Scanner(System.in);
        String input;
        Game[] game_list = app.search_game_name(coll_name);

        if(game_list.length == 0) {
            System.out.println("There is not a game with that name.");
            return;
        }

        Game selected_game = game_list[0];
        if(game_list.length > 1) {
            System.out.println("Which game would you like to rate (enter the number)?");
            for(int i = 0; i < game_list.length; ++i) {
                Game curr_game = game_list[i];
                System.out.println(i+1 + ".\tName: " + curr_game.title() + "\n\tPlay time: " +
                        curr_game.playtime());
            }

            int input_to_int;
            do {
                input = in.nextLine();
                input_to_int = Integer.parseInt(input);
                if(input_to_int > 0 && input_to_int < game_list.length) {
                    break;
                }
                System.out.println("Invalid game number. Try again.");
            } while (true);

            selected_game = game_list[input_to_int-1];
        }

        app.rate(selected_game, Integer.parseInt(rating));

        System.out.println("Successfully submitted rating.");
    }

    /**
     * Constructs a CLI by creating the application it uses as a backend
     * @param cs_username Credentials for logging into starbug server
     * @param cs_password ...
     */
    public CommandLineInterface(String cs_username, String cs_password){
        this.app = IApp.createApp(cs_username, cs_password);
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
                }
                case LOGIN -> {
                    this.user = app.logIn(tokens[1], tokens[2]);
                    System.out.println(this.user);
                }
                case LOGOUT -> {
                    app.logOut();
                }
                case SIGNUP -> {
                    this.user = app.signUp(tokens[1], tokens[2], tokens[3], tokens[4], tokens[5]);
                    System.out.println(this.user);
                }

                case GET_COLLECTIONS -> {
                    get_collections();
                }

                case COLLECTION_ADD -> {
                    collection_add(tokens[1], tokens[2]);
                }

                case COLLECTION_REMOVE -> {
                    collection_remove(tokens[1], tokens[2]);
                }

                case COLLECTION_DELETE -> {
                    collection_delete(tokens[1]);
                }

                case COLLECTION_RENAME -> {
                    collection_rename(tokens[1], tokens[2]);
                }

                case COLLECTION_CREATE -> {
                    collection_create(tokens[1]);
                }

                case RATE -> {
                    rate(tokens[1], tokens[2]);
                }//add more cases

                default -> {
                    System.out.println(ERR_MESSAGE);
                }
            }
        }

        in.close();
        app.exit(0);//exit successfully
    }
}
