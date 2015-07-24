package khaniukov.server.model;

import java.util.Observable;

public class SimpleAppModel extends Observable implements AppModel {
    private String message = "";

    public void setMessage(String message) {
        this.message = message;
        setChanged();
        notifyObservers();
    }

    public String getMessage() {
        return message;
    }

    public Observable observable() {
        return this;
    }
}
