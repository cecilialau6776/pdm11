package app;

import app.cli.CommandLineInterface;
import app.model.*;
import at.favre.lib.crypto.bcrypt.BCrypt;
import app.model.Collection;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.sql.*;
<<<<<<< HEAD
import java.util.ArrayList;
import java.util.Properties;
=======
import java.sql.Date;
import java.util.*;
>>>>>>> b4319af (added the user profile functionality)

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

    private PreparedStatement login, updateLastAccessDate, getUserPlatforms, getGamePlatforms, getTotalPlaytimeCollection, getUserCollections, getGamesInCollection, getCollecitonInfo, getCollectionByName, addToCollection, removeGameFromCollection, removeCollection, renameCollection, createCollection, searchGame, searchGamePrice, searchGamePlatform, searchGameReleaseDate, searchGameDeveloper, searchGameGenre, rate, play, searchUsers, addFriend, removeFriend, searchFriend, buyPlatform, getPlatforms, getUser, getGameRatings, getGameGenres, getGamePublisher, getGameDeveloper, getTotalGamePlaytimeUser, getTotalGamePlaytime, getDateRelease, getPrice, getGame, checkQuery, signUp;

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

            // Prepare our statements
            login = conn.prepareStatement("SELECT username, password, email, firstname, lastname, creation_date FROM \"user\" WHERE username = ?");
            updateLastAccessDate = conn.prepareStatement("UPDATE \"user\" SET last_access_date = ? WHERE username = ?");
            getUserPlatforms = conn.prepareStatement("SELECT p.pid, p.name FROM owned_platform op JOIN platform p ON op.pid = p.pid WHERE username = ?");
            getGamePlatforms = conn.prepareStatement(" SELECT platform.pid, \"name\" FROM platform JOIN game_platform gp on platform.pid = gp.pid WHERE gp.gid = ?");
            getTotalPlaytimeCollection = conn.prepareStatement("SELECT sum(time_played) FROM plays JOIN game g on g.gid = plays.gid JOIN game_collection gc on g.gid = gc.gid WHERE gc.collid = ? and plays.username = ?");
            getUserCollections = conn.prepareStatement("SELECT collid FROM collection WHERE coll_username = ?");
            getGamesInCollection = conn.prepareStatement("SELECT gid from game_collection WHERE collid = ?");
            getCollecitonInfo = conn.prepareStatement("SELECT coll_username, coll_name FROM collection WHERE collid = ?");
            getCollectionByName = conn.prepareStatement("SELECT collid FROM collection WHERE UPPER(coll_name) = UPPER(?) AND coll_username = ?");
            addToCollection = conn.prepareStatement("INSERT INTO game_collection (collid, gid) VALUES (?, ?)");
            removeGameFromCollection = conn.prepareStatement("DELETE FROM game_collection WHERE collid = ? AND gid = ?");
            removeCollection = conn.prepareStatement("DELETE FROM collection WHERE collid = ?");
            renameCollection = conn.prepareStatement("UPDATE collection SET coll_name = ? WHERE collid = ?");
            createCollection = conn.prepareStatement("INSERT INTO collection (coll_username, coll_name) VALUES (?, ?)");
            searchGame = conn.prepareStatement("SELECT gid FROM game WHERE UPPER(title) = UPPER(?)");
            searchGamePrice = conn.prepareStatement("SELECT gid FROM game_platform WHERE price = ?");
            searchGamePlatform = conn.prepareStatement("SELECT gid FROM game_platform JOIN platform p on game_platform.pid = p.pid WHERE UPPER(p.name) = UPPER(?)");
            searchGameReleaseDate = conn.prepareStatement("SELECT gid FROM game_platform WHERE release_date = ?");
            searchGameDeveloper = conn.prepareStatement("SELECT gid FROM develop JOIN company c on develop.compid = c.compid WHERE UPPER(c.name) = UPPER(?)");
            searchGameGenre = conn.prepareStatement("SELECT gid FROM game_genre JOIN genre g on g.geid = game_genre.geid WHERE UPPER(g.name) = UPPER(?)");
            rate = conn.prepareStatement("INSERT INTO ratings (username, gid, star_rating) VALUES(?, ?, ?)");
            play = conn.prepareStatement("INSERT INTO plays (username, gid, play_date, time_played) VALUES (?, ?, ?, ?)");
            searchUsers = conn.prepareStatement("SELECT * FROM \"user\" WHERE email = ?");
            addFriend = conn.prepareStatement("INSERT INTO friends (\"user\", friend) VALUES (?, ?)");
            removeFriend = conn.prepareStatement("DELETE FROM friends WHERE \"user\" = ? AND friend = ?");
            searchFriend = conn.prepareStatement("SELECT friend FROM friends WHERE \"user\" = ?");
            buyPlatform = conn.prepareStatement("INSERT INTO owned_platform (username, pid) VALUES (?, ?)");
            getPlatforms = conn.prepareStatement("SELECT * FROM \"platform\" WHERE UPPER(name) = UPPER(?)");
            getUser = conn.prepareStatement("SELECT * FROM \"user\" WHERE username = ?");
            getGameRatings = conn.prepareStatement("SELECT star_rating FROM ratings JOIN game g on ratings.gid = g.gid WHERE g.gid = ?");
            getGameGenres = conn.prepareStatement("SELECT name FROM genre JOIN game_genre g on genre.geid = g.geid WHERE g.gid = ?");
            getGamePublisher = conn.prepareStatement("SELECT p.compid, \"name\" from company LEFT JOIN publish p on company.compid = p.compid WHERE p.gid = ?");
            getGameDeveloper = conn.prepareStatement("SELECT d.compid, \"name\" from company LEFT JOIN develop d on company.compid = d.compid WHERE d.gid = ?");
            getTotalGamePlaytimeUser = conn.prepareStatement("SELECT sum(time_played) FROM plays WHERE gid = ? AND username = ?");
            getTotalGamePlaytime = conn.prepareStatement("SELECT sum(time_played) FROM plays WHERE gid = ?");
            getDateRelease = conn.prepareStatement("SELECT MIN(release_date) FROM game_platform WHERE gid = ?");
            getPrice = conn.prepareStatement("SELECT MIN(price) FROM game_platform WHERE gid = ?");
            getGame = conn.prepareStatement("SELECT title, esrb_rating from game WHERE gid = ?");
            checkQuery = conn.prepareStatement("SELECT username FROM \"user\" WHERE username = ?");
            signUp = conn.prepareStatement("INSERT INTO \"user\" VALUES (?, ?, ?, ?, ?, ?, ?)");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Connection failed, exiting application...");
            exit(1);//exit on error
        }
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

        try {
            login.setString(1, username);
            ResultSet rs = login.executeQuery();
            if (rs.next()) {
                String rs_username = rs.getString("username");
                String rsPwHash = rs.getString("password");
                String rs_email = rs.getString("email");
                String rs_firstname = rs.getString("firstname");
                String rs_lastname = rs.getString("lastname");
                Date rs_creation_date = rs.getDate("creation_date");

                BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), rsPwHash);
                if (!result.verified) {
                    System.out.println("Incorrect password");
                }
                Date current_date = new Date(new java.util.Date().getTime());//to update last access date
                updateLastAccessDate.setDate(1, current_date);
                updateLastAccessDate.setString(2, username);
                updateLastAccessDate.executeUpdate();

                currentUser = new User(rs_username, rsPwHash, rs_email,
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
        try {
            getUserPlatforms.setString(1, currentUser.username());
            ResultSet rs = getUserPlatforms.executeQuery();
            ArrayList<Platform> platforms = new ArrayList<>();
            while (rs.next()) {
                int pid = rs.getInt("pid");
                String name = rs.getString("name");
                platforms.add(new Platform(pid, name));
            }
            return platforms.toArray(new Platform[0]);
        } catch (Exception e) {
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
        try {
            getGamePlatforms.setInt(1, game.gid());
            ResultSet rs = getGamePlatforms.executeQuery();
            ArrayList<Platform> platforms = new ArrayList<>();
            while (rs.next()) {
                int pid = rs.getInt("pid");
                String name = rs.getString("name");
                platforms.add(new Platform(pid, name));
            }
            return platforms.toArray(new Platform[0]);
        } catch (Exception e) {
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
        try {
            getTotalPlaytimeCollection.setInt(1, collection.collid());
            getTotalPlaytimeCollection.setString(2, currentUser.username());
            ResultSet rs = getTotalPlaytimeCollection.executeQuery();
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
        Date sqlDate = new Date(new java.util.Date().getTime()); //the current date in sql format
        try {
            checkQuery.setString(1, username);
            ResultSet rs = checkQuery.executeQuery();
            if (!rs.next()) {//the username is not used by anybody else
                String hashedPw = BCrypt.withDefaults().hashToString(12, password.toCharArray());
                signUp.setString(1, username);
                signUp.setString(2, hashedPw);
                signUp.setString(3, email);
                signUp.setString(4, firstname);
                signUp.setString(5, lastname);
                signUp.setDate(6, sqlDate);
                signUp.setDate(7, sqlDate);
                signUp.executeUpdate();
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
        try {
            getUserCollections.setString(1, currentUser.username());
            ResultSet rs = getUserCollections.executeQuery();
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
        try {
            getGamesInCollection.setInt(1, collid);
            getCollecitonInfo.setInt(1, collid);
            ResultSet rs = getCollecitonInfo.executeQuery();
            if (rs.next()) {
                String collUsername = rs.getString("coll_username");
                String collName = rs.getString("coll_name");
                rs = getGamesInCollection.executeQuery();
                ArrayList<Game> games = new ArrayList<>();
                while (rs.next()) {
                    games.add(getGame(rs.getInt("gid")));
                }
                return new Collection(collid, collUsername, collName, games.toArray(new Game[0]));
            } else {
                System.err.println("No collection with id " + collid);
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
        try {
            getCollectionByName.setString(1, name);
            getCollectionByName.setString(2, currentUser.username());
            ResultSet rs = getCollectionByName.executeQuery();
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
        try {
            addToCollection.setInt(1, collection.collid());
            addToCollection.setInt(2, game.gid());
            addToCollection.execute();
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
        try {
            removeGameFromCollection.setInt(1, collection.collid());
            removeGameFromCollection.setInt(1, game.gid());
            removeGameFromCollection.execute();
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
        try {
            removeCollection.setInt(1, collection.collid());
            removeCollection.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Rename a collection the user has. Collection is confirmed as valid
     * prior to the call of the function.
     *
     * @param collection The collection to rename
     * @param newName    The new name
     * @return The updated collection
     */
    @Override
    public Collection collection_rename(Collection collection, String newName) {
        try {
            renameCollection.setString(1, newName);
            renameCollection.setInt(2, collection.collid());
            renameCollection.execute();
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
        try {
            createCollection.setString(1, currentUser.username());
            createCollection.setString(2, name);
            createCollection.execute();
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
        try {
            searchGame.setString(1, name);
            ResultSet rs = searchGame.executeQuery();
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
        try {
            searchGamePrice.setDouble(1, price);
            ResultSet rs = searchGamePrice.executeQuery();
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
        try {
            searchGamePlatform.setString(1, platform);
            ResultSet rs = searchGamePlatform.executeQuery();
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
        try {
            searchGameReleaseDate.setDate(1, release_date);
            ResultSet rs = searchGameReleaseDate.executeQuery();
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
        try {
            searchGameDeveloper.setString(1, developer);
            ResultSet rs = searchGameDeveloper.executeQuery();
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
        try {
            searchGameGenre.setString(1, genre);
            ResultSet rs = searchGameGenre.executeQuery();
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
        try {
            rate.setString(1, currentUser.username());
            rate.setInt(2, game.gid());
            rate.setInt(2, rating);
            rate.execute();
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
        try {
            play.setString(1, currentUser.username());
            play.setInt(2, game.gid());
            play.setDate(3, current_date);
            play.setTime(4, time);
            play.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Searches the database for a user based on their email.
     *
     * @param email The user's email
     * @return The user information if found, null if not
     */
    @Override
    public User[] search_users(String email) {
        try {
            searchUsers.setString(1, email);
            ResultSet rs = searchUsers.executeQuery();
            ArrayList<User> users = new ArrayList<>();
            while (rs.next()) {
                String rs_username = rs.getString("username");
                String rs_password = rs.getString("password");
                String rs_email = rs.getString("email");
                String rs_firstname = rs.getString("firstname");
                String rs_lastname = rs.getString("lastname");
                Date rs_creation_date = rs.getDate("creation_date");
                Date rs_last_access_date = rs.getDate("last_access_date");
                users.add(new User(rs_username, rs_password, rs_email,
                        rs_firstname, rs_lastname, rs_last_access_date,
                        rs_creation_date)
                );
            }
            return users.toArray(new User[0]);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
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
        try {
            addFriend.setString(1, currentUser.username());
            addFriend.setString(2, friend.username());
            addFriend.execute();
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
        try {
            removeFriend.setString(1, currentUser.username());
            removeFriend.setString(2, friend.username());
            removeFriend.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User[] search_friends() {
        try {
            searchFriend.setString(1, currentUser.username());
            ResultSet rs = searchFriend.executeQuery();
            ArrayList<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(getUser(rs.getString("friend")));
            }
            return users.toArray(new User[0]);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Adds a platform to your collection. Platform is assumed valid before execution.
     *
     * @param platform The platform to buy
     */
    @Override
    public void buy_platform(Platform platform) {
        try {
            buyPlatform.setString(1, currentUser.username());
            buyPlatform.setInt(2, platform.pid());
            buyPlatform.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets all the total platforms by name
     *
     * @param platform_name
     * @return Array of all platforms by name
     */
    @Override
    public Platform[] get_all_platform_name(String platform_name) {
        try {
            getPlatforms.setString(1, platform_name);
            ResultSet rs = getPlatforms.executeQuery();
            ArrayList<Platform> platforms = new ArrayList<>();
            while (rs.next()) {
                String rs_pid = rs.getString("pid");
                String rs_name = rs.getString("name");
                platforms.add(new Platform(Integer.parseInt(rs_pid), rs_name));
            }
            return platforms.toArray(new Platform[0]);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private User getUser(String username) {
        try {
            getUser.setString(1, username);
            ResultSet rs = getUser.executeQuery();
            if (rs.next()) {
                String rs_username = rs.getString("username");
                String rs_password = rs.getString("password");
                String rs_email = rs.getString("email");
                String rs_firstname = rs.getString("firstname");
                String rs_lastname = rs.getString("lastname");
                Date rs_creation_date = rs.getDate("creation_date");
                Date rs_last_access_date = rs.getDate("last_access_date");
                return new User(rs_username, rs_password, rs_email,
                        rs_firstname, rs_lastname, rs_last_access_date,
                        rs_creation_date);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Gets a list of the game's ratings
     *
     * @param gid The game's id
     * @return The game's ratings
     */
    private int[] getGameRatings(int gid) {
        try {
            getGameRatings.setInt(1, gid);
            ResultSet rs = getGameRatings.executeQuery();
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
        try {
            getGameGenres.setInt(1, gid);
            ResultSet rs = getGameGenres.executeQuery();
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
        try {
            getGamePublisher.setInt(1, gid);
            ResultSet rs = getGamePublisher.executeQuery();
            if (rs.next()) {
                int compId = rs.getInt("compid");
                String compName = rs.getString("name");
                return new Company(compId, compName);
            } else {
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
        try {
            getGameDeveloper.setInt(1, gid);
            ResultSet rs = getGameDeveloper.executeQuery();
            if (rs.next()) {
                int compId = rs.getInt("compid");
                String compName = rs.getString("name");
                return new Company(compId, compName);
            } else {
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
        try {
            getTotalGamePlaytimeUser.setInt(1, gid);
            getTotalGamePlaytimeUser.setString(2, currentUser.username());
            ResultSet rs = getTotalGamePlaytimeUser.executeQuery();
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
                """, gid);
        try {
            getTotalGamePlaytime.setInt(1, gid);
            ResultSet rs = getTotalGamePlaytime.executeQuery(q);
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
     * Gets the oldest date Release for a game
     *
     * @param gid the game's id
     * @return date release as a Date
     */
    private Date getDateRelease(int gid) {
        try {
            getDateRelease.setInt(1, gid);
            ResultSet rs = getDateRelease.executeQuery();
            if (rs.next()) {
                Date d = rs.getDate("min");
                return (d != null) ? d : new Date(0);
            } else {
                return new Date(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * get the cheapest price for a game
     *
     * @param gid the game's id
     * @return price of game as a double, -1 if not found or error.
     */
    private double getPrice(int gid) {
        try {
            getPrice.setInt(1, gid);
            ResultSet rs = getPrice.executeQuery();
            if (rs.next()) {
                return rs.getDouble("min");
            } else {
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }


    /**
     * Gets a game given the game's id
     *
     * @param gid The game's id
     * @return The matching Game
     */
    private Game getGame(int gid) {
        try {
            getGame.setInt(1, gid);
            ResultSet rs = getGame.executeQuery();
            if (rs.next()) {
                String title = rs.getString("title");
                String esrbRating = rs.getString("esrb_rating");
                int[] ratings = getGameRatings(gid);
                String[] genres = getGameGenres(gid);
                Company dev = getGameDeveloper(gid);
                Company pub = getGamePublisher(gid);
                Time playtime = getTotalGamePlaytimeUser(gid);
                Date dateRelease = getDateRelease(gid);
                double price = getPrice(gid);
                return new Game(gid, title, esrbRating, ratings, genres, dev, pub, playtime, dateRelease, price);
            } else {
                System.out.println("No game found");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public UserProfile get_profile() {
        String username = currentUser.username();
        int numCollections = get_collections().length;
        int numFollowers = num_followers();
        int numFollowing = num_following();
        Game[] topTen = get_topTen();
        return new UserProfile(username, numCollections, numFollowers, numFollowing, topTen);
    }

    /**
     * Gets the number of followers of the user currently logged
     * @return The number of followers
     */
    private int num_followers(){
        try {
            PreparedStatement statement = conn.prepareStatement("SELECT count(*) FROM friends " +
                    "WHERE friend = ?");
            statement.setString(1, currentUser.username());
            ResultSet rs = statement.executeQuery();
            if(rs.next())
                return rs.getInt("count");
        } catch (SQLException ignored){}
        return 0;
    }

    /**
     * Gets the number of people who this user is following
     * @return The number following
     */
    private int num_following(){
        try {
            PreparedStatement statement = conn.prepareStatement("SELECT count(*) FROM friends " +
                    "WHERE \"user\" = ?");
            statement.setString(1, currentUser.username());
            ResultSet rs = statement.executeQuery();
            if(rs.next())
                return rs.getInt("count");
        } catch (SQLException ignored){}
        return 0;
    }

    /**
     * Retrieves the top ten games of a user by total playtime of that game
     * for the current user. The length of the array will not exceed ten
     * @return The top ten games
     */
    private Game[] get_topTen(){
        try {
            PreparedStatement statement = conn.prepareStatement("SELECT gid FROM plays WHERE username = ? " +
                    "GROUP BY gid");
            statement.setString(1, currentUser.username());
            ResultSet rs = statement.executeQuery();
            List<Game> gids = new ArrayList<>();
            while(rs.next()){
                gids.add(getGame(rs.getInt(0)));
            }
            gids.sort(Comparator.comparing(Game::playtime));
            int size = 10;
            if(gids.size() < 10)
                size = gids.size();
            Game[] topTen = new Game[size];
            for(int i = 0; i < size; i++){
                topTen[i] = gids.get(i);
            }
            return topTen;
        } catch(SQLException ignored){}
        return new Game[0];
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
