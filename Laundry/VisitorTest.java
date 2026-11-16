package Laundry;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VisitorTest
{
    //~ Fields ................................................................
    private Visitor visitor;
    //~ Constructors ..........................................................

    //~Public  Methods ........................................................
    @BeforeEach
    public void setUp()
    {
        visitor = new Visitor(1, "john", null);
    }
    
    @Test
    public void testGetVisitorID()
    {
        assertEquals(1, visitor.getVisitorID());
    }
    
    @Test
    public void testGetName()
    {
        assertEquals("john", visitor.getName());
    }
    
    @Test
    public void testGetSelectedType()
    {
        assertEquals(null, visitor.getSelectedType());
    
    }
}
