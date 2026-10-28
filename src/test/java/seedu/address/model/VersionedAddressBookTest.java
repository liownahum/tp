package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class VersionedAddressBookTest {

    private static AddressBook bookOf(Person... persons) {
        AddressBook book = new AddressBook();
        for (Person p : persons) {
            book.addPerson(p);
        }
        return book;
    }

    @Test
    public void constructor_initialState_cannotUndo() {
        VersionedAddressBook vab = new VersionedAddressBook(new AddressBook());
        assertFalse(vab.canUndo());
        assertEquals(bookOf(), vab);
    }

    @Test
    public void commit_noChange_cannotUndo() {
        VersionedAddressBook vab = new VersionedAddressBook(new AddressBook());
        vab.commit();
        assertFalse(vab.canUndo());
    }

    @Test
    public void undo_afterCommittedChange_restoresPreviousState() {
        VersionedAddressBook vab = new VersionedAddressBook(new AddressBook());
        vab.addPerson(ALICE);
        vab.commit();
        assertTrue(vab.canUndo());
        vab.undo();
        assertEquals(bookOf(), vab);
        assertFalse(vab.canUndo());
    }

    @Test
    public void undo_multipleTimes_restoresStatesInOrder() {
        VersionedAddressBook vab = new VersionedAddressBook(new AddressBook());
        vab.addPerson(ALICE);
        vab.commit();
        vab.addPerson(BENSON);
        vab.commit();
        vab.undo();
        assertEquals(bookOf(ALICE), vab);
        vab.undo();
        assertEquals(bookOf(), vab);
        assertFalse(vab.canUndo());
    }

    @Test
    public void undo_afterRolledBackUndo_revertsAgain() {
        VersionedAddressBook vab = new VersionedAddressBook(new AddressBook());
        vab.addPerson(ALICE);
        vab.commit();
        vab.addPerson(BENSON);
        vab.commit();
        int pointerBeforeUndo = vab.getCurrentStatePointer();
        vab.undo();

        // roll back the undo, as LogicManager does when saving fails
        vab.resetData(bookOf(ALICE, BENSON));
        vab.setCurrentStatePointer(pointerBeforeUndo);

        vab.undo();
        assertEquals(bookOf(ALICE), vab);
        vab.undo();
        assertEquals(bookOf(), vab);
        assertFalse(vab.canUndo());
    }

    @Test
    public void commit_afterRolledBackUndo_keepsEarlierStates() {
        VersionedAddressBook vab = new VersionedAddressBook(new AddressBook());
        vab.addPerson(ALICE);
        vab.commit();
        vab.addPerson(BENSON);
        vab.commit();
        int pointerBeforeUndo = vab.getCurrentStatePointer();
        vab.undo();
        vab.resetData(bookOf(ALICE, BENSON));
        vab.setCurrentStatePointer(pointerBeforeUndo);

        vab.removePerson(ALICE);
        vab.commit();
        vab.undo();
        assertEquals(bookOf(ALICE, BENSON), vab);
    }

    @Test
    public void setCurrentStatePointer_outsideHistory_throwsIllegalArgumentException() {
        VersionedAddressBook vab = new VersionedAddressBook(new AddressBook());
        vab.addPerson(ALICE);
        vab.commit();
        assertThrows(IllegalArgumentException.class, () -> vab.setCurrentStatePointer(-1));
        assertThrows(IllegalArgumentException.class, () -> vab.setCurrentStatePointer(2));
    }

    @Test
    public void undo_nothingToUndo_throwsNoUndoableStateException() {
        VersionedAddressBook vab = new VersionedAddressBook(new AddressBook());
        assertThrows(VersionedAddressBook.NoUndoableStateException.class, vab::undo);
    }

    @Test
    public void commit_afterUndo_discardsLaterStates() {
        VersionedAddressBook vab = new VersionedAddressBook(new AddressBook());
        vab.addPerson(ALICE);
        vab.commit();
        vab.undo();
        vab.addPerson(BENSON);
        vab.commit();
        vab.undo();
        assertEquals(bookOf(), vab);
        assertFalse(vab.canUndo());
    }

    @Test
    public void getUndoableAndRedoableCount_afterCommitsAndUndo_countsStates() {
        VersionedAddressBook vab = new VersionedAddressBook(new AddressBook());
        assertEquals(0, vab.getUndoableCount());
        assertEquals(0, vab.getRedoableCount());
        vab.addPerson(ALICE);
        vab.commit();
        vab.addPerson(BENSON);
        vab.commit();
        assertEquals(2, vab.getUndoableCount());
        assertEquals(0, vab.getRedoableCount());
        vab.undo();
        assertEquals(1, vab.getUndoableCount());
        assertEquals(1, vab.getRedoableCount());
    }

    @Test
    public void commit_noChangeAfterUndo_discardsUndoneStates() {
        VersionedAddressBook vab = new VersionedAddressBook(new AddressBook());
        vab.addPerson(ALICE);
        vab.commit();
        vab.undo();
        vab.commit();
        assertFalse(vab.canRedo());
        assertFalse(vab.canUndo());
        assertEquals(bookOf(), vab);
    }

    @Test
    public void equals_variousScenarios() {
        VersionedAddressBook vab1 = new VersionedAddressBook(new AddressBook());
        VersionedAddressBook vab2 = new VersionedAddressBook(new AddressBook());

        assertTrue(vab1.equals(vab2));
        assertFalse(vab1.equals(null));
        assertFalse(vab1.equals("Not a VersionedAddressBook"));

        vab1.addPerson(ALICE);
        vab1.commit();
        assertFalse(vab1.equals(vab2));

        vab2.addPerson(ALICE);
        vab2.commit();
        assertTrue(vab1.equals(vab2));

        // same data but different undo history -> equal
        assertTrue(vab1.equals(new VersionedAddressBook(vab1)));
    }

    @Test
    public void canRedo_initialState_false() {
        VersionedAddressBook vab = new VersionedAddressBook(new AddressBook());
        assertFalse(vab.canRedo());
    }

    @Test
    public void redo_afterUndo_restoresUndoneState() {
        VersionedAddressBook vab = new VersionedAddressBook(new AddressBook());
        vab.addPerson(ALICE);
        vab.commit();
        vab.undo();
        assertTrue(vab.canRedo());
        vab.redo();
        assertEquals(bookOf(ALICE), vab);
        assertFalse(vab.canRedo());
    }

    @Test
    public void redo_multipleTimes_restoresStatesInOrder() {
        VersionedAddressBook vab = new VersionedAddressBook(new AddressBook());
        vab.addPerson(ALICE);
        vab.commit();
        vab.addPerson(BENSON);
        vab.commit();
        vab.undo();
        vab.undo();
        vab.redo();
        assertEquals(bookOf(ALICE), vab);
        vab.redo();
        assertEquals(bookOf(ALICE, BENSON), vab);
        assertFalse(vab.canRedo());
    }

    @Test
    public void redo_afterNewCommit_notPossible() {
        VersionedAddressBook vab = new VersionedAddressBook(new AddressBook());
        vab.addPerson(ALICE);
        vab.commit();
        vab.undo();
        vab.addPerson(BENSON);
        vab.commit();
        assertFalse(vab.canRedo());
    }

    @Test
    public void redo_afterRolledBackRedo_redoesAgain() {
        VersionedAddressBook vab = new VersionedAddressBook(new AddressBook());
        vab.addPerson(ALICE);
        vab.commit();
        vab.undo();
        int pointerBeforeRedo = vab.getCurrentStatePointer();
        vab.redo();

        // roll back the redo, as LogicManager does when saving fails
        vab.resetData(bookOf());
        vab.setCurrentStatePointer(pointerBeforeRedo);

        assertFalse(vab.canUndo());
        assertTrue(vab.canRedo());
        vab.redo();
        assertEquals(bookOf(ALICE), vab);
    }

    @Test
    public void redo_nothingToRedo_throwsNoRedoableStateException() {
        VersionedAddressBook vab = new VersionedAddressBook(new AddressBook());
        assertThrows(VersionedAddressBook.NoRedoableStateException.class, vab::redo);
    }

    @Test
    public void undoThenRedo_canUndoAgain() {
        VersionedAddressBook vab = new VersionedAddressBook(new AddressBook());
        vab.addPerson(ALICE);
        vab.commit();
        vab.undo();
        vab.redo();
        assertTrue(vab.canUndo());
        vab.undo();
        assertEquals(bookOf(), vab);
    }

    @Test
    public void commit_instrumentCaseOnlyChange_recordsStateAndUndoRestoresCapitalisation() {
        AddressBook original = new AddressBook();
        original.setPersons(List.of(new PersonBuilder(ALICE).withInstruments("Violin:8").build()));
        VersionedAddressBook versionedAddressBook = new VersionedAddressBook(original);

        Person oldPerson = versionedAddressBook.getPersonList().get(0);
        Person editedPerson = new PersonBuilder(oldPerson).withInstruments("violin:8").build();
        versionedAddressBook.setPerson(oldPerson, editedPerson);
        versionedAddressBook.commit();

        assertTrue(versionedAddressBook.canUndo());

        versionedAddressBook.undo();
        assertTrue(versionedAddressBook.hasIdenticalData(original));
    }

    @Test
    public void commit_groupCaseOnlyChange_recordsStateAndUndoRestoresCapitalisation() {
        AddressBook original = new AddressBook();
        original.setPersons(List.of(new PersonBuilder(ALICE).withGroups("Band").build()));
        VersionedAddressBook versionedAddressBook = new VersionedAddressBook(original);

        Person oldPerson = versionedAddressBook.getPersonList().get(0);
        versionedAddressBook.setPerson(oldPerson, new PersonBuilder(oldPerson).withGroups("band").build());
        versionedAddressBook.commit();

        assertTrue(versionedAddressBook.canUndo());
        versionedAddressBook.undo();
        assertTrue(versionedAddressBook.hasIdenticalData(original));
    }

    @Test
    public void hasIdenticalData_differentNumberOfPersons_returnsFalse() {
        VersionedAddressBook versionedAddressBook = new VersionedAddressBook(new AddressBook());
        versionedAddressBook.addPerson(ALICE);
        assertFalse(versionedAddressBook.hasIdenticalData(new AddressBook()));
    }
}
