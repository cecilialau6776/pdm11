package app;

import app.cli.CommandLineInterface;
import com.jcraft.jsch.*;

import java.sql.Connection;
import java.sql.DriverManager;
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

            // Do something with the database....

        } catch(Exception e){
            e.printStackTrace();
            System.err.println("Connection failed, exiting application...");
            exit(1);//exit on error
        }

        //TODO
    }

    @Override
    public boolean isUserLoggedIn() {
        //TODO
        return false;
    }

    @Override
    public boolean logIn(String username, String password) {
        //TODO
        return false;
    }

    @Override
    public void logOut() {
        //TODO
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
