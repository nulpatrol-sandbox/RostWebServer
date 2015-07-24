package khaniukov.server.view;

import java.awt.event.*;

/**
 * Interface
 */
public interface AppView {
    String ACTION_UPDATE = "update";
    String ACTION_CLOSE  = "close";
    void addActionListener(ActionListener al);
    void removeActionListener(ActionListener al);
    void update();
    void show();
    void close();
    void showError(String message);
}