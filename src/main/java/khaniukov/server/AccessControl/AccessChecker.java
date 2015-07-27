package khaniukov.server.AccessControl;

import java.net.Socket;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccessChecker {
    public enum RuleTypes { ALL, HOST, IPWITHMASK, IPWITHCIDRMASK }
    public enum RuleOrder { DENY_ALLOW, ALLOW_DENY }
    private RuleOrder   order       = null;
    private Set<String> allowRules  = null;
    private Set<String> denyRules   = null;
    private Socket      client      = null;

    public AccessChecker(Socket client, RuleOrder order, Set<String> allowRules, Set<String> denyRules) {
        this.client      = client;
        this.order       = order;
        this.allowRules  = allowRules;
        this.denyRules   = denyRules;
    }

    private boolean checkRules(String type) throws htaccessFileException {
        boolean result = true;
        Set<String> rules;
        if (type.equals("allow"))
            rules = allowRules;
        else
            rules = denyRules;

        for (String rule : rules) {
            result &= checkRule(rule);
        }
        return result;
    }

    private boolean checkRule(String rule) throws htaccessFileException {
        switch (getRuleType(rule)) {
            case ALL: // true because all clients have access
                return true;
            case HOST: // true if client host name contains rule-value
                return client
                        .getInetAddress()
                        .getCanonicalHostName()
                        .contains(rule);
            case IPWITHMASK: // Cap
                return true;
            case IPWITHCIDRMASK: // Cap
                return true;
        }

        return false;
    }

    /**
     * Check allow-rule, deny-rule and based on order make decision if file is available
     * @return true if file is available, false in another case
     * @throws htaccessFileException
     */
    public boolean check() throws htaccessFileException {
        boolean isDeny  = checkRules("deny");
        boolean isAllow = checkRules("allow");

        return (order == RuleOrder.DENY_ALLOW ?
                (isAllow || !isDeny) :
                (!(isDeny || !isAllow)));
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
                // Regexp for IP-address
                String PATTERN = "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
                Pattern pattern = Pattern.compile(PATTERN);
                Matcher matcher1 = pattern.matcher(parts[0]);
                Matcher matcher2 = pattern.matcher(parts[1]);
                // If first part is IP
                if (matcher1.matches()) {
                    // ... and second part is IP-like
                    if (matcher2.matches()) {
                        String[] ipParts = parts[1].split("\\.");
                        StringBuilder binaryIP = new StringBuilder("");
                        for (String ipPart : ipParts) {
                            binaryIP.append(Integer.toString(Integer.parseInt(ipPart), 2));
                        }
                        // If second part is not netmask
                        if (binaryIP.toString().contains("01")) {
                            throw new htaccessFileException("Bad mask");
                        }
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
                return RuleTypes.HOST;
            }
        }
    }
}
