package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.NameContainsKeywordsPredicate;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ", String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_trimEmptyControlKeywords_ignoresEmptyKeywords() {
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        for (String control : List.of("\u0000", "\u001C", "\u001D", "\u001E", "\u001F")) {
            assertParseSuccess(parser, "Alice " + control + " Bob", expectedFindCommand);
            assertParseSuccess(parser, control + "Alice " + control + " Bob" + control, expectedFindCommand);
        }
    }

    @Test
    public void parse_controlOnlyArgs_throwsParseException() {
        for (String control : List.of("\u0000", "\u001C", "\u001D", "\u001E", "\u001F")) {
            assertParseFailure(parser, " " + control + " ",
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        // no leading and trailing whitespaces
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_emSpaceSeparator_returnsFindCommand() {
        // U+2003 EM SPACE behaves like an ordinary space separator
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "Alice\u2003Bob", expectedFindCommand);
    }

    @Test
    public void parse_nbspSeparator_returnsFindCommand() {
        // U+00A0 NON-BREAKING SPACE behaves like an ordinary space separator
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "Alice\u00A0Bob", expectedFindCommand);
    }

    @Test
    public void parse_repeatedUnicodeSpaces_returnsFindCommand() {
        // repeated Unicode spaces collapse into a single separator
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "Alice\u2003\u2003\u2003Bob", expectedFindCommand);
    }

    @Test
    public void parse_mixedUnicodeSpaces_returnsFindCommand() {
        // a mix of different Unicode spaces yields the same tokenization
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob", "Carol")));
        assertParseSuccess(parser, "Alice\u2003Bob\u00A0Carol", expectedFindCommand);
    }

    @Test
    public void parse_leadingTrailingUnicodeSeparators_returnsFindCommand() {
        // leading and trailing Unicode separators are trimmed away
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "\u2003Alice\u00A0Bob\u2003", expectedFindCommand);
    }

    @Test
    public void parse_allUnicodeWhitespace_throwsParseException() {
        // a string consisting purely of Unicode White_Space is rejected
        assertParseFailure(parser, "\u2003\u2003\u00A0\u2003",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_mixedUnicodeAndOrdinarySpaces_returnsFindCommand() {
        // Unicode and ordinary spaces are treated uniformly
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob", "Carol")));
        assertParseSuccess(parser, " Alice\u2003 Bob\tCarol ", expectedFindCommand);
    }

}
