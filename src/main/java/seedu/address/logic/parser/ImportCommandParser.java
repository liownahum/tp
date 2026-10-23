package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.Locale;

import seedu.address.logic.commands.ImportCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates an {@link ImportCommand}.
 */
public class ImportCommandParser implements Parser<ImportCommand> {

    /**
     * Parses an import file name and returns an {@code ImportCommand}.
     *
     * @throws ParseException if the arguments do not conform to the expected format.
     */
    @Override
    public ImportCommand parse(String args) throws ParseException {
        String fileName = args.trim();
        if (fileName.isEmpty() || fileName.contains("/") || fileName.contains("\\")
                || !fileName.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ImportCommand.MESSAGE_USAGE));
        }
        return new ImportCommand(fileName);
    }
}
