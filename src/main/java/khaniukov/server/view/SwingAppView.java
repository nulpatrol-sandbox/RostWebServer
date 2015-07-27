package khaniukov.server.view;

import khaniukov.server.model.AppModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public abstract class SwingAppView implements AppView, Observer {
    protected JFrame   frame;
    protected AppModel model;

    public SwingAppView(AppModel model) {
        this.model = model;
        model.observable().addObserver(this);

        try {
            UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            try {
                createFrame();
            } catch (AWTException e) {
                /* Do */
            }
            update();
        });
    }

    protected abstract void createFrame() throws AWTException;
    public abstract void update(Observable source, Object arg);

    public void show() {
        SwingUtilities.invokeLater(() -> {
            frame.setVisible(true);
        });
    }

    public void close() {
        frame.setVisible(false);
        frame.dispose();
    }

    public void update() {
        update(null, null);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
