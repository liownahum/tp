package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_nullRoot_throwsDataLoadingException() throws IOException {
        Path filePath = testFolder.resolve("nullRootAddressBook.json");
        Files.writeString(filePath, "null");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        assertThrows(DataLoadingException.class, storage::readAddressBook);
    }

    @Test
    public void readAddressBook_nullPerson_throwsDataLoadingException() throws IOException {
        Path filePath = testFolder.resolve("nullPersonAddressBook.json");
        Files.writeString(filePath, "{\"persons\":[null]}");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        assertThrows(DataLoadingException.class, storage::readAddressBook);
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void backupAddressBook_noExistingBackup_createsFirstBackup() throws Exception {
        Path filePath = testFolder.resolve("addressbook.json");
        String invalidData = "invalid data";
        Files.writeString(filePath, invalidData);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        Path backupPath = storage.backupAddressBook();

        assertEquals(testFolder.resolve("addressbook.json.bak-01"), backupPath);
        assertEquals(invalidData, Files.readString(backupPath));
        assertEquals(invalidData, Files.readString(filePath));
    }

    @Test
    public void backupAddressBook_unchangedFile_reusesBackup() throws Exception {
        Path filePath = testFolder.resolve("addressbook.json");
        Files.writeString(filePath, "invalid data");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        Path firstBackup = storage.backupAddressBook();
        Path secondBackup = storage.backupAddressBook();

        assertEquals(firstBackup, secondBackup);
        assertFalse(Files.exists(testFolder.resolve("addressbook.json.bak-02")));
    }

    @Test
    public void backupAddressBook_changedFile_preservesEarlierBackup() throws Exception {
        Path filePath = testFolder.resolve("addressbook.json");
        Files.writeString(filePath, "first invalid data");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        Path firstBackup = storage.backupAddressBook();

        Files.writeString(filePath, "second invalid data");
        Path secondBackup = storage.backupAddressBook();

        assertEquals(testFolder.resolve("addressbook.json.bak-02"), secondBackup);
        assertEquals("first invalid data", Files.readString(firstBackup));
        assertEquals("second invalid data", Files.readString(secondBackup));
    }

    @Test
    public void backupAddressBook_numberingGap_usesFirstAvailableNumber() throws Exception {
        Path filePath = testFolder.resolve("addressbook.json");
        Files.writeString(filePath, "current invalid data");
        Files.writeString(testFolder.resolve("addressbook.json.bak-01"), "older data");
        Files.writeString(testFolder.resolve("addressbook.json.bak-03"), "older data");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        Path backupPath = storage.backupAddressBook();

        assertEquals(testFolder.resolve("addressbook.json.bak-02"), backupPath);
        assertEquals("current invalid data", Files.readString(backupPath));
    }

    @Test
    public void backupAddressBook_backupNameIsDirectory_skipsDirectory() throws Exception {
        Path filePath = testFolder.resolve("addressbook.json");
        Files.writeString(filePath, "invalid data");
        Files.createDirectory(testFolder.resolve("addressbook.json.bak-01"));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        Path backupPath = storage.backupAddressBook();

        assertEquals(testFolder.resolve("addressbook.json.bak-02"), backupPath);
        assertEquals("invalid data", Files.readString(backupPath));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void readAndSaveAddressBook_personWithAllMvpFields_success() throws Exception {
        Path filePath = testFolder.resolve("AllFieldsAddressBook.json");
        Person personWithAllMvpFields = new PersonBuilder()
                .withName("Fiona Lim")
                .withPhone("91234567")
                .withEmail("fiona@example.com")
                .withStatus("active-alumnus")
                .withMemberType("teacher")
                .withRole("Alumni Advisor")
                .withInstruments("Cello:11", "Piano:7")
                .build();
        AddressBook original = new AddressBook();
        original.addPerson(personWithAllMvpFields);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        storage.saveAddressBook(original, filePath);

        ReadOnlyAddressBook readBack = storage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));
    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
