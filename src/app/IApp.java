package app;

/**
 * Used by UI to interface to the application
 */
public interface IApp {

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
     * Attempts to log a user into this application with the given username
     * and password
     * @param username The username of the user
     * @param password The password of the user
     * @return true if the user was successfully logged in, false if
     * authentication was unsuccessful or there is currently another user
     * logged in
     */
    boolean logIn(String username, String password);

    /**
     * Logs out the current user. If there is no user logged in than it does
     * nothing.
     */
    void logOut();

    /**
     * Closes application and all relevant resources
     */
    void exit();

    //TODO add more methods as needed
}
