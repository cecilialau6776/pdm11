package app;

import app.model.Collection;
import app.model.Game;
import app.model.Platform;
import app.model.User;

import java.sql.Date;
import java.sql.Time;

/**
 * Used by UI to interface to the application
 */
public interface IApp {

    /**
     * Closes application and all relevant resources
     */
    void exit(int errCode);

    /**
     * Helper method to the user interfaces so that they do not ever
     * deal with the App class directly
     * @param cs_username The username to create an App with
     * @param cs_password The password to create an App with
     * @return An IApp
     */
    static IApp createApp(String cs_username, String cs_password){
        return new App(cs_username, cs_password);
    }

    /**
     * Tells the caller whether a user is logged in on this application
     * @return true if a user is logged in, false if not
     */
    boolean isUserLoggedIn();

    /**
     * Logs a user into the application with the given username and password,
     * updating their last_access_date naturally.
     * @param username The username of the user
     * @param password The password of the user
     * @return The user object if successful, null if authentication was unsuccessful
     * or another user is currently logged in
     */
    User logIn(String username, String password);

    /**
     * Gets all the platforms of the user logged in.
     * @return The list of platforms
     */
    Platform[] get_platforms();

    /**
     * Gets the platforms that the current game is on.
     * @param game The game to access
     * @return The platforms
     */
    Platform[] get_game_platforms(Game game);

    /**
     * Gets the total play time for all games in the collection.
     * @param collection The collection
     * @return Total play time
     */
    Time total_playtime_collection(Collection collection);

    /**
     * Logs out the current user. If there is no user logged in than it does
     * nothing.
     */
    void logOut();

    /**
     * Creates a new account for a new user using this app. Automatically logs
     * that user in regardless if there is another user logged in.
     * @param username The username of the user to create
     * @param password The password ...
     * @return The user object of the created user if successful, null if
     * unsuccessful(i.e. a user with that username already exists)
     */
    User signUp(String username, String password, String email,
                String firstname, String lastname);

    /**
     * Gets all the collections the user has.
     * @return The array of collections the user has
     */
    Collection[] get_collections();

    /**
     * Adds a game to a user's collection. Game and collection are confirmed
     * as valid prior to the call of the function.
     * @param collection The collection to add the game to
     * @param game The game to add to the collection
     * @return The updated collection
     */
    Collection collection_add(Collection collection, Game game);

    /**
     * Removes a game from a user's collection. Game and collection are confirmed
     * as valid prior to the call of the function.
     * @param collection The collection to remove the game from
     * @param game The game to remove from the collection
     * @return The updated collection
     */
    Collection collection_remove(Collection collection, Game game);

    /**
     * Deletes a collection from the user. Collection is confirmed as valid
     * prior to the call of the function.
     * @param collection The collection to be deleted
     */
    void collection_delete(Collection collection);

    /**
     * Rename a collection the user has. Collection is confirmed as valid
     * prior to the call of the function.
     * @param collection The collection to rename
     * @param new_name The new name
     * @return The updated collection
     */
    Collection collection_rename(Collection collection, String new_name);

    /**
     * Creates a new collection for the user.
     * @param name The name of the new collection
     * @return The new empty collection
     */
    Collection collection_create(String name);

    /**
     * Searches the database for games that have a substring of the
     * name given.
     * @param name The substring given
     * @return Array of games containing the substring in the title
     */
    Game[] search_game_name(String name);

    /**
     * Searches the database for games that match a certain price.
     * @param price The price of the game
     * @return Array of games of the certain price
     */
    Game[] search_game_price(String price);

    /**
     * Searches the database for games on a specified platform.
     * @param platform The platform
     * @return Array of games on a platform
     */
    Game[] search_game_platform(String platform);

    /**
     * Searches the database for games released on a specified
     * date.
     * @param release_date The release date
     * @return Array of games released on the date
     */
    Game[] search_game_release_date(Date release_date);

    /**
     * Searches the database for games by a specified developer.
     * @param developer The developer
     * @return Array of games by developer
     */
    Game[] search_game_developer(String developer);

    /**
     * Searches the database for games in a specified genre.
     * @param genre The genre
     * @return Array of games in genre.
     */
    Game[] search_game_genre(String genre);

    /**
     * User rates a game on a scale of 1-5 stars. Game is
     * assumed valid prior to function call.
     * @param game The game to be rated
     * @param rating The star rating
     * @return The game with updated rating
     */
    Game rate(Game game, int rating);

    /**
     * User plays a game for a certain amount of time and
     * the time played is added to user stats. Game is
     * assumed valid prior to function call.
     * @param game The game played
     * @param time The time played
     */
    void play(Game game, Time time);

    /**
     * Searches the database for users based on their email.
     * @param email The user's email
     * @return The user information if found, null if not
     */
    User[] search_friend(String email);

    /**
     * Adds a user as a friend. Friend to be added is assumed
     * as valid user and not the user's friend already prior to
     * the function call.
     * @param friend The friend to add
     */
    void add_friend(User friend);

    /**
     * Removes a user as a friend. Friend to be added is assumed
     * as valid user and already the user's friend prior to the
     * function call.
     * @param friend The friend to remove
     */
    void delete_friend(User friend);

    /**
     * gets a list of your friends with a given email
     * @param email list of user's with this email
     * @return list of users with this email that are friends
     */
    User[] check_friends(String email);

}
