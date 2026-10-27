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

public class RedoCommandTest {

    @Test
    public void execute_nothingUndone_failure() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        assertCommandFailure(new RedoCommand(), model, RedoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_afterUndo_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person zed = new PersonBuilder().withName("Zed").build();
        model.addPerson(zed);
        model.commitAddressBook();
        model.undoAddressBook();

        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.addPerson(zed);
        assertCommandSuccess(new RedoCommand(), model, RedoCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_afterNewChange_failure() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person zed = new PersonBuilder().withName("Zed").build();
        model.addPerson(zed);
        model.commitAddressBook();
        model.undoAddressBook();
        model.addPerson(new PersonBuilder().withName("Yan").build());
        model.commitAddressBook();

        assertCommandFailure(new RedoCommand(), model, RedoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_countWithinUndoneChanges_redoesThatManyChanges() {
        Model model = modelWithUndonePersons("Zed", "Yan", "Xia");

        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.addPerson(new PersonBuilder().withName("Zed").build());
        expectedModel.addPerson(new PersonBuilder().withName("Yan").build());
        assertCommandSuccess(new RedoCommand(2), model,
                String.format(RedoCommand.MESSAGE_SUCCESS_MULTIPLE, 2), expectedModel);
        assertEquals(1, model.getRedoableChangeCount());
    }

    @Test
    public void execute_countBeyondUndoneChanges_failureWithoutChange() {
        Model model = modelWithUndonePersons("Zed", "Yan");

        assertCommandFailure(new RedoCommand(5), model,
                String.format(RedoCommand.MESSAGE_NOT_ENOUGH_CHANGES, 2));
        assertEquals(2, model.getRedoableChangeCount());
    }

    @Test
    public void constructor_nonPositiveCount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new RedoCommand(0));
    }

    @Test
    public void recordsChange_returnsFalse() {
        assertFalse(new RedoCommand().recordsChange());
    }

    @Test
    public void equals() {
        RedoCommand redoOne = new RedoCommand();

        assertTrue(redoOne.equals(redoOne));
        assertTrue(redoOne.equals(new RedoCommand(1)));
        assertFalse(redoOne.equals(new RedoCommand(2)));
        assertFalse(redoOne.equals(null));
        assertFalse(redoOne.equals(new UndoCommand()));
    }

    @Test
    public void toStringMethod() {
        assertEquals(RedoCommand.class.getCanonicalName() + "{count=3}", new RedoCommand(3).toString());
    }

    /**
     * Returns a model of the typical persons in which each of {@code names} was added as a separate committed
     * change, and all of those changes were then undone.
     */
    private static Model modelWithUndonePersons(String... names) {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        for (String name : names) {
            model.addPerson(new PersonBuilder().withName(name).build());
            model.commitAddressBook();
        }
        for (int i = 0; i < names.length; i++) {
            model.undoAddressBook();
        }
        return model;
    }
}
