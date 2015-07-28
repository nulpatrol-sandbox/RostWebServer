package khaniukov.server.controller;

import khaniukov.server.AccessControl.AccessControl;
import khaniukov.server.Config;
import khaniukov.server.Utils;
import khaniukov.server.WebServer;
import khaniukov.server.model.*;
import khaniukov.server.view.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Main application controller
 */
public class ServerController implements ActionListener {
    /**
     * Logger for errors
     */
    public static final Logger errorLogger   = LogManager.getLogger("console");
    /**
     * Logger for HTTP-requests
     */
    public static final Logger requestLogger = LogManager.getLogger("requests");
    /**
     * Model for storing HTTP-requests data
     */
    private AppModel model;

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
                new WebServer(client, controller).run();
            }
        } catch (IOException e) {
            Utils.logStackTrace(e);
        }
    }

    /**
     * Constuct ServerController with specified model
     * @param model model for storing data
     */
    public ServerController(AppModel model) {
        this.model = model;
    }

    /**
     * Create view and show it
     */
    public void createView() {
        AppView view = new SwingTextAreaAppView(model);
        view.show();
    }

    /**
     * Method for reacting to an event
     * @param event an event that has occured
     */
    public void actionPerformed(ActionEvent event) {
        WebServer server = (WebServer)event.getSource();
        if (event.getActionCommand().equals(WebServer.ACTION_UPDATE)) {
            model.setMessage(server.getRequestedResource());
        }
    }
}