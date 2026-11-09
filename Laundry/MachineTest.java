package Laundry;

import student.TestCase;

/**
 * Tests machine state changes and invalid operations.
 *
 * @author Wenxin Zhang
 * @version 2026.11.07
 */
public class MachineTest extends TestCase
{
    private Machine machine;
    private Visitor amy;

    /** 
     * Creates an empty machine and a visitor before each test. 
     */
    @Override
    public void setUp()
    {
        machine = new Machine(1, MachineType.LARGE_WASHER);
        amy = new Visitor(1, "Amy", MachineType.LARGE_WASHER);
    }

    /** 
     * A new machine stores its ID and type and starts available. 
     */
    public void testConstructorAndGetters()
    {
        assertEquals(1, machine.getMachineID());
        assertEquals(MachineType.LARGE_WASHER, machine.getType());
        assertNull(machine.getCurrentVisitor());
        assertTrue(machine.isAvailable());
    }

    /** 
     * A machine cannot be created without an equipment type. 
     */
    public void testNullType()
    {
        try
        {
            new Machine(1, null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception.getMessage());
        }
    }

    /** 
     * Assignment occupies the machine; release returns the same visitor. 
     */
    public void testAssignAndRelease()
    {
        machine.assign(amy);
        assertFalse(machine.isAvailable());
        assertSame(amy, machine.getCurrentVisitor());
        assertSame(amy, machine.release());
        assertTrue(machine.isAvailable());
        assertNull(machine.getCurrentVisitor());

        Visitor ben = new Visitor(2, "Ben", MachineType.LARGE_WASHER);
        machine.assign(ben);
        assertSame(ben, machine.getCurrentVisitor());
    }

    /** 
     * Null assignment fails without changing an idle machine. 
     */
    public void testAssignNull()
    {
        try
        {
            machine.assign(null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException exception)
        {
            assertTrue(machine.isAvailable());
            assertNull(machine.getCurrentVisitor());
        }
    }


    /** 
     * An idle machine cannot complete a cycle. 
     */
    public void testReleaseIdle()
    {
        try
        {
            machine.release();
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException exception)
        {
            assertTrue(machine.isAvailable());
            assertNull(machine.getCurrentVisitor());
        }
    }
    
    /**
     * Assignment to an occupied machine fails without replacing its visitor.
     */
    public void testAssignOccupied()
    {
        machine.assign(amy);
        Visitor ben = new Visitor(2, "Ben", MachineType.LARGE_WASHER);

        try
        {
            machine.assign(ben);
            fail("Expected IllegalStateException");
        }
        catch (IllegalStateException exception)
        {
            assertSame(amy, machine.getCurrentVisitor());
            assertFalse(machine.isAvailable());
        }
    }
}
