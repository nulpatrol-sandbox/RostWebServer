package khaniukov.server.view;

import khaniukov.server.model.AppModel;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class SwingTextAreaAppView extends SwingAppView {

    public static final String FRAME_TITLE        = "RostWebServer";
    private JTextArea area;
    private JScrollPane sp;
    private MenuItem closeItem = new MenuItem("Close");
    private MenuItem openItem  = new MenuItem("Open");

    public SwingTextAreaAppView(AppModel model) {
        super(model);
    }

    /**
     * Create popup menu, add action listeners to items, add item to menu
     *
     * @return popup menu ready for use
     */
    private PopupMenu createPopupMenu() {
        PopupMenu menu = new PopupMenu();

        closeItem.addActionListener((e) -> System.exit(0));
        openItem.addActionListener((e) -> {
            frame.setVisible(true);
            frame.setState(Frame.NORMAL);
            frame.toFront();
            frame.requestFocus();
            menu.remove(openItem);
        });

        menu.add(closeItem);
        return menu;
    }

    /**
     * Create text area with scroll pane and add it to frame
     */
    private void createTextAreaWithScrollPane() {
        UIManager.put("TextArea.margin", new Insets(10, 10, 10, 10));
        area = new JTextArea();
        area.setEditable(false);
        // Scroll Pane on TextArea
        sp = new JScrollPane(area);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        sp.getVerticalScrollBar().addAdjustmentListener((e) -> e.getAdjustable().setValue(e.getAdjustable().getMaximum()));
        frame.getContentPane().add(sp);
    }

    protected void createFrame() throws AWTException {
        frame = new JFrame(FRAME_TITLE);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.setSize(600, 400);
        frame.setLocation(new Point(200, 200));
        frame.setResizable(false);

        createTextAreaWithScrollPane();

        // System Tray
        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage("icon.png");
        PopupMenu menu = createPopupMenu();
        TrayIcon icon = new TrayIcon(image, "SystemTray Demo", menu);
        icon.setImageAutoSize(true);
        tray.add(icon);

        frame.addWindowStateListener((e) -> {
            if (e.getNewState() == 1) {
                // Item "Open" should be above the item "Close"
                menu.remove(closeItem);
                menu.add(openItem);
                menu.add(closeItem);
                frame.setVisible(false);
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                menu.remove(closeItem);
                menu.add(openItem);
                menu.add(closeItem);
                frame.setVisible(false);
            }
        });
    }

    public void update(Observable source, Object arg) {
        area.append(model.getMessage() + System.getProperty("line.separator"));
    }
}