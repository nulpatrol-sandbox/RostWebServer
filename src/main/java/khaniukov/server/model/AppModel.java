package khaniukov.server.model;

import java.util.Observable;

public interface AppModel {
    void setMessage(String message);
    String  getMessage();
    Observable observable();
}
