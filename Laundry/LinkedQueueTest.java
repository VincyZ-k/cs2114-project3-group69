package Laundry;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LinkedQueueTest
{
    
    //~ Fields ................................................................
    private LinkedQueue<String> queue;

    
    //~ Constructors ..........................................................

    
    //~Public  Methods ........................................................
    @BeforeEach
    public void setUp()
    {
        queue = new LinkedQueue<>();
    }
    
    /**
     * Place a description of your method here.
     */
    @Test
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
    
    @Test
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
    
    @Test
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
    
    @Test
    public void testIsEmpty()
    {
       assertTrue(queue.isEmpty());
       queue.enqueue("one");
       assertFalse(queue.isEmpty());
    }
    
    @Test
    public void testSize()
    {
        assertEquals(0, queue.size());
        queue.enqueue("one");
        assertEquals(1, queue.size());
    }
    
    @Test
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
