package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INSTRUMENT_DESC_PIANO;
import static seedu.address.logic.commands.CommandTestUtil.INSTRUMENT_DESC_VIOLIN;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_INSTRUMENT_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_MEMBER_TYPE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ROLE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_STATUS_DESC;
import static seedu.address.logic.commands.CommandTestUtil.MEMBER_TYPE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_NON_EMPTY;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_WHITESPACE;
import static seedu.address.logic.commands.CommandTestUtil.ROLE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.STATUS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.STATUS_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_INSTRUMENT_PIANO;
import static seedu.address.logic.commands.CommandTestUtil.VALID_INSTRUMENT_VIOLIN;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INSTRUMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBER_TYPE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.Email;
import seedu.address.model.person.Group;
import seedu.address.model.person.Instrument;
import seedu.address.model.person.MemberType;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.person.Status;
import seedu.address.testutil.PersonBuilder;

/** Verifies add validation, including useful errors and legitimate slash-containing field values. */
public class AddCommandParserTest {
    private AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_unknownPrefixInPreamble_namesPrefix() {
        String fields = NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + STATUS_DESC_AMY;
        for (String prefix : new String[]{"t/", "a/", "N/", "TY/", "G/"}) {
            String expected = String.format(Messages.MESSAGE_UNKNOWN_PREFIX, prefix, "n/ p/ e/ s/ ty/ r/ i/ g/");
            assertParseFailure(parser, " " + prefix + "wrong" + fields, expected);
        }
    }

    @Test
    public void parse_slashTextAfterSupportedPrefix_preservesFieldValues() {
        Person expected = new PersonBuilder(AMY).withInstruments().withRole("N/A c/o a/v")
                .withGroups("Training gr/Orchestra").build();
        assertParseSuccess(parser, NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + STATUS_DESC_AMY
                + " r/N/A c/o a/v g/Training gr/Orchestra", new AddCommand(expected));
    }

    @Test
    public void parse_invisibleGroupAndRole_normalizesAndRejectsBlankValues() {
        String fields = NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + STATUS_DESC_AMY;
        Person expected = new PersonBuilder(AMY).withInstruments().withGroups("Jazz").withRole("Leader").build();
        assertParseSuccess(parser, fields + " g/Jazz g/Ja\u200bzz r/Lea\u200bder", new AddCommand(expected));
        assertParseFailure(parser, fields + " g/\u200b", Group.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, fields + " r/\ufeff", Role.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_allFieldsPresent_success() {
        Person expectedPerson = new PersonBuilder(BOB).withInstruments(VALID_INSTRUMENT_VIOLIN).build();

        // whitespace only preamble
        assertParseSuccess(parser, PREAMBLE_WHITESPACE + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + STATUS_DESC_BOB
                + INSTRUMENT_DESC_VIOLIN, new AddCommand(expectedPerson));


        // multiple instruments - all accepted
        Person expectedPersonMultipleInstruments = new PersonBuilder(BOB)
                .withInstruments(VALID_INSTRUMENT_VIOLIN, VALID_INSTRUMENT_PIANO).build();
        assertParseSuccess(parser,
                NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + STATUS_DESC_BOB
                        + INSTRUMENT_DESC_PIANO + INSTRUMENT_DESC_VIOLIN,
                new AddCommand(expectedPersonMultipleInstruments));

        Person teacherWithRole = new PersonBuilder(BOB).withInstruments().withMemberType("teacher")
                .withRole("Section Leader").build();
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + STATUS_DESC_BOB
                + MEMBER_TYPE_DESC_BOB + ROLE_DESC_BOB, new AddCommand(teacherWithRole));

        Person teacherWithRoleAndInstruments = new PersonBuilder(BOB)
                .withMemberType("teacher")
                .withRole("Section Leader")
                .withInstruments(VALID_INSTRUMENT_PIANO, VALID_INSTRUMENT_VIOLIN)
                .build();
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + STATUS_DESC_BOB
                + MEMBER_TYPE_DESC_BOB + ROLE_DESC_BOB
                + INSTRUMENT_DESC_PIANO + INSTRUMENT_DESC_VIOLIN,
                new AddCommand(teacherWithRoleAndInstruments));

        Person personWithGroups = new PersonBuilder(BOB)
                .withInstruments()
                .withGroups("Chamber Orchestra", "Training")
                .build();
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + STATUS_DESC_BOB
                + " " + PREFIX_GROUP + "Chamber Orchestra " + PREFIX_GROUP + "Training",
                new AddCommand(personWithGroups));

        // Repeating a group with different case or spacing records it only once.
        Person personWithOneGroup = new PersonBuilder(BOB).withInstruments().withGroups("Chamber Orchestra").build();
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + STATUS_DESC_BOB
                + " " + PREFIX_GROUP + "Chamber Orchestra " + PREFIX_GROUP + "chamber   orchestra",
                new AddCommand(personWithOneGroup));
    }

    @Test
    public void parse_repeatedNonInstrumentValue_failure() {
        String validExpectedPersonString = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + STATUS_DESC_BOB
                + INSTRUMENT_DESC_VIOLIN;

        // multiple names
        assertParseFailure(parser, NAME_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // multiple phones
        assertParseFailure(parser, PHONE_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // multiple emails
        assertParseFailure(parser, EMAIL_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        assertParseFailure(parser, STATUS_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_STATUS));
        assertParseFailure(parser, validExpectedPersonString + MEMBER_TYPE_DESC_BOB + MEMBER_TYPE_DESC_BOB,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_MEMBER_TYPE));
        assertParseFailure(parser, validExpectedPersonString + ROLE_DESC_BOB + ROLE_DESC_BOB,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ROLE));

        // multiple fields repeated
        assertParseFailure(parser,
                validExpectedPersonString + PHONE_DESC_AMY + EMAIL_DESC_AMY + NAME_DESC_AMY
                        + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME, PREFIX_EMAIL, PREFIX_PHONE, PREFIX_STATUS));

        // invalid value followed by valid value

        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // invalid email
        assertParseFailure(parser, INVALID_EMAIL_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // invalid phone
        assertParseFailure(parser, INVALID_PHONE_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // valid value followed by invalid value

        // invalid name
        assertParseFailure(parser, validExpectedPersonString + INVALID_NAME_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // invalid email
        assertParseFailure(parser, validExpectedPersonString + INVALID_EMAIL_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // invalid phone
        assertParseFailure(parser, validExpectedPersonString + INVALID_PHONE_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));
    }

    @Test
    public void parse_optionalFieldsMissing_success() {
        // zero instruments
        Person expectedPerson = new PersonBuilder(AMY).withInstruments().build();
        assertParseSuccess(parser, NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + STATUS_DESC_AMY,
                new AddCommand(expectedPerson));
        assertEquals(MemberType.STUDENT, expectedPerson.getMemberType());
        assertTrue(expectedPerson.getRole().isEmpty());
        assertTrue(expectedPerson.getGroups().isEmpty());
    }

    @Test
    public void parse_nameWithRelationshipMarker_success() {
        // uppercase S/O, as a lowercase s/ starts the status
        Person expectedPerson = new PersonBuilder(AMY).withName("Ravi S/O Kumar").withInstruments().build();
        assertParseSuccess(parser, " " + PREFIX_NAME + "Ravi S/O Kumar" + PHONE_DESC_AMY + EMAIL_DESC_AMY
                + STATUS_DESC_AMY, new AddCommand(expectedPerson));

        // d/ is not a prefix, so a lowercase d/o is kept as text
        expectedPerson = new PersonBuilder(AMY).withName("Priya d/o Raman").withInstruments().build();
        assertParseSuccess(parser, " " + PREFIX_NAME + "Priya d/o Raman" + PHONE_DESC_AMY + EMAIL_DESC_AMY
                + STATUS_DESC_AMY, new AddCommand(expectedPerson));
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        // missing name prefix
        assertParseFailure(parser, VALID_NAME_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + STATUS_DESC_BOB,
                expectedMessage);

        // missing phone prefix
        assertParseFailure(parser, NAME_DESC_BOB + VALID_PHONE_BOB + EMAIL_DESC_BOB + STATUS_DESC_BOB,
                expectedMessage);

        // missing email prefix
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + VALID_EMAIL_BOB + STATUS_DESC_BOB,
                expectedMessage);

        // missing status prefix
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB, expectedMessage);

        // all prefixes missing
        assertParseFailure(parser, VALID_NAME_BOB + VALID_PHONE_BOB + VALID_EMAIL_BOB,
                expectedMessage);
    }

    @Test
    public void parse_compulsoryFieldBlank_failure() {
        // blank name
        assertParseFailure(parser, " " + PREFIX_NAME + PHONE_DESC_BOB + EMAIL_DESC_BOB + STATUS_DESC_BOB,
                Name.MESSAGE_CONSTRAINTS);

        // blank phone
        assertParseFailure(parser, NAME_DESC_BOB + " " + PREFIX_PHONE + EMAIL_DESC_BOB + STATUS_DESC_BOB,
                Phone.MESSAGE_CONSTRAINTS);

        // blank email
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + " " + PREFIX_EMAIL + STATUS_DESC_BOB,
                Email.MESSAGE_CONSTRAINTS);

        // blank status
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + " " + PREFIX_STATUS,
                Status.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidValue_failure() {
        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB + STATUS_DESC_BOB
                + INSTRUMENT_DESC_PIANO + INSTRUMENT_DESC_VIOLIN, Name.MESSAGE_CONSTRAINTS);

        // invalid phone
        assertParseFailure(parser, NAME_DESC_BOB + INVALID_PHONE_DESC + EMAIL_DESC_BOB + STATUS_DESC_BOB
                + INSTRUMENT_DESC_PIANO + INSTRUMENT_DESC_VIOLIN, Phone.MESSAGE_CONSTRAINTS);

        // invalid email
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + INVALID_EMAIL_DESC + STATUS_DESC_BOB
                + INSTRUMENT_DESC_PIANO + INSTRUMENT_DESC_VIOLIN, Email.MESSAGE_CONSTRAINTS);

        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + INVALID_STATUS_DESC,
                Status.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + STATUS_DESC_BOB
                + INVALID_MEMBER_TYPE_DESC, MemberType.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + STATUS_DESC_BOB
                + INVALID_ROLE_DESC, Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + STATUS_DESC_BOB
                + " " + PREFIX_GROUP + " ", Group.MESSAGE_CONSTRAINTS);

        // invalid instrument
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + STATUS_DESC_BOB
                + INVALID_INSTRUMENT_DESC + INSTRUMENT_DESC_VIOLIN, Instrument.MESSAGE_FORMAT_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + STATUS_DESC_BOB
                + " " + PREFIX_INSTRUMENT + "Violin:100", Instrument.MESSAGE_YEARS_CONSTRAINTS);

        // same instrument name listed twice, ignoring case
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + STATUS_DESC_BOB
                + INSTRUMENT_DESC_VIOLIN + " " + PREFIX_INSTRUMENT + "VIOLIN:1", Instrument.MESSAGE_DUPLICATE_NAMES);

        // removed tag prefix is no longer recognised, so it is read as part of the preceding value
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + STATUS_DESC_BOB
                + " t/friends", Status.MESSAGE_CONSTRAINTS);

        // two invalid values, only first invalid value reported
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB + STATUS_DESC_BOB,
                Name.MESSAGE_CONSTRAINTS);

        // non-empty preamble
        assertParseFailure(parser, PREAMBLE_NON_EMPTY + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + STATUS_DESC_BOB
                + INSTRUMENT_DESC_PIANO + INSTRUMENT_DESC_VIOLIN,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }
}
