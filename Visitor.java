package Laundry;
public class Visitor
{
    //~ Fields ................................................................
    private int visitorID;
    private String name;
    private MachineType selectedType;
    
    //~ Constructors ..........................................................
    public Visitor(int visitorID, String name, MachineType selectedType)
    {
        this.visitorID = visitorID;
        this.name = name;
        this.selectedType = selectedType; 
    }
    
    //~Public  Methods ........................................................
    public int getVisitorID()
    {
        return visitorID;
    }
    
    public String getName()
    {
        return name;
    }
    
    public MachineType getSelectedType()
    {
        return selectedType;
    }
}
