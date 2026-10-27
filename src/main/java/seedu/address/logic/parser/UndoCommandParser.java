package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new UndoCommand object
 */
public class UndoCommandParser implements Parser<UndoCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the UndoCommand
     * and returns a UndoCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public UndoCommand parse(String args) throws ParseException {
        if (args.isBlank()) {
            return new UndoCommand();
        }
        try {
            return new UndoCommand(ParserUtil.parseCount(args));
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, UndoCommand.MESSAGE_USAGE), pe);
        }
    }

}
