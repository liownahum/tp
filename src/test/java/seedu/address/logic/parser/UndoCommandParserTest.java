package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.UndoCommand;

public class UndoCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, UndoCommand.MESSAGE_USAGE);

    private UndoCommandParser parser = new UndoCommandParser();

    @Test
    public void parse_noArgs_returnsSingleStep() {
        assertParseSuccess(parser, "", new UndoCommand());
        assertParseSuccess(parser, "   ", new UndoCommand(1));
    }

    @Test
    public void parse_validCount_returnsUndoCommand() {
        assertParseSuccess(parser, " 3", new UndoCommand(3));
        assertParseSuccess(parser, "  2  ", new UndoCommand(2));
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
