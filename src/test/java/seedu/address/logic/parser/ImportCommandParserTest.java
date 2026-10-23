package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ImportCommand;

public class ImportCommandParserTest {

    private final ImportCommandParser parser = new ImportCommandParser();

    @Test
    public void parse_validFileName_returnsImportCommand() throws Exception {
        assertTrue(parser.parse(" members.CSV ") instanceof ImportCommand);
    }

    @Test
    public void parse_invalidFileName_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ImportCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, "members.txt", expectedMessage);
        assertParseFailure(parser, "folder/members.csv", expectedMessage);
        assertParseFailure(parser, "folder\\members.csv", expectedMessage);
    }
}
