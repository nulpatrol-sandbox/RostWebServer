package khaniukov.server.AccessControl;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import khaniukov.server.Utils;

/**
 * Class for parsing .htaccess file and checking access for specified file
 */
public class AccessControl {
    public enum RuleTypes { ALL, HOST, IP, IPWITHMASK, IPWITHCIDRMASK }
    public enum RuleOrder { DENY_ALLOW, ALLOW_DENY }
    /**
     * Field for storing access rules
     */
    private Map<String, Set<String>> rules = new HashMap<>();

    private Socket client = null;
    private File file = null;
    private boolean withoutHtaccess = false;
    private Set<String> allowRules  = new HashSet<>();
    private Set<String> denyRules   = new HashSet<>();

    /**
     * Static method for checking access
     * @param client Client which requested file
     * @param file Requested file
     * @return true if client have access to file
     */
    public static boolean checkAccess(Socket client, File file) {
        try {
            return new AccessControl(client, file).checkAccess();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

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
            withoutHtaccess = true;
            return;
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

    /**
     * Check access to file
     * @return true if client have access
     */
    public boolean checkAccess() {
        if (withoutHtaccess) return true;
        // Get rules for specified file
        Set<String> rulesSet = (rules.containsKey(file.getName()) ?
                                    rules.get(file.getName()) :
                                    rules.get("dir"));

        RuleOrder order = RuleOrder.DENY_ALLOW;

        for (String rule : rulesSet) {
            if (rule.contains("from")) {
                String ruleValue = rule.substring(rule.indexOf("from") + "from".length() + 1);
                if (rule.startsWith("allow")) {
                    for (String singleRule : ruleValue.split(" ")) {
                        allowRules.add(singleRule.trim());
                    }
                } else {
                    for (String singleRule : ruleValue.split(" ")) {
                        denyRules.add(singleRule.trim());
                    }
                }
            } else {
                if (rule.toLowerCase().contains("order")) {
                    String tmpOrder = rule.split(" ")[1];
                    order = (tmpOrder.indexOf("allow") < tmpOrder.indexOf("deny")) ?
                            RuleOrder.ALLOW_DENY :
                            RuleOrder.DENY_ALLOW;
                } else {
                    order = RuleOrder.DENY_ALLOW;
                }
            }
        }

        try {
            boolean isDeny  = checkRules("deny");
            boolean isAllow = checkRules("allow");
            return (order == RuleOrder.DENY_ALLOW ?
                    (isAllow || !isDeny) :
                    (!(isDeny || !isAllow)));
        } catch (htaccessFileException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check all rules
     * @param type rules type - allow-rules or deny-rules
     * @return true if at least one satisfies
     * @throws htaccessFileException
     */
    private boolean checkRules(String type) throws htaccessFileException {
        boolean result = false;
        Set<String> rules;
        if (type.equals("allow")) {
            rules = allowRules;
            if (rules.isEmpty()) return true;
        } else {
            rules = denyRules;
            if (rules.isEmpty()) return false;
        }

        for (String rule : rules) {
            result |= checkRule(rule);
        }
        return result;
    }

    /**
     * Check specified rule
     * @param rule rule for checking
     * @return true if client satisfies rule
     * @throws htaccessFileException
     */
    private boolean checkRule(String rule) throws htaccessFileException {
        switch (getRuleType(rule)) {
            case ALL: // true because all clients have access
                return true;
            case HOST: // true if client host name contains rule-value
                return client
                        .getInetAddress()
                        .getCanonicalHostName()
                        .contains(rule);
            case IP:
                return client.getInetAddress().toString().substring(1).startsWith(rule);
            case IPWITHMASK:
                try {
                    String network = rule.split("/")[0];
                    String netMask = rule.split("/")[1];
                    String IPWithNetMask = client.getInetAddress().toString().substring(1) + "/" + netMask;
                    return Utils
                            .getNetworkFromIPandNetMask(IPWithNetMask)
                            .toString()
                            .substring(1)
                            .equals(network);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            case IPWITHCIDRMASK:
                try {
                    String network = rule.split("/")[0];
                    String netMask = rule.split("/")[1];
                    String IPWithNetMask = client.getInetAddress().toString().substring(1) + "/" +
                            Utils.CIDRMaskToInetAddress(Byte.parseByte(netMask)).toString().substring(1);

                    return Utils
                            .getNetworkFromIPandNetMask(IPWithNetMask)
                            .toString()
                            .substring(1)
                            .equals(network);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                return true;
        }

        return false;
    }

    /**
     * Determine type of parameter for allow/deny by second part of rule
     *
     * @param rule allow/deny rule
     * @return type of parameter
     * @throws htaccessFileException
     */
    public static RuleTypes getRuleType(String rule) throws htaccessFileException {
        if (rule.equals("all")) {
            return RuleTypes.ALL;
        } else {
            // If rule contains delimiter for IP-address and netmask
            if (rule.contains("/")) {
                String[] parts = rule.split("/");
                // If first part is IP
                if (Utils.isIPLike(parts[0])) {
                    // ... and second part is IP-like
                    if (Utils.isIPLike(parts[1])) {
                        Utils.checkNetMask(parts[1]);
                        return RuleTypes.IPWITHMASK;
                    }
                    // ... but second part is number in range 1-32
                    else {
                        if (Integer.parseInt(parts[1]) > 0 && Integer.parseInt(parts[1]) <= 32) {
                            return RuleTypes.IPWITHCIDRMASK;
                        } else {
                            throw new htaccessFileException("Bad mask");
                        }
                    }
                } else {
                    throw new htaccessFileException("Bad rule");
                }
            } else {
                if (Utils.isIPLike(rule)) {
                    return RuleTypes.IP;
                } else {
                    return RuleTypes.HOST;
                }
            }
        }
    }
}
