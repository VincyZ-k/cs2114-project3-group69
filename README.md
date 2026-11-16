## Reflection

### How Our Design Absorbed the Change

Our new feature is automatic wash-to-dry transfer. During registration, users can select “Dry after washing.” When their wash finishes, the model assigns an available dryer or adds them to the end of the dryer queue.

The feature changed `Visitor`, `LaundryModel`, `LaundryView`, and `LaundryController`, matching our D3 prediction. `Visitor` stores the drying preference, the model handles transfers, the view provides a checkbox, and the controller passes the preference to the model. `LinkedQueue`, `Machine`, and `MachineType` did not need changes.

We added a four-argument `Visitor` constructor and a three-argument `requestMachine()` method. The original versions remain available and default to no automatic drying, preserving existing calls.

### Model / View Boundary

The model still contains no interface code. The view knows the user's drying selection and displays machine states and waiting lists, but does not decide how transfers happen. The controller forwards the selection and refreshes the display after model operations.

No queueing or assignment logic had to move into the view or controller.

### Changes from Our D3 Plan

We implemented automatic transfers while retaining manual cycle completion. We did not add simulated cycle durations.

We retained the manual “Send to dryer” option for visitors who did not select automatic drying. This option still remembers only the most recent visitor who finished washing without automatic drying.

We added model tests for automatic transfers, FIFO order when dryers are full, wash-only requests, duplicate dryer requests, and invalid drying selections. The existing Visitor, Controller, and View tests have not yet been updated for the new feature.

### Remaining Limitations

The updated tests and complete application still need a final verification run. We cannot yet claim that all tests pass in the integrated project.

Visitor tests do not yet cover the new constructor or drying preference. Controller and View tests do not yet cover the checkbox or forwarding its value.

Cycle completion is manual. Repeated Finish actions can complete successive visitors; accidental double-click protection has not been implemented.

## Next Steps

### Remaining Work for the Final Build

1. Run all existing and new tests and fix any failures.
2. Update Visitor tests for both constructors and the drying preference.
3. Update Controller and View tests for the new registration option.
4. Verify automatic transfers through the interface with both available and occupied dryers.
5. Review messages and the remaining manual-drying limitation.
6. Finalize documentation and rehearse the demonstration.

### Stretch Goals

We are committing to automatic wash-to-dry transfer. We are deferring simulated cycle durations, wait-time estimates, machine breakdowns, and queue switching.

### Three-Minute Demo

We will first show registration and assignment to an available washer. We will then fill a washer category to demonstrate FIFO waiting and reassignment.

Next, we will register a visitor with automatic drying and finish their wash to show transfer to an available dryer. We will also show that transfers join the end of the dryer queue when dryers are full.

Finally, we will demonstrate an invalid input and show the model test results.

### Planned Ownership

| Member | Remaining Work |
|---|---|
| Wenxin | Verify automatic-transfer logic, expand model tests as needed, and update documentation. |
| Yug | Update Visitor tests and verify the Queue implementation and tests. |
| Ashwin | Update Controller and View tests and review interface behavior. |
| All members | Integrate changes, verify the complete application, and prepare the demo. |

## Attribution

| Class / Test File | Member(s) | Contributions |
|---|---|---|
| `MachineType.java` | Wenxin | Implemented and documented equipment types with GenAI assistance. |
| `Machine.java` | Wenxin | Implemented machine state, assignment, release, and validation with GenAI assistance. |
| `LaundryModel.java` | Wenxin | Implemented machine allocation, validation, dryer requests, and automatic transfers with GenAI assistance. |
| `MachineTest.java` | Wenxin | Added tests for machine state changes and invalid operations with GenAI assistance. |
| `LaundryModelTest.java` | Wenxin | Added model tests, including automatic-transfer cases, with GenAI assistance. |
| `LaundryApp.java` | Wenxin | Added the application entry point with GenAI assistance. |
| `LinkedQueue.java` | Yug | Implemented the singly linked FIFO queue. |
| `Visitor.java` | Yug, Wenxin | Yug wrote the original class. Wenxin added the drying preference, constructor overload, and documentation with GenAI assistance. |
| `LaundryView.java` | Ashwin, Wenxin | Ashwin wrote the original interface. Wenxin added the automatic-drying checkbox and its input behavior with GenAI assistance. |
| `LaundryController.java` | Ashwin, Wenxin | Ashwin wrote the original controller. Wenxin connected the drying preference to registration and updated completion messages with GenAI assistance. |
| `LaundryControllerTest.java` | Ashwin | Existing controller tests; not yet updated for automatic drying. |
| `LaundryViewTest.java` | Ashwin | Existing view tests; not yet updated for automatic drying. |

### GenAI Note

Wenxin used ChatGPT to help implement and review automatic drying, connect the checkbox to the controller and model, suggest test cases, and draft the README reflection on how our design supported the change.
