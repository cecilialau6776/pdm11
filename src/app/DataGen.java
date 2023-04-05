package app;

import app.model.Game;
import app.model.User;

import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class DataGen extends App {

    private static final String[] DOMAIN_NAMES = {"gmail.com", "aol.com", "ymail.com", "yahoo.com"};
    private static final String[] ESRB_RATINGS = {"E", "E 10", "T", "M", "AO", "RP", "RP Likely Mature 17"};
    private final ArrayList<String> firstnames;
    private final ArrayList<String> lastnames;
    private ArrayList<String> gamenames;
    private ArrayList<String> appnames;
    Random rnd;
    private static final long DAY = Time.valueOf("24:00:00").getTime();

    private PreparedStatement insertCompanyStatement;
    private PreparedStatement getRandGamesQuery;
    private PreparedStatement insertUserStatement;
    private PreparedStatement insertGameStatement;
    private PreparedStatement getGameByTitle;
    private PreparedStatement getRandCompany;
    private PreparedStatement insertDeveloper;
    private PreparedStatement insertPublisher;
    private PreparedStatement insertGamePlatform;
    private PreparedStatement insertGameGenre;
    private PreparedStatement getRandGenres;
    private PreparedStatement getRandPlatform;
    private PreparedStatement getUsers;
    private PreparedStatement insertCollection;
    private PreparedStatement insertGameIntoCollection;
    private PreparedStatement getCollection;
    private PreparedStatement insertPlay;
    private PreparedStatement insertFriend;
    private PreparedStatement getRandUsers;
    private PreparedStatement insertOwnedPlatform;
    private PreparedStatement insertRating;

    /**
     * Creates an App object by establishing a connection to our SQL server
     *
     * @param cs_username Credentials to log into starbug sql server
     * @param cs_password ...
     */
    public DataGen(String cs_username, String cs_password) {
        super(cs_username, cs_password);
        rnd = new Random();
        firstnames = new ArrayList<>();
        lastnames = new ArrayList<>();
        HashSet<String> hs = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader("names.txt"))) {
            while (br.ready()) {
                firstnames.add(br.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try (BufferedReader br = new BufferedReader(new FileReader("lastnames.txt"))) {
            while (br.ready()) {
                lastnames.add(br.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try (BufferedReader br = new BufferedReader(new FileReader("appnames.txt"))) {
            while (br.ready()) {
                hs.add(br.readLine());
            }
            appnames = new ArrayList<>(hs);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try (BufferedReader br = new BufferedReader(new FileReader("gameNames.txt"))) {
            hs.clear();
            while (br.ready()) {
                String n = br.readLine().strip();
                if (n.length() == 0 || n.length() > 100) continue;
                hs.add(n);
            }
            gamenames = new ArrayList<>(hs);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            insertCompanyStatement = conn.prepareStatement("INSERT INTO company (name) VALUES (?)");
            insertUserStatement = conn.prepareStatement("INSERT INTO \"user\" (username, password, email, firstname, lastname, last_access_date, creation_date) VALUES(?, ?, ?, ?, ?, ?, ?)");
            insertGameStatement = conn.prepareStatement("INSERT INTO game (title, esrb_rating) VALUES (?, ?)");
            insertDeveloper = conn.prepareStatement("INSERT INTO develop (gid, compid) VALUES (?, ?)");
            insertPublisher = conn.prepareStatement("INSERT INTO publish (gid, compid) VALUES (?, ?)");
            insertGamePlatform = conn.prepareStatement("INSERT INTO game_platform (gid, pid, release_date, price) VALUES (?, ?, ?, ?)");
            insertGameGenre = conn.prepareStatement("INSERT INTO game_genre (gid, geid) VALUES (?, ?)");
            getRandCompany = conn.prepareStatement("SELECT compid FROM company ORDER BY random() limit 1");
            getRandGamesQuery = conn.prepareStatement("SELECT gid FROM game ORDER BY random() limit ?");
            getRandGenres = conn.prepareStatement("SELECT geid FROM genre ORDER BY random() limit ?");
            getRandPlatform = conn.prepareStatement("SELECT pid FROM platform ORDER BY random() limit ?");
            getRandUsers = conn.prepareStatement("SELECT username FROM \"user\" ORDER BY random() limit ?");
            getGameByTitle = conn.prepareStatement("SELECT gid FROM game WHERE title = ?");
            insertRating = conn.prepareStatement("INSERT INTO ratings (username, gid, star_rating) VALUES(?, ?, ?)");
            insertPlay = conn.prepareStatement("INSERT INTO plays (username, gid, play_date, time_played) VALUES(?, ?, ?, ?)");
            insertCollection = conn.prepareStatement("INSERT INTO collection (coll_username, coll_name) VALUES (?, ?)");
            insertGameIntoCollection = conn.prepareStatement("INSERT INTO game_collection (gid, coll_id) VALUES (?, ?)");
            insertFriend = conn.prepareStatement("INSERT INTO friends (\"user\", friend) VALUES (?, ?)");
            getCollection = conn.prepareStatement("SELECT collid FROM collection WHERE coll_username = ? AND coll_name = ?");
            insertOwnedPlatform = conn.prepareStatement("INSERT INTO owned_platform (username, pid) VALUES (?, ?)");
            getUsers = conn.prepareStatement("SELECT username FROM \"user\"");
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void insertCompanies() {
        HashSet<String> companies = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader("devnames.txt"))) {
            while (br.ready()) {
                companies.add(br.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try (BufferedReader br = new BufferedReader(new FileReader("pubnames.txt"))) {
            while (br.ready()) {
                companies.add(br.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        for (String c : companies) {
            insertCompany(c);
        }
    }

    public void insertCompany(String name) {
        try {
            insertCompanyStatement.setString(1, name);
            insertCompanyStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User genUser() {
        String username = genUsername();
        String password = genPassword();
        String email = genEmail(username);
        String firstname = getFirstName();
        String lastname = getLastName();
        Date today = Date.valueOf("2022-4-3");
        Date cd = genDate(Date.valueOf("2002-1-1"), today);
        Date lad = genDate(cd, today);

        return new User(username, password, email, firstname, lastname, lad, cd);
    }

    private Date genDate(Date after, Date before) {
        return new Date(rnd.nextLong(after.getTime(), before.getTime()));
    }

    private String getFirstName() {
        return firstnames.get(rnd.nextInt(firstnames.size()));
    }

    private String getLastName() {
        char[] name = lastnames.get(rnd.nextInt(lastnames.size())).toLowerCase().toCharArray();
        name[0] = Character.toUpperCase(name[0]);
        return new String(name);
    }

    private String genEmail(String username) {
        return username + "@" + DOMAIN_NAMES[rnd.nextInt(DOMAIN_NAMES.length)];
    }

    private String genPassword() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rnd.nextInt(8, 32); i++) {
            sb.append((char) (rnd.nextInt(32, 126)));
        }
        return sb.toString();
    }

    private String genUsername() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rnd.nextInt(4, 16); i++) {
            sb.append((char) rnd.nextInt('a', 'z' + 1));
        }
        for (int i = 0; i < rnd.nextInt(4); i++) {
            sb.append(rnd.nextInt(10));
        }
        return sb.toString();
    }

    private void insertUser(User u) {
        try {
            insertUserStatement.setString(1, u.username());
            insertUserStatement.setString(2, u.password());
            insertUserStatement.setString(3, u.email());
            insertUserStatement.setString(4, u.firstname());
            insertUserStatement.setString(5, u.lastname());
            insertUserStatement.setDate(6, u.last_access_date());
            insertUserStatement.setDate(7, u.creation_date());
            insertUserStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Integer> genAndInsertCollections(User user) {
        Collections.shuffle(appnames);
        ArrayList<Integer> gidList = new ArrayList<>();
        for (int i = 0; i < rnd.nextInt(6); i++) {
            gidList.addAll(genAndInsertCollection(user, appnames.get(i)));
        }
        return gidList;
    }

    private ArrayList<Integer> genAndInsertCollection(User user, String collName) {
        try {
            ArrayList<Integer> gids = new ArrayList<>();
            insertCollection.setString(1, user.username());
            insertCollection.setString(2, collName);
            insertCollection.execute();
            getRandGamesQuery.setInt(1, rnd.nextInt(20));
            ResultSet rs;
            getCollection.setString(1, user.username());
            getCollection.setString(2, collName);
            rs = getCollection.executeQuery();
            if (rs.next()) {
                int collId = rs.getInt("collid");
                rs = getRandGamesQuery.executeQuery();
                while (rs.next()) {
                    int gid = rs.getInt("gid");
                    gids.add(gid);
                    insertGameIntoCollection.setInt(1, gid);
                    insertGameIntoCollection.setInt(2, collId);
                }
                return gids;
            } else {
                throw new SQLException("Couldn't get collection");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void genAndInsertGames() {
        System.out.printf("Inserting %d games\n", gamenames.size());
        int i = 0;
        for (String name : gamenames) {
            genAndInsertGame(name);
            if (i % 100 == 0) {
                System.out.printf("Inserted %d games\n", i);
            }
            i++;
        }
        System.out.printf("Inserted %d games\n", i);
    }

    private void genAndInsertGame(String name) {
        try {
            // insert into game
            String esrb = ESRB_RATINGS[rnd.nextInt(ESRB_RATINGS.length)];
            insertGameStatement.setString(1, name);
            insertGameStatement.setString(2, esrb);
            insertGameStatement.execute();

            getGameByTitle.setString(1, name);
            ResultSet rs = getGameByTitle.executeQuery();
            if (rs.next()) {
                int gid = rs.getInt("gid");

                // insert into develop and publish
                insertPublisher.clearParameters();
                insertDeveloper.clearParameters();
                insertPublisher.setInt(1, gid);
                insertDeveloper.setInt(1, gid);
                ResultSet r = getRandCompany.executeQuery();
                if (r.next()) {
                    insertPublisher.setInt(2, r.getInt("compid"));
                } else {
                    throw new SQLException("failed to get random company");
                }
                r = getRandCompany.executeQuery();
                if (r.next()) {
                    insertDeveloper.setInt(2, r.getInt("compid"));
                } else {
                    throw new SQLException("failed to get random company");
                }
                insertDeveloper.execute();
                insertPublisher.execute();

                // insert into platform
                getRandPlatform.setInt(1, rnd.nextInt(1, 5));
                r = getRandPlatform.executeQuery();
                double price = rnd.nextDouble(0.00, 120.00);
                Date releaseDate = genDate(Date.valueOf("1990-1-1"), Date.valueOf("2023-4-4"));
                while (r.next()) {
                    insertGamePlatform.setInt(1, gid);
                    insertGamePlatform.setInt(2, r.getInt("pid"));
                    insertGamePlatform.setDate(3, releaseDate);
                    insertGamePlatform.setDouble(4, price + rnd.nextDouble(-10.00, 10.00));
                    insertGamePlatform.execute();
                }
                // insert into genre
                getRandGenres.setInt(1, rnd.nextInt(1, 10));
                r = getRandGenres.executeQuery();
                while (r.next()) {
                    insertGameGenre.setInt(1, gid);
                    insertGameGenre.setInt(2, r.getInt("geid"));
                    insertGameGenre.execute();
                }

            } else {
                throw new SQLException("game not in db?");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private Game[] getGameArr(int count) {
        try {
            getRandGamesQuery.setInt(1, count);
            ResultSet rs = getRandGamesQuery.executeQuery();
            ArrayList<Game> g = new ArrayList<>();
            while (rs.next()) {
                g.add(getGame(rs.getInt("gid")));
            }
            return g.toArray(new Game[0]);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void genAndInsertRatings(User u, int gid) {
        if (rnd.nextInt(4) == 0) {
            try {
                insertRating.setString(1, u.username());
                insertRating.setInt(2, gid);
                insertRating.setInt(3, rnd.nextInt(6));
                insertRating.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void genAndInsertUsers(int count) {
        try {
            for (int i = 0; i < count; i++) {
                User u = genUser();
                insertUser(u);
                ArrayList<Integer> gids = genAndInsertCollections(u);
                for (int gid : gids) {
                    genAndInsertPlays(u, gid, rnd.nextInt(100));
                    genAndInsertRatings(u, gid);
                }
                getRandPlatform.setInt(1, rnd.nextInt(7));
                ResultSet rs = getRandPlatform.executeQuery();
                while (rs.next()) {
                    insertOwnedPlatform.setString(1, u.username());
                    insertOwnedPlatform.setInt(2, rs.getInt("pid"));
                    insertOwnedPlatform.execute();
                }
                if (i % 100 == 0) System.out.printf("Inserted %d users\n", i);
            }
            System.out.printf("Inserted %d users\n", count);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void genFriends() {
        try {
            ResultSet users = getUsers.executeQuery();
            while (users.next()) {
                String username = users.getString("username");
                getRandUsers.setInt(1, rnd.nextInt(30));
                ResultSet randUsers = getRandUsers.executeQuery();
                while (randUsers.next()) {
                    insertFriend.setString(1, username);
                    insertFriend.setString(2, randUsers.getString("username"));
                    insertFriend.execute();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Time genTime(Time min, Time max) {
        return new Time(rnd.nextLong(min.getTime(), max.getTime()));
    }

    private Time genTime(Time max) {
        return genTime(new Time(0), max);
    }

    private void genAndInsertPlays(User u, int gid, int count) {
        if (count == 0) return;
        try {
            long maxInc = new Date(u.last_access_date().getTime() / count).getTime();
            Date start = genDate(u.creation_date(), new Date(u.creation_date().getTime() + maxInc));
            Time playtime = genTime(Time.valueOf("8:00:00"));
            for (int i = 0; i < count; i++) {
                insertPlay.setString(1, u.username());
                insertPlay.setInt(2, gid);
                insertPlay.setDate(3, start);
                insertPlay.setTime(4, playtime);
                insertPlay.execute();
                long newStart = start.getTime() + DAY;
                start = genDate(new Date(newStart), new Date(newStart + maxInc));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println(USAGE);
            System.exit(1);
        }
        DataGen dg = new DataGen(args[0], args[1]);
        dg.genFriends();
        dg.close();
    }
}
