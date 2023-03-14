package app;

import com.jcraft.jsch.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

/**
 * Database application object.
 *
 * In a Model-View-Controller architecture this is the controller.
 *
 * @author Damon Gonzalez
 */
public class App {

    /** Initial message to user */
    private final static String msg= "Principles of Data Management, Group 11, Database Application";

    /** The name of our group's database on starbug */
    private final static String databaseName = "p320_11";

    /** The connection to the server, SQL statements are executed via this variable */
    private Connection conn;

    /** The SSH session on the server */
    private Session session;

    /**
     * Creates an App object by establishing a connection to our SQL server
     */
    public App(){
        System.out.println(msg + "\n\n");
        Scanner in = new Scanner(System.in);
        System.out.print("Enter your cs username -> ");
        String user = in.nextLine();
        System.out.print("Enter your cs password -> ");
        String password = in.nextLine();
        in.close();

        int lport = 5432;
        String rhost = "starbug.cs.rit.edu";
        int rport = 5432;

        String driverName = "org.postgresql.Driver";
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            session = jsch.getSession(user, rhost, 22);
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
            props.put("user", user);
            props.put("password", password);

            Class.forName(driverName);
            conn = DriverManager.getConnection(url, props);
            System.out.println("Database connection established");

            // Do something with the database....

        } catch(Exception e){
            e.printStackTrace();
            System.err.println("Connection failed, exiting application...");
            System.exit(1);
        }
    }

    /**
     * Close database connection and exit application
     */
    public void exit(){
        try{
            conn.close();
            session.disconnect();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Main method of this database application
     * @param args command line arguments
     */
    public static void main(String[] args) {
        App app = new App();
        app.exit();//close the app immediately
        //TODO
    }
}
