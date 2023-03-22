package app;

import app.cli.CommandLineInterface;
import app.model.*;
import app.model.Collection;
import com.jcraft.jsch.*;

import java.sql.*;
import java.sql.Date;
import java.util.Properties;

/**
 * Database application object.
 *
 * In a Model-View-Controller architecture this is the controller.
 *
 * @author Damon Gonzalez
 */
public class App implements IApp{

    /** Usage method for running this program*/
    private final static String USAGE = "Usage: java App <cs_username> <cs_password>";

    /** Initial message to user */
    private final static String MSG = "Principles of Data Management, Group 11, Database Application";

    /** The name of our group's database on starbug */
    private final static String databaseName = "p320_11";

    /** The connection to the server, SQL statements are executed via this variable */
    private Connection conn;

    /** The SSH session on the server */
    private Session session;

    /** The current user logged in, default null */
    private User currentUser;

    /**
     * Creates an App object by establishing a connection to our SQL server
     * @param cs_username Credentials to log into starbug sql server
     * @param cs_password ...
     */
    public App(String cs_username, String cs_password){
        System.out.println(MSG);

        int lport = 5432;
        String rhost = "starbug.cs.rit.edu";
        int rport = 5432;

        String driverName = "org.postgresql.Driver";
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            session = jsch.getSession(cs_username, rhost, 22);
            session.setPassword(cs_password);
            session.setConfig(config);
            session.setConfig("PreferredAuthentications","publickey,keyboard-interactive,password");
            session.connect();
            System.out.println("Connected");
            int assigned_port = session.setPortForwardingL(lport, "localhost", rport);
            System.out.println("Port Forwarded");

            // Assigned port could be different from 5432 but rarely happens
            String url = "jdbc:postgresql://localhost:"+ assigned_port + "/" + databaseName;

            System.out.println("database Url: " + url);
            Properties props = new Properties();
            props.put("user", cs_username);
            props.put("password", cs_password);

            Class.forName(driverName);
            conn = DriverManager.getConnection(url, props);
            System.out.println("Database connection established");
        } catch(Exception e){
            e.printStackTrace();
            System.err.println("Connection failed, exiting application...");
            exit(1);//exit on error
        }
        //TODO do some more stuff

        //comment for test commit
    }

    @Override
    public void exit(int errCode){
        try{
            if(conn != null){
                conn.close();
            }
            if(session != null){
                session.disconnect();
            }
            System.out.println("Application closing");
            System.exit(errCode);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean isUserLoggedIn() {
        return currentUser != null;
    }

    @Override
    public User logIn(String username, String password) {
        if(currentUser != null){
            return null;
        }

        String query = "SELECT * FROM \"user\" " +
                "WHERE username = \'" + username + "\' AND password = \'" + password + "\'";
        try{
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()){
                String rs_username = rs.getString("username");
                String rs_password = rs.getString("password");
                String rs_email = rs.getString("email");
                String rs_firstname = rs.getString("firstname");
                String rs_lastname = rs.getString("lastname");
                Date rs_creation_date = rs.getDate("creation_date");

                Date current_date = new Date(new java.util.Date().getTime());//to update last access date
                String update_query = "UPDATE \"user\" SET "
                        + "last_access_date = \'" + current_date.toString()
                        + "\' WHERE username = \'" + username + "\'";
                stmt.executeUpdate(update_query);

                currentUser = new User(rs_username, rs_password, rs_email,
                        rs_firstname, rs_lastname, current_date,
                        rs_creation_date);
                return currentUser;
            } else {
                System.out.println("No user found");
                return null;
            }
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void logOut() {
        currentUser = null;
    }

    @Override
    public User signUp(String username, String password, String email,
                       String firstname, String lastname) {
        String check_query = "SELECT username FROM \"user\""
                + " WHERE username = \'" + username + "\'";
        Date sqlDate = new Date(new java.util.Date().getTime());//the current date in sql format
        String insert_query = "INSERT INTO \"user\""
                + " VALUES (\'" + username + "\',\'" + password
                + "\',\'" + email + "\',\'" + firstname
                + "\',\'" + lastname + "\',\'" + sqlDate
                + "\',\'" + sqlDate + "\')";
        try{
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(check_query);
            if(!rs.next()){//the username is not used by anybody else
                stmt.executeUpdate(insert_query);
                return logIn(username, password);
            } else {
                return null;
            }
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets all the collections the user has.
     *
     * @return The array of collections the user has
     */
    @Override
    public Collection[] get_collections() {
        return new Collection[0];
    }

    /**
     * Adds a game to a user's collection. Game and collection are confirmed
     * as valid prior to the call of the function.
     *
     * @param collection The collection to add the game to
     * @param game       The game to add to the collection
     * @return The updated collection
     */
    @Override
    public Collection collection_add(Collection collection, Game game) {
        return null;
    }

    /**
     * Deletes a collection from the user. Collection is confirmed as valid
     * prior to the call of the function.
     *
     * @param collection The collection to be deleted
     */
    @Override
    public void collection_delete(Collection collection) {

    }

    /**
     * Rename a collection the user has. Collection is confirmed as valid
     * prior to the call of the function.
     *
     * @param collection The collection to rename
     * @param new_name   The new name
     * @return The updated collection
     */
    @Override
    public Collection collection_rename(Collection collection, String new_name) {
        return null;
    }

    /**
     * Creates a new collection for the user.
     *
     * @param name The name of the new collection
     * @return The new empty collection
     */
    @Override
    public Collection collection_create(String name) {
        return null;
    }

    /**
     * Searches the database for games that have a substring of the
     * name given.
     *
     * @param name The substring given
     * @return Array of games containing the substring in the title
     */
    @Override
    public Game[] search_game_name(String name) {
        return new Game[0];
    }

    /**
     * Searches the database for games that match a certain price.
     *
     * @param price The price of the game
     * @return Array of games of the certain price
     */
    @Override
    public Game[] search_game_price(String price) {
        return new Game[0];
    }

    /**
     * Searches the database for games on a specified platform.
     *
     * @param platform The platform
     * @return Array of games on a platform
     */
    @Override
    public Game[] search_game_platform(String platform) {
        return new Game[0];
    }

    /**
     * Searches the database for games released on a specified
     * date.
     *
     * @param release_date The release date
     * @return Array of games released on the date
     */
    @Override
    public Game[] search_game_release_date(Date release_date) {
        return new Game[0];
    }

    /**
     * Searches the database for games by a specified developer.
     *
     * @param developer The developer
     * @return Array of games by developer
     */
    @Override
    public Game[] search_game_developer(String developer) {
        return new Game[0];
    }

    /**
     * Searches the database for games in a specified genre.
     *
     * @param genre The genre
     * @return Array of games in genre.
     */
    @Override
    public Game[] search_game_genre(String genre) {
        return new Game[0];
    }

    /**
     * User rates a game on a scale of 1-5 stars. Game is
     * assumed valid prior to function call.
     *
     * @param game   The game to be rated
     * @param rating The star rating
     * @return The game with updated rating
     */
    @Override
    public Game rate(Game game, int rating) {
        return null;
    }

    /**
     * User plays a game for a certain amount of time and
     * the time played is added to user stats. Game is
     * assumed valid prior to function call.
     *
     * @param game The game played
     * @param time The time played
     */
    @Override
    public void play(Game game, Time time) {

    }

    /**
     * Searches the database for a user based on their email.
     *
     * @param email The user's email
     * @return The user information if found, null if not
     */
    @Override
    public User search_friend(String email) {
        return null;
    }

    /**
     * Adds a user as a friend. Friend to be added is assumed
     * as valid user and not the user's friend already prior to
     * the function call.
     *
     * @param friend The friend to add
     */
    @Override
    public void add_friend(User friend) {

    }

    /**
     * Removes a user as a friend. Friend to be added is assumed
     * as valid user and already the user's friend prior to the
     * function call.
     *
     * @param friend The friend to remove
     */
    @Override
    public void delete_friend(User friend) {

    }

    /**
     * Main method of this database application
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if(args.length != 2){
            System.err.println(USAGE);
            System.exit(1);
        }

        CommandLineInterface cli = new CommandLineInterface(args[0], args[1]);
        cli.launch();
    }
}
