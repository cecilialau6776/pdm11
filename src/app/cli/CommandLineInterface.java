package app.cli;

import java.sql.Array;
import java.sql.Time;
import java.util.*;

import app.IApp;
import app.model.Collection;
import app.model.Game;
import app.model.User;

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

    /** Command to delete a user's collection */
    private final static String COLLECTION_DELETE = "COLLECTION_DELETE";

    /** Command to rename a user's collection */
    private final static String COLLECTION_RENAME = "COLLECTION_RENAME";

    /** Command to create a new collection */
    private final static String COLLECTION_CREATE = "COLLECTION_CREATE";

    /** Command to rate a game */
    private final static String RATE = "RATE";

    /** The app this CLI communicates with */
    private IApp app;

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
                    User user = app.logIn(tokens[1], tokens[2]);
                    System.out.println(user);
                }
                case LOGOUT -> {
                    app.logOut();
                }
                case SIGNUP -> {
                    User user = app.signUp(tokens[1], tokens[2], tokens[3], tokens[4], tokens[5]);
                    System.out.println(user);
                }

                case GET_COLLECTIONS -> {
                    Collection[] collections = app.get_collections();
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
                        System.out.println("Name:\t" + collection.coll_name());
                        System.out.println("# of games\t" + collection.games().length);
                        System.out.println("Play time:\t" + collection.total_playtime());
                    }
                    System.out.println();
                }

                case COLLECTION_ADD -> {
                    Collection[] collections_list_init = app.get_collections();
                    ArrayList<Collection> collections_list = new ArrayList<>();
                    for (Collection collection : collections_list_init) {
                        if (collection.coll_name().equals(tokens[1])) {
                            collections_list.add(collection);
                        }
                    }
                    if(collections_list.size() == 0) {
                        System.out.println("You do not have a collection with that name.");
                        continue;
                    }
                    Collection selected_coll = collections_list.get(0);

                    if(collections_list.size() > 1) {
                        System.out.println("Which collection would you like to add the game to (enter the number)?");
                        for(int i = 0; i < collections_list.size(); ++i) {
                            Collection curr_coll = collections_list.get(i);
                            System.out.println(i+1 + ".\tName: " + curr_coll.coll_name() + "\n\t# of games: " +
                                    curr_coll.games().length + "\n\tPlay time: " + curr_coll.total_playtime() + "\n");
                        }
                        input = in.nextLine();
                        int input_to_int = Integer.parseInt(input);
                        selected_coll = collections_list.get(input_to_int-1);
                    }
                    Game[] game_list = app.search_game_name(tokens[2]);

                    if(game_list.length == 0) {
                        System.out.println("There is not a game with that name.");
                        continue;
                    }

                    Game selected_game = game_list[0];
                    if(game_list.length > 1) {
                        System.out.println("Which game would you like to add (enter the number)?");
                        for(int i = 0; i < game_list.length; ++i) {
                            Game curr_game = game_list[i];
                            System.out.println(i+1 + ".\tName: " + curr_game.title() + "\n\tPlay time: " +
                                    curr_game.playtime() + "\n\tPlatform: " + curr_game.platform());
                        }
                        input = in.nextLine();
                        int input_to_int = Integer.parseInt(input);
                        selected_game = game_list[input_to_int-1];
                    }

                    Collection updated_coll  = app.collection_add(selected_coll, selected_game);
                    System.out.println("Updated collection:");
                    System.out.println("Name:\t" + updated_coll.coll_name());
                    System.out.println("# of games\t" + updated_coll.games().length);
                    System.out.println("Play time:\t" + updated_coll.total_playtime());
                }

                case COLLECTION_DELETE -> {
                    Collection[] collections_list_init = app.get_collections();
                    ArrayList<Collection> collections_list = new ArrayList<>();
                    for (Collection collection : collections_list_init) {
                        if (collection.coll_name().equals(tokens[1])) {
                            collections_list.add(collection);
                        }
                    }
                    if(collections_list.size() == 0) {
                        System.out.println("You do not have a collection with that name.");
                        continue;
                    }
                    Collection selected_coll = collections_list.get(0);

                    if(collections_list.size() > 1) {
                        System.out.println("Which collection would you like to delete (enter the number)?");
                        for(int i = 0; i < collections_list.size(); ++i) {
                            Collection curr_coll = collections_list.get(i);
                            System.out.println(i+1 + ".\tName: " + curr_coll.coll_name() + "\n\t# of games: " +
                                    curr_coll.games().length + "\n\tPlay time: " + curr_coll.total_playtime() + "\n");
                        }
                        input = in.nextLine();
                        int input_to_int = Integer.parseInt(input);
                        selected_coll = collections_list.get(input_to_int-1);
                    }

                    app.collection_delete(selected_coll);
                    System.out.println("Collection deleted.");
                }

                case COLLECTION_RENAME -> {
                    Collection[] collections_list_init = app.get_collections();
                    ArrayList<Collection> collections_list = new ArrayList<>();
                    for (Collection collection : collections_list_init) {
                        if (collection.coll_name().equals(tokens[1])) {
                            collections_list.add(collection);
                        }
                    }
                    if(collections_list.size() == 0) {
                        System.out.println("You do not have a collection with that name.");
                        continue;
                    }
                    Collection selected_coll = collections_list.get(0);

                    if(collections_list.size() > 1) {
                        System.out.println("Which collection would you like to rename (enter the number)?");
                        for(int i = 0; i < collections_list.size(); ++i) {
                            Collection curr_coll = collections_list.get(i);
                            System.out.println(i+1 + ".\tName: " + curr_coll.coll_name() + "\n\t# of games: " +
                                    curr_coll.games().length + "\n\tPlay time: " + curr_coll.total_playtime() + "\n");
                        }
                        input = in.nextLine();
                        int input_to_int = Integer.parseInt(input);
                        selected_coll = collections_list.get(input_to_int-1);
                    }

                    Collection renamed_coll = app.collection_rename(selected_coll, tokens[2]);

                    System.out.println("Renamed collection:");
                    System.out.println("Name:\t" + renamed_coll.coll_name());
                    System.out.println("# of games\t" + renamed_coll.games().length);
                    System.out.println("Play time:\t" + renamed_coll.total_playtime());
                }

                case COLLECTION_CREATE -> {
                    Collection new_coll = app.collection_create(tokens[1]);

                    System.out.println("New collection:");
                    System.out.println("Name:\t" + new_coll.coll_name());
                    System.out.println("# of games\t" + new_coll.games().length);
                    System.out.println("Play time:\t" + new_coll.total_playtime());
                }

                case RATE -> {
                    Game[] game_list = app.search_game_name(tokens[1]);

                    if(game_list.length == 0) {
                        System.out.println("There is not a game with that name.");
                        continue;
                    }

                    Game selected_game = game_list[0];
                    if(game_list.length > 1) {
                        System.out.println("Which game would you like to rate (enter the number)?");
                        for(int i = 0; i < game_list.length; ++i) {
                            Game curr_game = game_list[i];
                            System.out.println(i+1 + ".\tName: " + curr_game.title() + "\n\tPlay time: " +
                                    curr_game.playtime() + "\n\tPlatform: " + curr_game.platform());
                        }
                        input = in.nextLine();
                        int input_to_int = Integer.parseInt(input);
                        selected_game = game_list[input_to_int-1];
                    }

                    Game updated_game = app.rate(selected_game, Integer.parseInt(tokens[2]));

                    System.out.println("Successfully rated "+ tokens[2] + " stars.");

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
