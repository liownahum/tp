package seedu.address.model;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@code AddressBook} that keeps a history of its committed states so that changes can be undone and redone.
 */
public class VersionedAddressBook extends AddressBook {

    private final List<ReadOnlyAddressBook> addressBookStateList;
    private int currentStatePointer;

    /**
     * Constructs a {@code VersionedAddressBook} with the given initial state.
     *
     * @param initialState the initial state of the address book
     */
    public VersionedAddressBook(ReadOnlyAddressBook initialState) {
        super(initialState);
        addressBookStateList = new ArrayList<>();
        addressBookStateList.add(new AddressBook(initialState));
        currentStatePointer = 0;
    }

    /**
     * Discards any states after the current one, then records the current data as a new state.
     * No new state is recorded if the data is unchanged since the current state, so commands that
     * do not modify data do not create empty undo steps.
     */
    public void commit() {
        addressBookStateList.subList(currentStatePointer + 1, addressBookStateList.size()).clear();
        if (!hasUncommittedChanges()) {
            return;
        }
        addressBookStateList.add(new AddressBook(this));
        currentStatePointer++;
        assert currentStatePointer == addressBookStateList.size() - 1;
    }

    /**
     * Returns true if there is a change that {@link #undo()} can revert.
     */
    public boolean canUndo() {
        return currentStatePointer > 0;
    }

    /**
     * Restores the data to the previous state in the history.
     *
     * @throws NoUndoableStateException if there is no change to revert.
     */
    public void undo() {
        if (!canUndo()) {
            throw new NoUndoableStateException();
        }
        assert !hasUncommittedChanges() : "Uncommitted changes would be lost by undo";
        currentStatePointer--;
        resetData(addressBookStateList.get(currentStatePointer));
    }

    /**
     * Returns the number of changes that {@link #undo()} can revert one after another.
     */
    public int getUndoableCount() {
        return currentStatePointer;
    }

    /**
     * Returns the number of undone changes that {@link #redo()} can restore one after another.
     */
    public int getRedoableCount() {
        return addressBookStateList.size() - 1 - currentStatePointer;
    }

    /**
     * Returns the position of the current state in the history.
     */
    public int getCurrentStatePointer() {
        return currentStatePointer;
    }

    /**
     * Moves the current state to {@code pointer} without changing the data.
     * Used to roll back an {@link #undo()} or {@link #redo()} whose result could not be saved.
     *
     * @throws IllegalArgumentException if {@code pointer} is not a position in the history.
     */
    public void setCurrentStatePointer(int pointer) {
        if (pointer < 0 || pointer >= addressBookStateList.size()) {
            throw new IllegalArgumentException("State pointer " + pointer + " is outside the history.");
        }
        currentStatePointer = pointer;
    }

    /**
     * Returns true if there is an undone change that {@link #redo()} can restore.
     */
    public boolean canRedo() {
        return currentStatePointer < addressBookStateList.size() - 1;
    }

    /**
     * Restores the most recently undone change.
     *
     * @throws NoRedoableStateException if there is no undone change to restore.
     */
    public void redo() {
        if (!canRedo()) {
            throw new NoRedoableStateException();
        }
        assert !hasUncommittedChanges() : "Uncommitted changes would be lost by redo";
        currentStatePointer++;
        resetData(addressBookStateList.get(currentStatePointer));
    }

    /**
     * Returns true if the data differs from the current state in the history.
     * The comparison is exact, so a difference in the capitalisation of instrument or group names
     * counts as a change.
     */
    private boolean hasUncommittedChanges() {
        return !hasIdenticalData(addressBookStateList.get(currentStatePointer));
    }

    /**
     * Exception thrown when an undo operation is attempted but no previous states exist.
     */
    public static class NoUndoableStateException extends RuntimeException {
        public NoUndoableStateException() {
            super("Current state pointer at start of addressBookState list, unable to undo.");
        }
    }

    /**
     * Thrown when a redo is attempted but there is no undone change to restore.
     */
    public static class NoRedoableStateException extends RuntimeException {
        public NoRedoableStateException() {
            super("Current state pointer at end of addressBookState list, unable to redo.");
        }
    }
}
