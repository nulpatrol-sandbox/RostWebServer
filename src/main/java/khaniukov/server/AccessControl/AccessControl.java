package khaniukov.server.AccessControl;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import khaniukov.server.AccessControl.AccessChecker.*;

/**
 * Class for parsing .htaccess file and checking access for specified file
 */
public class AccessControl {
    /**
     * Field for storing access rules
     */
    private Map<String, Set<String>> rules = new HashMap<>();

    private Socket client = null;
    private File file = null;

    /**
     * Read .htaccess file and create rules map
     *
     * @throws IOException
     */
    public AccessControl(Socket client, File file) throws IOException, htaccessFileException {
        this.client = client;
        this.file = file;
        // Check if parameter is dir
        if (!file.getParentFile().isDirectory()) {
            throw new htaccessFileException("Cannot read .htaccess from specified location");
        }
        // Read from .htaccess
        File htaccessFile = new File(file.getParentFile().getAbsolutePath() + "/.htaccess");
        List<String> lines;
        if (htaccessFile.exists()) {
            lines = Files.readAllLines(Paths.get(htaccessFile.getPath()), Charset.defaultCharset());
        } else {
            throw new htaccessFileException(".htaccess does not exist");
        }

        // Parsing .htaccess file
        if (lines != null) {
            String scope = "dir";
            HashSet<String> tmp = new HashSet<>();

            for (String line : lines) {
                line = line.toLowerCase();
                if (line.startsWith("deny from") || line.startsWith("allow from") || line.startsWith("order")) {
                    tmp.add(line);
                }
                if (line.startsWith("<files")) {
                    rules.put(scope, (HashSet)tmp.clone());
                    tmp.clear();
                    scope = line.substring("<files ".length(), line.indexOf(">"));
                }
                if (line.startsWith("</files>")) {
                    rules.put(scope, (HashSet)tmp.clone());
                    tmp.clear();
                    scope = "dir";
                }
            }
            if (!tmp.isEmpty()) rules.put(scope, (HashSet)tmp.clone());
        }
    }

    public boolean checkAccess() {
        // Get rules for specified file
        Set<String> rulesSet = (rules.containsKey(file.getName()) ?
                                    rules.get(file.getName()) :
                                    rules.get("dir"));

        RuleOrder order = null;
        Set<String> allowRules = new HashSet<>();
        Set<String> denyRules  = new HashSet<>();

        for (String rule : rulesSet) {
            if (rule.contains("from")) {
                String ruleValue = rule.substring(rule.indexOf("from") + "from".length() + 1);
                if (rule.startsWith("allow")) {
                    for (String singleRule : ruleValue.split(",")) {
                        allowRules.add(singleRule.trim());
                    }
                } else {
                    for (String singleRule : ruleValue.split(",")) {
                        denyRules.add(singleRule.trim());
                    }
                }
            } else {
                String tmpOrder = rule.split(" ")[1];
                order = (tmpOrder.indexOf("allow") < tmpOrder.indexOf("deny")) ?
                        RuleOrder.ALLOW_DENY :
                        RuleOrder.DENY_ALLOW;
            }
        }

        AccessChecker acheck = new AccessChecker(client, order, allowRules, denyRules);
        try {
            acheck.check();
        } catch (htaccessFileException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean checkAccess(Socket client, File file) {
        try {
            new AccessControl(client, file).checkAccess();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
