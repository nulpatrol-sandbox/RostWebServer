package khaniukov.server.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;

public class SimpleAppModel extends Observable implements AppModel {
    private String message = "[OK] RostWebServer initialised" + System.getProperty("line.separator");

    public void setMessage(String message) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
        Date date = new Date();
        this.message = "[" + dateFormat.format(date) + "] from " + message;
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
