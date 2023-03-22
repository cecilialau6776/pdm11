package app;

import app.model.User;

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
     * @param username The username to create an App with
     * @param password The password to create an App with
     * @return An IApp
     */
    static IApp createApp(String username, String password){
        return new App(username, password);
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

    //TODO add more methods as needed
}
