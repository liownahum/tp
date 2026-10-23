package seedu.address.logic.commands;

import static java.nio.charset.StandardCharsets.UTF_8;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model and CSV files) for {@code ImportCommand}.
 */
public class ImportCommandIntegrationTest {

    private static final Path IMPORT_DIRECTORY = Path.of("imports");
    private static final String FILE_NAME = "import-command-integration-test.csv";
    private static final Path IMPORT_FILE = IMPORT_DIRECTORY.resolve(FILE_NAME);
    private static final String HEADER = "name,phone,email,status,memberType,role,instruments,groups";

    private Model model;

    @BeforeEach
    public void setUp() throws IOException {
        Files.createDirectories(IMPORT_DIRECTORY);
        Files.deleteIfExists(IMPORT_FILE);
        model = new ModelManager(new AddressBook(), new UserPrefs());
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(IMPORT_FILE);
    }

    @Test
    public void execute_validPeople_success() throws IOException {
        Person existingPerson = new PersonBuilder().withName("Existing Person").withPhone("81111111")
                .withEmail("existing@example.com").build();
        Person personWithOptionalFields = new PersonBuilder().withName("Alice Tan").withPhone("91234567")
                .withEmail("alice@example.com").withStatus("member").withMemberType("student")
                .withRole("Section Leader").withInstruments("Violin:8", "Piano:5")
                .withGroups("Orchestra", "Leadership Team").build();
        Person personWithoutOptionalFields = new PersonBuilder().withName("Ben Lee").withPhone("92345678")
                .withEmail("ben@example.com").withStatus("active-alumnus").withMemberType("teacher")
                .withInstruments("Cello:12").build();
        model.addPerson(existingPerson);
        writeImportFile(
                HEADER,
                "\"Alice Tan\",\"91234567\",\"alice@example.com\",\"member\",\"student\","
                        + "\"Section Leader\",\"Violin:8;Piano:5\",\"Orchestra;Leadership Team\"",
                "\"Ben Lee\",\"92345678\",\"ben@example.com\",\"active-alumnus\",\"teacher\","
                        + "\"\",\"Cello:12\",\"\"");

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(personWithOptionalFields);
        expectedModel.addPerson(personWithoutOptionalFields);

        assertCommandSuccess(new ImportCommand(FILE_NAME), model,
                String.format(ImportCommand.MESSAGE_SUCCESS, 2, IMPORT_FILE), expectedModel);
    }

    @Test
    public void execute_rowWithWrongColumnCount_failureIsAtomic() throws IOException {
        writeImportFile(HEADER, "Alice Tan,91234567,alice@example.com,member,student,,Violin:8");

        assertCommandFailure(new ImportCommand(FILE_NAME), model,
                String.format(ImportCommand.MESSAGE_IMPORT_FAILURE, FILE_NAME, "Expected 8 columns at line 2."));
    }

    @Test
    public void execute_invalidStatus_failureIsAtomic() throws IOException {
        writeImportFile(HEADER, "Alice Tan,91234567,alice@example.com,alumnus,student,,Violin:8,");

        assertCommandFailure(new ImportCommand(FILE_NAME), model, String.format(ImportCommand.MESSAGE_IMPORT_FAILURE,
                FILE_NAME, "Invalid value at line 2: Status should be member, active-alumnus, or inactive-alumnus"));
    }

    @Test
    public void execute_invalidMemberType_failureIsAtomic() throws IOException {
        writeImportFile(HEADER, "Alice Tan,91234567,alice@example.com,member,parent,,Violin:8,");

        assertCommandFailure(new ImportCommand(FILE_NAME), model, String.format(ImportCommand.MESSAGE_IMPORT_FAILURE,
                FILE_NAME, "Invalid value at line 2: Member type should be student or teacher"));
    }

    @Test
    public void execute_duplicateExistingPerson_failureIsAtomic() throws IOException {
        Person existingPerson = new PersonBuilder().withName("Existing Person").withPhone("81111111")
                .withEmail("existing@example.com").build();
        model.addPerson(existingPerson);
        writeImportFile(HEADER, "Existing Person,81111111,existing@example.com,member,student,,,");

        assertCommandFailure(new ImportCommand(FILE_NAME), model, ImportCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_duplicatePeopleInCsv_failureIsAtomic() throws IOException {
        writeImportFile(
                HEADER,
                "Alice Tan,91234567,alice@example.com,member,student,,,",
                "Alice Tan,91234567,alice@example.com,member,student,,,");

        assertCommandFailure(new ImportCommand(FILE_NAME), model, ImportCommand.MESSAGE_DUPLICATE_PERSON);
    }

    private void writeImportFile(String... lines) throws IOException {
        Files.write(IMPORT_FILE, String.join(System.lineSeparator(), lines).getBytes(UTF_8));
    }
}
