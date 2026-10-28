package seedu.address.logic;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.person.Group;
import seedu.address.model.person.Instrument;
import seedu.address.model.person.Person;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX = "The person index provided is invalid.";
    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = "%1$d person(s) listed!";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";
    public static final String MESSAGE_UNKNOWN_PREFIX =
            "Unknown prefix %1$s. Prefixes are lowercase, and this command accepts only: %2$s";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Formats the {@code person} for display to the user.
     */
    public static String format(Person person) {
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName())
                .append("; Phone: ")
                .append(person.getPhone())
                .append("; Email: ")
                .append(person.getEmail())
                .append("; Status: ")
                .append(person.getStatus())
                .append("; Member type: ")
                .append(person.getMemberType());
        person.getRole().ifPresent(role -> builder.append("; Role: ").append(role));
        builder.append("; Instruments: ")
                .append(person.getInstruments().stream()
                        .sorted(Comparator.comparing(Instrument::getName, String.CASE_INSENSITIVE_ORDER))
                        .map(Instrument::toString)
                        .collect(Collectors.joining(", ")));
        if (!person.getGroups().isEmpty()) {
            builder.append("; Groups: ")
                    .append(person.getGroups().stream()
                            .sorted(Comparator.comparing(Group::getName, String.CASE_INSENSITIVE_ORDER))
                            .map(Group::toString)
                            .collect(Collectors.joining(", ")));
        }
        return builder.toString();
    }

}
