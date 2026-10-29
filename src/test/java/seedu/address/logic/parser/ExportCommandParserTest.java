package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ExportCommand;
import seedu.address.storage.ExportField;

public class ExportCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExportCommand.MESSAGE_USAGE);

    private final ExportCommandParser parser = new ExportCommandParser();

    @Test
    public void parse_noArguments_returnsExportCommand() throws Exception {
        assertParseSuccess(parser, "", new ExportCommand());
        assertParseSuccess(parser, "   ", new ExportCommand());
    }

    @Test
    public void parse_selectedFields_preservesRequestedOrder() {
        assertParseSuccess(parser, " f/name f/phone f/instruments",
                new ExportCommand(List.of(ExportField.NAME, ExportField.PHONE, ExportField.INSTRUMENTS)));
        assertParseSuccess(parser, "f/phone f/name",
                new ExportCommand(List.of(ExportField.PHONE, ExportField.NAME)));
    }

    @Test
    public void parse_eachSupportedField_returnsSingleFieldCommand() {
        for (ExportField field : ExportField.values()) {
            assertParseSuccess(parser, " f/" + field.getHeader(), new ExportCommand(List.of(field)));
        }
    }

    @Test
    public void parse_allFieldsInReverseOrder_preservesRequestedOrder() {
        assertParseSuccess(parser, " f/groups f/instruments f/role f/memberType f/status f/email f/phone f/name",
                new ExportCommand(ExportField.DEFAULT_FIELDS.reversed()));
    }

    @Test
    public void parse_surroundingSpaces_trimsValues() {
        assertParseSuccess(parser, "  f/ name   f/ phone  ",
                new ExportCommand(List.of(ExportField.NAME, ExportField.PHONE)));
    }

    @Test
    public void parse_emptyField_throwsParseException() {
        String expectedMessage = ExportCommandParser.MESSAGE_EMPTY_FIELD + "\n" + ExportCommand.MESSAGE_USAGE;
        assertParseFailure(parser, " f/", expectedMessage);
        assertParseFailure(parser, " f/   ", expectedMessage);
        assertParseFailure(parser, " f/name f/", expectedMessage);
        assertParseFailure(parser, " f/ f/name", expectedMessage);
    }

    @Test
    public void parse_unsupportedField_throwsParseException() {
        for (String fieldName : List.of("address", "Name", "membertype", "instrument", "name phone",
                "name n/Alex", "name x/phone", "namef/phone")) {
            assertParseFailure(parser, " f/" + fieldName,
                    String.format(ExportCommandParser.MESSAGE_UNSUPPORTED_FIELD, fieldName)
                            + "\n" + ExportCommand.MESSAGE_USAGE);
        }
    }

    @Test
    public void parse_repeatedField_throwsParseException() {
        String expectedMessage = String.format(ExportCommandParser.MESSAGE_REPEATED_FIELD, "name")
                + "\n" + ExportCommand.MESSAGE_USAGE;
        assertParseFailure(parser, " f/name f/name", expectedMessage);
        assertParseFailure(parser, " f/name f/phone f/name", expectedMessage);
    }

    @Test
    public void parse_argumentsPresent_throwsParseException() {
        assertParseFailure(parser, " n/Alex", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " unexpected", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " unexpected f/name", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " F/name", MESSAGE_INVALID_FORMAT);
    }
}
