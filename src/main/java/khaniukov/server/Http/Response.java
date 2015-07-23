package khaniukov.server.Http;

import khaniukov.server.App;
import khaniukov.server.Config;
import org.apache.logging.log4j.core.util.Charsets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class for creating response
 *
 * @author Rostislav Khaniukov
 */
public class Response {
    private static final String EOLN   = "\r\n";
    private static final Map<Integer, String> returnText = new HashMap<Integer, String>() {{
        put(200, "200 OK");
        put(400, "400 Bad Request");
        put(403, "403 Forbidden");
        put(404, "404 Not Found");
        put(405, "405 Method Not Allowed");
        put(500, "500 Internal Server Error");
        put(501, "501 Not Implemented");
    }};

    private StringBuilder       answer = new StringBuilder("HTTP/1.0 ");
    private String              stateText;
    private Map<String, String> headers;
    private byte[]  body;
    private boolean fromCGI;

    public Response(int returnCode) {
        headers = new LinkedHashMap<>();
        stateText = returnText.get(returnCode);
        if (returnCode == 404) {
            try {
                URL url = this.getClass().getResource("/default_pages/404.html");
                if (url == null) throw new NoSuchFileException("Cannot find /default_pages/404.html");
                this.body = Files.readAllBytes(Paths.get(url.toURI()));
            } catch (NoSuchFileException e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                App.errorLogger.error(sw.toString());
            } catch (IOException e) {
                /* Do nothing 2 */
            } catch (URISyntaxException e) {
                System.out.println(e.getClass().getSimpleName());
                /* Do nothing */
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Set HTTP header
     *
     * @param name name of header
     * @param value value of header
     * @return self for chaining methods
     */
    public Response setHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * Get HTTP header
     *
     * @param name name of header
     * @return header value
     */
    public String getHeader(String name) {
        return headers.get(name);
    }

    /**
     * Set HTTP body
     *
     * @param body HTTP body
     * @param type Content-type of data
     * @param fromCGI is body received from CGI program
     * @return self for chaining methods
     */
    public Response setBody(byte[] body, String type, boolean fromCGI) {
        this.body = body;

        if (type.equals("text/html")) {
            String b = "";
            try {
                b = new String(body, Charsets.UTF_8);
                b = makeFileInsertion(b);
            } catch (Exception e) {
                /**/
            }
            this.body = b.getBytes(Charsets.UTF_8);
        }
        this.fromCGI = fromCGI;
        setHeader("Content-type", type);
        return this;
    }

    /**
     * Provide SSI - include file
     *
     * @param line line with SSI directive
     * @return included file contents
     * @throws IOException
     */
    private String makeFileInsertion(String line) throws IOException {
        int start = line.indexOf("<!--#");
        int finish = line.indexOf("-->");
        String commandLine = line.substring(start + 5, finish);
        String[] command = commandLine.split("=");
        if (command[0].equals("include file")) {
            String fileName = command[1].trim().replaceAll("^\"|\"$", "");
            byte[] encoded = Files.readAllBytes(Paths.get(Config.getStringParam("WebDocRoot") + fileName));
            String toInclude = new String(encoded);
            return line.replace("<!--#" + commandLine + "-->", toInclude);
        }
        return line;
    }

    /**
     * Convert response to byte array
     *
     * @return byte array representation of response
     * @throws IOException
     */
    public byte[] toByteArray() throws IOException {
        answer.append(this.stateText);
        answer.append(EOLN);

        setHeader("Server", "RostislavWebServer v0");
        setHeader("Connection", "close");

        for (String key : headers.keySet()) {
            answer.append(key).append(": ").append(headers.get(key)).append(EOLN);
        }

        if (!fromCGI) {
            answer.append(EOLN);
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(answer.toString().getBytes(Charsets.UTF_8));
        if (body != null) {
            output.write(body);
        } else {
            output.write(EOLN.getBytes(Charsets.UTF_8));
        }
        return output.toByteArray();
    }
}
