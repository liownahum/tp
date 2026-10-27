package seedu.address.model;

import java.util.Comparator;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.Person;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    boolean hasPerson(Person person);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    void setPerson(Person target, Person editedPerson);

    /** Returns an unmodifiable view of the filtered person list in its current display order. */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);

    /**
     * Updates the ordering of the displayed person list.
     * A null comparator restores the source order.
     */
    void updateSortedPersonList(Comparator<Person> comparator);

    /** Returns the predicate currently used to filter the person list. */
    Predicate<Person> getFilteredPersonListPredicate();

    /**
     * Returns true if there is a change that {@link #undoAddressBook()} can revert.
     */
    boolean canUndoAddressBook();

    /**
     * Reverts the most recent change to the address book.
     */
    void undoAddressBook();

    /**
     * Returns the number of changes that {@link #undoAddressBook()} can revert one after another.
     */
    int getUndoableChangeCount();

    /**
     * Returns the number of undone changes that {@link #redoAddressBook()} can restore one after another.
     */
    int getRedoableChangeCount();

    /**
     * Returns true if there is an undone change that {@link #redoAddressBook()} can restore.
     */
    boolean canRedoAddressBook();

    /**
     * Restores the most recently undone change to the address book.
     */
    void redoAddressBook();

    /**
     * Records the current address book state so that the change leading to it can be undone.
     * Undone changes can no longer be redone afterwards, even if the address book is unchanged.
     */
    void commitAddressBook();

    /**
     * Returns the position of the current state in the address book's undo history.
     */
    int getAddressBookHistoryPosition();

    /**
     * Moves the undo history back to {@code position}, as returned by {@link #getAddressBookHistoryPosition()}.
     * The address book data is not changed.
     */
    void restoreAddressBookHistoryPosition(int position);
}
