package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.SortKey;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests sorting together with filtering, list reset and index-based commands.
 */
public class SortCommandTest {
    private final Person zoe = new PersonBuilder().withName("Zoe").build();
    private final Person alice = new PersonBuilder().withName("alice").build();
    private final Person bob = new PersonBuilder().withName("Bob").build();
    private final AddressBook addressBook = new AddressBookBuilder()
            .withPerson(zoe).withPerson(alice).withPerson(bob).build();
    private final ModelManager model = new ModelManager(addressBook, new UserPrefs());

    @Test
    public void constructor_nullAttribute_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SortCommand(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SortCommand(SortKey.NAME).execute(null));
    }

    @Test
    public void execute_repeatedSort_preservesSortedAndStoredOrder() {
        SortCommand command = new SortCommand(SortKey.NAME);
        command.execute(model);
        assertEquals(String.format(SortCommand.MESSAGE_SUCCESS, "name"), command.execute(model).getFeedbackToUser());
        assertEquals(List.of(alice, bob, zoe), model.getFilteredPersonList());
        assertEquals(List.of(zoe, alice, bob), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_addAfterSort_restoresFullListInNameOrder() throws Exception {
        new AddressBookParser().parseCommand("find zoe").execute(model);
        new SortCommand(SortKey.NAME).execute(model);
        Person added = new PersonBuilder().withName("Aaron").build();
        new AddCommand(added).execute(model);
        assertEquals(List.of(added, alice, bob, zoe), model.getFilteredPersonList());
        assertEquals(List.of(zoe, alice, bob, added), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_fullList_sortsIgnoringCaseWithoutChangingStoredOrder() throws Exception {
        Command command = new AddressBookParser().parseCommand("sort NAME");
        assertEquals(String.format(SortCommand.MESSAGE_SUCCESS, "name"),
                command.execute(model).getFeedbackToUser());
        assertEquals(List.of(alice, bob, zoe), model.getFilteredPersonList());
        assertEquals(List.of(zoe, alice, bob), model.getAddressBook().getPersonList());
        assertFalse(command.requiresSave());
        assertFalse(model.equals(new ModelManager(addressBook, new UserPrefs())));
    }

    @Test
    public void execute_repeatedNameWhitespace_sortsAlphabeticallyWithoutChangingNames() {
        Person zebra = new PersonBuilder().withName("Amy  Zebra").build();
        Person adams = new PersonBuilder().withName("Amy Adams").build();
        Person brown = new PersonBuilder().withName("Amy Brown").build();
        Person zulu = new PersonBuilder().withName("Amy Zulu").build();
        AddressBook directory = new AddressBookBuilder()
                .withPerson(zebra).withPerson(adams).withPerson(brown).withPerson(zulu).build();
        ModelManager whitespaceModel = new ModelManager(directory, new UserPrefs());

        new SortCommand(SortKey.NAME).execute(whitespaceModel);

        assertEquals(List.of(adams, brown, zebra, zulu), whitespaceModel.getFilteredPersonList());
        assertEquals("Amy  Zebra", whitespaceModel.getFilteredPersonList().get(2).getName().fullName);
        assertEquals(List.of(zebra, adams, brown, zulu), whitespaceModel.getAddressBook().getPersonList());
    }

    @Test
    public void execute_equivalentNamesWithDifferentWhitespace_preservesDirectoryOrder() {
        Person first = new PersonBuilder().withName("Amy   Zebra").build();
        Person second = new PersonBuilder().withName("amy Zebra").build();
        Person third = new PersonBuilder().withName("AMY  Zebra").build();
        AddressBook directory = new AddressBookBuilder()
                .withPerson(first).withPerson(second).withPerson(third).build();
        ModelManager whitespaceModel = new ModelManager(directory, new UserPrefs());

        new SortCommand(SortKey.NAME).execute(whitespaceModel);
        new SortCommand(SortKey.NAME).execute(whitespaceModel);

        assertEquals(List.of(first, second, third), whitespaceModel.getFilteredPersonList());
        assertEquals(List.of(first, second, third), whitespaceModel.getAddressBook().getPersonList());
    }

    @Test
    public void execute_filteredList_preservesFilterAndSortingAcrossFind() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        parser.parseCommand("find zoe bob").execute(model);
        new SortCommand(SortKey.NAME).execute(model);
        assertEquals(List.of(bob, zoe), model.getFilteredPersonList());

        parser.parseCommand("find zoe alice").execute(model);
        assertEquals(List.of(alice, zoe), model.getFilteredPersonList());

        new ListCommand().execute(model);
        assertEquals(List.of(zoe, alice, bob), model.getFilteredPersonList());
        assertEquals(new ModelManager(addressBook, new UserPrefs()), model);
    }

    @Test
    public void execute_emptyResults_remainsEmpty() throws Exception {
        new AddressBookParser().parseCommand("find missing").execute(model);
        new SortCommand(SortKey.NAME).execute(model);
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void execute_deleteAfterSort_deletesFirstDisplayedPerson() throws Exception {
        new SortCommand(SortKey.NAME).execute(model);
        new DeleteCommand(INDEX_FIRST_PERSON).execute(model);
        assertEquals(List.of(bob, zoe), model.getFilteredPersonList());
        assertEquals(List.of(zoe, bob), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_editAfterSort_editsFirstDisplayedPersonAndUpdatesOrder() throws Exception {
        new SortCommand(SortKey.NAME).execute(model);
        EditCommand.EditPersonDescriptor descriptor = new EditCommand.EditPersonDescriptor();
        Person editedAlice = new PersonBuilder(alice).withName("Zelda").build();
        descriptor.setName(editedAlice.getName());
        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);
        assertEquals(List.of(bob, editedAlice, zoe), model.getFilteredPersonList());
        assertEquals(List.of(zoe, editedAlice, bob), model.getAddressBook().getPersonList());
    }
}
