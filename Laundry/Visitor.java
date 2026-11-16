package Laundry;

public class Visitor
{
    // ~ Fields ................................................................
    private int visitorID;
    private String name;
    private MachineType selectedType;
    private boolean needsDrying;

    // ~ Constructors ..........................................................
    public Visitor(int visitorID, String name, MachineType selectedType)
    {
        this.visitorID = visitorID;
        this.name = name;
        this.selectedType = selectedType;
        this.needsDrying = false;
    }


    /**
     * Creates a visitor with a drying preference.
     *
     * @param visitorID
     *            the visitor's ID
     * @param name
     *            the visitor's name
     * @param selectedType
     *            the requested equipment type
     * @param needsDrying
     *            whether drying is requested after washing
     */
    public Visitor(
        int visitorID,
        String name,
        MachineType selectedType,
        boolean needsDrying)
    {
        this.visitorID = visitorID;
        this.name = name;
        this.selectedType = selectedType;
        this.needsDrying = needsDrying;
    }


    // ~Public Methods ........................................................
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


    /**
     * Checks whether drying is requested after washing.
     *
     * @return true if the visitor requested drying
     */
    public boolean needsDrying()
    {
        return needsDrying;
    }

}
