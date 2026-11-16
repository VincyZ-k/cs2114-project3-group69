
package Laundry;

import java.lang.reflect.Field;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import student.TestCase;

/**
 * Tests the laundry controller with a real model and view.
 * Requires a graphical display because LaundryView extends JFrame.
 *
 * @version 2026.11.16
 */
public class LaundryControllerTest extends TestCase
{
    private LaundryModel model;
    private LaundryView view;
    private LaundryController controller;

    /** Creates a fresh model, view, and controller. */
    @Override
    public void setUp()
    {
        onEDT(() -> {
            model = new LaundryModel();
            view = new LaundryView();
            controller = new LaundryController(model, view);
        });
    }

    /** Closes the window after each test. */
    @Override
    public void tearDown()
    {
        onEDT(() -> view.dispose());
    }

    /** Tests the initial display. */
    public void testInitialDisplay()
    {
        assertEquals(8, model.getMachines().size());

        for (MachineType type : MachineType.values())
        {
            assertEquals(0, displayedQueue(type).getSize());
        }

        assertEquals(8, machineRows());
        assertFalse(dryerButton().isEnabled());
    }

    /** Tests assigning visitors to available machines. */
    public void testJoinAvailableMachines()
    {
        int n = 0;

        for (MachineType type : MachineType.values())
        {
            String name = "Visitor" + n++;
            controller.handleJoin(name, type);

            assertTrue(message().contains(
                name + " assigned to machine "));
            assertEquals(0, model.getQueueSize(type));
            assertEquals(0, displayedQueue(type).getSize());
        }
    }

    /** Tests queues when machines are full. */
    public void testJoinQueuesForAllTypes()
    {
        for (MachineType type : MachineType.values())
        {
            int capacity = type == MachineType.DRYER ? 4 : 2;

            for (int i = 0; i < capacity; i++)
            {
                controller.handleJoin("Using" + i, type);
            }

            controller.handleJoin("Waiting", type);

            assertTrue(message().contains(
                "Waiting added to queue"));
            assertEquals(1, model.getQueueSize(type));
            assertEquals("1. Waiting",
                displayedQueue(type).get(0));
        }
    }

    /** Tests invalid registration inputs. */
    public void testJoinInvalidInputs()
    {
        controller.handleJoin("", MachineType.LARGE_WASHER);
        assertTrue(errorIsRed());
        assertEquals(0,
            model.getQueueSize(MachineType.LARGE_WASHER));

        controller.handleJoin("Alex", null);
        assertTrue(errorIsRed());
        assertTrue(model.getMachines().get(0).isAvailable());
    }

    /** Tests registration through the button. */
    public void testRegisterButtonSuccessAndClearInput()
    {
        onEDT(() -> {
            nameField().setText("  Alex  ");
            radio("largeBtn").setSelected(true);
            button("registerButton").doClick();
        });

        assertEquals("Alex",
            model.getMachine(1).getCurrentVisitor().getName());
        assertEquals("", nameField().getText());
        assertNull(view.getSelectedType());
    }

    /** Tests that invalid registration keeps the input. */
    public void testRegisterButtonInvalidInput()
    {
        onEDT(() -> {
            nameField().setText("Alex");
            button("registerButton").doClick();
        });

        assertEquals("Alex", nameField().getText());
        assertTrue(errorIsRed());

        onEDT(() -> {
            radio("smallBtn").setSelected(true);
            nameField().setText(" ");
            button("registerButton").doClick();
        });

        assertEquals(" ", nameField().getText());
        assertTrue(errorIsRed());
    }

    /** Tests finishing a washer with no waiting visitors. */
    public void testFinishWasherWithoutNextVisitor()
    {
        controller.handleJoin("Alex", MachineType.LARGE_WASHER);
        controller.handleFinish(1);

        assertTrue(message().contains(
            "Alex finished on machine 1"));
        assertTrue(message().contains("Send to dryer"));
        assertTrue(model.getMachine(1).isAvailable());
        assertTrue(dryerButton().isEnabled());
    }

    /** Tests finishing a washer with a waiting visitor. */
    public void testFinishWasherWithNextVisitor()
    {
        controller.handleJoin("A", MachineType.SMALL_WASHER);
        controller.handleJoin("B", MachineType.SMALL_WASHER);
        controller.handleJoin("C", MachineType.SMALL_WASHER);

        controller.handleFinish(3);

        assertTrue(message().contains("Next served: C"));
        assertEquals("C",
            model.getMachine(3).getCurrentVisitor().getName());
        assertEquals(0,
            displayedQueue(MachineType.SMALL_WASHER).getSize());
        assertTrue(dryerButton().isEnabled());
    }

    /** Tests finishing a dryer with no waiting visitor. */
    public void testFinishDryerWithoutNextVisitor()
    {
        controller.handleJoin("Alex", MachineType.DRYER);
        controller.handleFinish(5);

        assertEquals("Alex finished on machine 5", message());
        assertTrue(model.getMachine(5).isAvailable());
        assertFalse(dryerButton().isEnabled());
    }

    /** Tests finishing a dryer with a waiting visitor. */
    public void testFinishDryerWithNextVisitor()
    {
        for (int i = 0; i < 4; i++)
        {
            controller.handleJoin("D" + i, MachineType.DRYER);
        }

        controller.handleJoin("Next", MachineType.DRYER);
        controller.handleFinish(5);

        assertTrue(message().contains("Next served: Next"));
        assertEquals("Next",
            model.getMachine(5).getCurrentVisitor().getName());
        assertFalse(dryerButton().isEnabled());
    }

    /** Tests invalid machine IDs and free machines. */
    public void testFinishErrors()
    {
        controller.handleFinish(99);
        assertTrue(errorIsRed());

        controller.handleFinish(1);
        assertEquals("Machine already free", message());
        assertTrue(errorIsRed());
    }

    /** Tests the finish button listener. */
    public void testFinishButton()
    {
        controller.handleJoin("Alex", MachineType.LARGE_WASHER);

        onEDT(() -> machineFinishButton(0).doClick());

        assertTrue(model.getMachine(1).isAvailable());
        assertTrue(dryerButton().isEnabled());
    }

    /** Tests moving a finished visitor to an available dryer. */
    public void testSendToAvailableDryer()
    {
        controller.handleJoin("Alex", MachineType.LARGE_WASHER);
        controller.handleFinish(1);

        onEDT(() -> dryerButton().doClick());

        assertEquals("Alex assigned to dryer 5", message());
        assertEquals("Alex",
            model.getMachine(5).getCurrentVisitor().getName());
        assertFalse(dryerButton().isEnabled());
    }

    /** Tests adding a visitor to the dryer queue. */
    public void testSendToFullDryers()
    {
        for (int i = 0; i < 4; i++)
        {
            controller.handleJoin("Drying" + i, MachineType.DRYER);
        }

        controller.handleJoin("Alex", MachineType.SMALL_WASHER);
        controller.handleFinish(3);

        onEDT(() -> dryerButton().doClick());

        assertEquals("Alex added to dryer queue", message());
        assertEquals("Alex",
            model.getWaitingList(MachineType.DRYER)
                .get(0).getName());
        assertEquals("1. Alex",
            displayedQueue(MachineType.DRYER).get(0));
        assertFalse(dryerButton().isEnabled());
    }

    /** Tests an invalid dryer transfer. */
    public void testSendToDryerError()
    {
        controller.handleSendToDryer(null);

        assertTrue(errorIsRed());
        assertFalse(dryerButton().isEnabled());
    }

    /** Tests transferring the most recently finished washer. */
    public void testLastFinishedWasher()
    {
        controller.handleJoin("First", MachineType.LARGE_WASHER);
        controller.handleJoin("Second", MachineType.SMALL_WASHER);

        controller.handleFinish(1);
        controller.handleFinish(3);

        onEDT(() -> dryerButton().doClick());

        assertEquals("Second",
            model.getMachine(5).getCurrentVisitor().getName());
        assertFalse(dryerButton().isEnabled());
    }

    // ---------- Helper methods ----------

    private String message()
    {
        return field("messageLabel", JLabel.class).getText();
    }

    private boolean errorIsRed()
    {
        return java.awt.Color.RED.equals(
            field("messageLabel", JLabel.class).getForeground());
    }

    private JTextField nameField()
    {
        return field("nameField", JTextField.class);
    }

    private JRadioButton radio(String name)
    {
        return field(name, JRadioButton.class);
    }

    private JButton button(String name)
    {
        return field(name, JButton.class);
    }

    private JButton dryerButton()
    {
        return button("sendToDryerButton");
    }

    private int machineRows()
    {
        return field("machinePanel", javax.swing.JPanel.class)
            .getComponentCount();
    }

    private JButton machineFinishButton(int row)
    {
        javax.swing.JPanel panel =
            field("machinePanel", javax.swing.JPanel.class);

        javax.swing.JPanel machineRow =
            (javax.swing.JPanel)panel.getComponent(row);

        return (JButton)machineRow.getComponent(1);
    }

    @SuppressWarnings("unchecked")
    private javax.swing.DefaultListModel<String> displayedQueue(
        MachineType type)
    {
        String name = type == MachineType.LARGE_WASHER
            ? "largeListModel"
            : type == MachineType.SMALL_WASHER
                ? "smallListModel" : "dryerListModel";

        return field(name, javax.swing.DefaultListModel.class);
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
