package Laundry;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

/**
 * View: displays the name field, equipment selection, waiting lists, machine
 * status, buttons and messages. Contains NO queue or machine-assignment logic.
 * User actions are forwarded to whoever registers a listener (the controller).
 */
public class LaundryView extends JFrame {

    private final JTextField nameField = new JTextField(20);
    private final JRadioButton largeBtn = new JRadioButton("Large Washer");
    private final JRadioButton smallBtn = new JRadioButton("Small Washer");
    private final JRadioButton dryerBtn = new JRadioButton("Dryer");
    private final ButtonGroup typeGroup = new ButtonGroup();
    private final JButton registerButton = new JButton("Register");
    private final JButton sendToDryerButton = new JButton("Send finished visitor to dryer");

    private final JPanel machinePanel = new JPanel(new GridLayout(0, 1, 4, 4));
    private final DefaultListModel<String> largeListModel = new DefaultListModel<>();
    private final DefaultListModel<String> smallListModel = new DefaultListModel<>();
    private final DefaultListModel<String> dryerListModel = new DefaultListModel<>();
    private final JLabel messageLabel = new JLabel(" ");

    private BiConsumer<String, MachineType> joinListener;
    private IntConsumer finishListener;
    private Runnable dryerListener;

    public LaundryView() {
        super("Laundromat Simulator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildInputPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(messageLabel, BorderLayout.SOUTH);

        registerButton.addActionListener(e -> {
            if (joinListener != null) joinListener.accept(nameField.getText(), getSelectedType());
        });
        sendToDryerButton.addActionListener(e -> {
            if (dryerListener != null) dryerListener.run();
        });
        sendToDryerButton.setEnabled(false);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildInputPanel() {
        typeGroup.add(largeBtn);
        typeGroup.add(smallBtn);
        typeGroup.add(dryerBtn);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setBorder(BorderFactory.createTitledBorder("Join the laundromat"));
        p.add(new JLabel("Name:"));
        p.add(nameField);
        p.add(largeBtn);
        p.add(smallBtn);
        p.add(dryerBtn);
        p.add(registerButton);
        p.add(sendToDryerButton);
        return p;
    }

    private JPanel buildCenterPanel() {
        JPanel center = new JPanel(new GridLayout(1, 2, 8, 8));

        machinePanel.setBorder(BorderFactory.createTitledBorder("Machines"));
        center.add(new JScrollPane(machinePanel));

        JPanel queues = new JPanel(new GridLayout(1, 3, 4, 4));
        queues.setBorder(BorderFactory.createTitledBorder("Waiting lists (front first)"));
        queues.add(listPanel("Large Washers", largeListModel));
        queues.add(listPanel("Small Washers", smallListModel));
        queues.add(listPanel("Dryers", dryerListModel));
        center.add(queues);

        center.setPreferredSize(new Dimension(780, 330));
        return center;
    }

    private JPanel listPanel(String title, DefaultListModel<String> model) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(title, SwingConstants.CENTER), BorderLayout.NORTH);
        p.add(new JScrollPane(new JList<>(model)), BorderLayout.CENTER);
        return p;
    }

    // ---------- listener registration (called by the controller) ----------

    public void setJoinListener(BiConsumer<String, MachineType> l) { this.joinListener = l; }
    public void setFinishListener(IntConsumer l) { this.finishListener = l; }
    public void setDryerListener(Runnable l) { this.dryerListener = l; }

    // ---------- read input ----------

    /** @return the selected equipment type, or null if none is selected. */
    public MachineType getSelectedType() {
        if (largeBtn.isSelected()) return MachineType.LARGE_WASHER;
        if (smallBtn.isSelected()) return MachineType.SMALL_WASHER;
        if (dryerBtn.isSelected()) return MachineType.DRYER;
        return null;
    }

    // ---------- display updates (called by the controller) ----------

    /** Rebuilds the machine status rows from a read-only snapshot. */
    public void showMachines(List<Machine> machines) {
        machinePanel.removeAll();
        for (Machine m : machines) {
            JPanel row = new JPanel(new BorderLayout(6, 0));
            row.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

            Visitor v = m.getCurrentVisitor();
            String status = (v == null) ? "Available" : "In use by " + v.getName();
            JLabel label = new JLabel(" #" + m.getMachineID() + "  " + prettyType(m.getType())
                    + "  -  " + status);
            label.setForeground(v == null ? new Color(0, 120, 0) : new Color(170, 0, 0));

            JButton finish = new JButton("Finish");
            finish.setEnabled(v != null);
            final int id = m.getMachineID();
            finish.addActionListener(e -> {
                if (finishListener != null) finishListener.accept(id);
            });

            row.add(label, BorderLayout.CENTER);
            row.add(finish, BorderLayout.EAST);
            machinePanel.add(row);
        }
        machinePanel.revalidate();
        machinePanel.repaint();
    }

    /** Replaces the displayed waiting list for one equipment type (front to rear). */
    public void showWaitingList(MachineType type, List<Visitor> waiting) {
        DefaultListModel<String> model = modelFor(type);
        model.clear();
        int pos = 1;
        for (Visitor v : waiting) {
            model.addElement(pos++ + ". " + v.getName());
        }
    }

    public void showMessage(String text) {
        messageLabel.setForeground(Color.DARK_GRAY);
        messageLabel.setText(text);
    }

    public void showError(String text) {
        messageLabel.setForeground(Color.RED);
        messageLabel.setText(text);
    }

    /** Clears the name and type after a successful registration. */
    public void clearInput() {
        nameField.setText("");
        typeGroup.clearSelection();
    }

    public void setDryerButtonEnabled(boolean enabled) {
        sendToDryerButton.setEnabled(enabled);
    }

    // ---------- helpers ----------

    private DefaultListModel<String> modelFor(MachineType type) {
        switch (type) {
            case LARGE_WASHER: return largeListModel;
            case SMALL_WASHER: return smallListModel;
            default:           return dryerListModel;
        }
    }

    private static String prettyType(MachineType t) {
        switch (t) {
            case LARGE_WASHER: return "Large Washer";
            case SMALL_WASHER: return "Small Washer";
            default:           return "Dryer";
        }
    }
}
