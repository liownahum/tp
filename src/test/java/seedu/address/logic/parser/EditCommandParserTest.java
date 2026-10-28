package seedu.address.logic.parser;

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
import static seedu.address.logic.commands.CommandTestUtil.INVALID_STATUS_DESC;
import static seedu.address.logic.commands.CommandTestUtil.MEMBER_TYPE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.ROLE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.STATUS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_INSTRUMENT_PIANO;
import static seedu.address.logic.commands.CommandTestUtil.VALID_INSTRUMENT_VIOLIN;
import static seedu.address.logic.commands.CommandTestUtil.VALID_MEMBER_TYPE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ROLE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_STATUS_AMY;
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
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_THIRD_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Email;
import seedu.address.model.person.Group;
import seedu.address.model.person.Instrument;
import seedu.address.model.person.MemberType;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Status;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditCommandParserTest {

    private static final String INSTRUMENT_EMPTY = " " + PREFIX_INSTRUMENT;
    private static final String ROLE_EMPTY = " " + PREFIX_ROLE;

    private static final String ACCEPTED_PREFIXES = "n/ p/ e/ s/ ty/ r/ i/ g/";

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE);

    private EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_missingParts_failure() {
        // no index specified
        assertParseFailure(parser, VALID_NAME_AMY, MESSAGE_INVALID_FORMAT);

        // no field specified
        assertParseFailure(parser, "1", EditCommand.MESSAGE_NOT_EDITED);

        // no index and no field specified
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        // negative index
        assertParseFailure(parser, "-5" + NAME_DESC_AMY, ParserUtil.MESSAGE_INVALID_INDEX);

        // zero index
        assertParseFailure(parser, "0" + NAME_DESC_AMY, ParserUtil.MESSAGE_INVALID_INDEX);

        // plus-signed index
        assertParseFailure(parser, "+1" + NAME_DESC_AMY, ParserUtil.MESSAGE_INVALID_INDEX);

        // invalid arguments being parsed as preamble
        assertParseFailure(parser, "1 some random string", MESSAGE_INVALID_FORMAT);

        // invalid prefix being parsed as preamble
        assertParseFailure(parser, "1 x/ string", MESSAGE_INVALID_FORMAT);

        // preamble that is not an integer at all
        assertParseFailure(parser, "abc n/Alex", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_indexTooLargeForInt_failure() {
        assertParseFailure(parser, "99999999999 n/Alex", Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertParseFailure(parser, "+99999999999 n/Alex", Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertParseFailure(parser, "-99999999999 n/Alex", ParserUtil.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_fullWidthDigitIndex_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).build();
        assertParseSuccess(parser, "\uFF11" + NAME_DESC_AMY, new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_indexNotPositive_failure() {
        assertParseFailure(parser, "0 n/Alex", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "-3 n/Alex", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "+1 n/Alex", ParserUtil.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_removedPrefixInsideValue_keptAsText() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withRole("Head of a/v").build();
        assertParseSuccess(parser, "1 r/Head of a/v", new EditCommand(INDEX_FIRST_PERSON, descriptor));

        descriptor = new EditPersonDescriptorBuilder().withRole("a/v").build();
        assertParseSuccess(parser, "1 r/ a/v", new EditCommand(INDEX_FIRST_PERSON, descriptor));
        assertParseSuccess(parser, "1 r/a/v", new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_unknownPrefix_failure() {
        // removed tag prefix that the original AddressBook accepted, read as part of the index text before
        assertParseFailure(parser, "1 t/friends",
                String.format(Messages.MESSAGE_UNKNOWN_PREFIX, "t/", ACCEPTED_PREFIXES));

        // wrongly capitalised prefix, seen as part of the index text before
        assertParseFailure(parser, "1 S/member",
                String.format(Messages.MESSAGE_UNKNOWN_PREFIX, "S/", ACCEPTED_PREFIXES));
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, "1" + INVALID_NAME_DESC, Name.MESSAGE_CONSTRAINTS); // invalid name
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC, Phone.MESSAGE_CONSTRAINTS); // invalid phone
        assertParseFailure(parser, "1" + INVALID_EMAIL_DESC, Email.MESSAGE_CONSTRAINTS); // invalid email
        assertParseFailure(parser, "1" + INVALID_INSTRUMENT_DESC,
                Instrument.MESSAGE_FORMAT_CONSTRAINTS); // invalid instrument
        assertParseFailure(parser, "1" + INSTRUMENT_DESC_VIOLIN + " " + PREFIX_INSTRUMENT + "violin:1",
                Instrument.MESSAGE_DUPLICATE_NAMES); // repeated instrument name

        // invalid phone followed by valid email
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC + EMAIL_DESC_AMY, Phone.MESSAGE_CONSTRAINTS);

        // while parsing {@code PREFIX_INSTRUMENT} alone will reset the instruments of the {@code Person} being
        // edited, parsing it together with a valid instrument results in error
        assertParseFailure(parser, "1" + INSTRUMENT_DESC_VIOLIN + INSTRUMENT_DESC_PIANO + INSTRUMENT_EMPTY,
                Instrument.MESSAGE_FORMAT_CONSTRAINTS);
        assertParseFailure(parser, "1" + INSTRUMENT_DESC_VIOLIN + INSTRUMENT_EMPTY + INSTRUMENT_DESC_PIANO,
                Instrument.MESSAGE_FORMAT_CONSTRAINTS);
        assertParseFailure(parser, "1" + INSTRUMENT_EMPTY + INSTRUMENT_DESC_VIOLIN + INSTRUMENT_DESC_PIANO,
                Instrument.MESSAGE_FORMAT_CONSTRAINTS);

        assertParseFailure(parser, "1" + INVALID_STATUS_DESC, Status.MESSAGE_CONSTRAINTS); // invalid status
        assertParseFailure(parser, "1 " + PREFIX_STATUS, Status.MESSAGE_CONSTRAINTS); // blank status
        assertParseFailure(parser, "1" + INVALID_MEMBER_TYPE_DESC,
                MemberType.MESSAGE_CONSTRAINTS); // invalid member type
        assertParseFailure(parser, "1 " + PREFIX_GROUP + "Training " + PREFIX_GROUP,
                Group.MESSAGE_CONSTRAINTS);

        // multiple invalid values, but only the first invalid value is captured
        assertParseFailure(parser, "1" + INVALID_NAME_DESC + INVALID_EMAIL_DESC + VALID_PHONE_AMY,
                Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_allFieldsSpecified_success() {
        Index targetIndex = INDEX_SECOND_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + INSTRUMENT_DESC_PIANO
                + EMAIL_DESC_AMY + NAME_DESC_AMY + INSTRUMENT_DESC_VIOLIN;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY)
                .withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_AMY)
                .withInstruments(VALID_INSTRUMENT_PIANO, VALID_INSTRUMENT_VIOLIN).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_membershipFieldsSpecified_success() {
        Index targetIndex = INDEX_SECOND_PERSON;
        String userInput = targetIndex.getOneBased() + ROLE_DESC_BOB + STATUS_DESC_AMY + MEMBER_TYPE_DESC_BOB;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withStatus(VALID_STATUS_AMY)
                .withMemberType(VALID_MEMBER_TYPE_BOB).withRole(VALID_ROLE_BOB).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_groupsSpecified_replacesGroups() {
        Index targetIndex = INDEX_SECOND_PERSON;
        String userInput = targetIndex.getOneBased() + " " + PREFIX_GROUP + "Training "
                + PREFIX_GROUP + "Chamber Orchestra";
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withGroups("Training", "Chamber Orchestra").build();
        assertParseSuccess(parser, userInput, new EditCommand(targetIndex, descriptor));
    }

    @Test
    public void parse_singleGroup_replacesGroups() {
        Index targetIndex = INDEX_SECOND_PERSON;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withGroups("Training").build();
        assertParseSuccess(parser, targetIndex.getOneBased() + " " + PREFIX_GROUP + "Training",
                new EditCommand(targetIndex, descriptor));
    }

    @Test
    public void parse_groupsDifferingOnlyInCase_storesOneGroup() {
        Index targetIndex = INDEX_SECOND_PERSON;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withGroups("Training").build();
        assertParseSuccess(parser, targetIndex.getOneBased() + " " + PREFIX_GROUP + "Training "
                + PREFIX_GROUP + "training", new EditCommand(targetIndex, descriptor));
    }

    @Test
    public void parse_emptyGroup_clearsGroups() {
        Index targetIndex = INDEX_SECOND_PERSON;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withGroups().build();
        assertParseSuccess(parser, targetIndex.getOneBased() + " " + PREFIX_GROUP,
                new EditCommand(targetIndex, descriptor));
    }

    @Test
    public void parse_emptyGroupWithName_clearsGroupsAndEditsName() {
        Index targetIndex = INDEX_SECOND_PERSON;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName("Bob").withGroups().build();
        assertParseSuccess(parser, targetIndex.getOneBased() + " " + PREFIX_NAME + "Bob " + PREFIX_GROUP,
                new EditCommand(targetIndex, descriptor));
    }

    @Test
    public void parse_membershipFieldAfterInstrument_notAbsorbedIntoInstrument() {
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + INSTRUMENT_DESC_VIOLIN + " " + PREFIX_STATUS
                + "active-alumnus" + MEMBER_TYPE_DESC_BOB;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withInstruments(VALID_INSTRUMENT_VIOLIN)
                .withStatus("active-alumnus").withMemberType(VALID_MEMBER_TYPE_BOB).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_someFieldsSpecified_success() {
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + EMAIL_DESC_AMY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_oneFieldSpecified_success() {
        // name
        Index targetIndex = INDEX_THIRD_PERSON;
        String userInput = targetIndex.getOneBased() + NAME_DESC_AMY;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // phone
        userInput = targetIndex.getOneBased() + PHONE_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // email
        userInput = targetIndex.getOneBased() + EMAIL_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withEmail(VALID_EMAIL_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // instruments
        userInput = targetIndex.getOneBased() + INSTRUMENT_DESC_VIOLIN;
        descriptor = new EditPersonDescriptorBuilder().withInstruments(VALID_INSTRUMENT_VIOLIN).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_multipleRepeatedFields_failure() {
        // More extensive testing of duplicate parameter detections is done in
        // AddCommandParserTest#parse_repeatedNonInstrumentValue_failure()

        // valid followed by invalid
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + INVALID_PHONE_DESC + PHONE_DESC_BOB;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid followed by valid
        userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + INVALID_PHONE_DESC;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // multiple valid fields repeated
        userInput = targetIndex.getOneBased() + PHONE_DESC_AMY + EMAIL_DESC_AMY
                + INSTRUMENT_DESC_VIOLIN + PHONE_DESC_AMY + EMAIL_DESC_AMY + INSTRUMENT_DESC_VIOLIN
                + PHONE_DESC_BOB + EMAIL_DESC_BOB + INSTRUMENT_DESC_PIANO;

        assertParseFailure(parser, userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE, PREFIX_EMAIL));

        // repeated membership fields
        userInput = targetIndex.getOneBased() + STATUS_DESC_AMY + " " + PREFIX_STATUS + "active-alumnus"
                + MEMBER_TYPE_DESC_BOB + MEMBER_TYPE_DESC_BOB + ROLE_DESC_BOB + ROLE_EMPTY;

        assertParseFailure(parser, userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_STATUS, PREFIX_MEMBER_TYPE, PREFIX_ROLE));

        // multiple invalid values
        userInput = targetIndex.getOneBased() + INVALID_PHONE_DESC + INVALID_EMAIL_DESC
                + INVALID_PHONE_DESC + INVALID_EMAIL_DESC;

        assertParseFailure(parser, userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE, PREFIX_EMAIL));
    }

    @Test
    public void parse_resetInstruments_success() {
        Index targetIndex = INDEX_THIRD_PERSON;
        String userInput = targetIndex.getOneBased() + INSTRUMENT_EMPTY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withInstruments().build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_resetRole_success() {
        Index targetIndex = INDEX_THIRD_PERSON;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withoutRole().build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, targetIndex.getOneBased() + ROLE_EMPTY, expectedCommand);
        assertParseSuccess(parser, targetIndex.getOneBased() + ROLE_EMPTY + "   ", expectedCommand);
    }
}
