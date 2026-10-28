package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.stage.Stage;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;
import seedu.address.ui.Ui;

public class MainAppTest {
    private static final String VALID_ADDRESS_BOOK_JSON = "{\"persons\":[{\"name\":\"Alice\","
            + "\"phone\":\"81234567\",\"email\":\"alice@example.com\",\"status\":\"member\","
            + "\"memberType\":\"student\",\"role\":null,\"instruments\":[],\"groups\":[]}]}";

    @TempDir
    public Path temporaryFolder;

    @Test
    public void initModelManager_legacyBlankGroups_loadsMembersAndAllowsSaving() throws Exception {
        Path addressBookPath = temporaryFolder.resolve("addressbook.json");
        StorageManager storageManager = createStorageManager(addressBookPath);
        AddressBook legacyAddressBook = new AddressBook();
        legacyAddressBook.addPerson(new PersonBuilder(ALICE).withGroups("Legacy Blank", "Training").build());
        legacyAddressBook.addPerson(new PersonBuilder(HOON).withGroups("Legacy Blank").build());
        storageManager.saveAddressBook(legacyAddressBook);
        String legacyJson = Files.readString(addressBookPath).replace("\"Legacy Blank\"", "\"\\u00a0\"");
        Files.writeString(addressBookPath, legacyJson);

        Model model = new TestMainApp().initializeModelManager(storageManager);

        Person expectedPerson = new PersonBuilder(ALICE).withGroups("Training").build();
        Person expectedUngroupedPerson = new PersonBuilder(HOON).withGroups().build();
        assertEquals(List.of(expectedPerson, expectedUngroupedPerson), model.getAddressBook().getPersonList());
        assertEquals(legacyJson, Files.readString(addressBookPath));

        new LogicManager(model, storageManager)
                .execute("add n/Recovery Check p/91234567 e/recovery@example.com s/member");
    }

    @Test
    public void initModelManager_unicodeInstrumentEntries_preservesMembersAndAllowsSaving() throws Exception {
        Path addressBookPath = temporaryFolder.resolve("addressbook.json");
        StorageManager storageManager = createStorageManager(addressBookPath);
        AddressBook savedAddressBook = new AddressBook();
        savedAddressBook.addPerson(new PersonBuilder(ALICE).withGroups("Training")
                .withInstruments("Blank Placeholder:3", "Bass Guitar:3", "Bass Placeholder:5").build());
        savedAddressBook.addPerson(HOON);
        storageManager.saveAddressBook(savedAddressBook);
        String savedJson = Files.readString(addressBookPath)
                .replace("\"Blank Placeholder\"", "\"\\u00a0\"")
                .replace("\"Bass Placeholder\"", "\"Bass\\u00a0Guitar\"");
        Files.writeString(addressBookPath, savedJson);

        Model model = new TestMainApp().initializeModelManager(storageManager);

        assertEquals(List.of(new PersonBuilder(ALICE).withGroups("Training")
                        .withInstruments("Bass Guitar:5").build(), HOON),
                model.getAddressBook().getPersonList());
        assertEquals(savedJson, Files.readString(addressBookPath));

        new LogicManager(model, storageManager)
                .execute("add n/Loading Check p/91234567 e/loading@example.com s/member");
        assertEquals(3, model.getAddressBook().getPersonList().size());
        assertEquals(model.getAddressBook(), storageManager.readAddressBook().orElseThrow());
        assertFalse(Files.readString(addressBookPath).contains("\\u00a0"));
    }

    @Test
    public void getImportDirectory_returnsSharedImportDirectory() {
        assertSame(MainApp.IMPORT_DIRECTORY, new MainApp().getImportDirectory());
    }

    @Test
    public void initModelManager_invalidAddressBook_recoversWithoutOverwritingData() throws Exception {
        Path addressBookPath = temporaryFolder.resolve("addressbook.json");
        String invalidJson = "not valid JSON";
        Files.writeString(addressBookPath, invalidJson);
        StorageManager storageManager = createStorageManager(addressBookPath);

        TestMainApp app = new TestMainApp();
        Model model = app.initializeModelManager(storageManager);

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertEquals(invalidJson, Files.readString(addressBookPath.resolveSibling("addressbook.json.bak-01")));
        assertThrows(IOException.class, () -> storageManager.saveAddressBook(new AddressBook()));
        assertEquals(invalidJson, Files.readString(addressBookPath));

        RecordingUi ui = new RecordingUi();
        app.ui = ui;
        app.start(null);

        assertTrue(ui.wasStarted);
        assertTrue(ui.startupMessage.contains(addressBookPath.toAbsolutePath().toString()));
        assertTrue(ui.startupMessage.contains(addressBookPath.resolveSibling("addressbook.json.bak-01")
                .toAbsolutePath().toString()));
        assertTrue(ui.startupMessage.contains("empty recovery directory"));
        assertTrue(ui.startupMessage.contains("Changes cannot be saved"));
    }

    @Test
    public void recoveryMode_addFailsWithoutChangingRecoveredModel() throws Exception {
        Path addressBookPath = temporaryFolder.resolve("addressbook.json");
        String invalidJson = "not valid JSON";
        Files.writeString(addressBookPath, invalidJson);
        StorageManager storageManager = createStorageManager(addressBookPath);

        Model model = new TestMainApp().initializeModelManager(storageManager);
        LogicManager logic = new LogicManager(model, storageManager);

        CommandException exception = assertThrows(CommandException.class, () ->
            logic.execute("add n/Test User p/91234567 e/test@example.com s/member"));

        String expectedMessage = String.format(LogicManager.FILE_OPS_ERROR_FORMAT,
                "Address book saving is disabled after a failed data load.");
        assertEquals(expectedMessage, exception.getMessage());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertEquals(invalidJson, Files.readString(addressBookPath));
        assertEquals(invalidJson, Files.readString(addressBookPath.resolveSibling("addressbook.json.bak-01")));
    }

    @Test
    public void initModelManager_secondFailedLoad_preservesFirstBackup() throws Exception {
        Path addressBookPath = temporaryFolder.resolve("addressbook.json");
        Path firstBackup = temporaryFolder.resolve("addressbook.json.bak-01");
        Path secondBackup = temporaryFolder.resolve("addressbook.json.bak-02");
        TestMainApp app = new TestMainApp();

        // First failed startup
        Files.writeString(addressBookPath, "first invalid data");
        app.initializeModelManager(createStorageManager(addressBookPath));

        // The user attempts a repair, but the file is still invalid.
        Files.writeString(addressBookPath, "second invalid data");
        app.initializeModelManager(createStorageManager(addressBookPath));

        assertEquals("first invalid data", Files.readString(firstBackup));
        assertEquals("second invalid data", Files.readString(secondBackup));
        assertEquals("second invalid data", Files.readString(addressBookPath));
    }

    @Test
    public void initModelManager_backupFails_recoversWithSavingDisabled() {
        Path addressBookPath = temporaryFolder.resolve("addressbook.json");
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(addressBookPath) {
            @Override
            public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
                throw new DataLoadingException(new IOException("invalid address book"));
            }

            @Override
            public Path backupAddressBook() throws IOException {
                throw new IOException("backup failed");
            }
        };
        StorageManager storageManager = new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));

        TestMainApp app = new TestMainApp();
        Model model = app.initializeModelManager(storageManager);

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        IOException exception = assertThrows(IOException.class, () ->
                storageManager.saveAddressBook(new AddressBook()));
        assertEquals("Address book saving is disabled after a failed data load.", exception.getMessage());

        RecordingUi ui = new RecordingUi();
        app.ui = ui;
        app.start(null);

        assertTrue(ui.startupMessage.contains("A backup could not be created: backup failed"));
        assertTrue(ui.startupMessage.contains("Changes cannot be saved"));
    }

    @Test
    public void initModelManager_trailingGarbage_recoversWithoutOverwritingData() throws Exception {
        assertTrailingContentTriggersRecovery("trailing-garbage", "\nGARBAGE");
    }

    @Test
    public void initModelManager_secondJsonRoot_recoversWithoutOverwritingData() throws Exception {
        assertTrailingContentTriggersRecovery("second-root", "\n{\"persons\":[]}");
    }

    @Test
    public void init_usesStorageManagerToInitializeModel() throws Exception {
        Path importDirectory = temporaryFolder.resolve("imports");
        TestMainApp app = new TestMainApp(importDirectory);

        assertFalse(Files.exists(importDirectory));

        app.init();

        assertTrue(Files.isDirectory(importDirectory));
        assertInstanceOf(StorageManager.class, app.storage);
        assertSame(app.storage, app.storageProvidedToModelInitialization);
        assertSame(app.modelToInitialize, app.model);
    }

    private StorageManager createStorageManager(Path addressBookPath) {
        return new StorageManager(new JsonAddressBookStorage(addressBookPath),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
    }

    private void assertTrailingContentTriggersRecovery(String folderName, String trailingContent) throws Exception {
        Path testFolder = Files.createDirectory(temporaryFolder.resolve(folderName));
        Path addressBookPath = testFolder.resolve("addressbook.json");
        String invalidJson = VALID_ADDRESS_BOOK_JSON + trailingContent;
        Files.writeString(addressBookPath, invalidJson);
        StorageManager storageManager = createStorageManager(addressBookPath);

        Model model = new TestMainApp().initializeModelManager(storageManager);

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertEquals(invalidJson, Files.readString(testFolder.resolve("addressbook.json.bak-01")));
        assertThrows(IOException.class, () -> storageManager.saveAddressBook(new AddressBook()));
        assertEquals(invalidJson, Files.readString(addressBookPath));
    }

    private static class RecordingUi implements Ui {
        private boolean wasStarted;
        private String startupMessage;

        @Override
        public void start(Stage primaryStage) {
            wasStarted = true;
        }

        @Override
        public void showStartupMessage(String message) {
            startupMessage = message;
        }
    }

    private static class TestMainApp extends MainApp {
        private final Model modelToInitialize = new ModelManager();
        private final Path importDirectory;
        private StorageManager storageProvidedToModelInitialization;

        TestMainApp() {
            this(Path.of("imports"));
        }

        TestMainApp(Path importDirectory) {
            this.importDirectory = importDirectory;
        }

        @Override
        protected Path getImportDirectory() {
            return importDirectory;
        }

        @Override
        protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
            return new UserPrefs();
        }

        @Override
        protected Model initModelManager(StorageManager storage, ReadOnlyUserPrefs userPrefs) {
            storageProvidedToModelInitialization = storage;
            return modelToInitialize;
        }

        private Model initializeModelManager(StorageManager storage) {
            return super.initModelManager(storage, new UserPrefs());
        }
    }
}
