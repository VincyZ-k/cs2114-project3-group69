import java.util.Optional;

/**
 * Controller: handles user actions, calls model operations, and refreshes the
 * view from the resulting model state. Contains no queueing/assignment rules.
 */
public class LaundryController {

    private final LaundryModel model;
    private final LaundryView view;

    /** Most recent visitor who finished a wash and may be sent to a dryer. */
    private Visitor lastFinishedWasher;

    public LaundryController(LaundryModel model, LaundryView view) {
        this.model = model;
        this.view = view;

        view.setJoinListener(this::handleJoin);
        view.setFinishListener(this::handleFinish);
        view.setDryerListener(() -> handleSendToDryer(lastFinishedWasher));

        refreshView();
    }

    public void handleJoin(String name, MachineType type) {
        try {
            Optional<Machine> assigned = model.requestMachine(name, type);
            String who = model.validateName(name);
            if (assigned.isPresent()) {
                view.showMessage(who + " assigned to machine " + assigned.get().getMachineID());
            } else {
                view.showMessage(who + " added to queue");
            }
            view.clearInput();
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());   // view keeps the typed text for re-entry
        }
        refreshView();
    }

    public void handleFinish(int machineID) {
        try {
            MachineType type = model.getMachine(machineID).getType();
            Visitor finished = model.finish(machineID);

            StringBuilder msg = new StringBuilder(finished.getName() + " finished on machine " + machineID);
            Visitor next = model.getMachine(machineID).getCurrentVisitor();
            if (next != null) {
                msg.append(". Next served: ").append(next.getName());
            }

            if (type != MachineType.DRYER) {
                lastFinishedWasher = finished;
                view.setDryerButtonEnabled(true);
                msg.append(". Use \"Send to dryer\" if they need drying.");
            }
            view.showMessage(msg.toString());
        } catch (IllegalStateException ex) {
            view.showError("Machine already free");
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
        }
        refreshView();
    }

    public void handleSendToDryer(Visitor finished) {
        try {
            Optional<Machine> dryer = model.requestDryer(finished);
            if (dryer.isPresent()) {
                view.showMessage(finished.getName() + " assigned to dryer " + dryer.get().getMachineID());
            } else {
                view.showMessage(finished.getName() + " added to dryer queue");
            }
            lastFinishedWasher = null;
            view.setDryerButtonEnabled(false);
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
        }
        refreshView();
    }

    private void refreshView() {
        view.showMachines(model.getMachines());
        for (MachineType t : MachineType.values()) {
            view.showWaitingList(t, model.getWaitingList(t));
        }
    }
}
