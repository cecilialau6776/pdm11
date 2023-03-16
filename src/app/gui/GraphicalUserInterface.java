package app.gui;

import app.IApp;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.*;

/**
 * A graphical user interface to this database application
 */
public class GraphicalUserInterface extends Application {

    /** Exit message for when this GUI closes*/
    private final static String EXIT_MESSAGE = "GUI application closing";

    /** The app this interface communicates with */
    private IApp app;

    @Override
    public void init() throws Exception {
        List<String> parameters = getParameters().getRaw();
        String username = parameters.get(0);
        String password = parameters.get(1);
        app = IApp.createApp(username, password);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.show();
        //TODO
    }

    /**
     * Prints exit message to standard output and calls .exit() on database app
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        System.out.println(EXIT_MESSAGE);
        app.exit();
    }
}
