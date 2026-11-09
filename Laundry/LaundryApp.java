package Laundry;

import javax.swing.SwingUtilities;

/**
 * Starts the laundromat application.
 *
 * @author Wenxin Zhang
 * @version 2026.11.09
 */
public class LaundryApp
{
    /**
     * Creates and displays the application on Swing's event thread.
     *
     * @param args
     *            command-line arguments, not used
     */
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() ->
        {
            LaundryModel model = new LaundryModel();
            LaundryView view = new LaundryView();

            new LaundryController(model, view);

            view.setVisible(true);
        });
    }
}