package khaniukov.server.view;

import java.awt.event.*;

/**
 * Interface
 */
public interface AppView {
    void update();
    void show();
    void close();
    void showError(String message);
}