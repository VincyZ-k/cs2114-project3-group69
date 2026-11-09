package Laundry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Manages the laundromat's machines, waiting queues, and assignment rules.
 * Contains no user interface code.
 *
 * @author Wenxin Zhang
 * @version 2026.11.09
 */
public class LaundryModel
{
    private final LinkedQueue<Visitor> largeQueue;
    private final LinkedQueue<Visitor> smallQueue;
    private final LinkedQueue<Visitor> dryerQueue;

    private final List<Machine> machines;
    private final Set<Visitor> finishedWashers;

    private int nextVisitorID;

    /**
     * Creates three waiting queues and eight available machines.
     * Machines 1-2 are large washers, 3-4 are small washers,
     * and 5-8 are dryers.
     */
    public LaundryModel()
    {
        largeQueue = new LinkedQueue<>();
        smallQueue = new LinkedQueue<>();
        dryerQueue = new LinkedQueue<>();

        machines = new ArrayList<>();
        finishedWashers = new HashSet<>();
        nextVisitorID = 1;

        for (int id = 1; id <= 8; id++)
        {
            MachineType type;

            if (id <= 2)
            {
                type = MachineType.LARGE_WASHER;
            }
            else if (id <= 4)
            {
                type = MachineType.SMALL_WASHER;
            }
            else
            {
                type = MachineType.DRYER;
            }

            machines.add(new Machine(id, type));
        }
    }

    /**
     * Validates a name and returns it without surrounding spaces.
     *
     * @param rawName
     *            the name entered by the user
     * @return the trimmed name
     * @throws IllegalArgumentException
     *             if the name is null, blank, longer than 50 characters
     *             after trimming, or contains control characters
     */
    public String validateName(String rawName)
    {
        if (rawName == null || rawName.trim().isEmpty())
        {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        for (int i = 0; i < rawName.length(); i++)
        {
            if (Character.isISOControl(rawName.charAt(i)))
            {
                throw new IllegalArgumentException(
                    "Name cannot contain control characters");
            }
        }

        String name = rawName.trim();

        if (name.length() > 50)
        {
            throw new IllegalArgumentException(
                "Name cannot exceed 50 characters");
        }

        return name;
    }

    /**
     * Registers a new visitor and assigns an available matching machine,
     * or adds the visitor to the matching waiting queue.
     * 
     * @param name
     *            the visitor's name
     * @param type
     *            the requested equipment type
     * @return a snapshot of the assigned machine, or an empty Optional
     *         if the visitor was queued
     * @throws IllegalArgumentException
     *             if the name is invalid or type is null
     */
    public Optional<Machine> requestMachine(
        String name,
        MachineType type)
    {
        String cleanedName = validateName(name);
        LinkedQueue<Visitor> queue = queueFor(type);


        Visitor visitor = new Visitor(
            nextVisitorID,
            cleanedName,
            type);
        nextVisitorID++;

        return assignOrQueue(visitor, type, queue);
    }

    /**
     * Finishes the current visitor's cycle and assigns the next visitor
     * from the matching waiting queue, if any.
     * If no visitors are waiting, the machine remains available.
     * Visitors who finish washing are recorded for a later dryer request.
     *
     * @param machineID
     *            the ID of the machine to finish
     * @return the visitor who completed the cycle
     * @throws IllegalArgumentException
     *             if the machine ID does not exist
     * @throws IllegalStateException
     *             if the machine is already available
     */
    public Visitor finish(int machineID)
    {
        Machine machine = findMachine(machineID);
        Visitor finished = machine.release();

        if (machine.getType() != MachineType.DRYER)
        {
            finishedWashers.add(finished);
        }

        LinkedQueue<Visitor> queue = queueFor(machine.getType());

        if (!queue.isEmpty())
        {
            machine.assign(queue.dequeue());
        }

        return finished;
    }

    /**
     * Sends a visitor who finished washing to a dryer.
     * If all dryers are occupied, adds the visitor to the dryer queue.
     * Each completed wash may be sent to drying only once.
     *
     * @param finished
     *            the visitor returned by finish after a wash
     * @return a snapshot of the assigned dryer, or an empty Optional
     *         if the visitor was queued
     * @throws IllegalArgumentException
     *             if the visitor is null, has not finished washing in
     *             this model, or has already requested drying
     */
    public Optional<Machine> requestDryer(Visitor finished)
    {
        if (finished == null)
        {
            throw new IllegalArgumentException(
                "Visitor cannot be null");
        }

        if (!finishedWashers.contains(finished))
        {
            throw new IllegalArgumentException(
                "Visitor must have finished washing "
                    + "and not requested drying yet");
        }

        Optional<Machine> result = assignOrQueue(
            finished,
            MachineType.DRYER,
            dryerQueue);

        finishedWashers.remove(finished);

        return result;
    }

    /**
     * Gets snapshots of all eight machines.
     * Changing the returned list or machines does not change the model.
     *
     * @return a new list of machine snapshots
     */
    public List<Machine> getMachines()
    {
        List<Machine> result = new ArrayList<>();

        for (Machine machine : machines)
        {
            result.add(snapshot(machine));
        }

        return result;
    }

    /**
     * Gets a snapshot of one machine.
     *
     * @param machineID
     *            the machine's ID
     * @return a snapshot of the requested machine
     * @throws IllegalArgumentException
     *             if the machine ID does not exist
     */
    public Machine getMachine(int machineID)
    {
        return snapshot(findMachine(machineID));
    }

    /**
     * Gets a copy of the waiting list for an equipment type.
     *
     * @param type
     *            the equipment type
     * @return a new list of waiting visitors in FIFO order
     * @throws IllegalArgumentException
     *             if type is null
     */
    public List<Visitor> getWaitingList(MachineType type)
    {
        return queueFor(type).toList();
    }

    /**
     * Gets the number of visitors waiting for an equipment type.
     *
     * @param type
     *            the equipment type
     * @return the number of waiting visitors
     * @throws IllegalArgumentException
     *             if type is null
     */
    public int getQueueSize(MachineType type)
    {
        return queueFor(type).size();
    }

    /**
     * Finds the waiting queue for an equipment type.
     *
     * @param type
     *            the equipment type
     * @return the corresponding internal queue
     * @throws IllegalArgumentException
     *             if type is null or unsupported
     */
    private LinkedQueue<Visitor> queueFor(MachineType type)
    {
        if (type == null)
        {
            throw new IllegalArgumentException(
                "Select an equipment type");
        }

        switch (type)
        {
            case LARGE_WASHER:
                return largeQueue;

            case SMALL_WASHER:
                return smallQueue;

            case DRYER:
                return dryerQueue;

            default:
                throw new IllegalArgumentException(
                    "Unknown equipment type");
        }
    }

    /**
     * Assigns a matching available machine or adds the visitor
     * to the supplied queue.
     *
     * @param visitor
     *            the visitor requesting service
     * @param type
     *            the requested equipment type
     * @param queue
     *            the waiting queue for that type
     * @return a machine snapshot, or an empty Optional if queued
     */
    private Optional<Machine> assignOrQueue(
        Visitor visitor,
        MachineType type,
        LinkedQueue<Visitor> queue)
    {
        for (Machine machine : machines)
        {
            if (machine.getType() == type && machine.isAvailable())
            {
                machine.assign(visitor);
                return Optional.of(snapshot(machine));
            }
        }

        queue.enqueue(visitor);
        return Optional.empty();
    }

    /**
     * Finds an internal machine by ID.
     *
     * @param machineID
     *            the machine's ID
     * @return the internal machine
     * @throws IllegalArgumentException
     *             if the machine ID does not exist
     */
    private Machine findMachine(int machineID)
    {
        for (Machine machine : machines)
        {
            if (machine.getMachineID() == machineID)
            {
                return machine;
            }
        }

        throw new IllegalArgumentException(
            "Unknown machine ID: " + machineID);
    }

    /**
     * Copies a machine's current state so callers cannot modify
     * the internal machine.
     *
     * @param original
     *            the machine to copy
     * @return a detached copy of the machine
     */
    private Machine snapshot(Machine original)
    {
        Machine copy = new Machine(
            original.getMachineID(),
            original.getType());

        if (!original.isAvailable())
        {
            copy.assign(original.getCurrentVisitor());
        }

        return copy;
    }
}