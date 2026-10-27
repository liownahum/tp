package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new RedoCommand object
 */
public class RedoCommandParser implements Parser<RedoCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the RedoCommand
     * and returns a RedoCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public RedoCommand parse(String args) throws ParseException {
        if (args.isBlank()) {
            return new RedoCommand();
        }
        try {
            return new RedoCommand(ParserUtil.parseCount(args));
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, RedoCommand.MESSAGE_USAGE), pe);
        }
    }

}
