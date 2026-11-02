package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * A class to access AddressBook data stored as a JSON file on the hard disk.
 */
public class JsonAddressBookStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonAddressBookStorage.class);

    private Path filePath;

    public JsonAddressBookStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getAddressBookFilePath() {
        return filePath;
    }

    /**
     * Copies the address book file to a backup in the same directory.
     * Existing backups are not overwritten.
     *
     * @return the path of the backup file.
     * @throws IOException if the address book file cannot be backed up.
     */
    public Path backupAddressBook() throws IOException {
        if (!Files.isRegularFile(filePath)) {
            throw new IOException("The address book path is not a regular file.");
        }
        Optional<Path> newestBackup = findMostRecentlyCreatedBackup();

        // Reuse an identical backup to avoid duplicates on repeated failed startups.
        if (newestBackup.isPresent() && Files.mismatch(filePath, newestBackup.get()) == -1) {
            return newestBackup.get();
        }

        int backupNumber = 1;

        while (true) {
            String suffix = String.format(".bak-%02d", backupNumber);
            Path backupPath = filePath.resolveSibling(filePath.getFileName() + suffix);

            if (Files.exists(backupPath)) {
                backupNumber++;
                continue;
            }

            try {
                Files.copy(filePath, backupPath);
                return backupPath;
            } catch (FileAlreadyExistsException e) {
                backupNumber++;
            }
        }
    }

    /**
     * Returns the numbered backup with the latest creation time, if one exists.
     */
    private Optional<Path> findMostRecentlyCreatedBackup() throws IOException {
        Path directory = filePath.toAbsolutePath().getParent();
        String backupNamePattern =
                Pattern.quote(filePath.getFileName().toString()) + "\\.bak-[0-9]+";

        Path newestBackup = null;
        FileTime newestCreationTime = null;

        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory)) {
            for (Path candidate : files) {
                if (!candidate.getFileName().toString().matches(backupNamePattern)) {
                    continue;
                }

                BasicFileAttributes attributes = Files.readAttributes(candidate, BasicFileAttributes.class);
                if (!attributes.isRegularFile()) {
                    continue;
                }

                FileTime creationTime = attributes.creationTime();
                if (newestBackup == null || creationTime.compareTo(newestCreationTime) > 0) {
                    newestBackup = candidate;
                    newestCreationTime = creationTime;
                }
            }
        }

        return Optional.ofNullable(newestBackup);
    }

    /**
     * Returns AddressBook data as a {@link ReadOnlyAddressBook}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        return readAddressBook(filePath);
    }

    /**
     * Similar to {@link #readAddressBook()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);

        Optional<JsonSerializableAddressBook> jsonAddressBook = JsonUtil.readJsonFile(
                filePath, JsonSerializableAddressBook.class);
        if (!jsonAddressBook.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonAddressBook.get().toModelType());
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
        }
    }

    /**
     * Saves the given {@link ReadOnlyAddressBook} to the storage.
     * @param addressBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        saveAddressBook(addressBook, filePath);
    }

    /**
     * Similar to {@link #saveAddressBook(ReadOnlyAddressBook)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(filePath);

        FileUtil.createIfMissing(filePath);
        JsonUtil.saveJsonFile(new JsonSerializableAddressBook(addressBook), filePath);
    }

}
