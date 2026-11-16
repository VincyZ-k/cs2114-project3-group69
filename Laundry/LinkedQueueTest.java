package Laundry;

import student.TestCase;
import java.util.*;

public class LinkedQueueTest
    extends TestCase
{
    // ~ Fields ................................................................
    private LinkedQueue<String> queue;

    // ~ Constructors ..........................................................

    // ~Public Methods .........................................................

    @Override
    public void setUp()
    {
        queue = new LinkedQueue<>();
    }


    /**
     * Place a description of your method here.
     */
    public void testEnqueue()
    {
        IllegalArgumentException exception = null;
        try
        {
            queue.enqueue(null);
        }
        catch (IllegalArgumentException e)
        {
            exception = e;
        }
        assertNotNull(exception);

        queue.enqueue("one");
        assertEquals(1, queue.size());
        assertEquals("one", queue.peek());

        queue.enqueue("two");
        queue.enqueue("three");
        assertEquals(3, queue.size());
        List<String> list1 = new ArrayList<>(List.of("one", "two", "three"));
        List<String> list2 = queue.toList();
        assertEquals(list1, list2);
    }


    public void testDequeue()
    {
        NoSuchElementException exception = null;
        try
        {
            queue.dequeue();
        }
        catch (NoSuchElementException e)
        {
            exception = e;
        }
        assertNotNull(exception);

        queue.enqueue("one");
        assertEquals("one", queue.dequeue());
    }


    public void testPeek()
    {
        NoSuchElementException exception = null;
        try
        {
            queue.peek();
        }
        catch (NoSuchElementException e)
        {
            exception = e;
        }
        assertNotNull(exception);

        queue.enqueue("one");
        assertEquals("one", queue.peek());
    }


    public void testIsEmpty()
    {
        assertTrue(queue.isEmpty());
        queue.enqueue("one");
        assertFalse(queue.isEmpty());
    }


    public void testSize()
    {
        assertEquals(0, queue.size());
        queue.enqueue("one");
        assertEquals(1, queue.size());
    }


    public void testToList()
    {
        queue.enqueue("one");
        queue.enqueue("two");
        queue.enqueue("three");
        List<String> list1 = new ArrayList<>(List.of("one", "two", "three"));
        List<String> list2 = queue.toList();
        assertEquals(list1, list2);
    }
}
