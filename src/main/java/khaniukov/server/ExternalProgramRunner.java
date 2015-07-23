package khaniukov.server;

import org.apache.logging.log4j.core.util.Charsets;

import java.io.*;

/**
 * This class is designed for invocation an external program and processing specified file
 *
 * @author Rostislav Khaniukov
 */
public class ExternalProgramRunner {

    private File file;
    private File tmp = null;
    private ProcessBuilder pb;

    /**
     * Constructs an ProcessBuilder which will create process for processing file
     *
     * @param pathToProgram path to external program
     * @param file path to file to be processed
     */
    public ExternalProgramRunner(String pathToProgram, File file) {
        this.file = file;
        try {
            // Create temp file in temporary directory
            tmp = File.createTempFile("cgi_", ".tmp", new File(Config.getStringParam("TmpDir")));
        } catch (IOException e) {
            App.errorLogger.error("[I/O Error]: Cannot create a temp file for redirecting external program output");
        }

        pb = new ProcessBuilder(pathToProgram, file.getAbsolutePath());
        pb.redirectOutput(tmp);
    }

    /**
     * Set an environment variable
     *
     * @param name name of environment variable
     * @param value value of environment variable
     * @return self for chaining methods
     */
    public ExternalProgramRunner setEnvironmentVariable(String name, String value) {
        pb.environment().put(name, value);
        return this;
    }

    /**
     * Specifying environment variables, execution external program and read output.
     *
     * @param queryString query string from HTTP request
     * @return result of program execution
     * @throws IOException
     * @throws InterruptedException
     */
    public byte[] run(QueryString queryString) throws IOException, InterruptedException {
        setEnvironmentVariable("REDIRECT_STATUS",   "true");
        setEnvironmentVariable("SCRIPT_FILENAME",   file.getPath());
        setEnvironmentVariable("REQUEST_METHOD",    "POST");
        setEnvironmentVariable("GATEWAY_INTERFACE", "CGI/1.1");
        setEnvironmentVariable("CONTENT_LENGTH",    queryString.getLength());
        setEnvironmentVariable("CONTENT_TYPE",      "application/x-www-form-urlencoded");

        Process process = pb.start();
        DataOutputStream writer = new DataOutputStream(process.getOutputStream());
        writer.writeBytes(queryString.toString());
        writer.flush();
        writer.close();
        process.waitFor();

        StringBuilder sb = new StringBuilder();
        String line;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tmp.getAbsoluteFile()), Charsets.UTF_8))) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(System.getProperty("line.separator"));
            }
        }

        //tmp.delete();
        return sb.toString().getBytes(Charsets.UTF_8);
    }
}
