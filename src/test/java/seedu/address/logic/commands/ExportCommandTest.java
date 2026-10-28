package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.CsvExporter;

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
    }
}
