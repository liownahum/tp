package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Redoes one or more of the most recently undone changes to the address book.
 */
public class RedoCommand extends Command {

    public static final String COMMAND_WORD = "redo";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Restores the most recently undone changes to the member directory.\n"
            + "Parameters: [COUNT] (number of changes to redo, a positive integer; default 1)\n"
            + "Example: " + COMMAND_WORD + " 2";

    public static final String MESSAGE_SUCCESS = "Redid the most recently undone change.";
    public static final String MESSAGE_SUCCESS_MULTIPLE = "Redid the %1$d most recently undone changes.";
    public static final String MESSAGE_FAILURE = "There is no undone change to redo.";
    public static final String MESSAGE_NOT_ENOUGH_CHANGES =
            "Not enough undone changes to redo: only %1$d can be redone.";

    private final int count;

    /**
     * Creates a RedoCommand that redoes the most recently undone change.
     */
    public RedoCommand() {
        this(1);
    }

    /**
     * Creates a RedoCommand that redoes the {@code count} most recently undone changes.
     */
    public RedoCommand(int count) {
        checkArgument(count > 0, "Count must be positive.");
        this.count = count;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        int redoableCount = model.getRedoableChangeCount();
        if (redoableCount == 0) {
            throw new CommandException(MESSAGE_FAILURE);
        }
        if (count > redoableCount) {
            throw new CommandException(String.format(MESSAGE_NOT_ENOUGH_CHANGES, redoableCount));
        }
        for (int i = 0; i < count; i++) {
            model.redoAddressBook();
        }
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(count == 1 ? MESSAGE_SUCCESS : String.format(MESSAGE_SUCCESS_MULTIPLE, count));
    }

    @Override
    public boolean recordsChange() {
        return false;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof RedoCommand)) {
            return false;
        }

        RedoCommand otherRedoCommand = (RedoCommand) other;
        return count == otherRedoCommand.count;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("count", count)
                .toString();
    }
}
