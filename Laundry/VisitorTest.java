package Laundry;

import student.TestCase;

public class VisitorTest
    extends TestCase
{
    // ~ Fields ................................................................
    private Visitor visitor;

    // ~ Constructors ..........................................................

    // ~Public Methods .........................................................

    @Override
    public void setUp()
    {
        visitor = new Visitor(1, "john", null);
    }


    public void testGetVisitorID()
    {
        assertEquals(1, visitor.getVisitorID());
    }


    public void testGetName()
    {
        assertEquals("john", visitor.getName());
    }


    public void testGetSelectedType()
    {
        assertEquals(null, visitor.getSelectedType());
    }
}
