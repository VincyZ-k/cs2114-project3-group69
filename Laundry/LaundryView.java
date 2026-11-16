package Laundry;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

/**
 * Displays registration controls, machine status, waiting lists, and messages.
 * Forwards user actions to the controller.
 */
public class LaundryView
    extends JFrame
{
    private final JTextField nameField = new JTextField(20);
    private final JRadioButton largeBtn = new JRadioButton("Large Washer");
    private final JRadioButton smallBtn = new JRadioButton("Small Washer");
    private final JRadioButton dryerBtn = new JRadioButton("Dryer");
    private final ButtonGroup typeGroup = new ButtonGroup();
    private final JCheckBox automaticDryingBox =
        new JCheckBox("Dry after washing");
    private final JButton registerButton = new JButton("Register");
    private final JButton sendToDryerButton =
        new JButton("Send finished visitor to dryer");
    private final JPanel machinePanel = new JPanel(new GridLayout(0, 1, 4, 4));
    private final DefaultListModel<String> largeListModel =
        new DefaultListModel<>();
    private final DefaultListModel<String> smallListModel =
        new DefaultListModel<>();
    private final DefaultListModel<String> dryerListModel =
        new DefaultListModel<>();
    private final JLabel messageLabel = new JLabel(" ");
    private BiConsumer<String, MachineType> joinListener;
    private IntConsumer finishListener;
    private Runnable dryerListener;

    /**
     * Creates the laundromat interface.
     */
    public LaundryView()
    {
        super("Laundromat Simulator");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        ((JComponent)getContentPane())
            .setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildInputPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(messageLabel, BorderLayout.SOUTH);

        registerButton.addActionListener(e -> {
            if (joinListener != null)
            {
                joinListener.accept(nameField.getText(), getSelectedType());
            }
        });

        sendToDryerButton.addActionListener(e -> {
            if (dryerListener != null)
            {
                dryerListener.run();
            }
        });

        sendToDryerButton.setEnabled(false);
        automaticDryingBox.setEnabled(false);

        largeBtn.addActionListener(e -> automaticDryingBox.setEnabled(true));

        smallBtn.addActionListener(e -> automaticDryingBox.setEnabled(true));

        dryerBtn.addActionListener(e -> {
            automaticDryingBox.setSelected(false);
            automaticDryingBox.setEnabled(false);
        });

        pack();
        setLocationRelativeTo(null);
    }


    /**
     * Builds registration controls in two rows.
     *
     * @return the input panel
     */
    private JPanel buildInputPanel()
    {
        typeGroup.add(largeBtn);
        typeGroup.add(smallBtn);
        typeGroup.add(dryerBtn);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel
            .setBorder(BorderFactory.createTitledBorder("Join the laundromat"));

        JPanel registrationRow =
            new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        registrationRow.add(new JLabel("Name:"));
        registrationRow.add(nameField);
        registrationRow.add(largeBtn);
        registrationRow.add(smallBtn);
        registrationRow.add(dryerBtn);
        registrationRow.add(registerButton);

        JPanel optionsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        optionsRow.add(automaticDryingBox);
        optionsRow.add(sendToDryerButton);

        panel.add(registrationRow);
        panel.add(optionsRow);

        return panel;
    }


    /**
     * Builds machine status and waiting-list panels.
     *
     * @return the center panel
     */
    private JPanel buildCenterPanel()
    {
        JPanel center = new JPanel(new GridLayout(1, 2, 8, 8));

        machinePanel.setBorder(BorderFactory.createTitledBorder("Machines"));
        center.add(new JScrollPane(machinePanel));

        JPanel queues = new JPanel(new GridLayout(1, 3, 4, 4));

        queues.setBorder(
            BorderFactory.createTitledBorder("Waiting lists (front first)"));

        queues.add(listPanel("Large Washers", largeListModel));
        queues.add(listPanel("Small Washers", smallListModel));
        queues.add(listPanel("Dryers", dryerListModel));

        center.add(queues);
        center.setPreferredSize(new Dimension(900, 330));

        return center;
    }


    /**
     * Creates a labeled waiting-list panel.
     *
     * @param title
     *            the list title
     * @param model
     *            the displayed list data
     * @return the list panel
     */
    private JPanel listPanel(String title, DefaultListModel<String> model)
    {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(new JLabel(title, SwingConstants.CENTER), BorderLayout.NORTH);

        panel.add(new JScrollPane(new JList<>(model)), BorderLayout.CENTER);

        return panel;
    }

    // ---------- Listener registration ----------


    /**
     * Registers the visitor-registration listener.
     *
     * @param listener
     *            the listener to call
     */
    public void setJoinListener(BiConsumer<String, MachineType> listener)
    {
        joinListener = listener;
    }


    /**
     * Registers the machine-completion listener.
     *
     * @param listener
     *            the listener to call
     */
    public void setFinishListener(IntConsumer listener)
    {
        finishListener = listener;
    }


    /**
     * Registers the manual dryer-request listener.
     *
     * @param listener
     *            the listener to call
     */
    public void setDryerListener(Runnable listener)
    {
        dryerListener = listener;
    }

    // ---------- Read input ----------


    /**
     * Gets the selected equipment type.
     *
     * @return the selected type, or null if none is selected
     */
    public MachineType getSelectedType()
    {
        if (largeBtn.isSelected())
        {
            return MachineType.LARGE_WASHER;
        }

        if (smallBtn.isSelected())
        {
            return MachineType.SMALL_WASHER;
        }

        if (dryerBtn.isSelected())
        {
            return MachineType.DRYER;
        }

        return null;
    }


    /**
     * Gets the automatic drying preference.
     *
     * @return true if drying after washing is selected
     */
    public boolean isAutomaticDryingSelected()
    {
        return automaticDryingBox.isSelected();
    }

    // ---------- Display updates ----------


    /**
     * Rebuilds the displayed machine status rows.
     *
     * @param machines
     *            snapshots of the machines to display
     */
    public void showMachines(List<Machine> machines)
    {
        machinePanel.removeAll();

        for (Machine machine : machines)
        {
            JPanel row = new JPanel(new BorderLayout(6, 0));
            row.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

            Visitor visitor = machine.getCurrentVisitor();

            String status = visitor == null
                ? "Available"
                : "In use by " + visitor.getName();

            JLabel label = new JLabel(
                " #" + machine.getMachineID() + "  "
                    + prettyType(machine.getType()) + "  -  " + status);

            label.setForeground(
                visitor == null ? new Color(0, 120, 0) : new Color(170, 0, 0));

            JButton finish = new JButton("Finish");
            finish.setEnabled(visitor != null);

            final int id = machine.getMachineID();

            finish.addActionListener(e -> {
                if (finishListener != null)
                {
                    finishListener.accept(id);
                }
            });

            row.add(label, BorderLayout.CENTER);
            row.add(finish, BorderLayout.EAST);
            machinePanel.add(row);
        }

        machinePanel.revalidate();
        machinePanel.repaint();
    }


    /**
     * Displays one waiting list in front-to-rear order.
     *
     * @param type
     *            the equipment type
     * @param waiting
     *            visitors in FIFO order
     */
    public void showWaitingList(MachineType type, List<Visitor> waiting)
    {
        DefaultListModel<String> listModel = modelFor(type);
        listModel.clear();

        int position = 1;

        for (Visitor visitor : waiting)
        {
            listModel.addElement(position++ + ". " + visitor.getName());
        }
    }


    /**
     * Displays a normal operation message.
     *
     * @param text
     *            the message
     */
    public void showMessage(String text)
    {
        messageLabel.setForeground(Color.DARK_GRAY);
        messageLabel.setText(text);
    }


    /**
     * Displays an error message.
     *
     * @param text
     *            the error message
     */
    public void showError(String text)
    {
        messageLabel.setForeground(Color.RED);
        messageLabel.setText(text);
    }


    /**
     * Clears registration input after a successful request.
     */
    public void clearInput()
    {
        nameField.setText("");
        typeGroup.clearSelection();
        automaticDryingBox.setSelected(false);
        automaticDryingBox.setEnabled(false);
    }


    /**
     * Enables or disables the manual dryer-request button.
     *
     * @param enabled
     *            whether the button should be enabled
     */
    public void setDryerButtonEnabled(boolean enabled)
    {
        sendToDryerButton.setEnabled(enabled);
    }

    // ---------- Presentation helpers ----------


    /**
     * Finds the display list for an equipment type.
     *
     * @param type
     *            the equipment type
     * @return the corresponding display list
     */
    private DefaultListModel<String> modelFor(MachineType type)
    {
        switch (type)
        {
            case LARGE_WASHER:
                return largeListModel;

            case SMALL_WASHER:
                return smallListModel;

            default:
                return dryerListModel;
        }
    }


    /**
     * Formats an equipment type for display.
     *
     * @param type
     *            the equipment type
     * @return the display name
     */
    private static String prettyType(MachineType type)
    {
        switch (type)
        {
            case LARGE_WASHER:
                return "Large Washer";

            case SMALL_WASHER:
                return "Small Washer";

            default:
                return "Dryer";
        }
    }
}
