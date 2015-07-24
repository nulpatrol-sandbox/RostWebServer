package khaniukov.server.model;

import java.util.Observable;

public class SimpleAppModel extends Observable implements AppModel {
    private String message = "[OK] RostWebServer initialised" + System.getProperty("line.separator");

    public void setMessage(String message) {
        this.message = message;
        setChanged();
        notifyObservers();
    }

    public String getMessage() {
        System.out.println(message);
        return message;
    }

    public Observable observable() {
        return this;
    }
}
