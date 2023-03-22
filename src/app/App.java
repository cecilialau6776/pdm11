package app;

import app.cli.CommandLineInterface;
import app.model.*;
import com.jcraft.jsch.*;

import java.sql.*;
import java.sql.Date;
import java.util.Properties;

import java.util.*;

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
     * @param username The username of someone with a valid CS account to
     *                 connect to the postgreSQL server
     * @param password The password of ...
     */
    public App(String username, String password){
        System.out.println(MSG);

        int lport = 5432;
        String rhost = "starbug.cs.rit.edu";
        int rport = 5432;

        String driverName = "org.postgresql.Driver";
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            session = jsch.getSession(username, rhost, 22);
            session.setPassword(password);
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
            props.put("user", username);
            props.put("password", password);

            Class.forName(driverName);
            conn = DriverManager.getConnection(url, props);
            System.out.println("Database connection established");
        } catch(Exception e){
            e.printStackTrace();
            System.err.println("Connection failed, exiting application...");
            exit(1);//exit on error
        }
        //TODO do some more stuff
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
