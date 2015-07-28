package khaniukov.server.model;

import khaniukov.server.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;

/**
 * Simple model for application
 */
public class SimpleAppModel extends Observable implements AppModel {
    /**
     * Init message
     */
    private String message = "[OK] RostWebServer initialised" + System.getProperty("line.separator");

    /**
     * Set message
     * @param message message to storing in model
     */
    public void setMessage(String message) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
            Date date = new Date();
            this.message = "[" + dateFormat.format(date) + "] from " + message;
        } catch (IllegalArgumentException e) {
            Utils.logStackTrace(e);
        }
        setChanged();
        notifyObservers();
    }

    /**
     * Get message
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Method for implementation Observer pattern
     * @return self
     */
    public Observable observable() {
        return this;
    }
}
