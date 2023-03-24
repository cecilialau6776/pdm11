package app.cli;

import java.sql.Array;
import java.sql.Time;
import java.sql.Date;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import java.util.ArrayList;

import app.IApp;
import app.model.Collection;
import app.model.Game;
import app.model.Platform;
import app.model.User;

/**
 * A command line interface to this database application
 */
public class CommandLineInterface {

    /** Constant prompt for user input */
    private final static String PROMPT = "pdm320_11 interface> ";

    /** Error message for unrecognized command */
    private final static String ERR_MESSAGE = "Unrecognized command, Type \"help\" to get list of commands";

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

    /** command to play a game */
    private final static String PLAY = "PLAY";

    /** command to search a friend */
    private final static String SEARCH_USER = "SEARCH_FRIEND";

    /** command to add a friend */
    private final static String ADD_FRIEND = "ADD_FRIEND";

    /** command to remove a friend */
    private final static String REMOVE_FRIEND = "REMOVE_FRIEND";

    /** command to search for a list of games */
    private final static String SEARCH_GAME = "SEARCH_GAME";
    /** command to see list of usable commands */
    private final static String HELP = "HELP";

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
                    Collection[] collections = app.get_collections();
                    if(collections.length == 0) {
                        System.out.println("You do not have any collections.");
                        continue;
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
                        System.out.println("Name:\t" + collection.coll_name());
                        System.out.println("# of games\t" + collection.games().length);
                        System.out.println("Play time:\t" + app.total_playtime_collection(collection));
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
                                    curr_coll.games().length + "\n\tPlay time: " +
                                    app.total_playtime_collection(curr_coll) + "\n");
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
                                    curr_game.playtime());
                        }
                        input = in.nextLine();
                        int input_to_int = Integer.parseInt(input);
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

                        if(input.equals("n")) {
                            continue;
                        }
                    }

                    Collection updated_coll  = app.collection_add(selected_coll, selected_game);
                    System.out.println("Updated collection:");
                    System.out.println("Name:\t" + updated_coll.coll_name());
                    System.out.println("# of games\t" + updated_coll.games().length);
                    System.out.println("Play time:\t" + app.total_playtime_collection(updated_coll));
                }

                case COLLECTION_REMOVE -> {
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
                        System.out.println("Which collection would you like to remove the game from (enter the number)?");
                        for(int i = 0; i < collections_list.size(); ++i) {
                            Collection curr_coll = collections_list.get(i);
                            System.out.println(i+1 + ".\tName: " + curr_coll.coll_name() + "\n\t# of games: " +
                                    curr_coll.games().length + "\n\tPlay time: " +
                                    app.total_playtime_collection(curr_coll) + "\n");
                        }
                        input = in.nextLine();
                        int input_to_int = Integer.parseInt(input);
                        selected_coll = collections_list.get(input_to_int-1);
                    }

                    Game[] game_list_init = selected_coll.games();

                    ArrayList<Game> game_list = new ArrayList<>();

                    for (Game game : game_list_init) {
                        if (game.title().equals(tokens[1])) {
                            game_list.add(game);
                        }
                    }

                    if(game_list.size() == 0) {
                        System.out.println("There is not a game with that name in your collection.");
                        continue;
                    }

                    Game selected_game = game_list.get(0);
                    if(game_list.size() > 1) {
                        System.out.println("Which game would you like to remove (enter the number)?");
                        for(int i = 0; i < game_list.size(); ++i) {
                            Game curr_game = game_list.get(i);
                            System.out.println(i+1 + ".\tName: " + curr_game.title() + "\n\tPlay time: " +
                                    curr_game.playtime());
                        }
                        input = in.nextLine();
                        int input_to_int = Integer.parseInt(input);
                        selected_game = game_list.get(input_to_int-1);
                    }

                    Collection updated_coll  = app.collection_remove(selected_coll, selected_game);
                    System.out.println("Updated collection:");
                    System.out.println("Name:\t" + updated_coll.coll_name());
                    System.out.println("# of games\t" + updated_coll.games().length);
                    System.out.println("Play time:\t" + app.total_playtime_collection(updated_coll));
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
                                    curr_coll.games().length + "\n\tPlay time: " +
                                    app.total_playtime_collection(curr_coll) + "\n");
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
                                    curr_coll.games().length + "\n\tPlay time: " +
                                    app.total_playtime_collection(curr_coll) + "\n");
                        }
                        input = in.nextLine();
                        int input_to_int = Integer.parseInt(input);
                        selected_coll = collections_list.get(input_to_int-1);
                    }

                    Collection renamed_coll = app.collection_rename(selected_coll, tokens[2]);

                    System.out.println("Renamed collection:");
                    System.out.println("Name:\t" + renamed_coll.coll_name());
                    System.out.println("# of games\t" + renamed_coll.games().length);
                    System.out.println("Play time:\t" + app.total_playtime_collection(renamed_coll));
                }

                case COLLECTION_CREATE -> {
                    Collection new_coll = app.collection_create(tokens[1]);

                    System.out.println("New collection:");
                    System.out.println("Name:\t" + new_coll.coll_name());
                    System.out.println("# of games\t" + new_coll.games().length);
                    System.out.println("Play time:\t" + app.total_playtime_collection(new_coll));
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
                                    curr_game.playtime());
                        }
                        input = in.nextLine();
                        int input_to_int = Integer.parseInt(input);
                        selected_game = game_list[input_to_int-1];
                    }

                    Game updated_game = app.rate(selected_game, Integer.parseInt(tokens[2]));

                    System.out.println("Successfully submitted rating.");

                }

                case PLAY -> {
                    if(tokens.length <3){
                        System.out.println("\nUsage:");
                        System.out.println("PLAY { game | time }");
                        System.out.println("game            game to be played");
                        System.out.println("time            time in milliseconds to be added\n"); //Might change later to a more useful format that milliseconds
                        continue;
                    }
                    Game[] game_list = app.search_game_name(tokens[1]);

                    if(game_list.length == 0) {
                        System.out.println("There is not a game with that name.");
                        continue;
                    }

                    Game selected_game = game_list[0];
                    if(game_list.length > 1) {
                        System.out.println("Which game would you like to play (enter the number)?");
                        for(int i = 0; i < game_list.length; ++i) {
                            Game curr_game = game_list[i];
                            System.out.println(i+1 + ".\tName: " + curr_game.title() + "\n\tPlay time: " +
                                    curr_game.playtime());
                        }
                        input = in.nextLine();
                        int input_to_int = Integer.parseInt(input);
                        selected_game = game_list[input_to_int-1];
                    }
                    Time run_time = new Time(Integer.parseInt(tokens[2]));
                    app.play(selected_game,run_time);
                }

                case SEARCH_USER -> {
                    if(tokens.length <2){
                        System.out.println("\nUsage:");
                        System.out.println("SEARCH_USER { email }");
                        System.out.println("email           email that the account is associated with to be searched\n");
                        continue;
                    }
                    User[] user_list = app.search_users(tokens[1]);

                    if(user_list.length==0) {
                        System.out.println("There no users linked to this email address");
                        continue;
                    }
                    if(user_list.length > 1) {
                        System.out.println("Here's all the Usernames associated with this email");
                        for(int i = 0; i < user_list.length; ++i) {
                            User curr_user = user_list[i];
                            System.out.println(i+1 + ".\tUsername: " + curr_user.username());
                        }
                    }
                }

                case ADD_FRIEND -> {
                    if(tokens.length <2){
                        System.out.println("\nUsage:");
                        System.out.println("ADD_FRIEND { email }");
                        System.out.println("email           email that the account is associated with to be added\n");
                        continue;
                    }
                    User[] user_list = app.search_users(tokens[1]);

                    if(user_list.length==0) {
                        System.out.println("There no users linked to this email address");
                        continue;
                    }
                    User selected_user = user_list[0];
                    if(user_list.length > 1) {
                        System.out.println("Which friend would you like to add (enter the number)?");
                        for(int i = 0; i < user_list.length; ++i) {
                            User curr_user = user_list[i];
                            System.out.println(i+1 + ".\tUsername: " + curr_user.username());
                        }
                        input = in.nextLine();
                        int input_to_int = Integer.parseInt(input);
                        selected_user = user_list[input_to_int-1];
                    }
                    app.add_friend(selected_user);
                    System.out.println(selected_user.username() + "added to your friend list");
                }

                case REMOVE_FRIEND -> {
                    if(tokens.length <2){
                        System.out.println("\nUsage:");
                        System.out.println("REMOVE_FRIEND { email }");
                        System.out.println("email           email that the account is associated with to be removed\n");
                        continue;
                    }
                    User[] user_list = app.search_friends();

                    if(user_list.length==0) {
                        System.out.println("There are no friends linked to this email address");
                        continue;
                    }

                    ArrayList<User> searched_users = new ArrayList<>();
                    for(int i = 0; i < user_list.length; i++){
                        String curr_user_email = user_list[i].email();
                        if(curr_user_email.equals(tokens[1])){
                            searched_users.add(user_list[i]);
                        }
                    }
                    if(searched_users.size()==0) {
                        System.out.println("There are no friends linked to this email address");
                        continue;
                    }
                    User selected_user = searched_users.get(0);
                    if(searched_users.size() > 1) {
                        System.out.println("Which friend would you like to remove (enter the number)?");
                        for(int i = 0; i < searched_users.size(); ++i) {
                            User curr_user = searched_users.get(i);
                            System.out.println(i+1 + ".\tUsername: " + curr_user.username());
                        }
                        input = in.nextLine();
                        int input_to_int = Integer.parseInt(input);
                        selected_user = searched_users.get(input_to_int);
                    }
                    app.delete_friend(selected_user);
                    System.out.println(selected_user.username() + "removed from your friend list");
                }

                case SEARCH_GAME -> {
                    if(tokens.length<3){
                        System.out.println("\nUsage: ");
                        System.out.println("SEARCH_GAME { search_val | search_type | sort_val | descend }");
                        System.out.println("search_val      value being searched");
                        System.out.println("search_type     type that the being search is, below are acceptable inputs");
                        System.out.println("                {title},{platform},{date},{developer},{price},{genre}");
                        System.out.println("sort_val        optional argument to sort the results by, below are acceptable inputs");
                        System.out.println("                {title},{price},{genre},{developer},{date}");
                        System.out.println("descend         makes sort into a descend if val=\"D\" otherwise stays ascended\n");
                        continue;
                    }
                    ArrayList<Game> games_list= new ArrayList<>();
                    if(tokens[2].equals("title")){
                        Game[] game_array = app.search_game_name(tokens[1]);
                        games_list = new ArrayList<>(Arrays.stream(game_array).toList());
                    }
                    else if (tokens[2].equals("platform")){
                        Game[] game_array = app.search_game_platform(tokens[1]);
                        games_list = new ArrayList<>(Arrays.stream(game_array).toList());
                    }
                    else if (tokens[2].equals("date")){
                        Date release_date = new Date(Long.parseLong(tokens[1]));
                        Game[] game_array = app.search_game_release_date(release_date);
                        games_list = new ArrayList<>(Arrays.stream(game_array).toList());
                    }
                    else if (tokens[2].equals("developer")){
                        Game[] game_array = app.search_game_developer(tokens[1]);
                        games_list = new ArrayList<>(Arrays.stream(game_array).toList());
                    }
                    else if (tokens[2].equals("price")){
                        Game[] game_array = app.search_game_price(tokens[1]);
                        games_list = new ArrayList<>(Arrays.stream(game_array).toList());

                    }
                    else if (tokens[2].equals("genre")){
                        Game[] game_array = app.search_game_genre(tokens[1]);
                        games_list = new ArrayList<>(Arrays.stream(game_array).toList());
                    }
                    else {
                        System.out.println("\nUsage: ");
                        System.out.println("SEARCH_GAME { search_val | search_type | sort_val | descend }");
                        System.out.println("search_val      value being searched");
                        System.out.println("search_type     type that the being search is, below are acceptable inputs");
                        System.out.println("                {title},{platform},{date},{developer},{price},{genre}");
                        System.out.println("sort_val        optional argument to sort the results by, below are acceptable inputs");
                        System.out.println("                {title},{price},{genre},{developer},{date}");
                        System.out.println("descend         makes sort into a descend if val=\"D\" otherwise stays ascended\n");
                        continue;
                    }
                    Comparator<Game> default_comparator = new Comparator<Game>() {
                        @Override
                        public int compare(Game o1, Game o2) {
                            int result = o1.title().compareTo(o2.title());
                            if(result == 0){
                                return o1.date_release().compareTo(o2.date_release());
                            } else {
                                return result;
                            }
                        }
                    };
                    Comparator<Game> title_comparator = new Comparator<Game>() {
                        @Override
                        public int compare(Game o1, Game o2) {
                            int result;
                            if((tokens.length>3)&&(tokens[4].equals("D"))) {
                                result = o2.title().compareTo(o1.title());
                            }
                            else{
                                result = o1.title().compareTo(o2.title());
                            }
                                return result;
                            }
                        };
                    Comparator<Game> price_comparator = new Comparator<Game>() {
                        @Override
                        public int compare(Game o1, Game o2) {
                            int result;
                            if((tokens.length>3)&&(tokens[4].equals("D"))) {
                                if(o2.price() - o1.price()>0)
                                    result = -1;
                                else{
                                    result = 1;
                                }
                            }
                            else{
                                if(o2.price() - o1.price()>0)
                                    result = 1;
                                else{
                                    result = -1;
                                }
                            }
                            return result;
                        }
                    };
                    Comparator<Game> release_date_comparator = new Comparator<Game>() {
                        @Override
                        public int compare(Game o1, Game o2) {
                            int result;
                            if((tokens.length>3)&&(tokens[4].equals("D"))) {
                                result = o2.date_release().compareTo(o1.date_release());
                            }
                            else{
                                result = o1.date_release().compareTo(o2.date_release());
                            }
                            return result;
                        }
                    };

                    // Temp Genre_comparator, will need to be changed when merged when genre is arrayed
                    Comparator<Game> genre_comparator = new Comparator<Game>() {
                        @Override
                        public int compare(Game o1, Game o2) {
                            int result;
                            if((tokens.length>3)&&(tokens[4].equals("D"))) {
                                result = o2.genre().compareTo(o1.genre());
                            }
                            else{
                                result = o1.genre().compareTo(o2.genre());
                            }
                            return result;
                        }
                    };
                    if(tokens.length==3){
                        games_list.sort(default_comparator);
                    }
                    else if(tokens.length>3){
                        if(tokens[3].equals("title")){
                            games_list.sort(title_comparator);
                        } else if (tokens[3].equals("price")) {
                            games_list.sort(price_comparator);
                        } else if (tokens[3].equals("genre")){
                            games_list.sort(genre_comparator);
                        }
                        else if (tokens[3].equals("date")){
                            games_list.sort(release_date_comparator);
                        }
                        else {
                            System.out.println("Unknown sort argument: default sort used");
                            games_list.sort(default_comparator);
                        }
                    }

                    for(int i = 0; i < games_list.size(); i++) {
                        System.out.println(games_list.get(i));
                    }
                }
                case HELP  -> {
                    System.out.println("\nFor a more in depth usage of each command, Enter the command with no arguments\n");  //Not universal true, will be changed based on feedback
                    System.out.println("\nCOMMAND                 DESCRIPTION\n");
                    System.out.println("login                   login to user account");
                    System.out.println("logout                  logout of user that is logged in right now");
                    System.out.println("signup                  creates a new user account in database");
                    System.out.println("get_collections         get owned user's collections");     //will not send usage with 0 arguments
                    System.out.println("collection_add          add game to a owned collection");
                    System.out.println("collection_remove       remove game from collection");
                    System.out.println("collection_rename       rename a owned collection");
                    System.out.println("collection_delete       delete a owned collection");
                    System.out.println("collection_create       create new collection");
                    System.out.println("rate                    rate a game from 1-5");
                    System.out.println("play                    play a game by {millisecond} amount");
                    System.out.println("search_user             get a list of users owned by a given email");
                    System.out.println("add_friend              add a user as a friend with provided email");
                    System.out.println("remove_friend           remove user from friends with provided email");
                    System.out.println("search_game             search a list of games based on value given");
                    System.out.println("\nFor a more in depth usage of each command, Enter the command with no arguments\n");
                }

                default -> {
                    System.out.println(ERR_MESSAGE);
                }
            }
        }

        in.close();
        app.exit(0);//exit successfully
    }
}
