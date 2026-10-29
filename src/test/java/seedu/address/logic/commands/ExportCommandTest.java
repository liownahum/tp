package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.CsvExporter;
import seedu.address.storage.CsvImporter;
import seedu.address.storage.ExportField;

/**
 * Contains integration tests (interaction with the Model and storage) for {@code ExportCommand}.
 */
public class ExportCommandTest {
    @TempDir
    public Path testFolder;

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void constructor_emptyFields_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new ExportCommand(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new ExportCommand(testFolder, List.of()));
    }

    @Test
    public void constructor_repeatedFields_throwsIllegalArgumentException() {
        List<ExportField> adjacentDuplicates = List.of(ExportField.NAME, ExportField.NAME);
        List<ExportField> separatedDuplicates = List.of(ExportField.NAME, ExportField.PHONE, ExportField.NAME);

        assertThrows(IllegalArgumentException.class, () -> new ExportCommand(adjacentDuplicates));
        assertThrows(IllegalArgumentException.class, () -> new ExportCommand(separatedDuplicates));
        assertThrows(IllegalArgumentException.class, () -> new ExportCommand(testFolder, adjacentDuplicates));
        assertThrows(IllegalArgumentException.class, () -> new ExportCommand(testFolder, separatedDuplicates));
    }

    @Test
    public void execute_filteredList_exportsOnlyDisplayedPersons() throws Exception {
        model.updateFilteredPersonList(person -> person.equals(ALICE));
        Path outputPath = testFolder.resolve("current-results-01.csv");

        CommandResult result = new ExportCommand(testFolder).execute(model);

        assertEquals(String.format(ExportCommand.MESSAGE_EXPORT_SUCCESS_SINGULAR, 1, outputPath),
                result.getFeedbackToUser());
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        assertEquals(String.join(System.lineSeparator(),
                CsvExporter.HEADER,
                "\"Alice Pauline\",\"94351253\",\"alice@example.com\","
                        + "\"member\",\"student\",\"\",\"Violin:3\",\"\""),
                Files.readString(outputPath));
    }

    @Test
    public void execute_multiplePeople_usesPluralSuccessMessage() throws Exception {
        Path outputPath = testFolder.resolve("current-results-01.csv");
        int displayedPersonCount = model.getFilteredPersonList().size();

        CommandResult result = new ExportCommand(testFolder).execute(model);

        assertEquals(String.format(ExportCommand.MESSAGE_EXPORT_SUCCESS, displayedPersonCount, outputPath),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_afterFind_exportsSelectedFieldsForMatchingPersons() throws Exception {
        new AddressBookParser().parseCommand("find Meier").execute(model);

        new ExportCommand(testFolder, List.of(ExportField.PHONE, ExportField.NAME)).execute(model);

        assertEquals(String.join(System.lineSeparator(), "phone,name",
                "\"98765432\",\"Benson Meier\"", "\"87652533\",\"Daniel Meier\""),
                Files.readString(testFolder.resolve("current-results-01.csv")));
    }

    @Test
    public void execute_afterFilter_exportsSelectedFieldsForMatchingPersons() throws Exception {
        new AddressBookParser().parseCommand("filter s/member i/Violin").execute(model);

        new ExportCommand(testFolder, List.of(ExportField.NAME, ExportField.INSTRUMENTS)).execute(model);

        assertEquals(String.join(System.lineSeparator(), "name,instruments",
                "\"Alice Pauline\",\"Violin:3\"", "\"Daniel Meier\",\"Violin:20\""),
                Files.readString(testFolder.resolve("current-results-01.csv")));
    }

    @Test
    public void execute_afterFindAndSort_preservesDisplayedOrder() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        parser.parseCommand("find Meier").execute(model);
        parser.parseCommand("sort status").execute(model);

        new ExportCommand(testFolder, List.of(ExportField.NAME)).execute(model);

        assertEquals(String.join(System.lineSeparator(), "name", "\"Daniel Meier\"", "\"Benson Meier\""),
                Files.readString(testFolder.resolve("current-results-01.csv")));
    }

    @Test
    public void execute_emptyResults_writesHeadersAndReportsZeroPersons() throws Exception {
        new AddressBookParser().parseCommand("find Nobody").execute(model);
        Path outputPath = testFolder.resolve("current-results-01.csv");

        CommandResult result = new ExportCommand(testFolder,
                List.of(ExportField.GROUPS, ExportField.NAME)).execute(model);

        assertEquals("groups,name", Files.readString(outputPath));
        assertEquals(String.format(ExportCommand.MESSAGE_EXPORT_SUCCESS, 0, outputPath), result.getFeedbackToUser());
        new ExportCommand(testFolder).execute(model);
        assertEquals(CsvExporter.HEADER, Files.readString(testFolder.resolve("current-results-02.csv")));
    }

    @Test
    public void execute_defaultFields_retainsImportCompatibility() throws Exception {
        new ExportCommand(testFolder).execute(model);

        assertEquals(model.getFilteredPersonList(),
                CsvImporter.importPeople(testFolder.resolve("current-results-01.csv")));
    }

    @Test
    public void execute_preservesRecordsDisplayedResultsAndUndoRedoHistory() throws Exception {
        model.addPerson(HOON);
        model.commitAddressBook();
        model.addPerson(IDA);
        model.commitAddressBook();
        model.undoAddressBook();
        AddressBookParser parser = new AddressBookParser();
        parser.parseCommand("find Meier").execute(model);
        parser.parseCommand("sort status").execute(model);
        AddressBook originalRecords = new AddressBook(model.getAddressBook());
        List<Person> displayedResults = List.copyOf(model.getFilteredPersonList());

        for (ExportCommand command : List.of(new ExportCommand(testFolder),
                new ExportCommand(testFolder, List.of(ExportField.NAME)))) {
            command.execute(model);

            assertTrue(originalRecords.hasIdenticalData(model.getAddressBook()));
            assertEquals(displayedResults, model.getFilteredPersonList());
            assertEquals(1, model.getUndoableChangeCount());
            assertEquals(1, model.getRedoableChangeCount());
        }

        model.redoAddressBook();
        assertTrue(model.hasPerson(IDA));
        model.undoAddressBook();
        assertTrue(originalRecords.hasIdenticalData(model.getAddressBook()));
        model.undoAddressBook();
        assertTrue(getTypicalAddressBook().hasIdenticalData(model.getAddressBook()));
    }

    @Test
    public void execute_selectionListChangedAfterConstruction_keepsOriginalSelection() throws Exception {
        List<ExportField> fields = new ArrayList<>(List.of(ExportField.NAME));
        ExportCommand command = new ExportCommand(testFolder, fields);
        fields.add(ExportField.PHONE);
        model.updateFilteredPersonList(person -> person.equals(ALICE));

        command.execute(model);

        assertEquals(String.join(System.lineSeparator(), "name", "\"Alice Pauline\""),
                Files.readString(testFolder.resolve("current-results-01.csv")));
    }

    @Test
    public void execute_exportDirectoryIsFile_throwsCommandException() throws Exception {
        Path exportDirectory = testFolder.resolve("not-a-directory");
        Files.createFile(exportDirectory);

        CommandException exception = assertThrows(CommandException.class, () ->
            new ExportCommand(exportDirectory).execute(model));

        String expectedMessage = String.format(ExportCommand.MESSAGE_EXPORT_FAILURE, exportDirectory);
        assertEquals(expectedMessage, exception.getMessage());
        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    public void requiresSave_returnsFalse() {
        assertFalse(new ExportCommand(testFolder).requiresSave());
        assertFalse(new ExportCommand(testFolder, List.of(ExportField.NAME)).requiresSave());
    }
}
