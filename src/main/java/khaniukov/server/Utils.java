package khaniukov.server;

import khaniukov.server.controller.ServerController;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A set of static methods which providing different utility functions
 *
 * @author Rostislav Khaniukov
 */
public final class Utils {

    /**
     * Fetch file extension from path
     *
     * @param path path to file
     * @return an extension of specified file
     * @throws IOException
     */
    public static String getFileExtension(String path) throws IOException {
        String extension = "";
        int i = path.lastIndexOf('.');
        int p = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (i > p)
            extension = path.substring(i + 1);
        if (extension.equals("")) {
            throw new IOException("File without extension");
        }
        return extension;
    }

    public static void logStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        ServerController.errorLogger.error(sw.toString());
    }

    public static void o(String i) {
        System.out.println(i);
    }
}
