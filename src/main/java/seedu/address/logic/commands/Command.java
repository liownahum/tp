package seedu.address.logic.commands;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Represents a command with hidden internal logic and the ability to be executed.
 */
public abstract class Command {

    /**
     * Executes the command and returns the result message.
     *
     * @param model {@code Model} which the command should operate on.
     * @return feedback message of the operation result for display
     * @throws CommandException If an error occurs during command execution.
     */
    public abstract CommandResult execute(Model model) throws CommandException;

    /**
     * Returns whether this command requires the address book to be saved.
     * Commands that do not modify stored records should override this to return false.
     */
    public boolean requiresSave() {
        return true;
    }

    /**
     * Returns whether a successful execution of this command is recorded as a change in the undo history.
     * Recording a change discards any undone changes, even if the data is unchanged.
     * Commands that only move through the undo history should override this to return false.
     */
    public boolean recordsChange() {
        return true;
    }
}
