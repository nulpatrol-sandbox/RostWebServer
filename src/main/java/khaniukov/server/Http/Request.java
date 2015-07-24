package khaniukov.server.Http;

import khaniukov.server.App;
import khaniukov.server.QueryString;
import khaniukov.server.Utils;
import khaniukov.server.WebServer.HttpMethods;
import org.apache.logging.log4j.Level;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for processing HTTP Request
 *
 * @author Rostislav Khaniukov
 */
public class Request {
    private Map<String, String> headers     = new HashMap<>();
    private BufferedReader      input       = null;
    private HttpMethods         method      = null;
    private String              path        = null;
    private QueryString         queryString = new QueryString("");
    private String remoteIP   = "";
    private String remotePort = "";
    private String startLine  = "";

    /**
     * Contructor of Request class
     *
     * @param input BufferedReader instance for reading HTTP Request from it.
     */
    public Request(BufferedReader input) {
        this.input = input;
        try {
            parseStartString();
            readHeaders();
        } catch (IOException e) {
            Utils.logStackTrace(e);
        }
    }

    /**
     * Method to access method field, which store the HTTP method.
     *
     * @return HTTP method of request
     */
    public HttpMethods getMethod() {
        return method;
    }

    /**
     * Set remote address (IP address)
     *
     * @param remoteIP IP of client
     * @return self for chaining methods
     */
    public Request setRemoteAddress(String remoteIP) {
        this.remoteIP = remoteIP;
        return this;
    }

    /**
     * Set remote port
     *
     * @param remotePort port of client
     * @return self for chaining methods
     */
    public Request setRemotePort(String remotePort) {
        this.remotePort = remotePort;
        return this;
    }

    /**
     * Get remote address (IP address)
     *
     * @return remote IP
     */
    public String getRemoteAddress() {
        return remoteIP;
    }

    /**
     * Get remote port
     *
     * @return remote port
     */
    public String getRemotePort() {
        return remotePort;
    }

    /**
     * Parse first line of HTTP request and fetch method and resource
     */
    public void parseStartString() {
        try {
            startLine = input.readLine();
            String tmp = startLine.toUpperCase();

            if (tmp.startsWith("GET")) {
                method = HttpMethods.GET;
            } else if (tmp.startsWith("POST")) {
                method = HttpMethods.POST;
            } else if (tmp.startsWith("OPTIONS")) {
                method = HttpMethods.OPTIONS;
            } else {
                method = HttpMethods.UNKNOWN;
            }

            int start = 0, end = 0;
            for (int a = 0; a < startLine.length(); a++) {
                if (startLine.charAt(a) == ' ' && start != 0) {
                    end = a;
                    break;
                }
                if (startLine.charAt(a) == ' ' && start == 0) {
                    start = a;
                }
            }
            path = startLine.substring(start + 2, end);
        } catch (Exception e) {
            Utils.logStackTrace(e);
        }
    }

    /**
     * Read headers from HTTP request
     *
     * @throws IOException
     */
    public void readHeaders() throws IOException {
        String line;
        while ((line = input.readLine()) != null) {
            if (line.equals(""))
                break;

            int index = line.indexOf(':') + 1;
            String key   = line.substring(0, index - 1).trim();
            String value = line.substring(index).trim();

            headers.put(key, value);
        }
    }

    /**
     * Processing HTTP request
     *
     * @return path to resource
     * @throws IOException
     */
    public String process() throws IOException {
        App.requestLogger.info(getRemoteAddress() + ":" + getRemotePort() + " " + this.startLine);
        int length = 0;
        if (headers.get("Content-Length") != null) {
            length = Integer.parseInt(headers.get("Content-Length"));
        }

        StringBuilder body = new StringBuilder();
        if (length > 0) {
            int read;
            while ((read = input.read()) != -1) {
                body.append((char) read);
                if (body.length() == length)
                    break;
            }
        }

        switch (method) {
            case GET:
                if (path.contains("?")) {
                    String[] tmp = path.split("\\?");
                    path = tmp[0];
                    queryString = new QueryString(tmp[1]);
                }
            case POST:
                if (headers.get("Content-Type") != null &&
                        headers.get("Content-Type").equals("application/x-www-form-urlencoded")) {
                    String qs = body.toString();
                    queryString = new QueryString(qs);
                }
        }

        return path;
    }

    /**
     * @return query string
     */
    public QueryString getQueryString() {
        return queryString;
    }
}
