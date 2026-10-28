package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

import seedu.address.MainApp;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.storage.CsvImporter;
import seedu.address.storage.CsvImporter.ImportedPerson;

/**
 * Imports people from a CSV file in the imports directory.
 */
public class ImportCommand extends Command {
    public static final String COMMAND_WORD = "import";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Imports people from a CSV file in the imports folder.\n"
            + "Parameters: FILE_NAME\n"
            + "Example: " + COMMAND_WORD + " members.csv";
    public static final String MESSAGE_SUCCESS = "Imported %d people from %s.";
    public static final String MESSAGE_SUCCESS_SINGULAR = "Imported %d person from %s.";
    public static final String MESSAGE_IMPORT_FAILURE = "Could not import %s: %s";
    public static final String MESSAGE_FILE_NOT_FOUND = "No file named %s in the imports folder.";
    public static final String MESSAGE_DUPLICATE_PERSON = "Import failed because \"%s\" at line %d already exists "
            + "in the address book or appears earlier in the CSV.";
    public static final String MESSAGE_INVALID_FILENAME = "Invalid filename. Use a valid Windows filename without "
            + "reserved characters, control characters, trailing periods, or reserved names such as CON or COM1.";

    private final String fileName;

    /**
     * Creates an ImportCommand to import the specified CSV file.
     *
     * @param fileName Name of the CSV file to import; must not be null.
     * @throws NullPointerException if {@code fileName} is null.
     */
    public ImportCommand(String fileName) {
        requireNonNull(fileName);
        this.fileName = fileName;
    }

    /**
     * Returns the file name of the CSV file to import.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Path importPath = MainApp.IMPORT_DIRECTORY.resolve(fileName);
        List<ImportedPerson> importedPeople;
        try {
            importedPeople = CsvImporter.importPeopleWithLineNumbers(importPath);
        } catch (NoSuchFileException exception) {
            throw new CommandException(String.format(MESSAGE_FILE_NOT_FOUND, fileName), exception);
        } catch (IOException | IllegalValueException exception) {
            throw new CommandException(String.format(MESSAGE_IMPORT_FAILURE, fileName, exception.getMessage()),
                    exception);
        }

        AddressBook updatedAddressBook = new AddressBook(model.getAddressBook());
        for (ImportedPerson importedPerson : importedPeople) {
            Person person = importedPerson.person();
            // CSV has no record IDs: reject identical recorded details, but allow shared names/phones.
            if (updatedAddressBook.getPersonList().contains(person)) {
                throw new CommandException(String.format(MESSAGE_DUPLICATE_PERSON,
                        person.getName(), importedPerson.lineNumber()));
            }
            updatedAddressBook.addPerson(person);
        }
        model.setAddressBook(updatedAddressBook);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        String successMessage = importedPeople.size() == 1 ? MESSAGE_SUCCESS_SINGULAR : MESSAGE_SUCCESS;
        return new CommandResult(String.format(successMessage, importedPeople.size(), importPath));
    }
}
