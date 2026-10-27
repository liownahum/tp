package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final VersionedAddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final SortedList<Person> sortedPersons;
    private final Map<Person, Integer> sourcePositions = new HashMap<>();

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new VersionedAddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        updateSourcePositions();
        // Refresh positions before the filtered/sorted views process additions, edits or resets.
        this.addressBook.getPersonList().addListener((ListChangeListener<Person>) change -> updateSourcePositions());
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
        sortedPersons = new SortedList<>(filteredPersons);
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return addressBook.hasPerson(person);
    }

    @Override
    public void deletePerson(Person target) {
        addressBook.removePerson(target);
    }

    @Override
    public void addPerson(Person person) {
        addressBook.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        addressBook.setPerson(target, editedPerson);
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code addressBook}, in its current filtered and sorted display order.
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return sortedPersons;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
    }

    //=========== Undo/Redo ================================================================================

    @Override
    public boolean canUndoAddressBook() {
        return addressBook.canUndo();
    }

    @Override
    public void undoAddressBook() {
        addressBook.undo();
    }

    @Override
    public int getUndoableChangeCount() {
        return addressBook.getUndoableCount();
    }

    @Override
    public int getRedoableChangeCount() {
        return addressBook.getRedoableCount();
    }

    @Override
    public boolean canRedoAddressBook() {
        return addressBook.canRedo();
    }

    @Override
    public void redoAddressBook() {
        addressBook.redo();
    }

    @Override
    public void commitAddressBook() {
        addressBook.commit();
    }

    @Override
    public int getAddressBookHistoryPosition() {
        return addressBook.getCurrentStatePointer();
    }

    @Override
    public void restoreAddressBookHistoryPosition(int position) {
        addressBook.setCurrentStatePointer(position);
    }

    @Override
    public void updateSortedPersonList(Comparator<Person> comparator) {
        sortedPersons.setComparator(comparator == null ? null : comparator.thenComparingInt(sourcePositions::get));
    }

    /**
     * Caches directory positions once per source change for constant-time tie comparisons.
     */
    private void updateSourcePositions() {
        sourcePositions.clear();
        ObservableList<Person> persons = addressBook.getPersonList();
        for (int i = 0; i < persons.size(); i++) {
            sourcePositions.put(persons.get(i), i);
        }
    }

    @Override
    public Predicate<Person> getFilteredPersonListPredicate() {
        Predicate<? super Person> predicate = filteredPersons.getPredicate();
        return predicate == null ? PREDICATE_SHOW_ALL_PERSONS : predicate::test;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredPersons.equals(otherModelManager.filteredPersons)
                && sortedPersons.equals(otherModelManager.sortedPersons);
    }

}
