package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class MainAppTest {
    @TempDir
    public Path temporaryFolder;

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
    public void initModelManager_backupFails_throwsIllegalStateException() {
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

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                new TestMainApp().initializeModelManager(storageManager));

        assertInstanceOf(IOException.class, exception.getCause());
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
