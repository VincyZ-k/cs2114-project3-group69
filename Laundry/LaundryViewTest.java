
package Laundry;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import student.TestCase;

/**
 * Tests the LaundryView class.
 * Requires a graphical display for Swing.
 *
 * @version 2026.11.16
 */
public class LaundryViewTest extends TestCase
{
    private LaundryView view;
    private LaundryModel model;

    /** Creates a fresh view and model. */
    @Override
    public void setUp()
    {
        onEDT(() -> {
            view = new LaundryView();
            model = new LaundryModel();
        });
    }

    /** Closes the window after each test. */
    @Override
    public void tearDown()
    {
        onEDT(() -> view.dispose());
    }

    /** Tests initial input and button states. */
    public void testInitialState()
    {
        assertNull(view.getSelectedType());
        assertEquals("", nameField().getText());
        assertFalse(dryerButton().isEnabled());
    }

    /** Tests all equipment selections. */
    public void testAllSelections()
    {
        assertNull(view.getSelectedType());

        onEDT(() -> radio("largeBtn").setSelected(true));
        assertEquals(MachineType.LARGE_WASHER,
            view.getSelectedType());

        onEDT(() -> radio("smallBtn").setSelected(true));
        assertEquals(MachineType.SMALL_WASHER,
            view.getSelectedType());

        onEDT(() -> radio("dryerBtn").setSelected(true));
        assertEquals(MachineType.DRYER,
            view.getSelectedType());
    }

    /** Tests clearing the registration input. */
    public void testClearInput()
    {
        onEDT(() -> {
            nameField().setText("Alex");
            radio("smallBtn").setSelected(true);
            view.clearInput();
        });

        assertEquals("", nameField().getText());
        assertNull(view.getSelectedType());
    }

    /** Tests the registration listener. */
    public void testRegisterListener()
    {
        AtomicReference<String> receivedName =
            new AtomicReference<>();

        AtomicReference<MachineType> receivedType =
            new AtomicReference<>();

        onEDT(() -> {
            view.setJoinListener((name, type) -> {
                receivedName.set(name);
                receivedType.set(type);
            });

            nameField().setText("Alex");
            radio("largeBtn").setSelected(true);
            button("registerButton").doClick();
        });

        assertEquals("Alex", receivedName.get());
        assertEquals(MachineType.LARGE_WASHER,
            receivedType.get());
    }

    /** Tests registration without selecting equipment. */
    public void testRegisterWithoutSelectedType()
    {
        AtomicReference<MachineType> type =
            new AtomicReference<>(MachineType.DRYER);

        onEDT(() -> {
            view.setJoinListener((name, selected) ->
                type.set(selected));

            button("registerButton").doClick();
        });

        assertNull(type.get());
    }

    /** Tests registration without a listener. */
    public void testRegisterWithoutListener()
    {
        onEDT(() -> {
            view.setJoinListener(null);
            nameField().setText("Alex");
            button("registerButton").doClick();
        });

        assertEquals("Alex", nameField().getText());
    }

    /** Tests success and error messages. */
    public void testMessages()
    {
        onEDT(() -> view.showError("Bad name"));

        assertEquals("Bad name", message().getText());
        assertEquals(Color.RED, message().getForeground());

        onEDT(() -> view.showMessage("Registered"));

        assertEquals("Registered", message().getText());
        assertEquals(Color.DARK_GRAY,
            message().getForeground());
    }

    /** Tests the disabled dryer button. */
    public void testDisabledDryerButton()
    {
        AtomicInteger calls = new AtomicInteger();

        onEDT(() -> {
            view.setDryerListener(calls::incrementAndGet);
            view.setDryerButtonEnabled(false);
            dryerButton().doClick();
        });

        assertEquals(0, calls.get());
    }

    /** Tests the enabled dryer button. */
    public void testEnabledDryerButton()
    {
        AtomicInteger calls = new AtomicInteger();

        onEDT(() -> {
            view.setDryerListener(calls::incrementAndGet);
            view.setDryerButtonEnabled(true);
            dryerButton().doClick();
        });

        assertEquals(1, calls.get());

        onEDT(() -> view.setDryerButtonEnabled(false));
        assertFalse(dryerButton().isEnabled());
    }

    /** Tests dryer button without a listener. */
    public void testDryerWithoutListener()
    {
        onEDT(() -> {
            view.setDryerListener(null);
            view.setDryerButtonEnabled(true);
            dryerButton().doClick();
        });

        assertTrue(dryerButton().isEnabled());
    }

    /** Tests displaying all available machines. */
    public void testAvailableMachines()
    {
        onEDT(() -> view.showMachines(model.getMachines()));

        assertEquals(8, machines().getComponentCount());

        for (int i = 0; i < 8; i++)
        {
            assertTrue(machineLabel(i).getText()
                .contains("Available"));

            assertEquals(new Color(0, 120, 0),
                machineLabel(i).getForeground());

            assertFalse(finishButton(i).isEnabled());
        }

        assertTrue(machineLabel(0).getText()
            .contains("Large Washer"));

        assertTrue(machineLabel(2).getText()
            .contains("Small Washer"));

        assertTrue(machineLabel(4).getText()
            .contains("Dryer"));
    }

    /** Tests displaying an occupied machine. */
    public void testOccupiedMachine()
    {
        model.requestMachine("Alex", MachineType.LARGE_WASHER);

        onEDT(() -> view.showMachines(model.getMachines()));

        assertTrue(machineLabel(0).getText()
            .contains("In use by Alex"));

        assertEquals(new Color(170, 0, 0),
            machineLabel(0).getForeground());

        assertTrue(finishButton(0).isEnabled());
    }

    /** Tests forwarding the machine ID when finish is clicked. */
    public void testFinishListener()
    {
        model.requestMachine("Alex", MachineType.LARGE_WASHER);

        AtomicInteger receivedID = new AtomicInteger(-1);

        onEDT(() -> {
            view.setFinishListener(receivedID::set);
            view.showMachines(model.getMachines());
            finishButton(0).doClick();
        });

        assertEquals(1, receivedID.get());
    }

    /** Tests finishing without a listener. */
    public void testFinishWithoutListener()
    {
        model.requestMachine("Alex", MachineType.LARGE_WASHER);

        onEDT(() -> {
            view.setFinishListener(null);
            view.showMachines(model.getMachines());
            finishButton(0).doClick();
        });

        assertTrue(finishButton(0).isEnabled());
    }

    /** Tests that disabled finish buttons cannot be clicked. */
    public void testDisabledFinishButton()
    {
        AtomicInteger calls = new AtomicInteger();

        onEDT(() -> {
            view.setFinishListener(id -> calls.incrementAndGet());
            view.showMachines(model.getMachines());
            finishButton(0).doClick();
        });

        assertEquals(0, calls.get());
    }

    /** Tests replacing machine rows. */
    public void testReplaceMachineRows()
    {
        onEDT(() -> {
            view.showMachines(model.getMachines());

            assertEquals(8, machines().getComponentCount());

            view.showMachines(Collections.emptyList());
        });

        assertEquals(0, machines().getComponentCount());
    }

    /** Tests waiting lists for all machine types. */
    public void testWaitingListAllTypes()
    {
        for (MachineType type : MachineType.values())
        {
            int capacity = type == MachineType.DRYER ? 4 : 2;

            for (int i = 0; i < capacity; i++)
            {
                model.requestMachine("Busy" + i, type);
            }

            model.requestMachine("First", type);
            model.requestMachine("Second", type);

            onEDT(() -> view.showWaitingList(
                type, model.getWaitingList(type)));

            assertEquals(2, list(type).getSize());
            assertEquals("1. First", list(type).get(0));
            assertEquals("2. Second", list(type).get(1));
        }
    }

    /** Tests replacing and clearing waiting lists. */
    public void testReplaceAndClearWaitingList()
    {
        model.requestMachine("A", MachineType.LARGE_WASHER);
        model.requestMachine("B", MachineType.LARGE_WASHER);
        model.requestMachine("C", MachineType.LARGE_WASHER);

        onEDT(() -> view.showWaitingList(
            MachineType.LARGE_WASHER,
            model.getWaitingList(MachineType.LARGE_WASHER)));

        assertEquals(1,
            list(MachineType.LARGE_WASHER).getSize());

        onEDT(() -> view.showWaitingList(
            MachineType.LARGE_WASHER,
            Collections.emptyList()));

        assertEquals(0,
            list(MachineType.LARGE_WASHER).getSize());
    }

    /** Tests that waiting lists remain independent. */
    public void testWaitingListIndependence()
    {
        model.requestMachine("A", MachineType.SMALL_WASHER);
        model.requestMachine("B", MachineType.SMALL_WASHER);
        model.requestMachine("C", MachineType.SMALL_WASHER);

        onEDT(() -> view.showWaitingList(
            MachineType.SMALL_WASHER,
            model.getWaitingList(MachineType.SMALL_WASHER)));

        assertEquals(1,
            list(MachineType.SMALL_WASHER).getSize());

        assertEquals(0,
            list(MachineType.LARGE_WASHER).getSize());

        assertEquals(0,
            list(MachineType.DRYER).getSize());
    }

    // ---------- Helper methods ----------

    private JTextField nameField()
    {
        return field("nameField", JTextField.class);
    }

    private JLabel message()
    {
        return field("messageLabel", JLabel.class);
    }

    private JButton button(String name)
    {
        return field(name, JButton.class);
    }

    private JButton dryerButton()
    {
        return button("sendToDryerButton");
    }

    private JRadioButton radio(String name)
    {
        return field(name, JRadioButton.class);
    }

    private JPanel machines()
    {
        return field("machinePanel", JPanel.class);
    }

    private JPanel machineRow(int index)
    {
        return (JPanel)machines().getComponent(index);
    }

    private JLabel machineLabel(int index)
    {
        return (JLabel)machineRow(index).getComponent(0);
    }

    private JButton finishButton(int index)
    {
        return (JButton)machineRow(index).getComponent(1);
    }

    @SuppressWarnings("unchecked")
    private DefaultListModel<String> list(MachineType type)
    {
        String name = type == MachineType.LARGE_WASHER
            ? "largeListModel"
            : type == MachineType.SMALL_WASHER
                ? "smallListModel" : "dryerListModel";

        return field(name, DefaultListModel.class);
    }

    private <T> T field(String name, Class<T> type)
    {
        try
        {
            Field f = LaundryView.class.getDeclaredField(name);
            f.setAccessible(true);
            return type.cast(f.get(view));
        }
        catch (ReflectiveOperationException e)
        {
            throw new AssertionError(e);
        }
    }

    private void onEDT(Runnable action)
    {
        try
        {
            if (SwingUtilities.isEventDispatchThread())
            {
                action.run();
            }
            else
            {
                SwingUtilities.invokeAndWait(action);
            }
        }
        catch (Exception e)
        {
            throw new AssertionError(e);
        }
    }
}
