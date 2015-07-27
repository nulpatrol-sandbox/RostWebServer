package khaniukov.server.controller;

import khaniukov.server.AccessControl.AccessControl;
import khaniukov.server.Config;
import khaniukov.server.Utils;
import khaniukov.server.WebServer;
import khaniukov.server.model.AppModel;
import khaniukov.server.model.SimpleAppModel;
import khaniukov.server.view.AppView;
import khaniukov.server.view.SwingTextAreaAppView;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerController implements ActionListener {
    public static final Logger errorLogger   = LogManager.getLogger("console");
    public static final Logger requestLogger = LogManager.getLogger("requests");
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
                try {
                    AccessControl.checkAccess(client, new File("www/index.php"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new WebServer(client, controller).run();
            }
        } catch (IOException e) {
            ServerController.errorLogger.error("[I/O error]" + e.getMessage());
            System.exit(-1);
        }
    }

    public ServerController(AppModel model) {
        this.model = model;
    }

    public void createView() {
        AppView view = new SwingTextAreaAppView(model);
        view.show();
    }

    public void actionPerformed(ActionEvent event) {
        WebServer server = (WebServer)event.getSource();
        if (event.getActionCommand().equals(WebServer.ACTION_UPDATE)) {
            try {
                model.setMessage(server.getRequestedResource());
            }
            catch (Exception e) {
                /*  */
            }
        }
    }
}