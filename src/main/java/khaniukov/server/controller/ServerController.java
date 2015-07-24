package khaniukov.server.controller;

import khaniukov.server.model.AppModel;
import khaniukov.server.view.AppView;
import khaniukov.server.view.SwingTextAreaAppView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerController implements ActionListener {

    private AppModel model;

    public ServerController(AppModel model) {
        this.model = model;
    }

    public void createView() {
        AppView view = new SwingTextAreaAppView(model);
        view.addActionListener(this);
        view.show();
    }

    public void actionPerformed(ActionEvent event) {
        System.out.println("Action");
        AppView view = (AppView) event.getSource();
        if (event.getActionCommand().equals(AppView.ACTION_CLOSE)) {
            view.close();
            System.exit(0);
        }
        if (event.getActionCommand().equals(AppView.ACTION_UPDATE)) {
            try {
                model.setMessage("Hello");
            }
            catch (Exception e) {
                view.update();
                view.showError(e.getMessage());
            }
        }
    }
}