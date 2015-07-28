package khaniukov.server.model;

import java.util.Observable;

/**
 * Model interface
 */
public interface AppModel {
    /**
     * Set message
     * @param message message to storing in model
     */
    void setMessage(String message);
    /**
     * Get message
     * @return message
     */
    String  getMessage();
    /**
     * Method for implementation Observer pattern
     * @return self
     */
    Observable observable();
}
