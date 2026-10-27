package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class UndoCommandTest {

    @Test
    public void execute_noChange_failure() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        assertCommandFailure(new UndoCommand(), model, UndoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_afterCommittedChange_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.addPerson(new PersonBuilder().withName("Zed").build());
        model.commitAddressBook();

        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        assertCommandSuccess(new UndoCommand(), model, UndoCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_afterFilteredList_showsAllPersons() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.addPerson(new PersonBuilder().withName("Zed").build());
        model.commitAddressBook();
        model.updateFilteredPersonList(person -> false);

        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        assertCommandSuccess(new UndoCommand(), model, UndoCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_countWithinHistory_undoesThatManyChanges() {
        Model model = modelWithCommittedPersons("Zed", "Yan", "Xia");

        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.addPerson(new PersonBuilder().withName("Zed").build());
        assertCommandSuccess(new UndoCommand(2), model,
                String.format(UndoCommand.MESSAGE_SUCCESS_MULTIPLE, 2), expectedModel);
        assertEquals(1, model.getUndoableChangeCount());
        assertEquals(2, model.getRedoableChangeCount());
    }

    @Test
    public void execute_countEqualToHistory_undoesAllChanges() {
        Model model = modelWithCommittedPersons("Zed", "Yan");

        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        assertCommandSuccess(new UndoCommand(2), model,
                String.format(UndoCommand.MESSAGE_SUCCESS_MULTIPLE, 2), expectedModel);
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_countBeyondHistory_failureWithoutChange() {
        Model model = modelWithCommittedPersons("Zed", "Yan");

        assertCommandFailure(new UndoCommand(3), model,
                String.format(UndoCommand.MESSAGE_NOT_ENOUGH_CHANGES, 2));
        assertEquals(2, model.getUndoableChangeCount());
    }

    @Test
    public void execute_countWithNoHistory_failure() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        assertCommandFailure(new UndoCommand(Integer.MAX_VALUE), model, UndoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void constructor_nonPositiveCount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new UndoCommand(0));
        assertThrows(IllegalArgumentException.class, () -> new UndoCommand(-1));
    }

    @Test
    public void recordsChange_returnsFalse() {
        assertFalse(new UndoCommand().recordsChange());
    }

    @Test
    public void equals() {
        UndoCommand undoOne = new UndoCommand();
        UndoCommand undoTwo = new UndoCommand(2);

        assertTrue(undoOne.equals(undoOne));
        assertTrue(undoOne.equals(new UndoCommand(1)));
        assertFalse(undoOne.equals(undoTwo));
        assertFalse(undoOne.equals(null));
        assertFalse(undoOne.equals(new RedoCommand()));
    }

    @Test
    public void toStringMethod() {
        assertEquals(UndoCommand.class.getCanonicalName() + "{count=2}", new UndoCommand(2).toString());
    }

    /**
     * Returns a model of the typical persons in which each of {@code names} was added as a separate committed change.
     */
    private static Model modelWithCommittedPersons(String... names) {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        for (String name : names) {
            Person person = new PersonBuilder().withName(name).build();
            model.addPerson(person);
            model.commitAddressBook();
        }
        return model;
    }
}
