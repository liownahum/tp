package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.storage.JsonAdaptedPerson.MESSAGE_NULL_INSTRUMENT;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.DANIEL;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
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

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_EMAIL = "example.com";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final String VALID_STATUS = BENSON.getStatus().toString();
    private static final String VALID_MEMBER_TYPE = BENSON.getMemberType().toString();
    private static final List<JsonAdaptedInstrument> VALID_INSTRUMENTS = BENSON.getInstruments().stream()
            .map(JsonAdaptedInstrument::new)
            .collect(Collectors.toList());

    @Test
    public void jsonRoundTrip_preservesRecordIdentity() throws Exception {
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(BENSON));
        Person restored = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();
        assertTrue(BENSON.isSamePerson(restored));
    }

    @Test
    public void toModelType_invalidRecordId_throwsIllegalValueException() {
        for (String id : List.of("not-an-id", "1-1-1-1-1", "")) {
            JsonAdaptedPerson person = new JsonAdaptedPerson(id, VALID_NAME, VALID_PHONE, VALID_EMAIL,
                    VALID_STATUS, VALID_MEMBER_TYPE, null, VALID_INSTRUMENTS, List.of());
            assertThrows(IllegalValueException.class, JsonAdaptedPerson.INVALID_ID_MESSAGE, person::toModelType);
        }
    }

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                adaptedPerson(INVALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_INSTRUMENTS);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person = adaptedPerson(null, VALID_PHONE, VALID_EMAIL, VALID_INSTRUMENTS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                adaptedPerson(VALID_NAME, INVALID_PHONE, VALID_EMAIL, VALID_INSTRUMENTS);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person = adaptedPerson(VALID_NAME, null, VALID_EMAIL, VALID_INSTRUMENTS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                adaptedPerson(VALID_NAME, VALID_PHONE, INVALID_EMAIL, VALID_INSTRUMENTS);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person = adaptedPerson(VALID_NAME, VALID_PHONE, null, VALID_INSTRUMENTS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidInstruments_throwsIllegalValueException() {
        List<JsonAdaptedInstrument> invalidInstruments = new ArrayList<>(VALID_INSTRUMENTS);
        invalidInstruments.add(new JsonAdaptedInstrument("Flute", 100));
        JsonAdaptedPerson person =
                adaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, invalidInstruments);
        assertThrows(IllegalValueException.class, Instrument.MESSAGE_YEARS_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_unicodeBlankInstrument_preservesPersonAndOtherInstruments() throws Exception {
        for (String blank : List.of("\u00a0", "\u2007", "\u202f", "\u0085", " \u2002\u00a0\t")) {
            List<JsonAdaptedInstrument> instruments = List.of(
                    new JsonAdaptedInstrument(blank, 3), new JsonAdaptedInstrument("Cello", 5));
            JsonAdaptedPerson person = adaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, instruments);
            assertEquals(new PersonBuilder(BENSON).withoutRole().withInstruments("Cello:5").build(),
                    person.toModelType());
        }
    }

    @Test
    public void toModelType_onlyUnicodeBlankInstrument_preservesPersonWithoutInstruments() throws Exception {
        JsonAdaptedPerson person = adaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                List.of(new JsonAdaptedInstrument("\u00a0", 3)));
        assertEquals(new PersonBuilder(BENSON).withoutRole().withInstruments().build(), person.toModelType());
    }

    @Test
    public void toModelType_invalidBlankInstrument_throwsIllegalValueException() {
        for (String blank : List.of("", " ", "\t", "\u2002", ":")) {
            JsonAdaptedPerson person = adaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                    List.of(new JsonAdaptedInstrument(blank, 3)));
            assertThrows(IllegalValueException.class, Instrument.MESSAGE_NAME_CONSTRAINTS, person::toModelType);
        }
        for (Number years : new Number[] {null, -1, 100, 3.5, 4294967299L}) {
            JsonAdaptedPerson person = adaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                    List.of(new JsonAdaptedInstrument("\u00a0", years)));
            assertThrows(IllegalValueException.class, person::toModelType);
        }
    }

    @Test
    public void toModelType_unicodeDuplicateNames_keepsMaximumYearsInEitherOrder() throws Exception {
        for (String whitespace : List.of("\u00a0", "\u2002", "\u202f", "\u0085")) {
            JsonAdaptedInstrument lower = new JsonAdaptedInstrument("Bass Guitar", 3);
            JsonAdaptedInstrument higher = new JsonAdaptedInstrument("BASS" + whitespace + "GUITAR", 5);
            for (List<JsonAdaptedInstrument> instruments : List.of(List.of(lower, higher), List.of(higher, lower))) {
                JsonAdaptedPerson person = adaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, instruments);
                assertEquals(Set.of(new Instrument("Bass Guitar", 5)), person.toModelType().getInstruments());
            }
        }
    }

    @Test
    public void toModelType_duplicateInstrumentNames_keepsMaximumYears() throws Exception {
        List<JsonAdaptedInstrument> duplicateInstruments = List.of(
                new JsonAdaptedInstrument("Violin", 4), new JsonAdaptedInstrument("violin", 2));
        JsonAdaptedPerson person =
                adaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, duplicateInstruments);
        assertEquals(Set.of(new Instrument("Violin", 4)), person.toModelType().getInstruments());
    }

    @Test
    public void toModelType_asciiWhitespaceDuplicateNames_keepsMaximumYears() throws Exception {
        JsonAdaptedPerson person = adaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, List.of(
                new JsonAdaptedInstrument("Bass Guitar", 3), new JsonAdaptedInstrument(" BASS \t GUITAR ", 5)));
        assertEquals(Set.of(new Instrument("Bass Guitar", 5)), person.toModelType().getInstruments());
    }

    @Test
    public void toModelType_repeatedIdenticalInstrument_keepsOneEntry() throws Exception {
        // same name and years, in the same or a different case, is still a repeated instrument
        for (String repeatedName : List.of("Violin", "violin")) {
            List<JsonAdaptedInstrument> repeatedInstruments = List.of(
                    new JsonAdaptedInstrument("Violin", 4), new JsonAdaptedInstrument(repeatedName, 4));
            JsonAdaptedPerson person =
                    adaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, repeatedInstruments);
            assertEquals(Set.of(new Instrument("Violin", 4)), person.toModelType().getInstruments());
        }
    }

    @Test
    public void toModelType_nullInstruments_returnsPersonWithoutInstruments() throws Exception {
        JsonAdaptedPerson person = adaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, null);
        assertEquals(Set.of(), person.toModelType().getInstruments());
    }

    @Test
    public void toModelType_nullInstrument_throwsIllegalValueException() {
        List<JsonAdaptedInstrument> instruments = new ArrayList<>(VALID_INSTRUMENTS);
        instruments.add(null);
        JsonAdaptedPerson person = adaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, instruments);

        assertThrows(IllegalValueException.class, MESSAGE_NULL_INSTRUMENT, person::toModelType);
    }

    @Test
    public void toModelType_missingGroups_returnsPersonWithoutGroups() throws Exception {
        String json = "{\"name\":\"" + VALID_NAME + "\",\"phone\":\"" + VALID_PHONE
                + "\",\"email\":\"" + VALID_EMAIL + "\",\"status\":\"" + VALID_STATUS
                + "\",\"memberType\":\"" + VALID_MEMBER_TYPE + "\"}";
        JsonAdaptedPerson person = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
        assertEquals(Set.of(), person.toModelType().getGroups());
    }

    @Test
    public void toModelType_blankGroup_throwsIllegalValueException() {
        for (String blank : List.of("", " ", "\t", "\u2002")) {
            JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                    VALID_STATUS, VALID_MEMBER_TYPE, null, VALID_INSTRUMENTS, List.of(blank));
            assertThrows(IllegalValueException.class, Group.MESSAGE_CONSTRAINTS, person::toModelType);
        }
    }

    @Test
    public void toModelType_legacyBlankGroups_preservesPersonAndValidGroups() throws Exception {
        for (String blank : List.of("\u00a0", "\u2007", "\u202f", "\u0085", " \u2002\u00a0\t")) {
            JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                    VALID_STATUS, VALID_MEMBER_TYPE, null, VALID_INSTRUMENTS,
                    List.of(blank, "Chamber\u00a0Orchestra", "Training"));
            Person expected = new PersonBuilder(BENSON).withoutRole()
                    .withGroups("Chamber Orchestra", "Training").build();
            assertEquals(expected, person.toModelType());
        }
    }

    @Test
    public void toModelType_onlyLegacyBlankGroups_preservesPersonWithoutGroups() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                VALID_STATUS, VALID_MEMBER_TYPE, null, VALID_INSTRUMENTS, List.of("\u00a0", "\u202f"));
        assertEquals(new PersonBuilder(BENSON).withoutRole().withGroups().build(), person.toModelType());
    }

    @Test
    public void jsonRoundTrip_legacyUnicodeBlankGroup_keepsMemberAndOtherGroups() throws Exception {
        Person expected = new PersonBuilder(BENSON).withGroups("Orchestra").build();
        for (String whitespace : List.of("\u00a0", "\u0085", "\u2007\u202f")) {
            String json = JsonUtil.toJsonString(new JsonAdaptedPerson(expected))
                    .replace("\"Orchestra\"", "\"" + whitespace + "\", \"Orchestra\"");
            Person restored = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();
            assertEquals(expected, restored);
            assertTrue(expected.isSamePerson(restored));
        }
    }

    @Test
    public void toModelType_nullGroup_throwsIllegalValueException() {
        List<String> groups = new ArrayList<>();
        groups.add(null);
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                VALID_STATUS, VALID_MEMBER_TYPE, null, VALID_INSTRUMENTS, groups);
        assertThrows(IllegalValueException.class, Group.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void jsonRoundTrip_preservesGroups() throws Exception {
        Person groupedPerson = new PersonBuilder(BENSON)
                .withGroups("Chamber Orchestra", "Training").build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(groupedPerson));
        JsonAdaptedPerson restored = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
        assertEquals(groupedPerson, restored.toModelType());
    }

    @Test
    public void toModelType_legacyTagsField_ignoresTags() throws Exception {
        String json = "{\"name\": \"" + VALID_NAME + "\", \"phone\": \"" + VALID_PHONE + "\", "
                + "\"email\": \"" + VALID_EMAIL + "\", \"status\": \"" + VALID_STATUS + "\", "
                + "\"memberType\": \"" + VALID_MEMBER_TYPE + "\", \"tags\": [\"friends\"]}";
        JsonAdaptedPerson person = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
        assertEquals(Set.of(), person.toModelType().getInstruments());
    }

    @Test
    public void toModelType_nullStatus_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                null, VALID_MEMBER_TYPE, null, VALID_INSTRUMENTS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Status.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidStatus_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                "alumnus", VALID_MEMBER_TYPE, null, VALID_INSTRUMENTS);
        assertThrows(IllegalValueException.class, Status.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_nullMemberType_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                VALID_STATUS, null, null, VALID_INSTRUMENTS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, MemberType.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidMemberType_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                VALID_STATUS, "alumnus", null, VALID_INSTRUMENTS);
        assertThrows(IllegalValueException.class, MemberType.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_blankRole_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                VALID_STATUS, VALID_MEMBER_TYPE, "  ", VALID_INSTRUMENTS);
        assertThrows(IllegalValueException.class, Role.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_unicodeSpaceOnlyRole_returnsPersonWithoutRole() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                VALID_STATUS, VALID_MEMBER_TYPE, "\u00A0\u2007", VALID_INSTRUMENTS);
        assertTrue(person.toModelType().getRole().isEmpty());
    }

    @Test
    public void toModelType_validRole_returnsPersonWithRole() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                VALID_STATUS, VALID_MEMBER_TYPE, "Section Leader", VALID_INSTRUMENTS);
        assertEquals(new Role("Section Leader"), person.toModelType().getRole().orElseThrow());
    }

    @Test
    public void jsonRoundTrip_includesMembershipFields() throws Exception {
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(BENSON));
        JsonAdaptedPerson restored = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
        assertEquals(BENSON, restored.toModelType());
    }

    @Test
    public void jsonRoundTrip_includesOptionalRole() throws Exception {
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(DANIEL));
        JsonAdaptedPerson restored = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
        assertEquals(DANIEL, restored.toModelType());
    }

    private static JsonAdaptedPerson adaptedPerson(String name, String phone, String email,
            List<JsonAdaptedInstrument> instruments) {
        return new JsonAdaptedPerson(name, phone, email, VALID_STATUS, VALID_MEMBER_TYPE, null, instruments);
    }

}
