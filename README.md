# cs2114-project3-group69
A laundromat simulator that helps users understand how customers wait for and use laundry machines.

## Reflection

### Changes from the Specification

We kept the planned MVC structure, three FIFO queues, and eight machines. The main public method signatures remain consistent with the specification.

We added a `LaundryApp` class to create the model, view, and controller and launch the application. We also added private helper methods in `LaundryModel` for selecting queues, locating machines, assigning visitors, and creating machine snapshots.

The specification described returning a copy of the machine list. Our implementation also copies the machine objects so callers cannot change internal machine state through the returned objects.

We added tracking of visitors who have finished washing. This lets `requestDryer()` reject visitors who have not finished washing or have already requested drying. Corresponding tests cover unfinished, duplicate, and already-dried transfers.

Validation remains in the model, as planned. The controller catches exceptions and asks the view to display messages.

### Model Independence

The model operates independently of the user interface. LaundryModel handles validation, waiting queues, machine assignment, and cycle completion without referencing the view or controller. The controller calls model methods and passes updated information to the view. Machine snapshots and waiting-list copies protect the model’s internal state. We can test the model directly without opening the interface.

### Our Original Unknowns

We worked out how button listeners connect user actions to controller methods and how the controller refreshes the display. We also developed tests that exercise the model independently of the interface.

We identified an enqueue error in `LinkedQueue`: it reused the head node instead of creating a new node. The correction must be included in the integrated version. Empty-queue handling also requires resetting both head and tail when the last element leaves.

We still need to verify the complete application and tests together.

### What Does Not Work Yet

The controller currently remembers only the most recent visitor who finished washing. If several visitors finish before requesting drying, earlier visitors cannot be selected through the current interface.

Repeated Finish actions can complete successive visitors because each action represents a completed cycle. The application does not currently distinguish an accidental double-click from two intentional actions.

## Next Steps

### Finish the MVP

1. Fix the queue's enqueue operation and confirm all classes use the same package.
2. Resolve test dependencies and run `MachineTest` and `LaundryModelTest`.
3. Verify registration, machine assignment, FIFO service, and error messages through the interface.
4. Replace the single most-recent-finished-visitor selection with a way to select among visitors who finished washing and still need drying.
5. Document the final test results and any remaining limitations.

### Change Feature: Automatic Wash-to-Dry Transfer

We will let visitors request washing and drying when they register. When washing finishes, the model will automatically assign them to an available dryer or add them to the end of the dryer queue.

We predict changes to:

- **Model:** `Visitor` will store whether drying is requested. `LaundryModel` will accept this choice and coordinate the transfer after washing.
- **View:** `LaundryView` will provide a wash-and-dry option and display transfer results.
- **Controller:** `LaundryController` will pass the new registration choice to the model and refresh the display.

`LinkedQueue`, `Machine`, and `MachineType` should not need changes because their existing responsibilities still apply.

### Stretch Goals

We are committing to automatic wash-to-dry transfer. We are deferring simulated cycle durations, estimated waiting times, machine breakdowns, and queue switching to keep Deliverable 4 manageable.

### New Tests

We will test that:

- Wash-only visitors do not transfer automatically.
- Wash-and-dry visitors receive an available dryer after washing.
- Visitors join the dryer queue when all dryers are occupied.
- Transfers join behind people already waiting for dryers.
- Both the washer queue and dryer queue remain correct after a transfer.
- Invalid requests do not change state.
- A visitor cannot receive duplicate dryer assignments.

### Ownership

| Member | Planned Work |
|---|---|
| Wenxin | Update `LaundryModel` for automatic transfers; expand model tests. |
| Yug | Add the drying preference to `Visitor`; fix and test `LinkedQueue`. |
| Ashwin | Update the view and controller, including selection of finished visitors and the new registration option. |
| All members | Review interfaces, integrate changes, and verify the complete workflow. |

## Attribution

| Class / Test File | Member | Contribution |
|---|---|---|
| `MachineType.java` | Wenxin | Developed equipment types and documentation with GenAI assistance. |
| `Machine.java` | Wenxin | Developed machine state, assignment, release, and validation with GenAI assistance. |
| `LaundryModel.java` | Wenxin | Developed queue coordination, machine assignment, input validation, and dryer requests with GenAI assistance. |
| `MachineTest.java` | Wenxin | Prepared tests for machine state changes and exceptions with GenAI assistance. |
| `LaundryModelTest.java` | Wenxin | Prepared tests for FIFO service, validation, dryer transfers, and state protection with GenAI assistance. |
| `LaundryApp.java` | Wenxin | Prepared the application entry point with GenAI assistance. |
| `LinkedQueue.java` | Yug | Wrote the linked queue implementation. |
| `Visitor.java` | Yug | Wrote visitor fields, constructor, and getters. |
| `LaundryView.java` | Ashwin | Developed the Swing interface and event-listener connections. |
| `LaundryController.java` | Ashwin | Developed user-action handling and view refresh logic. |

### GenAI Note

Wenxin used ChatGPT to refine the project specification, review my code, help write model methods and Javadoc comments, suggest test cases, and draft README reflection sections.
Yug used ChatGPT to debug LinkedQueue class and to verify my class methods and fields align with our spec.
Ashwin used ChatGPT to help outline the LaundryView and LaundryController classes. He first asked for an outline, then sample methods and fields. He used the general ideas but adjusted them for the project. ChatGPT helped with planning and structure, but the code still needed to be adapted and tested.
