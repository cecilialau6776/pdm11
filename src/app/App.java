package app;

import app.cli.CommandLineInterface;
import app.model.*;
import app.model.Collection;
import com.jcraft.jsch.*;
import org.postgresql.core.Query;

import javax.xml.transform.Result;
import java.sql.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Database application object.
 * <p>
 * In a Model-View-Controller architecture this is the controller.
 *
 * @author Damon Gonzalez
 */
public class App implements IApp {

    /**
     * Usage method for running this program
     */
    private final static String USAGE = "Usage: java App <cs_username> <cs_password>";

    /**
     * Initial message to user
     */
    private final static String MSG = "Principles of Data Management, Group 11, Database Application";

    /**
     * The name of our group's database on starbug
     */
    private final static String databaseName = "p320_11";

    /**
     * The connection to the server, SQL statements are executed via this variable
     */
    private Connection conn;

    /**
     * The SSH session on the server
     */
    private Session session;

    /**
     * The current user logged in, default null
     */
    private User currentUser;

    /**
     * Creates an App object by establishing a connection to our SQL server
     *
     * @param cs_username Credentials to log into starbug sql server
     * @param cs_password ...
     */
    public App(String cs_username, String cs_password) {
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
            session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
            session.connect();
            System.out.println("Connected");
            int assigned_port = session.setPortForwardingL(lport, "localhost", rport);
            System.out.println("Port Forwarded");

            // Assigned port could be different from 5432 but rarely happens
            String url = "jdbc:postgresql://localhost:" + assigned_port + "/" + databaseName;

            System.out.println("database Url: " + url);
            Properties props = new Properties();
            props.put("user", cs_username);
            props.put("password", cs_password);

            Class.forName(driverName);
            conn = DriverManager.getConnection(url, props);
            System.out.println("Database connection established");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Connection failed, exiting application...");
            exit(1);//exit on error
        }
        //TODO do some more stuff

        //comment for test commit
    }

    @Override
    public void exit(int errCode) {
        try {
            if (conn != null) {
                conn.close();
            }
            if (session != null) {
                session.disconnect();
            }
            System.out.println("Application closing");
            System.exit(errCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isUserLoggedIn() {
        return currentUser != null;
    }

    @Override
    public User logIn(String username, String password) {
        if (currentUser != null) {
            return null;
        }

        String query = "SELECT * FROM \"user\" " +
                "WHERE username = \'" + username + "\' AND password = \'" + password + "\'";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
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
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets all the platforms of the user logged in.
     *
     * @return The list of platforms
     */
    @Override
    public Platform[] get_platforms() {
        String query_format = "SELECT p.pid, p.name FROM owned_platform op join platform p on op.pid = p.pid"
                            + "WHERE username = '%s'";
        String query = String.format(query_format, currentUser.username());
        try{
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            ArrayList<Platform> platforms = new ArrayList<>();
            while(rs.next()){
                int pid = rs.getInt("pid");
                String name = rs.getString("name");
                platforms.add(new Platform(pid, name));
            }
            return platforms.toArray(new Platform[0]);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the platforms that the current game is on.
     *
     * @param game The game to access
     * @return The platforms
     */
    @Override
    public Platform[] get_game_platforms(Game game) {
        String q = String.format("""
                SELECT platform.pid, "name" FROM platform
                    JOIN game_platform gp on platform.pid = gp.pid
                    WHERE gp.gid = %d""", game.gid());
        try{
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(q);
            ArrayList<Platform> platforms = new ArrayList<>();
            while(rs.next()){
                int pid = rs.getInt("pid");
                String name = rs.getString("name");
                platforms.add(new Platform(pid, name));
            }
            return platforms.toArray(new Platform[0]);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the total play time for all games in the collection.
     *
     * @param collection The collection
     * @return Total play time
     */
    @Override
    public Time total_playtime_collection(Collection collection) {
        String q = String.format("""
                SELECT sum(time_played) FROM plays
                    JOIN game g on g.gid = plays.gid
                    JOIN game_collection gc on g.gid = gc.gid
                    WHERE gc.collid = '%d'""", collection.collid());
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery(q);
            if (rs.next()) {
                return rs.getTime("sum");
            } else {
                return new Time(0);
            }
        } catch (SQLException e) {
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
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(check_query);
            if (!rs.next()) {//the username is not used by anybody else
                stmt.executeUpdate(insert_query);
                return logIn(username, password);
            } else {
                return null;
            }
        } catch (SQLException e) {
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
        String q = String.format("SELECT collid FROM collection WHERE coll_username = '%s'", currentUser.username());
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery(q);
            ArrayList<Collection> collections = new ArrayList<>();
            while (rs.next()) {
                collections.add(getCollection(rs.getInt("collid")));
            }
            return collections.toArray(new Collection[0]);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets a collection
     *
     * @param collid The Collection's id
     * @return The matching Collection
     */
    private Collection getCollection(int collid) {
        String gamesQuery = String.format("SELECT gid from game_collection WHERE collid = '%d'", collid);
        String collQuery = String.format("SELECT coll_username, coll_name FROM collection WHERE collid = '%d'", collid);
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery(collQuery);
            if (rs.next()) {
                rs = s.executeQuery(gamesQuery);
                ArrayList<Game> games = new ArrayList<>();
                while (rs.next()) {
                    games.add(getGame(rs.getInt("gid")));
                }
                String collUsername = rs.getString("coll_username");
                String collName = rs.getString("coll_name");
                return new Collection(collid, collUsername, collName, games.toArray(new Game[0]));
            } else {
                System.out.println("No collection with id " + collid);
                return null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets all the collections the user has with the given name, case-insensitive.
     *
     * @param name The name
     * @return Array of Collections.
     */
    @Override
    public Collection[] get_collection_name(String name) {
        String q = String.format("""
                    SELECT collid FROM collection
                WHERE UPPER(coll_name) = UPPER('%s') AND coll_username = '%s'""", name, currentUser.username());
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(q);
            ArrayList<Collection> collections = new ArrayList<>();
            while (rs.next()) {
                int collId = rs.getInt("collid");
                collections.add(getCollection(collId));
            }
            return collections.toArray(new Collection[0]);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
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
        String q = String.format("""
                    INSERT INTO game_collection (coll_id, gid)
                    VALUES ('%d', '%d')""", collection.collid(), game.gid());

        try {
            Statement statement = conn.createStatement();
            statement.executeQuery(q);
            return getCollection(collection.collid());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Removes a game from a user's collection. Game and collection are confirmed
     * as valid prior to the call of the function.
     *
     * @param collection The collection to remove the game from
     * @param game       The game to remove from the collection
     * @return The updated collection
     */
    @Override
    public Collection collection_remove(Collection collection, Game game) {
        String q = String.format("""
                    DELETE FROM game_collection
                    WHERE collid = '%d' AND gid = '%d'""", collection.collid(), game.gid());

        try {
            Statement statement = conn.createStatement();
            statement.executeQuery(q);
            return getCollection(collection.collid());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Deletes a collection from the user. Collection is confirmed as valid
     * prior to the call of the function.
     *
     * @param collection The collection to be deleted
     */
    @Override
    public void collection_delete(Collection collection) {
        String q = String.format("""
                    DELETE FROM collection
                    WHERE collid = '%d'""", collection.collid());
        try {
            Statement statement = conn.createStatement();
            statement.executeQuery(q);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        String q = String.format("""
                    UPDATE collection
                    SET coll_name = '%s'
                    WHERE coll_id = '%d'""", new_name, collection.collid());

        try {
            Statement statement = conn.createStatement();
            statement.executeQuery(q);
            return getCollection(collection.collid());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates a new collection for the user.
     *
     * @param name The name of the new collection
     * @return The new empty collection
     */
    @Override
    public Collection collection_create(String name) {
        String q = String.format("INSERT INTO collection (coll_username, coll_name) VALUES ('%s', '%s')", currentUser.username(), name);
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(q);
            Collection[] collections = get_collection_name(name);
            if (collections.length == 0) {
                System.out.println("Creation of collection failed");
                return null;
            }
            Collection out = collections[0];
            for (Collection c : collections) {
                   if (c.collid() > out.collid()) out = c;
            }
            return out;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
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
        String q = String.format("SELECT gid FROM game WHERE UPPER(title) = UPPER(%s)", name);
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(q);
            ArrayList<Game> games = new ArrayList<>();
            while (rs.next()) {
                games.add(getGame(rs.getInt("gid")));
            }
            return games.toArray(new Game[0]);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Searches the database for games that match a certain price.
     *
     * @param price The price of the game
     * @return Array of games of the certain price
     */
    @Override
    public Game[] search_game_price(double price) {
        String query = String.format("SELECT gid FROM game_platform WHERE price = %.2f", price);
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            ArrayList<Game> games = new ArrayList<>();
            while (rs.next()) {
                games.add(getGame(rs.getInt("gid")));
            }
            return games.toArray(new Game[0]);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Searches the database for games on a specified platform.
     *
     * @param platform The platform
     * @return Array of games on a platform
     */
    @Override
    public Game[] search_game_platform(String platform) {
        String q = String.format("SELECT gid FROM game_platform\n" +
                "    JOIN platform p on game_platform.pid = p.pid\n" +
                "    WHERE UPPER(p.name) = UPPER(%s)", platform);
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(q);
            ArrayList<Game> games = new ArrayList<>();
            while (rs.next()) {
                games.add(getGame(rs.getInt("gid")));
            }
            return games.toArray(new Game[0]);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
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
        String query = String.format("SELECT gid FROM game_platform WHERE release_date = '%s'", release_date.toString());
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            ArrayList<Game> games = new ArrayList<>();
            while (rs.next()) {
                games.add(getGame(rs.getInt("gid")));
            }
            return games.toArray(new Game[0]);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Searches the database for games by a specified developer.
     *
     * @param developer The developer
     * @return Array of games by developer
     */
    @Override
    public Game[] search_game_developer(String developer) {
        String q = String.format("""
                SELECT gid FROM develop
                    JOIN company c on develop.compid = c.compid
                    WHERE UPPER(c.name) = UPPER(%s)""", developer);
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(q);
            ArrayList<Game> games = new ArrayList<>();
            while (rs.next()) {
                games.add(getGame(rs.getInt("gid")));
            }
            return games.toArray(new Game[0]);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Searches the database for games in a specified genre.
     *
     * @param genre The genre
     * @return Array of games in genre.
     */
    @Override
    public Game[] search_game_genre(String genre) {
        String q = String.format("""
                SELECT gid FROM game_genre
                JOIN genre g on g.geid = game_genre.geid
                WHERE UPPER(g.name) = UPPER(%s)""", genre);
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(q);
            ArrayList<Game> games = new ArrayList<>();
            while (rs.next()) {
                games.add(getGame(rs.getInt("gid")));
            }
            return games.toArray(new Game[0]);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
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
        if (!(rating >= 0 && rating <= 5)) {
            System.out.println("Rating must be in range 0-5 inclusive.");
            return null;
        }
        String query = String.format("INSERT INTO ratings (username, gid, star_rating) VALUES('mbooymk', %d, %d)", game.gid(), rating);
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            return getGame(game.gid());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
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
        Date current_date = new Date(new java.util.Date().getTime());
        String query = String.format("INSERT INTO plays (username, gid, play_date, time_played) VALUES ('%s', %d, '%s', '%s')", currentUser.username(), game.gid(), current_date, time.toString());
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
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
        String q = String.format("""
                    INSERT INTO friends (user, friend)
                    VALUES ('%s', '%s')""", currentUser.username(), friend.username());

        try {
            Statement statement = conn.createStatement();
            statement.executeQuery(q);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        String q = String.format("""
                    DELETE FROM friends
                    WHERE user = '%s' AND friend = '%s'""", currentUser.username(), friend.username());

        try {
            Statement statement = conn.createStatement();
            statement.executeQuery(q);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets a list of the game's ratings
     *
     * @param gid The game's id
     * @return The game's ratings
     */
    private int[] getGameRatings(int gid) {
        String query = String.format("""
                SELECT star_rating FROM ratings
                    JOIN game g on ratings.gid = g.gid
                    WHERE g.gid = '%d'""", gid);
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            ArrayList<Integer> ratings = new ArrayList<>();
            while (rs.next()) {
                ratings.add(rs.getInt("star_rating"));
            }
            return ratings.stream().mapToInt(i -> i).toArray();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets a list of the game's genres
     *
     * @param gid The game's id
     * @return The game's genres
     */
    private String[] getGameGenres(int gid) {
        String query = String.format("""
                SELECT name FROM genre
                    JOIN game_genre g on genre.geid = g.geid
                    WHERE g.gid = '%d'
                """, gid);
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            ArrayList<String> genres = new ArrayList<>();
            while (rs.next()) {
                genres.add(rs.getString("name"));
            }
            return genres.toArray(new String[0]);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets a game's publisher
     *
     * @param gid The game's id
     * @return The Compnay that published the game
     */
    private Company getGamePublisher(int gid) {
        String query = String.format("""
                SELECT d.compid, "name" from company
                    LEFT JOIN publish p on company.compid = p.compid
                    WHERE p.gid = '%d'""", gid);
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            if (rs.next()) {
                int compId = rs.getInt("compid");
                String compName = rs.getString("name");
                return new Company(compId, compName);
            } else {
                System.out.println("Game has no publisher.");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets a game's developer
     *
     * @param gid The game's id
     * @return The Compnay that developed the game
     */
    private Company getGameDeveloper(int gid) {
        String query = String.format("""
                SELECT d.compid, "name" from company
                    LEFT JOIN develop d on company.compid = d.compid
                    WHERE d.gid = '%d'""", gid);
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            if (rs.next()) {
                int compId = rs.getInt("compid");
                String compName = rs.getString("name");
                return new Company(compId, compName);
            } else {
                System.out.println("Game has no developer.");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the total playtime for a game for the current user.
     *
     * @param gid The game's id
     * @return Total playtime as a Time
     */
    private Time getTotalGamePlaytimeUser(int gid) {
        String q = String.format("""
                SELECT sum(time_played) FROM plays
                    WHERE gid = '%d' AND username = '%s'""", gid, currentUser.username());
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery(q);
            if (rs.next()) {
                return rs.getTime("sum");
            } else {
                return new Time(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the total playtime for a game.
     *
     * @param gid The game's id
     * @return Total playtime as a Time
     */
    private Time getTotalGamePlaytime(int gid) {
        String q = String.format("""
                SELECT sum(time_played) FROM plays
                    WHERE gid = '%d'""", gid);
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery(q);
            if (rs.next()) {
                return rs.getTime("sum");
            } else {
                return new Time(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets a game given the game's id
     *
     * @param gid The game's id
     * @return The matching Game
     */
    private Game getGame(int gid) {
        String query = String.format("""
                SELECT title, esrb_rating from game
                    WHERE gid = '%d'""", gid);
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            if (rs.next()) {
                String title = rs.getString("title");
                String esrbRating = rs.getString("esrb_rating");
                int[] ratings = getGameRatings(gid);
                String[] genres = getGameGenres(gid);
                Company dev = getGameDeveloper(gid);
                Company pub = getGamePublisher(gid);
                Time playtime = getTotalGamePlaytimeUser(gid);
                return new Game(gid, title, esrbRating, ratings, genres, dev, pub, playtime);
            } else {
                System.out.println("No game found");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Main method of this database application
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println(USAGE);
            System.exit(1);
        }

        CommandLineInterface cli = new CommandLineInterface(args[0], args[1]);
        cli.launch();
    }
}
