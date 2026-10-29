package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.storage.ExportField;

/**
 * Parses input arguments and creates a new {@code ExportCommand} object.
 */
public class ExportCommandParser implements Parser<ExportCommand> {
    public static final String MESSAGE_EMPTY_FIELD = "Export field cannot be empty.";
    public static final String MESSAGE_UNSUPPORTED_FIELD = "Unsupported export field: %s.";
    public static final String MESSAGE_REPEATED_FIELD = "Export field selected more than once: %s.";

    private static final Prefix PREFIX_FIELD = new Prefix("f/");

    /**
     * Parses the given {@code String} of arguments in the context of the {@code ExportCommand}.
     *
     * @throws ParseException if the arguments contain invalid, empty or repeated field selections.
     */
    @Override
    public ExportCommand parse(String args) throws ParseException {
        if (args.trim().isEmpty()) {
            return new ExportCommand();
        }

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(" " + args.trim(), PREFIX_FIELD);
        List<String> fieldNames = argMultimap.getAllValues(PREFIX_FIELD);
        if (!argMultimap.getPreamble().isEmpty() || fieldNames.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExportCommand.MESSAGE_USAGE));
        }

        List<ExportField> fields = new ArrayList<>();
        for (String fieldName : fieldNames) {
            if (fieldName.isEmpty()) {
                throw new ParseException(MESSAGE_EMPTY_FIELD + "\n" + ExportCommand.MESSAGE_USAGE);
            }
            ExportField field = Arrays.stream(ExportField.values())
                    .filter(candidate -> candidate.getHeader().equals(fieldName))
                    .findFirst()
                    .orElseThrow(() -> new ParseException(String.format(MESSAGE_UNSUPPORTED_FIELD, fieldName)
                            + "\n" + ExportCommand.MESSAGE_USAGE));
            if (fields.contains(field)) {
                throw new ParseException(String.format(MESSAGE_REPEATED_FIELD, fieldName)
                        + "\n" + ExportCommand.MESSAGE_USAGE);
            }
            fields.add(field);
        }
        return new ExportCommand(fields);
    }
}
