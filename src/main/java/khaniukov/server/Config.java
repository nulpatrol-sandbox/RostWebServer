package khaniukov.server;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Static class for receiving configuration item values from xml-file
 *
 * @author Rostislav Khaniukov
 */
public final class Config {

    /**
     * Root element of config file
     */
    static Element configuration;

    /**
     * Blank private-access constructor to denied creating class instances
     */
    private Config() { /* Nothing here */ }

    /**
     * Initialising config. Read and validate xml config file and fetching root element
     *
     * @param configPath path to xml-file with configuration
     */
    public static void initialize(String configPath) {
        SAXBuilder builder = new SAXBuilder(XMLReaders.DTDVALIDATING);
        Document xml = null;
        try {
            xml = builder.build(new File(configPath));
        } catch (Exception e) {
            Utils.logStackTrace(e);
        }

        if (xml != null)
            configuration = xml.getRootElement();
    }

    /**
     * Receive parameter which represented as string
     *
     * @param name name of parameter
     * @return parameter value as string
     */
    public static String getStringParam(String name) {
        return configuration.getChild(name).getText();
    }

    /**
     * Receive parameter which represented as integer
     *
     * @param name name of parameter
     * @return parameter value as integer
     */
    public static int getIntParam(String name) {
        int param = 0;
        try {
            param = Integer.parseInt(getStringParam(name));
        } catch (NumberFormatException e) {
            Utils.logStackTrace(e);
        }
        return param;
    }

    /**
     * Receive parameter which represented as list
     *
     * @param name name of parameter
     * @return parameter value as list
     */
    public static List<String> getListParam(String name) {
        List<Element> elements = configuration.getChild(name).getChildren();
        List<String> listValue = new ArrayList<>();
        for (Element element : elements) {
            listValue.add(element.getText());
        }
        return listValue;
    }

    /**
     * Receive parameter which represented as map
     *
     * @param name name of parameter
     * @param attribute appropriate attribute of xml-tag, which acts as a map key
     * @return parameter value as map
     */
    public static Map<String, String> getMapParam(String name, String attribute) {
        List<Element> elements = configuration.getChild(name).getChildren();
        Map<String, String> mapValue = new HashMap<>();

        for (Element element : elements) {
            String tmp = element.getAttributeValue(attribute);
            if (tmp != null) {
                String[] extensions = tmp.split(",");
                for (String extension : extensions) {
                    mapValue.put(extension, element.getValue());
                }
            }
        }
        return mapValue;
    }
}
