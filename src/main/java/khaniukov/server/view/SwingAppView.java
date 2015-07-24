package khaniukov.server.view;

import khaniukov.server.model.AppModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public abstract class SwingAppView implements AppView, Observer {
    protected JFrame   frame;
    protected AppModel model;
    private ArrayList<ActionListener> listeners = new ArrayList<>();

    public SwingAppView(AppModel model) {
        this.model = model;
        model.observable().addObserver(this);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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

    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    public void removeActionListener(ActionListener listener) {
        listeners.remove(listener);
    }

    protected void fireAction(String command) {
        ActionEvent event = new ActionEvent(this, 0, command);
        for (ActionListener listener : listeners) {
            listener.actionPerformed(event);
        }
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
