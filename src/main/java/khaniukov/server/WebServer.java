package khaniukov.server;

import khaniukov.server.Http.Request;
import khaniukov.server.Http.Response;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Main web-server functionality
 *
 * @author Rostislav Khaniukov
 */
public class WebServer extends Thread {
    /**
     * Enumeration which consist of all supported methods
     */
    public enum HttpMethods { GET, OPTIONS, POST, UNKNOWN }

    private Socket           client  = null;
    private BufferedReader   in      = null;
    private DataOutputStream out     = null;
    private Request request;

    /**
     * Main class constructor
     *
     * @param client Client socket
     * @throws IOException
     */
    public WebServer(Socket client) throws IOException {
        this.client = client;
        this.in     = new BufferedReader(new InputStreamReader(client.getInputStream()));
        this.out    = new DataOutputStream(client.getOutputStream());
    }

    /**
     * Method calling on new thread creation
     */
    public void run() {
        try {
            httpQueryHandler(in, out);
        } catch (IOException e) {
            App.errorLogger.error("[I/O Error]: " + e.getMessage());
            System.exit(-1);
        } finally {
            try {
                out.close();
                in.close();
                client.close();
            } catch (IOException e) {
                App.errorLogger.error("[I/O Error]: " + e.getMessage());
                System.exit(-1);
            }
        }
    }

    private void sendFile(String path, DataOutputStream output) throws IOException {
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
                    new Response(200).setBody(cgi.setEnvironmentVariable("REMOTE_ADDR", request.getRemoteAddress())
                                                     .setEnvironmentVariable("REMOTE_PORT", request.getRemotePort())
                                                     .run(request.getQueryString()),
                                                  contentType, true)
                                         .toByteArray()
                );
            } catch (Exception e) {
                App.errorLogger.error("[InterruptedException]: " + e.getMessage());
                System.exit(-1);
            }
        } else {
            try {
                output.write(
                        new Response(200).setBody(Files.readAllBytes(Paths.get(path)), contentType, false)
                                .toByteArray()
                );
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void httpQueryHandler(BufferedReader input, DataOutputStream output) throws IOException {
        String[] clientInfo = client.getRemoteSocketAddress()
                .toString()
                .substring(1)
                .split(":");

        request = new Request(input);
        request.setRemoteAddress(clientInfo[0])
               .setRemotePort(clientInfo[1]);

        String path;
        File requestedFile;

        if (request.getMethod() == HttpMethods.GET || request.getMethod() == HttpMethods.POST) {
            path = request.process();
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