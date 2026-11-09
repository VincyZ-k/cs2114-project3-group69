package Laundry;

/**
 * Represents one laundry machine and its current visitor.
 * A machine is available when it has no current visitor.
 *
 * @author Wenxin Zhang
 * @version 2026.11.07
 */
public class Machine
{
    private final int machineID;
    private final MachineType type;
    private Visitor currentVisitor;

    /**
     * Creates an available machine with the given ID and type.
     *
     * @param machineID
     *            the machine's ID
     * @param type
     *            the equipment type
     * @throws IllegalArgumentException
     *             if type is null
     */
    public Machine(int machineID, MachineType type)
    {
        if (type == null)
        {
            throw new IllegalArgumentException("Select an equipment type");
        }

        this.machineID = machineID;
        this.type = type;
        currentVisitor = null;
    }

    /**
     * Gets the machine's ID.
     *
     * @return the machine ID
     */
    public int getMachineID()
    {
        return machineID;
    }

    /**
     * Gets the machine's equipment type.
     *
     * @return the equipment type
     */
    public MachineType getType()
    {
        return type;
    }

    /**
     * Gets the visitor currently using this machine.
     *
     * @return the current visitor, or null if available
     */
    public Visitor getCurrentVisitor()
    {
        return currentVisitor;
    }

    /**
     * Checks whether the machine is available.
     *
     * @return true if no visitor is using the machine
     */
    public boolean isAvailable()
    {
        return currentVisitor == null;
    }

    /**
     * Assigns a visitor to this machine.
     *
     * @param visitor
     *            the visitor to assign
     * @throws IllegalArgumentException
     *             if visitor is null
     * @throws IllegalStateException
     *             if the machine is already occupied
     */
    public void assign(Visitor visitor)
    {
        if (visitor == null)
        {
            throw new IllegalArgumentException("Visitor cannot be null");
        }

        if (!isAvailable())
        {
            throw new IllegalStateException("Machine is occupied");
        }

        currentVisitor = visitor;
    }

    /**
     * Removes and returns the current visitor, making the machine available.
     *
     * @return the visitor who finished
     * @throws IllegalStateException
     *             if the machine is already available
     */
    public Visitor release()
    {
        if (isAvailable())
        {
            throw new IllegalStateException("Machine already free");
        }

        Visitor finished = currentVisitor;
        currentVisitor = null;
        return finished;
    }
}