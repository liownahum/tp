package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Command to undo one or more of the most recent changes in the address book.
 */
public class UndoCommand extends Command {

    public static final String COMMAND_WORD = "undo";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Reverts the most recent changes to the member directory.\n"
            + "Parameters: [COUNT] (number of changes to undo, a positive integer; default 1)\n"
            + "Example: " + COMMAND_WORD + " 2";

    public static final String MESSAGE_SUCCESS = "Undid the most recent change.";
    public static final String MESSAGE_SUCCESS_MULTIPLE = "Undid the %1$d most recent changes.";
    public static final String MESSAGE_FAILURE = "There is no change to undo.";
    public static final String MESSAGE_NOT_ENOUGH_CHANGES = "Not enough changes to undo: only %1$d can be undone.";

    private final int count;

    /**
     * Creates an UndoCommand that undoes the most recent change.
     */
    public UndoCommand() {
        this(1);
    }

    /**
     * Creates an UndoCommand that undoes the {@code count} most recent changes.
     */
    public UndoCommand(int count) {
        checkArgument(count > 0, "Count must be positive.");
        this.count = count;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        int undoableCount = model.getUndoableChangeCount();
        if (undoableCount == 0) {
            throw new CommandException(MESSAGE_FAILURE);
        }
        if (count > undoableCount) {
            throw new CommandException(String.format(MESSAGE_NOT_ENOUGH_CHANGES, undoableCount));
        }
        for (int i = 0; i < count; i++) {
            model.undoAddressBook();
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
        if (!(other instanceof UndoCommand)) {
            return false;
        }

        UndoCommand otherUndoCommand = (UndoCommand) other;
        return count == otherUndoCommand.count;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("count", count)
                .toString();
    }
}
