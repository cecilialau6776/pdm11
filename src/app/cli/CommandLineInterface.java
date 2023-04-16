package app.cli;

import java.sql.Time;
import java.sql.Date;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import java.util.ArrayList;

import app.IApp;
import app.model.*;

/**
 * A command line interface to this database application
 */
public class CommandLineInterface {

    /**
     * Error message for unrecognized command
     */
    private final static String ERR_MESSAGE = "Unrecognized command, Type \"help\" to get list of commands";

    /**
     * Exit message for when exit command is called
     */
    private final static String EXIT_MESSAGE = "Exiting command line interface";

    /**
     * Command to log in as a user
     */
    private final static String LOGIN = "LOGIN";

    /**
     * Command to log out as a user and exit
     */
    private final static String LOGOUT = "LOGOUT";

    /**
     * Command to sign up as a user
     */
    private final static String SIGNUP = "SIGNUP";

    /**
     * Command to list all of user's collections
     */
    private final static String GET_COLLECTIONS = "GET_COLLECTIONS";

    /**
     * Command to add a game to a user's collection
     */
    private final static String COLLECTION_ADD = "ADD_COLLECTION";

    /**
     * Command to remove a game from a user's collection
     */
    private final static String COLLECTION_REMOVE = "REMOVE_COLLECTION";

    /**
     * Command to delete a user's collection
     */
    private final static String COLLECTION_DELETE = "DELETE_COLLECTION";

    /**
     * Command to rename a user's collection
     */
    private final static String COLLECTION_RENAME = "RENAME_COLLECTION";

    /**
     * Command to create a new collection
     */
    private final static String COLLECTION_CREATE = "CREATE_COLLECTION";

    private final static String BUY_PLATFORM = "BUY_PLATFORM";

    private final static String PROFILE = "PROFILE";

    /**
     * Declares current user
     */
    private User user;

    /**
     * Command to rate a game
     */
    private final static String RATE = "RATE";

    /**
     * The app this CLI communicates with
     */
    private IApp app;

    private void printCollection(Collection collection) {
        System.out.println("\nName:\t" + collection.coll_name());
        System.out.println("# of games\t" + collection.games().length);
        System.out.println("Play time:\t" + app.total_playtime_collection(collection) + "\n");
    }

    private void printFullGame(Game game) {
        System.out.println("\nName: " + game.title());
        System.out.printf("Price: $%.2f\n", game.price());
        System.out.print("Playtime: ");
        if (game.playtime() == null) System.out.println("0:00:00");
        else System.out.println(game.playtime());

        System.out.print("Platforms: ");
        Platform[] platforms = app.get_game_platforms(game);
        if (platforms.length == 0) System.out.println("None");
        for (int i = 0; i < platforms.length; i++) {
            if (i == platforms.length - 1) {
                System.out.println(platforms[i].name());
                break;
            }
            System.out.print(platforms[i].name() + ", ");
        }

        System.out.println("Publisher: " + game.publisher().name());
        System.out.println("Developer: " + game.developer().name());
        System.out.print("Genres: ");
        if (game.genres().length == 0) System.out.println("None");
        for (int i = 0; i < game.genres().length; i++) {
            if (i == game.genres().length - 1) {
                System.out.println(game.genres()[i]);
                break;
            }
            System.out.print(game.genres()[i] + ", ");
        }
        System.out.println("Date Released: " + game.date_release());
        System.out.print("Ratings: ");
        if (game.ratings().length == 0) System.out.println("None");
        for (int i = 0; i < game.ratings().length; i++) {
            if (i == game.ratings().length - 1) {
                System.out.println(game.ratings()[i]);
                break;
            }
            System.out.print(game.ratings()[i] + ", ");
        }
        System.out.println();

    }

    private void get_collections() {
        Collection[] collections = app.get_collections();
        if (collections.length == 0) {
            System.out.println("You do not have any collections.");
            return;
        }
        for (int i = 0; i < collections.length; ++i) {
            for (int j = i + 1; j < collections.length; ++j) {
                Collection first = collections[i];
                Collection second = collections[j];
                if (first.coll_name().compareTo(second.coll_name()) > 0) {
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

        if (collections_list.length == 0) {
            System.out.println("You do not have a collection with that name.");
            return;
        }
        Collection selected_coll = collections_list[0];

        if (collections_list.length > 1) {
            System.out.println("Which collection would you like to add the game to (enter the number)?");
            for (int i = 0; i < collections_list.length; ++i) {
                Collection curr_coll = collections_list[i];
                System.out.println(i + 1);
                printCollection(curr_coll);
            }
            int input_to_int;
            do {
                input = in.nextLine();
                input_to_int = Integer.parseInt(input);
                if (input_to_int > 0 && input_to_int < collections_list.length) {
                    break;
                }
                System.out.println("Invalid collection number. Try again.");
            } while (true);

            selected_coll = collections_list[input_to_int - 1];
        }
        Game[] game_list = app.search_game_name(game_name);

        if (game_list.length == 0) {
            System.out.println("There is not a game with that name.");
            return;
        }

        Game selected_game = game_list[0];
        if (game_list.length > 1) {
            System.out.println("Which game would you like to add (enter the number)?");
            for (int i = 0; i < game_list.length; ++i) {
                Game curr_game = game_list[i];
                System.out.println(i + 1 + ":");
                printFullGame(curr_game);
            }

            int input_to_int;
            do {
                input = in.nextLine();
                input_to_int = Integer.parseInt(input);
                if (input_to_int > 0 && input_to_int < game_list.length) {
                    break;
                }
                System.out.println("Invalid game number. Try again.");
            } while (true);

            selected_game = game_list[input_to_int - 1];
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

        if (!platform_match) {
            System.out.println("*WARNING*\nYou do not own the platform this game is on. Proceed (y/n)?");
            input = in.nextLine();

            if (input.toLowerCase().charAt(0) == 'n') {
                return;
            }
        }

        Collection updated_coll = app.collection_add(selected_coll, selected_game);
        System.out.println("Updated collection:");
        printCollection(updated_coll);
    }

    private void collection_remove(String coll_name, String game_name) {
        Scanner in = new Scanner(System.in);
        String input;
        Collection[] collections_list = app.get_collection_name(coll_name);

        if (collections_list.length == 0) {
            System.out.println("You do not have a collection with that name.");
            return;
        }
        Collection selected_coll = collections_list[0];

        if (collections_list.length > 1) {
            System.out.println("Which collection would you like to remove the game from (enter the number)?");
            for (int i = 0; i < collections_list.length; ++i) {
                Collection curr_coll = collections_list[i];
                System.out.println(i + 1);
                printCollection(curr_coll);
            }

            int input_to_int;
            do {
                input = in.nextLine();
                input_to_int = Integer.parseInt(input);
                if (input_to_int > 0 && input_to_int < collections_list.length) {
                    break;
                }
                System.out.println("Invalid collection number. Try again.");
            } while (true);

            selected_coll = collections_list[input_to_int - 1];
        }

        Game[] game_list_init = selected_coll.games();

        ArrayList<Game> game_list = new ArrayList<>();

        for (Game game : game_list_init) {
            if (game.title().toLowerCase().equals(game_name.toLowerCase())) {
                game_list.add(game);
            }
        }

        if (game_list.size() == 0) {
            System.out.println("There is not a game with that name in your collection.");
            return;
        }

        Game selected_game = game_list.get(0);
        if (game_list.size() > 1) {
            System.out.println("Which game would you like to remove (enter the number)?");
            for (int i = 0; i < game_list.size(); ++i) {
                Game curr_game = game_list.get(i);
                System.out.println(i + 1 + ":");
                printFullGame(curr_game);
            }

            int input_to_int;
            do {
                input = in.nextLine();
                input_to_int = Integer.parseInt(input);
                if (input_to_int > 0 && input_to_int < game_list.size()) {
                    break;
                }
                System.out.println("Invalid game number. Try again.");
            } while (true);

            selected_game = game_list.get(input_to_int - 1);
        }

        Collection updated_coll = app.collection_remove(selected_coll, selected_game);
        System.out.println("Updated collection:");
        printCollection(updated_coll);
    }

    private void collection_delete(String coll_name) {
        Scanner in = new Scanner(System.in);
        String input;
        Collection[] collections_list = app.get_collection_name(coll_name);

        if (collections_list.length == 0) {
            System.out.println("You do not have a collection with that name.");
            return;
        }
        Collection selected_coll = collections_list[0];

        if (collections_list.length > 1) {
            System.out.println("Which collection would you like to delete (enter the number)?");
            for (int i = 0; i < collections_list.length; ++i) {
                Collection curr_coll = collections_list[i];
                System.out.println(i + 1);
                printCollection(curr_coll);
            }

            int input_to_int;
            do {
                input = in.nextLine();
                input_to_int = Integer.parseInt(input);
                if (input_to_int > 0 && input_to_int < collections_list.length) {
                    break;
                }
                System.out.println("Invalid collection number. Try again.");
            } while (true);

            selected_coll = collections_list[input_to_int - 1];
        }

        app.collection_delete(selected_coll);
        System.out.println("Collection deleted.");
    }

    private void collection_rename(String old_coll_name, String new_coll_name) {
        Scanner in = new Scanner(System.in);
        String input;
        Collection[] collections_list = app.get_collection_name(old_coll_name);

        if (collections_list.length == 0) {
            System.out.println("You do not have a collection with that name.");
            return;
        }
        Collection selected_coll = collections_list[0];

        if (collections_list.length > 1) {
            System.out.println("Which collection would you like to rename (enter the number)?");
            for (int i = 0; i < collections_list.length; ++i) {
                Collection curr_coll = collections_list[i];
                System.out.println(i + 1);
                printCollection(curr_coll);
            }

            int input_to_int;
            do {
                input = in.nextLine();
                input_to_int = Integer.parseInt(input);
                if (input_to_int > 0 && input_to_int < collections_list.length) {
                    break;
                }
                System.out.println("Invalid collection number. Try again.");
            } while (true);

            selected_coll = collections_list[input_to_int - 1];
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

        if (game_list.length == 0) {
            System.out.println("There is not a game with that name.");
            return;
        }

        Game selected_game = game_list[0];
        if (game_list.length > 1) {
            System.out.println("Which game would you like to rate (enter the number)?");
            for (int i = 0; i < game_list.length; ++i) {
                Game curr_game = game_list[i];
                System.out.println(i + 1 + ".\tName: " + curr_game.title() + "\n\tPlay time: " +
                        curr_game.playtime());
            }

            int input_to_int;
            do {
                input = in.nextLine();
                input_to_int = Integer.parseInt(input);
                if (input_to_int > 0 && input_to_int < game_list.length) {
                    break;
                }
                System.out.println("Invalid game number. Try again.");
            } while (true);

            selected_game = game_list[input_to_int - 1];
        }

        app.rate(selected_game, Integer.parseInt(rating));

        System.out.println("Successfully submitted rating.");
    }

    private void play(String coll_name, String time) {
        int total_minutes = Integer.parseInt(time);
        int hour = total_minutes / 60;
        int minutes = total_minutes % 60;
        Time run_time = new Time(hour, minutes, 0);
        Scanner in = new Scanner(System.in);
        String input;
        Game[] game_list = app.search_game_name(coll_name);

        if (game_list.length == 0) {
            System.out.println("There is not a game with that name.");
        } else {
            Game selected_game = game_list[0];
            if (game_list.length > 1) {
                System.out.println("Which game would you like to play (enter the number)?");
                for (int i = 0; i < game_list.length; ++i) {
                    Game curr_game = game_list[i];
                    System.out.println(i + 1 + ":");
                    printFullGame(curr_game);
                }

                int input_to_int;
                do {
                    input = in.nextLine();
                    input_to_int = Integer.parseInt(input);
                    if (input_to_int > 0 && input_to_int < game_list.length) {
                        break;
                    }
                    System.out.println("Invalid game number. Try again.");
                } while (true);
                selected_game = game_list[input_to_int - 1];
            }
            app.play(selected_game, run_time);
        }
    }

    private void search_user(String coll_name) {
        User[] user_list = app.search_users(coll_name);

        if (user_list.length == 0) {
            System.out.println("There no users linked to this email address");

        } else {
            System.out.println("Here's all the Usernames associated with this email");
            for (int i = 0; i < user_list.length; ++i) {
                User curr_user = user_list[i];
                System.out.println(i + 1 + ".\tUsername: " + curr_user.username());
            }
        }
    }

    private void add_friend(String coll_name) {
        Scanner in = new Scanner(System.in);
        String input;

        User[] user_list = app.search_users(coll_name);
        User[] friends = app.search_friends();

        if (user_list.length == 0) {
            System.out.println("There no users linked to this email address");
        } else {
            User selected_user = user_list[0];
            if (user_list.length > 0) {
                System.out.println("Which friend would you like to add (enter the number)?");
                for (int i = 0; i < user_list.length; ++i) {
                    User curr_user = user_list[i];
                    System.out.println(i + 1 + ".\tUsername: " + curr_user.username());
                }
                int input_to_int;
                do {
                    input = in.nextLine();
                    input_to_int = Integer.parseInt(input);
                    if (input_to_int > 0 && input_to_int <= user_list.length) {
                        break;
                    }
                    System.out.println("Invalid user number. Try again.");
                } while (true);
                selected_user = user_list[input_to_int - 1];
            }
            boolean can_add = true;
            for (int i = 0; i < friends.length; i++) {
                if (selected_user.equals(friends[i])) {
                    can_add = false;
                }
            }
            if (can_add) {
                app.add_friend(selected_user);
                System.out.println(selected_user.username() + "added to your friend list");
            } else {
                System.out.println("This user is already a friend!");
            }
        }
    }

    private void remove_friend(String coll_name) {
        Scanner in = new Scanner(System.in);
        String input;
        User[] user_list = app.search_friends();
        if (user_list.length == 0) {
            System.out.println("There are no friends linked to this email address");
        } else {
            ArrayList<User> removable_users = new ArrayList<>();
            for (int i = 0; i < user_list.length; i++) {
                String curr_user_email = user_list[i].email();
                if (curr_user_email.equals(coll_name)) {
                    removable_users.add(user_list[i]);
                }
            }
            if (removable_users.size() > 0) {
                User selected_user = removable_users.get(0);
                System.out.println("Which friend would you like to remove (enter the number)?");
                for (int i = 0; i < removable_users.size(); ++i) {
                    User curr_user = removable_users.get(i);
                    System.out.println(i + 1 + ".\tUsername: " + curr_user.username());
                }
                int input_to_int;
                do {
                    input = in.nextLine();
                    input_to_int = Integer.parseInt(input);
                    if (input_to_int > 0 && input_to_int <= user_list.length) {
                        break;
                    }
                    System.out.println("Invalid user number. Try again.");
                } while (true);
                selected_user = removable_users.get(input_to_int - 1);
                app.delete_friend(selected_user);
                System.out.println(selected_user.username() + " removed from your friend list");
            } else {
                System.out.println("There are no friends linked to this email address");
            }
        }

    }

    private void search_game_usage() {
        System.out.println("\nUsage: ");
        System.out.println("SEARCH_GAME { search_val | search_type | sort_val | descend }");
        System.out.println("search_val      value being searched");
        System.out.println("search_type     type that the being search is, below are acceptable inputs");
        System.out.println("                {title},{platform},{date},{developer},{price},{genre}");
        System.out.println("sort_val        optional argument to sort the results by, below are acceptable inputs");
        System.out.println("                {title},{price},{genre},{date}");
        System.out.println("descend         makes sort into a descend if val=\"D\" otherwise stays ascended\n");
    }

    private void search_game(String search_val, String search_type, String sort_val, String descend) {

        ArrayList<Game> games_list = new ArrayList<>();
        boolean correct_format = true;
        if (search_type.equals("title")) {
            Game[] game_array = app.search_game_name(search_val);
            games_list = new ArrayList<>(Arrays.stream(game_array).toList());
        } else if (search_type.equals("platform")) {
            Game[] game_array = app.search_game_platform(search_val);
            games_list = new ArrayList<>(Arrays.stream(game_array).toList());
        } else if (search_type.equals("date")) {
            Date release_date = new Date(Long.parseLong(search_val));
            Game[] game_array = app.search_game_release_date(release_date);
            games_list = new ArrayList<>(Arrays.stream(game_array).toList());
        } else if (search_type.equals("developer")) {
            Game[] game_array = app.search_game_developer(search_val);
            games_list = new ArrayList<>(Arrays.stream(game_array).toList());
        } else if (search_type.equals("price")) {
            double val = Double.parseDouble(search_val);
            Game[] game_array = app.search_game_price(val);
            games_list = new ArrayList<>(Arrays.stream(game_array).toList());
        } else if (search_type.equals("genre")) {
            Game[] game_array = app.search_game_genre(search_val);
            games_list = new ArrayList<>(Arrays.stream(game_array).toList());
        } else {
            correct_format = false;
        }
        if (correct_format) {
            Comparator<Game> default_comparator = new Comparator<Game>() {
                @Override
                public int compare(Game o1, Game o2) {
                    int result = o1.title().compareTo(o2.title());
                    if (result == 0) {
                        return o1.date_release().compareTo(o2.date_release());
                    } else {
                        return result;
                    }
                }
            };
            Comparator<Game> title_comparator = new Comparator<Game>() {
                @Override
                public int compare(Game o1, Game o2) {
                    return o2.title().compareTo(o1.title());
                }
            };
            Comparator<Game> price_comparator = new Comparator<Game>() {
                @Override
                public int compare(Game o1, Game o2) {
                    int result;
                    if (o2.price() - o1.price() > 0)
                        result = 1;
                    else {
                        result = -1;
                    }
                    return result;
                }
            };
            Comparator<Game> release_date_comparator = new Comparator<Game>() {
                @Override
                public int compare(Game o1, Game o2) {
                    return o1.date_release().compareTo(o2.date_release());
                }
            };

            // Temp Genre_comparator, will need to be changed when merged when genre is arrayed
            Comparator<Game> genre_comparator = new Comparator<Game>() {
                @Override
                public int compare(Game o1, Game o2) {
                    int result = 0;
                    int o1_genres = o1.genres().length;
                    int o2_genres = o2.genres().length;
                    for (int i = 0; (i < o1_genres) && (i < o2_genres); i++) {
                        result = o1.genres()[i].compareTo(o2.genres()[i]);
                        if (result != 0) {
                            return result;
                        }
                    }
                    return result;
                }
            };
            if (sort_val.equals("default")) {
                games_list.sort(default_comparator);
            } else if (sort_val.equals("title")) {
                if (descend.equals("D")) {
                    games_list.sort(title_comparator.reversed());
                } else {
                    games_list.sort(title_comparator);
                }
            } else if (sort_val.equals("price")) {
                if (descend.equals("D")) {
                    games_list.sort(price_comparator.reversed());
                } else {
                    games_list.sort(price_comparator);
                }
            } else if (sort_val.equals("genre")) {
                if (descend.equals("D")) {
                    games_list.sort(genre_comparator.reversed());
                } else {
                    games_list.sort(genre_comparator);
                }
            } else if (sort_val.equals("date")) {
                if (descend.equals("D")) {
                    games_list.sort(release_date_comparator.reversed());
                } else {
                    games_list.sort(release_date_comparator);
                }
            } else {
                System.out.println("Unknown sort argument: default sort used");
                games_list.sort(default_comparator);
            }
            for (int i = 0; i < games_list.size(); i++) {
                printFullGame(games_list.get(i));
            }
        } else {
            search_game_usage();
        }
    }

    private void search_game(String search_val, String search_type, String sort_val) {
        search_game(search_val, search_type, sort_val, "A");
    }

    private void search_game(String search_val, String search_type) {
        search_game(search_val, search_type, "default", "A");
    }

    private void buy_platform(String platform_name) {

    }


    /**
     * command to play a game
     */
    private final static String PLAY = "PLAY";

    /**
     * command to search a friend
     */
    private final static String SEARCH_USER = "SEARCH_USER";

    /**
     * command to add a friend
     */
    private final static String ADD_FRIEND = "ADD_FRIEND";

    /**
     * command to remove a friend
     */
    private final static String REMOVE_FRIEND = "REMOVE_FRIEND";

    /**
     * command to search for a list of games
     */
    private final static String SEARCH_GAME = "SEARCH_GAME";
    /**
     * command to see list of usable commands
     */
    private final static String HELP = "HELP";

    /**
     * Constructs a CLI by creating the application it uses as a backend
     *
     * @param cs_username Credentials for logging into starbug server
     * @param cs_password ...
     */
    public CommandLineInterface(String cs_username, String cs_password) {
        this.app = IApp.createApp(cs_username, cs_password);
    }

    /**
     * Primary method to launch this CLI, takes no arguments and returns nothing.
     * When the exit command is given it closes this class's resources and calls
     * .exit() on the database app.
     */
    public void launch() {
        Scanner in = new Scanner(System.in);
        boolean exit = false;

        System.out.println("Please enter login or signup:");

        do {
            System.out.print(">\t");
            String input = in.nextLine();
            switch (input.toUpperCase()) {
                case LOGIN -> {
                    System.out.print("Username:\n>\t");
                    String username = in.nextLine();
                    System.out.print("Password:\n>\t");
                    String password = in.nextLine();
                    this.user = app.logIn(username, password);

                }
                case SIGNUP -> {
                    System.out.print("Username:\n>\t");
                    String username = in.nextLine();
                    System.out.print("Password:\n>\t");
                    String password = in.nextLine();
                    System.out.print("Email:\n>\t");
                    String email = in.nextLine();
                    System.out.print("Firstname:\n>\t");
                    String firstname = in.nextLine();
                    System.out.print("Lastname:\n>\t");
                    String lastname = in.nextLine();
                    this.user = app.signUp(username, password, email, firstname, lastname);
                }

                default -> {
                    System.out.println("Invalid input. Try again.");
                }
            }
        } while (!app.isUserLoggedIn());

        System.out.println("Welcome " + this.user.firstname() + "!");

        while (!exit) {
            System.out.print(">\t");
            String input = in.nextLine();
            String[] tokens = input.strip().split("\\s+");

            switch (tokens[0].toUpperCase()) {
                case LOGOUT -> {
                    app.logOut();
                    System.out.println(EXIT_MESSAGE);
                    exit = true;
                }

                case GET_COLLECTIONS -> {
                    if (!app.isUserLoggedIn()) {
                        System.out.println("You are not logged in, please log in first.");
                    }

                    if (tokens.length != 1) {
                        System.out.println("Usage: get_collections");
                        continue;
                    }
                    get_collections();
                }

                case COLLECTION_ADD -> {
                    if (!app.isUserLoggedIn()) {
                        System.out.println("You are not logged in, please log in first.");
                    }

                    if (tokens.length != 3) {
                        System.out.println("Usage: add_collection collection-name game-name");
                        continue;
                    }
                    collection_add(tokens[1], tokens[2]);
                }

                case COLLECTION_REMOVE -> {
                    if (!app.isUserLoggedIn()) {
                        System.out.println("You are not logged in, please log in first.");
                    }

                    if (tokens.length != 3) {
                        System.out.println("Usage: remove_collection collection-name game-name");
                        continue;
                    }
                    collection_remove(tokens[1], tokens[2]);
                }

                case COLLECTION_DELETE -> {
                    if (!app.isUserLoggedIn()) {
                        System.out.println("You are not logged in, please log in first.");
                    }

                    if (tokens.length != 2) {
                        System.out.println("Usage: delete_collection collection-name");
                        continue;
                    }
                    collection_delete(tokens[1]);
                }

                case COLLECTION_RENAME -> {
                    if (!app.isUserLoggedIn()) {
                        System.out.println("You are not logged in, please log in first.");
                    }

                    if (tokens.length != 3) {
                        System.out.println("Usage: rename_collection old-collection-name new-collection-name");
                        continue;
                    }
                    collection_rename(tokens[1], tokens[2]);
                }

                case COLLECTION_CREATE -> {
                    if (!app.isUserLoggedIn()) {
                        System.out.println("You are not logged in, please log in first.");
                    }

                    if (tokens.length != 2) {
                        System.out.println("Usage: create_collection collection-name");
                        continue;
                    }
                    collection_create(tokens[1]);
                }

                case RATE -> {
                    if (!app.isUserLoggedIn()) {
                        System.out.println("You are not logged in, please log in first.");
                    }

                    if (tokens.length != 3) {
                        System.out.println("Usage: rate game-name rating");
                        continue;
                    }
                    rate(tokens[1], tokens[2]);
                }//add more cases

                case PLAY -> {
                    if (tokens.length < 3) {
                        System.out.println("\nUsage:");
                        System.out.println("PLAY { game | time }");
                        System.out.println("game            game to be played");
                        System.out.println("time            time in minutes to be added\n"); //Might change later to a more useful format that milliseconds
                        continue;
                    }
                    play(tokens[1], tokens[2]);
                }

                case SEARCH_USER -> {
                    if (tokens.length < 2) {
                        System.out.println("\nUsage:");
                        System.out.println("SEARCH_USER { email }");
                        System.out.println("email           email that the account is associated with to be searched\n");
                        continue;
                    }
                    search_user(tokens[1]);
                }

                case ADD_FRIEND -> {
                    if (tokens.length < 2) {
                        System.out.println("\nUsage:");
                        System.out.println("ADD_FRIEND { email }");
                        System.out.println("email           email that the account is associated with to be added\n");
                        continue;
                    }
                    add_friend(tokens[1]);
                }

                case REMOVE_FRIEND -> {
                    if (tokens.length < 2) {
                        System.out.println("\nUsage:");
                        System.out.println("REMOVE_FRIEND { email }");
                        System.out.println("email           email that the account is associated with to be removed\n");
                        continue;
                    }
                    remove_friend(tokens[1]);
                }

                case SEARCH_GAME -> {
                    if (tokens.length < 3) {
                        search_game_usage();
                    } else if (tokens.length == 3) {
                        search_game(tokens[1], tokens[2]);
                    } else if (tokens.length == 4) {
                        search_game(tokens[1], tokens[2], tokens[3]);
                    } else if (tokens.length == 5) {
                        search_game(tokens[1], tokens[2], tokens[3], tokens[4]);
                    }
                }

                case BUY_PLATFORM -> {
                    if (tokens.length != 2) {
                        System.out.println("Usage: buy_platform platform-name");
                        continue;
                    }
                    buy_platform(tokens[1]);
                }

                case PROFILE -> {
                    UserProfile profile = app.get_profile();
                    System.out.println(profile);
                    Game[] topTen = profile.topTen();
                    if(topTen.length == 0){
                        System.out.println("\t0 games");
                    } else {
                        for (Game game : topTen) {
                            printFullGame(game);
                        }
                    }
                }

                case HELP -> {
                    System.out.println("\nFor a more in depth usage of each command, Enter the command with no arguments\n");  //Not universal true, will be changed based on feedback
                    System.out.println("\nCOMMAND                 DESCRIPTION\n");
                    System.out.println("login                   login to user account");
                    System.out.println("logout                  logout of user that is logged in right now");
                    System.out.println("signup                  creates a new user account in database");
                    System.out.println("get_collections         get owned user's collections");     //will not send usage with 0 arguments
                    System.out.println("add_collection          add game to a owned collection");
                    System.out.println("remove_collection       remove game from collection");
                    System.out.println("rename_collection       rename a owned collection");
                    System.out.println("delete_collection       delete a owned collection");
                    System.out.println("create_collection       create new collection");
                    System.out.println("rate                    rate a game from 1-5");
                    System.out.println("play                    play a game by {minutes} amount");
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
