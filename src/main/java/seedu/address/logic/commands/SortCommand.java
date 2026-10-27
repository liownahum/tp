package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;
import seedu.address.model.person.SortKey;

/**
 * Sorts the displayed persons by the specified attribute.
 */
public class SortCommand extends Command {
    public static final String COMMAND_WORD = "sort";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Sorts the displayed members by name or status.\n"
            + "Name order: alphabetical, ignoring case and repeated whitespace.\n"
            + "Status order: member, active alumnus, inactive alumnus.\n"
            + "Parameters: name | status\n"
            + "Example: " + COMMAND_WORD + " name";

    public static final String MESSAGE_SUCCESS = "Sorted displayed members by %s.";

    private final SortKey sortKey;

    /**
     * Creates a SortCommand to sort by the specified attribute.
     */
    public SortCommand(SortKey sortKey) {
        this.sortKey = requireNonNull(sortKey);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateSortedPersonList(sortKey.getComparator());
        return new CommandResult(String.format(MESSAGE_SUCCESS, sortKey.getKeyword()));
    }

    @Override
    public boolean requiresSave() {
        return false;
    }
}
