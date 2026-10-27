package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        Command command = addressBookParser.parseCommand(commandText);
        AddressBook addressBookBeforeCommand = new AddressBook(model.getAddressBook());
        Predicate<Person> filterBeforeCommand = model.getFilteredPersonListPredicate();
        int historyPositionBeforeCommand = model.getAddressBookHistoryPosition();
        CommandResult commandResult = command.execute(model);

        if (!command.requiresSave()) {
            return commandResult;
        }

        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (AccessDeniedException e) {
            rollBack(addressBookBeforeCommand, filterBeforeCommand, historyPositionBeforeCommand);
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            rollBack(addressBookBeforeCommand, filterBeforeCommand, historyPositionBeforeCommand);
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }

        // Only changes that were saved successfully become undoable states
        if (command.recordsChange()) {
            model.commitAddressBook();
        }
        return commandResult;
    }

    /**
     * Restores the model to how it was before a command whose changes could not be saved.
     * The undo history is restored too, so that a failed {@code undo} or {@code redo} leaves it unchanged.
     */
    private void rollBack(AddressBook addressBook, Predicate<Person> filter, int historyPosition) {
        model.setAddressBook(addressBook);
        model.updateFilteredPersonList(filter);
        model.restoreAddressBookHistoryPosition(historyPosition);
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
