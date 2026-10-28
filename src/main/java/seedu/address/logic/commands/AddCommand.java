package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INSTRUMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBER_TYPE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;

import java.util.stream.Collectors;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Adds a person to the address book.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a person to the address book. "
            + "Parameters: "
            + PREFIX_NAME + "NAME "
            + PREFIX_PHONE + "PHONE "
            + PREFIX_EMAIL + "EMAIL "
            + PREFIX_STATUS + "STATUS "
            + "[" + PREFIX_MEMBER_TYPE + "TYPE] "
            + "[" + PREFIX_ROLE + "ROLE] "
            + "[" + PREFIX_INSTRUMENT + "INSTRUMENT:YEARS]... "
            + "[" + PREFIX_GROUP + "GROUP]...\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "John Doe "
            + PREFIX_PHONE + "98765432 "
            + PREFIX_EMAIL + "johnd@example.com "
            + PREFIX_STATUS + "member "
            + PREFIX_ROLE + "Section Leader "
            + PREFIX_INSTRUMENT + "Violin:5 "
            + PREFIX_INSTRUMENT + "Piano:2 "
            + PREFIX_GROUP + "Chamber Orchestra";

    public static final String MESSAGE_SUCCESS = "New person added: %1$s";
    public static final String MESSAGE_DUPLICATE_PERSON = "This person already exists in the address book.";
    public static final String MESSAGE_POSSIBLE_DUPLICATE =
            "Warning: possible duplicate (matching name or phone number): %s";

    private final Person toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public AddCommand(Person person) {
        requireNonNull(person);
        toAdd = person;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        // Guard against reusing this command for the same record; matching contact details remain allowed.
        if (model.hasPerson(toAdd)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        String warnings = model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getName().fullName.equalsIgnoreCase(toAdd.getName().fullName)
                        || person.getPhone().equals(toAdd.getPhone()))
                .map(person -> String.format(MESSAGE_POSSIBLE_DUPLICATE, Messages.format(person)))
                .collect(Collectors.joining("\n"));
        model.addPerson(toAdd);
        String success = String.format(MESSAGE_SUCCESS, Messages.format(toAdd));
        return new CommandResult(warnings.isEmpty() ? success : warnings + "\n" + success);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
