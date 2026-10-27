package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RedoCommand;

public class RedoCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, RedoCommand.MESSAGE_USAGE);

    private RedoCommandParser parser = new RedoCommandParser();

    @Test
    public void parse_noArgs_returnsSingleStep() {
        assertParseSuccess(parser, "", new RedoCommand());
        assertParseSuccess(parser, "   ", new RedoCommand(1));
    }

    @Test
    public void parse_validCount_returnsRedoCommand() {
        assertParseSuccess(parser, " 3", new RedoCommand(3));
        assertParseSuccess(parser, "  2  ", new RedoCommand(2));
    }

    @Test
    public void parse_invalidCount_throwsParseException() {
        assertParseFailure(parser, " 0", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " -1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " +2", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " two", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " 1.5", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " 2 3", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " 2 extra", MESSAGE_INVALID_FORMAT);
    }
}
