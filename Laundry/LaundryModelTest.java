package Laundry;

import java.util.List;
import java.util.Optional;
import student.TestCase;

/**
 * Tests the laundry model without creating a view or controller.
 *
 * @author Wenxin Zhang
 * @version 2026.11.09
 */
public class LaundryModelTest
    extends TestCase
{
    private LaundryModel model;

    /**
     * Creates a fresh laundromat before each test.
     */
    @Override
    public void setUp()
    {
        model = new LaundryModel();
    }


    /**
     * Verifies the equipment counts and initially empty queues.
     */
    public void testInitialState()
    {
        int large = 0;
        int small = 0;
        int dryers = 0;
        assertEquals(8, model.getMachines().size());
        for (Machine machine : model.getMachines())
        {
            assertTrue(machine.isAvailable());
            assertNull(machine.getCurrentVisitor());
            switch (machine.getType())
            {
                case LARGE_WASHER:
                    large++;
                    break;
                case SMALL_WASHER:
                    small++;
                    break;
                case DRYER:
                    dryers++;
                    break;
                default:
                    fail("Unexpected machine type");
            }
        }
        assertEquals(2, large);
        assertEquals(2, small);
        assertEquals(4, dryers);
        for (MachineType type : MachineType.values())
        {
            assertEquals(0, model.getQueueSize(type));
            assertTrue(model.getWaitingList(type).isEmpty());
        }
    }


    /**
     * Trims spaces and accepts the maximum permitted name length.
     */
    public void testValidateName()
    {
        assertEquals("Ann", model.validateName("  Ann  "));
        String fifty = "A".repeat(50);
        assertEquals(fifty, model.validateName(fifty));
        assertEquals(fifty, model.validateName(" " + fifty + " "));
    }


    /**
     * Rejects missing, excessive, or control-character input.
     */
    public void testInvalidNames()
    {
        String[] invalid =
            { null, "", "   ", "A".repeat(51), "A\tB", "Ann\n", "\tAnn" };
        for (String name : invalid)
        {
            expectException(
                IllegalArgumentException.class,
                () -> model.validateName(name));
        }
    }


    /**
     * Requests use matching machines and preserve cleaned visitor data.
     */
    public void testRequestAvailableMachines()
    {
        for (MachineType type : MachineType.values())
        {
            Optional<Machine> result = model.requestMachine(" Amy ", type);
            assertTrue(result.isPresent());
            Machine machine = result.get();
            assertEquals(type, machine.getType());
            assertFalse(machine.isAvailable());
            assertEquals("Amy", machine.getCurrentVisitor().getName());
            assertEquals(type, machine.getCurrentVisitor().getSelectedType());
            assertEquals(0, model.getQueueSize(type));
        }
    }


    /**
     * Full machines create independent FIFO queues for every type.
     */
    public void testQueuesAndFifo()
    {
        for (MachineType type : MachineType.values())
        {
            int capacity = type == MachineType.DRYER ? 4 : 2;
            int machineID = 0;
            for (int i = 0; i < capacity; i++)
            {
                Machine assigned =
                    model.requestMachine("Using" + i, type).get();
                machineID = assigned.getMachineID();
            }
            assertFalse(model.requestMachine("First", type).isPresent());
            assertFalse(model.requestMachine("Second", type).isPresent());
            assertEquals(2, model.getQueueSize(type));
            List<Visitor> waiting = model.getWaitingList(type);
            assertEquals("First", waiting.get(0).getName());
            assertEquals("Second", waiting.get(1).getName());

            Visitor finished = model.finish(machineID);
            assertEquals("Using" + (capacity - 1), finished.getName());
            assertEquals(
                "First",
                model.getMachine(machineID).getCurrentVisitor().getName());
            assertEquals(1, model.getQueueSize(type));

            assertEquals("First", model.finish(machineID).getName());
            assertEquals(
                "Second",
                model.getMachine(machineID).getCurrentVisitor().getName());
            assertEquals(0, model.getQueueSize(type));

            assertEquals("Second", model.finish(machineID).getName());
            assertTrue(model.getMachine(machineID).isAvailable());
        }
    }


    /**
     * Waiting for one type does not add visitors to other queues.
     */
    public void testQueueIndependence()
    {
        model.requestMachine("A", MachineType.LARGE_WASHER);
        model.requestMachine("B", MachineType.LARGE_WASHER);
        model.requestMachine("C", MachineType.LARGE_WASHER);
        assertEquals(1, model.getQueueSize(MachineType.LARGE_WASHER));
        assertEquals(0, model.getQueueSize(MachineType.SMALL_WASHER));
        assertEquals(0, model.getQueueSize(MachineType.DRYER));
        Machine small =
            model.requestMachine("D", MachineType.SMALL_WASHER).get();
        model.finish(small.getMachineID());
        assertEquals(1, model.getQueueSize(MachineType.LARGE_WASHER));
    }


    /**
     * Identical names still represent distinct visitors.
     */
    public void testDuplicateNames()
    {
        Visitor first = model.requestMachine("Amy", MachineType.LARGE_WASHER)
            .get().getCurrentVisitor();
        Visitor second = model.requestMachine("Amy", MachineType.LARGE_WASHER)
            .get().getCurrentVisitor();
        assertFalse(first.getVisitorID() == second.getVisitorID());
    }


    /**
     * Failed requests leave existing machines and queues unchanged.
     */
    public void testInvalidRequestsPreserveState()
    {
        model.requestMachine("A", MachineType.LARGE_WASHER);
        model.requestMachine("B", MachineType.LARGE_WASHER);
        model.requestMachine("C", MachineType.LARGE_WASHER);
        String[] invalid = { null, "", " ", "A".repeat(51), "A\tB" };
        for (String name : invalid)
        {
            expectException(
                IllegalArgumentException.class,
                () -> model.requestMachine(name, MachineType.LARGE_WASHER));
        }
        expectException(
            IllegalArgumentException.class,
            () -> model.requestMachine("D", null));
        assertEquals("A", model.getMachine(1).getCurrentVisitor().getName());
        assertEquals("B", model.getMachine(2).getCurrentVisitor().getName());
        assertEquals(1, model.getQueueSize(MachineType.LARGE_WASHER));
        assertEquals(
            "C",
            model.getWaitingList(MachineType.LARGE_WASHER).get(0).getName());
        assertTrue(model.getMachine(3).isAvailable());
    }


    /**
     * Idle and unknown machines cannot finish a cycle.
     */
    public void testInvalidFinish()
    {
        model.requestMachine("A", MachineType.LARGE_WASHER);
        expectException(IllegalStateException.class, () -> model.finish(2));
        expectException(IllegalArgumentException.class, () -> model.finish(99));
        assertEquals("A", model.getMachine(1).getCurrentVisitor().getName());
        assertTrue(model.getMachine(2).isAvailable());

        model.finish(1);
        expectException(IllegalStateException.class, () -> model.finish(1));
        assertTrue(model.getMachine(1).isAvailable());
    }


    /**
     * A finished washer visitor can use a free dryer with the same ID.
     */
    public void testRequestAvailableDryer()
    {
        Machine washer =
            model.requestMachine("Amy", MachineType.SMALL_WASHER).get();
        Visitor finished = model.finish(washer.getMachineID());
        Optional<Machine> dryer = model.requestDryer(finished);
        assertTrue(dryer.isPresent());
        assertEquals(MachineType.DRYER, dryer.get().getType());
        assertSame(finished, dryer.get().getCurrentVisitor());
        assertEquals(0, model.getQueueSize(MachineType.DRYER));
    }


    /**
     * A full dryer group queues transfers behind existing dryer requests.
     */
    public void testRequestFullDryers()
    {
        for (int i = 0; i < 4; i++)
        {
            model.requestMachine("Drying" + i, MachineType.DRYER);
        }
        model.requestMachine("Waiting", MachineType.DRYER);
        Machine washer =
            model.requestMachine("Amy", MachineType.LARGE_WASHER).get();
        Visitor finished = model.finish(washer.getMachineID());
        assertFalse(model.requestDryer(finished).isPresent());
        assertEquals(2, model.getQueueSize(MachineType.DRYER));
        assertSame(finished, model.getWaitingList(MachineType.DRYER).get(1));
        model.finish(5);
        assertEquals(
            "Waiting",
            model.getMachine(5).getCurrentVisitor().getName());
        model.finish(5);
        assertSame(finished, model.getMachine(5).getCurrentVisitor());
    }


    /**
     * Rejects null, unfinished, duplicate, and already-dried transfers.
     */
    public void testInvalidDryerRequests()
    {
        expectException(
            IllegalArgumentException.class,
            () -> model.requestDryer(null));
        Machine washer =
            model.requestMachine("Amy", MachineType.LARGE_WASHER).get();
        Visitor visitor = washer.getCurrentVisitor();
        expectException(
            IllegalArgumentException.class,
            () -> model.requestDryer(visitor));
        Visitor finished = model.finish(washer.getMachineID());
        Machine dryer = model.requestDryer(finished).get();
        expectException(
            IllegalArgumentException.class,
            () -> model.requestDryer(finished));
        Visitor dried = model.finish(dryer.getMachineID());
        expectException(
            IllegalArgumentException.class,
            () -> model.requestDryer(dried));
        assertEquals(0, model.getQueueSize(MachineType.DRYER));
        assertTrue(model.getMachine(dryer.getMachineID()).isAvailable());
    }


    /**
     * Returned machine objects and lists cannot change internal machines.
     */
    public void testMachineSnapshots()
    {
        Machine assigned =
            model.requestMachine("Amy", MachineType.LARGE_WASHER).get();
        assigned.release();
        assertEquals("Amy", model.getMachine(1).getCurrentVisitor().getName());

        Machine queried = model.getMachine(1);
        queried.release();
        assertFalse(model.getMachine(1).isAvailable());

        List<Machine> machines = model.getMachines();
        machines.get(0).release();
        machines.clear();
        assertEquals(8, model.getMachines().size());
        assertFalse(model.getMachine(1).isAvailable());

        Machine idle = model.getMachine(2);
        idle.assign(new Visitor(99, "Outside", MachineType.LARGE_WASHER));
        assertTrue(model.getMachine(2).isAvailable());
    }


    /**
     * Modifying a returned waiting list cannot change its queue.
     */
    public void testWaitingListCopy()
    {
        model.requestMachine("A", MachineType.LARGE_WASHER);
        model.requestMachine("B", MachineType.LARGE_WASHER);
        model.requestMachine("C", MachineType.LARGE_WASHER);
        model.getWaitingList(MachineType.LARGE_WASHER).clear();
        assertEquals(1, model.getQueueSize(MachineType.LARGE_WASHER));
        assertEquals(
            "C",
            model.getWaitingList(MachineType.LARGE_WASHER).get(0).getName());
    }


    /**
     * Queries reject missing types and unknown machine IDs.
     */
    public void testInvalidQueries()
    {
        expectException(
            IllegalArgumentException.class,
            () -> model.getMachine(99));
        expectException(
            IllegalArgumentException.class,
            () -> model.getWaitingList(null));
        expectException(
            IllegalArgumentException.class,
            () -> model.getQueueSize(null));
    }


    /**
     * Checks that an operation throws the expected exception type.
     *
     * @param expected
     *            the required exception class
     * @param action
     *            the operation to execute
     */
    private void expectException(
        Class<? extends RuntimeException> expected,
        Runnable action)
    {
        try
        {
            action.run();
            fail("Expected " + expected.getSimpleName());
        }
        catch (RuntimeException exception)
        {
            assertTrue(
                "Unexpected exception: " + exception,
                expected.isInstance(exception));
        }
    }


    /**
     * Wash-only visitors do not automatically enter dryer service.
     */
    public void testWashOnlyDoesNotTransfer()
    {
        Machine washer =
            model.requestMachine("Amy", MachineType.LARGE_WASHER, false).get();

        Visitor finished = model.finish(washer.getMachineID());

        assertFalse(finished.needsDrying());
        assertTrue(model.getMachine(washer.getMachineID()).isAvailable());
        assertEquals(0, model.getQueueSize(MachineType.DRYER));

        for (Machine machine : model.getMachines())
        {
            if (machine.getType() == MachineType.DRYER)
            {
                assertTrue(machine.isAvailable());
            }
        }
    }


    /**
     * Wash-and-dry visitors automatically receive an available dryer. Both
     * washer types support automatic transfer.
     */
    public void testAutomaticTransferToAvailableDryer()
    {
        MachineType[] washerTypes =
            { MachineType.LARGE_WASHER, MachineType.SMALL_WASHER };

        for (MachineType type : washerTypes)
        {
            Machine washer = model.requestMachine("Amy", type, true).get();

            Visitor finished = model.finish(washer.getMachineID());

            assertTrue(finished.needsDrying());
            assertEquals(type, finished.getSelectedType());
            assertTrue(model.getMachine(washer.getMachineID()).isAvailable());

            int assignedDryers = 0;

            for (Machine machine : model.getMachines())
            {
                if (machine.getType() == MachineType.DRYER
                    && machine.getCurrentVisitor() == finished)
                {
                    assignedDryers++;
                }
            }

            assertEquals(1, assignedDryers);
            assertEquals(0, model.getQueueSize(MachineType.DRYER));
        }
    }


    /**
     * Automatic transfers join behind existing dryer requests. The washer also
     * serves its own next waiting visitor.
     */
    public void testAutomaticTransferPreservesBothQueues()
    {
        int dryerID = 0;

        for (int i = 0; i < 4; i++)
        {
            dryerID = model.requestMachine("Drying" + i, MachineType.DRYER)
                .get().getMachineID();
        }

        model.requestMachine("Already waiting", MachineType.DRYER);

        Machine washer =
            model.requestMachine("Amy", MachineType.LARGE_WASHER, true).get();

        model.requestMachine("Ben", MachineType.LARGE_WASHER);
        model.requestMachine("Chris", MachineType.LARGE_WASHER);

        Visitor finished = model.finish(washer.getMachineID());

        assertEquals(
            "Chris",
            model.getMachine(washer.getMachineID()).getCurrentVisitor()
                .getName());
        assertEquals(0, model.getQueueSize(MachineType.LARGE_WASHER));

        assertEquals(2, model.getQueueSize(MachineType.DRYER));
        assertEquals(
            "Already waiting",
            model.getWaitingList(MachineType.DRYER).get(0).getName());
        assertSame(finished, model.getWaitingList(MachineType.DRYER).get(1));

        model.finish(dryerID);

        assertEquals(
            "Already waiting",
            model.getMachine(dryerID).getCurrentVisitor().getName());
        assertEquals(1, model.getQueueSize(MachineType.DRYER));

        model.finish(dryerID);

        assertSame(finished, model.getMachine(dryerID).getCurrentVisitor());
        assertEquals(0, model.getQueueSize(MachineType.DRYER));
    }


    /**
     * Finishing a dryer does not send the visitor back into drying.
     */
    public void testDryerCompletionDoesNotTransferAgain()
    {
        Machine washer =
            model.requestMachine("Amy", MachineType.LARGE_WASHER, true).get();

        Visitor finished = model.finish(washer.getMachineID());
        int dryerID = -1;

        for (Machine machine : model.getMachines())
        {
            if (machine.getType() == MachineType.DRYER
                && machine.getCurrentVisitor() == finished)
            {
                dryerID = machine.getMachineID();
            }
        }

        assertTrue(dryerID != -1);
        assertSame(finished, model.finish(dryerID));
        assertEquals(0, model.getQueueSize(MachineType.DRYER));

        for (Machine machine : model.getMachines())
        {
            if (machine.getType() == MachineType.DRYER)
            {
                assertTrue(machine.isAvailable());
            }
        }
    }


    /**
     * A visitor already transferred automatically cannot request drying twice.
     */
    public void testAutomaticTransferRejectsDuplicateRequest()
    {
        Machine washer =
            model.requestMachine("Amy", MachineType.LARGE_WASHER, true).get();

        Visitor finished = model.finish(washer.getMachineID());

        expectException(
            IllegalArgumentException.class,
            () -> model.requestDryer(finished));

        assertEquals(0, model.getQueueSize(MachineType.DRYER));
    }


    /**
     * Requesting automatic drying with a dryer registration is invalid and must
     * not change machine or queue state.
     */
    public void testInvalidAutomaticDryingRequest()
    {
        expectException(
            IllegalArgumentException.class,
            () -> model.requestMachine("Amy", MachineType.DRYER, true));

        for (Machine machine : model.getMachines())
        {
            assertTrue(machine.isAvailable());
        }

        for (MachineType type : MachineType.values())
        {
            assertEquals(0, model.getQueueSize(type));
        }
    }
}
