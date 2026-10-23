package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.storage.CsvImporter;

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
    public static final String MESSAGE_DUPLICATE_PERSON =
            "Import failed because a person already exists in the address book or is repeated in the CSV.";

    private static final Path IMPORT_DIRECTORY = Path.of("imports");

    private final String fileName;

    public ImportCommand(String fileName) {
        this.fileName = requireNonNull(fileName);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Path importPath = IMPORT_DIRECTORY.resolve(fileName);
        List<Person> importedPeople;
        try {
            importedPeople = CsvImporter.importPeople(importPath);
        } catch (NoSuchFileException exception) {
            throw new CommandException(String.format(MESSAGE_FILE_NOT_FOUND, fileName), exception);
        } catch (IOException | IllegalValueException exception) {
            throw new CommandException(String.format(MESSAGE_IMPORT_FAILURE, fileName, exception.getMessage()),
                    exception);
        }

        try {
            AddressBook updatedAddressBook = new AddressBook(model.getAddressBook());
            for (Person person : importedPeople) {
                updatedAddressBook.addPerson(person);
            }
            model.setAddressBook(updatedAddressBook);
        } catch (DuplicatePersonException exception) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON, exception);
        }

        String successMessage = importedPeople.size() == 1 ? MESSAGE_SUCCESS_SINGULAR : MESSAGE_SUCCESS;
        return new CommandResult(String.format(successMessage, importedPeople.size(), importPath));
    }
}
