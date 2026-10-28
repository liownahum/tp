package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_COUNT;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Instrument;
import seedu.address.model.person.MemberType;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.person.Status;

public class ParserUtilTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_INSTRUMENT = "Violin:abc";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_PHONE = "123456";
    private static final String VALID_EMAIL = "rachel@example.com";
    private static final String VALID_INSTRUMENT_NAME = "Violin";
    private static final String VALID_INSTRUMENT_1 = "Violin:5";
    private static final String VALID_INSTRUMENT_2 = "Piano:2";

    private static final String WHITESPACE = " \t\r\n";

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_outOfRangeInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, ()
            -> ParserUtil.parseIndex(Long.toString(Integer.MAX_VALUE + 1)));
    }

    @Test
    public void parseCount_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_COUNT, () -> ParserUtil.parseCount(""));
        assertThrows(ParseException.class, MESSAGE_INVALID_COUNT, () -> ParserUtil.parseCount("0"));
        assertThrows(ParseException.class, MESSAGE_INVALID_COUNT, () -> ParserUtil.parseCount("000"));
        assertThrows(ParseException.class, MESSAGE_INVALID_COUNT, () -> ParserUtil.parseCount("-1"));
        assertThrows(ParseException.class, MESSAGE_INVALID_COUNT, () -> ParserUtil.parseCount("+1"));
        assertThrows(ParseException.class, MESSAGE_INVALID_COUNT, () -> ParserUtil.parseCount("1 2"));
        assertThrows(ParseException.class, MESSAGE_INVALID_COUNT, () -> ParserUtil.parseCount("one"));
    }

    @Test
    public void parseCount_validInput_success() throws Exception {
        assertEquals(1, ParserUtil.parseCount("1"));
        assertEquals(12, ParserUtil.parseCount("  12  "));
        assertEquals(3, ParserUtil.parseCount("03"));
    }

    @Test
    public void parseCount_tooLargeForInt_returnsMaxValue() throws Exception {
        assertEquals(Integer.MAX_VALUE, ParserUtil.parseCount(Long.toString(Integer.MAX_VALUE + 1L)));
        assertEquals(Integer.MAX_VALUE, ParserUtil.parseCount("99999999999999999999"));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // No whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("1"));

        // Leading and trailing whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("  1  "));
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseName(INVALID_NAME));
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
    }

    @Test
    public void parsePhone_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePhone((String) null));
    }

    @Test
    public void parsePhone_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parsePhone(INVALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithoutWhitespace_returnsPhone() throws Exception {
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(VALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithWhitespace_returnsTrimmedPhone() throws Exception {
        String phoneWithWhitespace = WHITESPACE + VALID_PHONE + WHITESPACE;
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(phoneWithWhitespace));
    }

    @Test
    public void parseEmail_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEmail((String) null));
    }

    @Test
    public void parseEmail_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseEmail(INVALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithoutWhitespace_returnsEmail() throws Exception {
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(VALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithWhitespace_returnsTrimmedEmail() throws Exception {
        String emailWithWhitespace = WHITESPACE + VALID_EMAIL + WHITESPACE;
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(emailWithWhitespace));
    }

    @Test
    public void parseInstrument_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseInstrument(null));
    }

    @Test
    public void parseInstrument_missingYears_throwsParseException() {
        assertThrows(ParseException.class, Instrument.MESSAGE_FORMAT_CONSTRAINTS, ()
            -> ParserUtil.parseInstrument("Violin"));
        assertThrows(ParseException.class, Instrument.MESSAGE_FORMAT_CONSTRAINTS, ()
            -> ParserUtil.parseInstrument(""));
    }

    @Test
    public void parseInstrument_invalidName_throwsParseException() {
        assertThrows(ParseException.class, Instrument.MESSAGE_NAME_CONSTRAINTS, ()
            -> ParserUtil.parseInstrument(":3"));
        assertThrows(ParseException.class, Instrument.MESSAGE_NAME_CONSTRAINTS, ()
            -> ParserUtil.parseInstrument("   :3"));
        assertThrows(ParseException.class, Instrument.MESSAGE_NAME_CONSTRAINTS, ()
            -> ParserUtil.parseInstrument("\u00a0\u2002:3"));
    }

    @Test
    public void parseInstrument_invalidYears_throwsParseException() {
        for (String invalidYears : List.of("", "abc", "-1", "+1", "1.5", "100", "1 2", "3:4",
                "99999999999999999999")) {
            assertThrows(ParseException.class, Instrument.MESSAGE_YEARS_CONSTRAINTS, ()
                -> ParserUtil.parseInstrument("Violin:" + invalidYears));
        }
    }

    @Test
    public void parseInstrument_validValue_returnsInstrument() throws Exception {
        assertEquals(new Instrument(VALID_INSTRUMENT_NAME, 5), ParserUtil.parseInstrument(VALID_INSTRUMENT_1));
        assertEquals(new Instrument("Bass Guitar", 0), ParserUtil.parseInstrument("Bass Guitar:0"));
        assertEquals(new Instrument(VALID_INSTRUMENT_NAME, Instrument.MAX_YEARS),
                ParserUtil.parseInstrument(VALID_INSTRUMENT_NAME + ":" + Instrument.MAX_YEARS));
        assertEquals(new Instrument(VALID_INSTRUMENT_NAME, 7), ParserUtil.parseInstrument("Violin:007"));
    }

    @Test
    public void parseInstrument_validValueWithWhitespace_returnsTrimmedInstrument() throws Exception {
        String instrumentWithWhitespace = WHITESPACE + VALID_INSTRUMENT_NAME + WHITESPACE + ":" + WHITESPACE + "5"
                + WHITESPACE;
        Instrument parsedInstrument = ParserUtil.parseInstrument(instrumentWithWhitespace);
        assertEquals(new Instrument(VALID_INSTRUMENT_NAME, 5), parsedInstrument);
        assertEquals(VALID_INSTRUMENT_NAME, parsedInstrument.getName());
    }

    @Test
    public void parseInstrument_unicodeWhitespaceInName_normalizesName() throws Exception {
        Instrument parsedInstrument = ParserUtil.parseInstrument("\u00a0French\u2002Horn\u202f:3");
        assertEquals(new Instrument("French Horn", 3), parsedInstrument);
        assertEquals("French Horn", parsedInstrument.getName());
    }

    @Test
    public void parseInstruments_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseInstruments(null));
    }

    @Test
    public void parseInstruments_collectionWithInvalidInstruments_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseInstruments(
                List.of(VALID_INSTRUMENT_1, INVALID_INSTRUMENT)));
    }

    @Test
    public void parseInstruments_duplicateNames_throwsParseException() {
        assertThrows(ParseException.class, Instrument.MESSAGE_DUPLICATE_NAMES, ()
            -> ParserUtil.parseInstruments(List.of("Violin:5", "violin:2")));
        assertThrows(ParseException.class, Instrument.MESSAGE_DUPLICATE_NAMES, ()
            -> ParserUtil.parseInstruments(List.of("Violin:5", "VIOLIN:5")));
        assertThrows(ParseException.class, Instrument.MESSAGE_DUPLICATE_NAMES, ()
            -> ParserUtil.parseInstruments(List.of("French Horn:3", "\u00a0french\u2002horn\u202f:5")));
    }

    @Test
    public void parseInstruments_emptyCollection_returnsEmptySet() throws Exception {
        assertTrue(ParserUtil.parseInstruments(List.of()).isEmpty());
    }

    @Test
    public void parseInstruments_collectionWithValidInstruments_returnsInstrumentSet() throws Exception {
        Set<Instrument> actualInstrumentSet = ParserUtil.parseInstruments(List.of(VALID_INSTRUMENT_1,
                VALID_INSTRUMENT_2));
        Set<Instrument> expectedInstrumentSet = Set.of(new Instrument(VALID_INSTRUMENT_NAME, 5),
                new Instrument("Piano", 2));

        assertEquals(expectedInstrumentSet, actualInstrumentSet);
    }

    @Test
    public void parseInstrumentName_invalidValue_throwsParseException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseInstrumentName(null));
        assertThrows(ParseException.class, Instrument.MESSAGE_NAME_CONSTRAINTS, ()
            -> ParserUtil.parseInstrumentName(WHITESPACE));
        assertThrows(ParseException.class, Instrument.MESSAGE_NAME_CONSTRAINTS, ()
            -> ParserUtil.parseInstrumentName(VALID_INSTRUMENT_1));
    }

    @Test
    public void parseInstrumentName_validValue_returnsTrimmedName() throws Exception {
        assertEquals(VALID_INSTRUMENT_NAME, ParserUtil.parseInstrumentName(WHITESPACE + VALID_INSTRUMENT_NAME));
    }

    @Test
    public void parseStatus_nullOrInvalid_throwsException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseStatus(null));
        assertThrows(ParseException.class, Status.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseStatus("alumnus"));
    }

    @Test
    public void parseStatus_validValue_returnsStatus() throws Exception {
        assertEquals(Status.ACTIVE_ALUMNUS, ParserUtil.parseStatus(" ACTIVE-ALUMNUS "));
    }

    @Test
    public void parseMemberType_nullOrInvalid_throwsException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseMemberType(null));
        assertThrows(ParseException.class, MemberType.MESSAGE_CONSTRAINTS, () ->
                ParserUtil.parseMemberType("alumnus"));
    }

    @Test
    public void parseMemberType_validValue_returnsMemberType() throws Exception {
        assertEquals(MemberType.TEACHER, ParserUtil.parseMemberType(" TEACHER "));
    }

    @Test
    public void parseRole_nullOrBlank_throwsException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseRole(null));
        assertThrows(ParseException.class, Role.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseRole(" \t "));
        assertThrows(ParseException.class, Role.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseRole("\u00A0"));
        assertThrows(ParseException.class, Role.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseRole("\u2007\u202F"));
    }

    @Test
    public void parseRole_validValue_returnsTrimmedRole() throws Exception {
        assertEquals(new Role("Section Leader / Conductor"),
                ParserUtil.parseRole("  Section Leader / Conductor  "));
        assertEquals(new Role("Vice President"), ParserUtil.parseRole("  Vice   President  "));
        assertEquals(new Role("Vice President"), ParserUtil.parseRole("Vice\u00A0\u2003 President"));
        assertEquals(new Role("Vice President"), ParserUtil.parseRole(WHITESPACE + "Vice\t President" + WHITESPACE));
    }
}
