package khaniukov.server;

import khaniukov.server.controller.ServerController;
import khaniukov.server.model.SimpleAppModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Main application class
 *
 * @author Rostislav Khaniukov
 */
public class App {

    public static final String FRAME_TITLE        = "RostWebServer";
    public static final Logger errorLogger   = LogManager.getLogger("console");
    public static final Logger requestLogger = LogManager.getLogger("requests");

    /**
     * Main application method
     */
    public static void main(String[] args) {
        ServerController controller = new ServerController(new SimpleAppModel());
        controller.createView();

        ServerSocket servers;
        Socket client;
        Config.initialize(WebServer.class.getResource("/config.xml").getFile());

        try {
            servers = new ServerSocket(Config.getIntParam("Port"));
            while (true) {
                client = servers.accept();
                new WebServer(client).run();
            }
        } catch (IOException e) {
            errorLogger.error("[I/O error]" + e.getMessage());
            System.exit(-1);
        }
    }
}
