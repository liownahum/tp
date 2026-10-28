package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ExportCommand;

public class ExportCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExportCommand.MESSAGE_USAGE);

    private final ExportCommandParser parser = new ExportCommandParser();

    @Test
    public void parse_noArguments_returnsExportCommand() throws Exception {
        assertTrue(parser.parse("") instanceof ExportCommand);
        assertTrue(parser.parse("   ") instanceof ExportCommand);
    }

    @Test
    public void parse_argumentsPresent_throwsParseException() {
        assertParseFailure(parser, " n/Alex", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " unexpected", MESSAGE_INVALID_FORMAT);
    }
}
