package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_PREFIX;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FilterCommand;
import seedu.address.model.person.Group;
import seedu.address.model.person.Instrument;
import seedu.address.model.person.InstrumentCriterion;
import seedu.address.model.person.MemberType;
import seedu.address.model.person.Role;
import seedu.address.model.person.Status;

public class FilterCommandParserTest {
    private final FilterCommandParser parser = new FilterCommandParser();

    @Test
    public void parse_groups_returnsFilterCommand() {
        FilterCommand singleGroup = new FilterCommand(Set.of(), Set.of(), Set.of(), Set.of(),
                Set.of(new Group("Chamber Orchestra")));
        assertParseSuccess(parser, "g/Chamber Orchestra", singleGroup);
        assertParseSuccess(parser, "  g/  CHAMBER   orchestra  ", singleGroup);
        assertParseSuccess(parser, "g/Chamber Orchestra g/chamber orchestra", singleGroup);
        assertParseSuccess(parser, "g/\u00a0Chamber\u2002Orchestra\u202f g/Chamber Orchestra", singleGroup);

        FilterCommand multipleGroups = new FilterCommand(Set.of(), Set.of(), Set.of(), Set.of(),
                Set.of(new Group("Chamber Orchestra"), new Group("Training")));
        assertParseSuccess(parser, "g/Chamber Orchestra g/Training", multipleGroups);
        assertParseSuccess(parser, "g/Training g/Chamber Orchestra g/TRAINING", multipleGroups);
    }

    @Test
    public void parse_groupsWithOtherFields_returnsFilterCommand() {
        FilterCommand expected = new FilterCommand(Set.of(Status.MEMBER, Status.ACTIVE_ALUMNUS),
                Set.of(new InstrumentCriterion("Violin", 3), new InstrumentCriterion("Cello", 0)),
                Set.of(MemberType.TEACHER), Set.of(new Role("Section Leader")),
                Set.of(new Group("Chamber Orchestra"), new Group("Training")));
        assertParseSuccess(parser, "g/Chamber Orchestra s/member i/Violin:3 ty/teacher r/Section Leader"
                + " g/Training s/active-alumnus i/Cello", expected);
        assertParseSuccess(parser, "i/Cello g/Training r/Section Leader s/active-alumnus ty/teacher"
                + " i/Violin:3 s/member g/Chamber Orchestra", expected);
    }

    @Test
    public void parse_blankGroup_throwsParseException() {
        for (String args : new String[] {"g/", "g/   ", "g/\u00a0", "g/\u2002\u202f", "g/Orchestra g/\u00a0",
            "g/Orchestra g/", "g/ g/Orchestra",
            "s/member g/", "i/Violin:3 g/", "ty/teacher g/", "r/Conductor g/"}) {
            assertParseFailure(parser, args, Group.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_groupWithInvalidCriteria_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "1 g/Orchestra", expectedMessage);
        assertUnknownPrefixRejected("g/Orchestra n/Alice", "n/");
        assertUnknownPrefixRejected("g/Orchestra p/91234567 e/alice@example.com", "p/");
        assertParseFailure(parser, "g/Orchestra s/unknown", Status.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "g/Orchestra ty/unknown", MemberType.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "g/Orchestra r/", Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "g/Orchestra i/", Instrument.MESSAGE_NAME_CONSTRAINTS);
        assertParseFailure(parser, "g/Orchestra i/Violin:abc", InstrumentCriterion.MESSAGE_MINIMUM_YEARS_CONSTRAINTS);
    }

    @Test
    public void parse_validInstrument_returnsFilterCommand() {
        FilterCommand expectedCommand = new FilterCommand(Set.of(),
                Set.of(new InstrumentCriterion("Violin", 0)), Set.of(), Set.of(), Set.of());
        assertParseSuccess(parser, "i/Violin", expectedCommand);
        assertParseSuccess(parser, "  i/  Violin  ", expectedCommand);
        assertParseSuccess(parser, "i/Bass Guitar",
                new FilterCommand(Set.of(), Set.of(new InstrumentCriterion("Bass Guitar", 0)), Set.of(), Set.of(),
                        Set.of()));
    }

    @Test
    public void parse_unicodeWhitespaceInInstrumentName_normalizesAndDeduplicatesCriteria() {
        FilterCommand expected = new FilterCommand(Set.of(),
                Set.of(new InstrumentCriterion("French Horn", 0)), Set.of(), Set.of(), Set.of());
        assertParseSuccess(parser, "i/\u00a0French\u2002Horn\u202f i/French Horn", expected);
        FilterCommand withMinimum = new FilterCommand(Set.of(),
                Set.of(new InstrumentCriterion("French Horn", 3)), Set.of(), Set.of(), Set.of());
        assertParseSuccess(parser, "i/\u00a0French\u2002Horn\u202f:3 i/French Horn:3", withMinimum);
    }

    @Test
    public void parse_missingCriteria_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, "   ", expectedMessage);
        assertParseFailure(parser, "Violin", expectedMessage);
    }

    @Test
    public void parse_tagPrefix_throwsParseException() {
        assertUnknownPrefixRejected("t/friends", "t/");
    }

    @Test
    public void parse_unsupportedPrefix_throwsParseException() {
        assertUnknownPrefixRejected("s/member n/Alice", "n/");
        assertUnknownPrefixRejected("ty/student s/member n/Alice", "n/");
        assertUnknownPrefixRejected("i/Violin n/Alice", "n/");
        assertUnknownPrefixRejected("i/Violin ty/teacher r/Conductor n/Alice", "n/");
        assertUnknownPrefixRejected("i/Violin p/91234567 e/alice@example.com", "p/");
        assertUnknownPrefixRejected("r/Lead e/alice@example.com", "e/");
        assertUnknownPrefixRejected("n/Alice s/member", "n/");
        assertUnknownPrefixRejected("p/91234567", "p/");
        assertUnknownPrefixRejected("e/alice@example.com", "e/");
    }

    @Test
    public void parse_removedOrWrongCasePrefixInPreamble_throwsParseException() {
        assertUnknownPrefixRejected("  a/Somewhere s/member", "a/");
        for (String prefix : new String[] {"S/", "I/", "TY/", "Ty/", "R/", "G/", "N/", "P/", "E/"}) {
            assertUnknownPrefixRejected(prefix + "value", prefix);
            assertUnknownPrefixRejected(prefix + "value s/member", prefix);
        }
        assertUnknownPrefixRejected("S/member t/friend", "S/");
        assertUnknownPrefixRejected("t/friend S/member", "t/");
    }

    @Test
    public void parse_slashTextInsideValues_returnsFilterCommand() {
        for (String role : new String[] {"a/v", "Head of a/v", "Student t/a", "N/A", "Lead S/member",
            "Lead t/friend", "Lead x/1", "Lead Singer/Songwriter", "Lead c/o Chamber and/or Training"}) {
            FilterCommand expected = new FilterCommand(Set.of(), Set.of(), Set.of(),
                    Set.of(new Role(role)), Set.of());
            assertParseSuccess(parser, "r/" + role, expected);
            assertParseSuccess(parser, "  r/ " + role + "  ", expected);
        }
        FilterCommand expected = new FilterCommand(Set.of(), Set.of(new InstrumentCriterion("Keyboard a/v", 2)),
                Set.of(), Set.of(new Role("Student t/a")), Set.of(new Group("Chamber a/v")));
        assertParseSuccess(parser, "r/Student t/a i/Keyboard a/v:2 g/Chamber a/v", expected);
    }

    @Test
    public void parse_supportedPrefixAfterRole_startsNewCriterion() {
        FilterCommand expected = new FilterCommand(Set.of(), Set.of(), Set.of(), Set.of(new Role("Head")),
                Set.of(new Group("Coach")));
        assertParseSuccess(parser, "r/Head g/Coach", expected);
    }

    @Test
    public void parse_unknownTextInRestrictedValues_throwsFieldValidationException() {
        assertParseFailure(parser, "s/member t/friend", Status.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "ty/student x/1", MemberType.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "i/Violin:3 x/1", InstrumentCriterion.MESSAGE_MINIMUM_YEARS_CONSTRAINTS);
    }

    @Test
    public void parse_arbitraryUnknownPrefixInPreamble_throwsCommandFormatException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "x/1", expectedMessage);
        assertParseFailure(parser, "x/1 s/member", expectedMessage);
    }

    @Test
    public void parse_unexpectedPreamble_throwsParseException() {
        assertParseFailure(parser, "1 i/Violin",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidInstrument_throwsParseException() {
        assertParseFailure(parser, "i/", Instrument.MESSAGE_NAME_CONSTRAINTS);
        assertParseFailure(parser, "i/\u00a0", Instrument.MESSAGE_NAME_CONSTRAINTS);
        assertParseFailure(parser, "i/\u2002\u202f:3", Instrument.MESSAGE_NAME_CONSTRAINTS);
        assertParseFailure(parser, "i/:3", Instrument.MESSAGE_NAME_CONSTRAINTS);
        assertParseFailure(parser, "i/Violin i/", Instrument.MESSAGE_NAME_CONSTRAINTS);
        assertParseFailure(parser, "i/:3 i/Cello", Instrument.MESSAGE_NAME_CONSTRAINTS);
    }

    @Test
    public void parse_multipleInstruments_returnsFilterCommand() {
        FilterCommand expectedCommand = new FilterCommand(Set.of(),
                Set.of(new InstrumentCriterion("Violin", 0), new InstrumentCriterion("Cello", 0)), Set.of(), Set.of(),
                Set.of());
        assertParseSuccess(parser, "i/Violin i/Cello", expectedCommand);
        assertParseSuccess(parser, "i/ Cello i/ Violin i/Violin", expectedCommand);
    }

    @Test
    public void parse_validStatus_returnsFilterCommand() {
        for (Status status : Status.values()) {
            FilterCommand expectedCommand = new FilterCommand(Set.of(status), Set.of(), Set.of(), Set.of(), Set.of());
            assertParseSuccess(parser, "s/" + status, expectedCommand);
        }
        assertParseSuccess(parser, "  s/  ACTIVE-ALUMNUS  ",
                new FilterCommand(Set.of(Status.ACTIVE_ALUMNUS), Set.of(), Set.of(), Set.of(), Set.of()));
    }

    @Test
    public void parse_multipleStatuses_returnsFilterCommand() {
        FilterCommand expectedCommand = new FilterCommand(
                Set.of(Status.MEMBER, Status.ACTIVE_ALUMNUS), Set.of(), Set.of(), Set.of(), Set.of());
        assertParseSuccess(parser, "s/member s/active-alumnus", expectedCommand);
        assertParseSuccess(parser, "s/ACTIVE-ALUMNUS s/member s/MEMBER", expectedCommand);
    }

    @Test
    public void parse_statusesAndInstruments_returnsFilterCommand() {
        FilterCommand expectedCommand = new FilterCommand(
                Set.of(Status.MEMBER, Status.ACTIVE_ALUMNUS),
                Set.of(new InstrumentCriterion("Violin", 0), new InstrumentCriterion("Cello", 0)), Set.of(), Set.of(),
                Set.of());
        assertParseSuccess(parser, "s/member i/Violin s/active-alumnus i/Cello", expectedCommand);
        assertParseSuccess(parser, "i/Cello s/active-alumnus i/Violin s/member", expectedCommand);
    }

    @Test
    public void parse_invalidStatus_throwsParseException() {
        assertParseFailure(parser, "s/", Status.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "s/alumnus", Status.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "s/student", Status.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "s/member active-alumnus", Status.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "s/member s/", Status.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "s/unknown s/member", Status.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "i/Violin s/unknown", Status.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_statusWithInvalidInstrument_throwsParseException() {
        assertParseFailure(parser, "s/member i/", Instrument.MESSAGE_NAME_CONSTRAINTS);
        assertParseFailure(parser, "s/member i/:3", Instrument.MESSAGE_NAME_CONSTRAINTS);
    }

    @Test
    public void parse_statusWithUnexpectedPreamble_throwsParseException() {
        assertParseFailure(parser, "1 s/member",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_instrumentWithMinimumYears_returnsFilterCommand() {
        for (int years : new int[] {0, 3, Instrument.MAX_YEARS}) {
            FilterCommand expectedCommand = new FilterCommand(Set.of(),
                    Set.of(new InstrumentCriterion("Violin", years)), Set.of(), Set.of(), Set.of());
            assertParseSuccess(parser, "i/Violin:" + years, expectedCommand);
        }
        assertParseSuccess(parser, "  i/  BASS   guitar : 3  ",
                new FilterCommand(Set.of(), Set.of(new InstrumentCriterion("Bass Guitar", 3)), Set.of(), Set.of(),
                        Set.of()));
        assertParseSuccess(parser, "i/Violin i/VIOLIN:0",
                new FilterCommand(Set.of(), Set.of(new InstrumentCriterion("Violin", 0)), Set.of(), Set.of(),
                        Set.of()));
    }

    @Test
    public void parse_mixedInstrumentCriteriaAndStatuses_returnsFilterCommand() {
        FilterCommand expectedCommand = new FilterCommand(Set.of(Status.MEMBER, Status.ACTIVE_ALUMNUS),
                Set.of(new InstrumentCriterion("Violin", 3), new InstrumentCriterion("Cello", 0)), Set.of(), Set.of(),
                Set.of());
        assertParseSuccess(parser, "s/member i/Violin:3 s/active-alumnus i/Cello", expectedCommand);
        assertParseSuccess(parser, "i/Cello s/ACTIVE-ALUMNUS i/violin:3 s/member", expectedCommand);
    }

    @Test
    public void parse_repeatedInstrumentWithDifferentMinimumYears_returnsFilterCommand() {
        FilterCommand expectedCommand = new FilterCommand(Set.of(),
                Set.of(new InstrumentCriterion("Violin", 3), new InstrumentCriterion("Violin", 5)), Set.of(), Set.of(),
                Set.of());
        assertParseSuccess(parser, "i/Violin:3 i/violin:5 i/VIOLIN:3", expectedCommand);
    }

    @Test
    public void parse_invalidMinimumYears_throwsParseException() {
        for (String years : new String[] {"", "-1", "100", "1.5", "3.0", "abc", "+3", "1e1",
            "999999999999999999999", "3:4"}) {
            assertParseFailure(parser, "i/Violin:" + years, InstrumentCriterion.MESSAGE_MINIMUM_YEARS_CONSTRAINTS);
            assertParseFailure(parser, "s/member i/Violin:" + years + " i/Cello",
                    InstrumentCriterion.MESSAGE_MINIMUM_YEARS_CONSTRAINTS);
        }
    }

    @Test
    public void parse_memberTypes_returnsFilterCommand() {
        assertParseSuccess(parser, "ty/ TEACHER ",
                new FilterCommand(Set.of(), Set.of(), Set.of(MemberType.TEACHER), Set.of(), Set.of()));
        assertParseSuccess(parser, "ty/student ty/teacher ty/STUDENT",
                new FilterCommand(Set.of(), Set.of(), Set.of(MemberType.STUDENT, MemberType.TEACHER), Set.of(),
                        Set.of()));
    }

    @Test
    public void parse_roles_returnsFilterCommand() {
        assertParseSuccess(parser, "r/ Section Leader ",
                new FilterCommand(Set.of(), Set.of(), Set.of(), Set.of(new Role("Section Leader")), Set.of()));
        assertParseSuccess(parser, "r/Conductor r/Section Leader r/Conductor",
                new FilterCommand(Set.of(), Set.of(), Set.of(),
                        Set.of(new Role("Conductor"), new Role("Section Leader")), Set.of()));
    }

    @Test
    public void parse_allFields_returnsFilterCommand() {
        FilterCommand expected = new FilterCommand(Set.of(Status.MEMBER),
                Set.of(new InstrumentCriterion("Violin", 3)), Set.of(MemberType.TEACHER),
                Set.of(new Role("Conductor")), Set.of());
        assertParseSuccess(parser, "ty/teacher r/Conductor s/member i/Violin:3", expected);
        assertParseSuccess(parser, "i/Violin:3 s/member r/Conductor ty/teacher", expected);
    }

    @Test
    public void parse_invalidMemberTypeOrRole_throwsParseException() {
        for (String value : new String[] {"", "member", "teacher ty/"}) {
            assertParseFailure(parser, "ty/" + value, MemberType.MESSAGE_CONSTRAINTS);
        }
        assertParseFailure(parser, "r/ ", Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "r/Conductor r/", Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "ty/teacher r/", Role.MESSAGE_CONSTRAINTS);
    }

    /**
     * Asserts that parsing reports the rejected prefix and the prefixes accepted by {@code filter}.
     */
    private void assertUnknownPrefixRejected(String args, String prefix) {
        assertParseFailure(parser, args, String.format(MESSAGE_UNKNOWN_PREFIX, prefix, "s/ i/ ty/ r/ g/"));
    }
}
