package khaniukov.server;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is designed for parsing "query string" from string representation to map.
 *
 * @author Rostislav Khaniukov
 */
public class QueryString {
    private String queryString;
    private Map<String, String> parameters;

    /**
     * Create a QueryString object
     *
     * @param queryString the string representation of "query string".
     */
    public QueryString(String queryString) {
        this.queryString = queryString;
        parameters = new HashMap<>();
        if (!this.queryString.equals("")) {
            String[] params = queryString.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                parameters.put(keyValue[0], keyValue[1]);
            }
        }
    }

    /**
     * @return String the string representation of query "string" length
     */
    public String getLength() {
        return Integer.toString(queryString.length());
    }

    /**
     * @return String raw "query string"
     */
    public String toString() {
        return queryString;
    }
}
