package khaniukov.server;

import khaniukov.server.AccessControl.AccessControl;
import khaniukov.server.Http.Request;
import khaniukov.server.Http.Response;
import khaniukov.server.controller.ServerController;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

/**
 * Main web-server functionality
 *
 * @author Rostislav Khaniukov
 */
public class WebServer implements Runnable {
    /**
     * Enumeration which consist of all supported methods
     */
    public enum HttpMethods { GET, OPTIONS, POST, UNKNOWN }
    public static String ACTION_UPDATE = "update";

    private Socket           client  = null;
    private BufferedReader   in      = null;
    private DataOutputStream out     = null;
    private Request request;
    private ArrayList<ActionListener> listeners = new ArrayList<>();

    /**
     * Constructs WebServer
     *
     * @param client Client socket
     * @param controller Main application controller
     * @throws IOException
     */
    public WebServer(Socket client, ServerController controller) throws IOException {
        addActionListener(controller);
        this.client = client;
        this.in     = new BufferedReader(new InputStreamReader(client.getInputStream()));
        this.out    = new DataOutputStream(client.getOutputStream());
    }

    /**
     * Add action listener
     *
     * @param listener Action listener
     */
    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    /**
     * Fire action to action listener
     *
     * @param command Action command
     */
    protected void fireAction(String command) {
        ActionEvent event = new ActionEvent(this, 0, command);
        for (ActionListener listener : listeners) {
            listener.actionPerformed(event);
        }
    }

    /**
     * Method to access method field, which store the HTTP method.
     *
     * @return HTTP method of request
     */
    public String getRequestedResource() {
        return request.getRequestedResource();
    }

    /**
     * Method calling on new thread creation
     */
    public void run() {
        try {
            httpQueryHandler(in, out);
        } catch (IOException e) {
            Utils.logStackTrace(e);
        } finally {
            try {
                out.close();
                in.close();
                client.close();
            } catch (IOException e) {
                Utils.logStackTrace(e);
            }
        }
    }

    /**
     * Send file to output
     * @param path Path to file
     * @param output DataOutputStream for sending file
     * @throws IOException
     */
    private void sendFile(String path, DataOutputStream output) throws IOException {
        try {
            if (!AccessControl.checkAccess(client, new File(path))) {
                output.write(new Response(403).toByteArray());
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String extension = Utils.getFileExtension(path);
        Map<String, String> handlers = Config.getMapParam("Handlers", "ext");
        Map<String, String> types    = Config.getMapParam("ContentTypes", "ext");
        String handlerCommand = handlers.get(extension);
        String contentType = ((types.get(extension) != null) ? types.get(extension) :
                                                               "application/octet-stream");
        if (handlerCommand != null) {
            ExternalProgramRunner cgi = new ExternalProgramRunner(handlerCommand, new File(path));
            try {
                output.write(
                    new Response(200)
                        .setBody(cgi
                                     .setEnvironmentVariable("REMOTE_ADDR", request.getRemoteAddress())
                                     .setEnvironmentVariable("REMOTE_PORT", request.getRemotePort())
                                     .run(request.getMethod(), request.getQueryString()),
                                contentType,
                                true)
                        .toByteArray()
                );
            } catch (Exception e) {
                Utils.logStackTrace(e);
            }
        } else {
            try {
                output.write(
                    new Response(200)
                        .setBody(Files.readAllBytes(Paths.get(path)), contentType, false)
                        .toByteArray()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Processes the request
     *
     * @param input Data source
     * @param output Data destination
     * @throws IOException
     */
    private void httpQueryHandler(BufferedReader input, DataOutputStream output) throws IOException {
        String[] clientInfo = client
                                  .getRemoteSocketAddress()
                                  .toString()
                                  .substring(1)
                                  .split(":");

        request = new Request(input);
        request
            .setRemoteAddress(clientInfo[0])
            .setRemotePort(clientInfo[1]);

        String path;
        File requestedFile;

        if (request.getMethod() == HttpMethods.GET ||
            request.getMethod() == HttpMethods.POST) {
            path = request.process();
            fireAction(ACTION_UPDATE);
            requestedFile = new File(Config.getStringParam("WebDocRoot") + path);

            if (!requestedFile.exists()) {
                output.write(new Response(404).toByteArray());
                return;
            }

            if (requestedFile.isDirectory()) {
                File tmpFile;
                for (String f : Config.getListParam("IndexFiles")) {
                    tmpFile = new File(requestedFile.getAbsoluteFile() + "/" + f);
                    if (tmpFile.exists()) {
                        sendFile(tmpFile.getAbsolutePath(), output);
                        return;
                    }
                }

                File[] files = requestedFile.listFiles();
                output.writeBytes("<h1>Directory index</h1>");
                if (files != null) {
                    for (File f : files) {
                        output.writeBytes("<a href=\"file://" + f.getCanonicalPath() + "\">" + f.getCanonicalPath() + "</a><br />");
                    }
                }
            } else {
                sendFile(requestedFile.getAbsolutePath(), output);
            }
        } else if (request.getMethod() == HttpMethods.OPTIONS) {
            output.write(
                    new Response(200)
                        .setHeader("Allow", "POST,GET,OPTIONS")
                        .toByteArray()
            );
        } else if (request.getMethod() == HttpMethods.UNKNOWN) {
            output.write(new Response(405).toByteArray());
        }
    }
}