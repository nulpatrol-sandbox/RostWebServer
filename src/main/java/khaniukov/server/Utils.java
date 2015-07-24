package khaniukov.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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

    /**
     * Read .htaccess file
     *
     * @throws IOException
     */
    private void readHtaccess() throws IOException {
        File htaccessFile = new File(Config.getStringParam("WebDocRoot") + ".htaccess");
        List<String> lines = null;
        if (htaccessFile.exists()) {
            lines = Files.readAllLines(Paths.get(htaccessFile.getPath()), Charset.defaultCharset());
        }
        if (lines != null) {
            for (String line : lines) {
                line = line.toLowerCase();
                if (line.startsWith("deny from")) {
                    String deniedIP = line.substring("deny from".length()).trim();
                    System.out.println(deniedIP);
                }
                if (line.startsWith("allow from")) {
                    String allowedIP = line.substring("allow from".length()).trim();
                    System.out.println(allowedIP);
                }
            }
        }
    }

    public static void logStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        App.errorLogger.error(sw.toString());
    }
}
