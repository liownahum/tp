package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INSTRUMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBER_TYPE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.parser.exceptions.ParseException;

public class ArgumentTokenizerTest {

    private static final Prefix[] HIVEMIND_PREFIXES = {PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_STATUS,
        PREFIX_MEMBER_TYPE, PREFIX_ROLE, PREFIX_INSTRUMENT, PREFIX_GROUP};
    private static final String ACCEPTED_PREFIXES = "n/ p/ e/ s/ ty/ r/ i/ g/";

    private final Prefix unknownPrefix = new Prefix("--u");
    private final Prefix pSlash = new Prefix("p/");
    private final Prefix dashT = new Prefix("-t");
    private final Prefix hatQ = new Prefix("^Q");

    @Test
    public void tokenize_emptyArgsString_noValues() {
        String argsString = "  ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);

        assertPreambleEmpty(argMultimap);
        assertArgumentAbsent(argMultimap, pSlash);
    }

    private void assertPreamblePresent(ArgumentMultimap argMultimap, String expectedPreamble) {
        assertEquals(expectedPreamble, argMultimap.getPreamble());
    }

    private void assertPreambleEmpty(ArgumentMultimap argMultimap) {
        assertTrue(argMultimap.getPreamble().isEmpty());
    }

    /**
     * Asserts all the arguments in {@code argMultimap} with {@code prefix} match the {@code expectedValues}
     * and only the last value is returned upon calling {@code ArgumentMultimap#getValue(Prefix)}.
     */
    private void assertArgumentPresent(ArgumentMultimap argMultimap, Prefix prefix, String... expectedValues) {

        // Verify the last value is returned
        assertEquals(expectedValues[expectedValues.length - 1], argMultimap.getValue(prefix).get());

        // Verify the number of values returned is as expected
        assertEquals(expectedValues.length, argMultimap.getAllValues(prefix).size());

        // Verify all values returned are as expected and in order
        for (int i = 0; i < expectedValues.length; i++) {
            assertEquals(expectedValues[i], argMultimap.getAllValues(prefix).get(i));
        }
    }

    private void assertArgumentAbsent(ArgumentMultimap argMultimap, Prefix prefix) {
        assertFalse(argMultimap.getValue(prefix).isPresent());
    }

    @Test
    public void tokenize_noPrefixes_allTakenAsPreamble() {
        String argsString = "  some random string /t tag with leading and trailing spaces ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString);

        // Same string expected as preamble, but leading/trailing spaces should be trimmed
        assertPreamblePresent(argMultimap, argsString.trim());

    }

    @Test
    public void tokenize_oneArgument() {
        // Preamble present
        String argsString = "  Some preamble string p/ Argument value ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);
        assertPreamblePresent(argMultimap, "Some preamble string");
        assertArgumentPresent(argMultimap, pSlash, "Argument value");

        // No preamble
        argsString = " p/   Argument value ";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);
        assertPreambleEmpty(argMultimap);
        assertArgumentPresent(argMultimap, pSlash, "Argument value");

    }

    @Test
    public void tokenize_multipleArguments() {
        // Only two arguments are present
        String argsString = "SomePreambleString -t dashT-Value p/pSlash value";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleString");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value");
        assertArgumentAbsent(argMultimap, hatQ);

        // All three arguments are present
        argsString = "Different Preamble String ^Q111 -t dashT-Value p/pSlash value";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "Different Preamble String");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value");
        assertArgumentPresent(argMultimap, hatQ, "111");

        /* Also covers: Reusing of the tokenizer multiple times */

        // Reuse tokenizer on an empty string to ensure ArgumentMultimap is correctly reset
        // (i.e. no stale values from the previous tokenizing remain)
        argsString = "";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreambleEmpty(argMultimap);
        assertArgumentAbsent(argMultimap, pSlash);

        /* Also covers: testing for prefixes not specified as a prefix */

        // Prefixes not previously given to the tokenizer should not return any values
        argsString = unknownPrefix + "some value";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertArgumentAbsent(argMultimap, unknownPrefix);
        assertPreamblePresent(argMultimap, argsString); // Unknown prefix is taken as part of preamble
    }

    @Test
    public void tokenize_multipleArgumentsWithRepeats() {
        // Two arguments repeated, some have empty values
        String argsString = "SomePreambleString -t dashT-Value ^Q ^Q -t another dashT value p/ pSlash value -t";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleString");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value", "another dashT value", "");
        assertArgumentPresent(argMultimap, hatQ, "", "");
    }

    @Test
    public void tokenize_multipleArgumentsJoined() {
        String argsString = "SomePreambleStringp/ pSlash joined-tjoined -t not joined^Qjoined";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleStringp/ pSlash joined-tjoined");
        assertArgumentAbsent(argMultimap, pSlash);
        assertArgumentPresent(argMultimap, dashT, "not joined^Qjoined");
        assertArgumentAbsent(argMultimap, hatQ);
    }

    @Test
    public void equalsMethod() {
        Prefix aaa = new Prefix("aaa");

        assertEquals(aaa, aaa);
        assertEquals(aaa, new Prefix("aaa"));

        assertNotEquals(aaa, "aaa");
        assertNotEquals(aaa, new Prefix("aab"));
    }

    @Test
    public void verifyNoUnknownPrefixes_acceptedPrefixes_success() {
        String argsString = "1 " + PREFIX_NAME + "Alex " + PREFIX_PHONE + "91234567 " + PREFIX_EMAIL
                + "alex@example.com " + PREFIX_STATUS + "active " + PREFIX_MEMBER_TYPE + "External "
                + PREFIX_ROLE + "Lead " + PREFIX_INSTRUMENT + "Violin:5 " + PREFIX_GROUP + "Training";

        assertDoesNotThrow(() -> ArgumentTokenizer.verifyNoUnknownPrefixes(argsString, HIVEMIND_PREFIXES));
    }

    @Test
    public void verifyNoUnknownPrefixes_removedPrefixes_throwsParseException() {
        assertUnknownPrefixRejected("1 t/friend", "t/");
        assertUnknownPrefixRejected("1 a/Somewhere", "a/");
    }

    @Test
    public void verifyNoUnknownPrefixes_wronglyCapitalisedPrefixes_throwsParseException() {
        assertUnknownPrefixRejected("1 N/Alex", "N/");
        assertUnknownPrefixRejected("1 S/member", "S/");
        assertUnknownPrefixRejected("1 TY/member", "TY/");
        assertUnknownPrefixRejected("1 Ty/member", "Ty/");
    }

    @Test
    public void verifyNoUnknownPrefixes_tokenAtStartOfString_throwsParseException() {
        assertUnknownPrefixRejected("S/member", "S/");
    }

    @Test
    public void verifyNoUnknownPrefixes_unrecognisedTokensInValues_success() {
        String argsString = "1 r/Lead c/o Chamber w/ friends and/or family http://example.com 1s/member";

        assertDoesNotThrow(() -> ArgumentTokenizer.verifyNoUnknownPrefixes(argsString, HIVEMIND_PREFIXES));
    }

    @Test
    public void verifyNoUnknownPrefixes_multipleUnknownPrefixes_firstByPositionReported() {
        assertUnknownPrefixRejected("1 S/member t/friend", "S/");
        assertUnknownPrefixRejected("1 t/friend S/member", "t/");
    }

    @Test
    public void verifyNoUnknownPrefixes_wrongCaseTokensInValues_success() {
        assertDoesNotThrow(() -> ArgumentTokenizer.verifyNoUnknownPrefixes(
                "1 r/Head of A/V g/N/A Ensemble", HIVEMIND_PREFIXES));
        // removed prefixes inside values are kept as text, however they are spaced
        assertDoesNotThrow(() -> ArgumentTokenizer.verifyNoUnknownPrefixes("1 r/a/v", HIVEMIND_PREFIXES));
        assertDoesNotThrow(() -> ArgumentTokenizer.verifyNoUnknownPrefixes("1 r/ a/v", HIVEMIND_PREFIXES));
        assertDoesNotThrow(() -> ArgumentTokenizer.verifyNoUnknownPrefixes(
                "1 r/Student t/a g/Head of a/v", HIVEMIND_PREFIXES));
        assertDoesNotThrow(() -> ArgumentTokenizer.verifyNoUnknownPrefixes("1 r/ N/A", HIVEMIND_PREFIXES));
        assertDoesNotThrow(() -> ArgumentTokenizer.verifyNoUnknownPrefixes("1 r/ T/A S/O", HIVEMIND_PREFIXES));
        assertDoesNotThrow(() -> ArgumentTokenizer.verifyNoUnknownPrefixes("1 n/Alex S/member", HIVEMIND_PREFIXES));
    }

    @Test
    public void verifyNoUnknownPrefixes_tokenAfterNonSpaceWhitespace_success() {
        // the tokenizer only splits on spaces, so a tab does not start a prefix
        assertDoesNotThrow(() -> ArgumentTokenizer.verifyNoUnknownPrefixes("1\tS/member", HIVEMIND_PREFIXES));
        assertDoesNotThrow(() -> ArgumentTokenizer.verifyNoUnknownPrefixes("1 r/Lead\tt/friend", HIVEMIND_PREFIXES));
    }

    @Test
    public void verifyNoUnknownPrefixes_prefixNotAcceptedByCommand_throwsParseException() {
        ParseException pe = assertThrows(ParseException.class, () ->
                ArgumentTokenizer.verifyNoUnknownPrefixes("s/member n/Alex", PREFIX_STATUS, PREFIX_ROLE));
        assertEquals(String.format(Messages.MESSAGE_UNKNOWN_PREFIX, "n/", "s/ r/"), pe.getMessage());
        assertDoesNotThrow(() -> ArgumentTokenizer.verifyNoUnknownPrefixes(
                "s/member r/ N/A", PREFIX_STATUS, PREFIX_ROLE));
    }

    /**
     * Asserts that {@code argsString} is rejected by {@link ArgumentTokenizer#verifyNoUnknownPrefixes} because of
     * the unknown prefix {@code expectedToken}.
     */
    private void assertUnknownPrefixRejected(String argsString, String expectedToken) {
        ParseException pe = assertThrows(ParseException.class, () ->
                ArgumentTokenizer.verifyNoUnknownPrefixes(argsString, HIVEMIND_PREFIXES));
        assertEquals(String.format(Messages.MESSAGE_UNKNOWN_PREFIX, expectedToken, ACCEPTED_PREFIXES),
                pe.getMessage());
    }

}
