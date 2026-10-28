package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.storage.JsonAdaptedInstrument.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.Instrument;

public class JsonAdaptedInstrumentTest {
    private static final String VALID_NAME = "Trumpet";
    private static final int VALID_YEARS = 3;

    @Test
    public void canIgnoreBlankEntry_unicodeBlankNameWithValidYears_returnsTrue() {
        for (String name : new String[] {"\u00a0", "\u2007", "\u202f", "\u0085", " \u2002\u00a0\t"}) {
            assertTrue(new JsonAdaptedInstrument(name, VALID_YEARS).canIgnoreBlankEntry());
        }
    }

    @Test
    public void canIgnoreBlankEntry_otherNames_returnsFalse() {
        for (String name : new String[] {null, "", " ", "\t", "\u2002", ":", "\u00a0:",
            VALID_NAME, "French\u00a0Horn"}) {
            assertFalse(new JsonAdaptedInstrument(name, VALID_YEARS).canIgnoreBlankEntry());
        }
    }

    @Test
    public void canIgnoreBlankEntry_validWholeYearsIncludingBoundaries_returnsTrue() {
        for (Number years : new Number[] {0, Instrument.MAX_YEARS, 3L, (short) 3, (byte) 3}) {
            assertTrue(new JsonAdaptedInstrument("\u00a0", years).canIgnoreBlankEntry());
        }
    }

    @Test
    public void canIgnoreBlankEntry_missingOrInvalidYears_returnsFalse() {
        for (Number years : new Number[] {null, -1, Instrument.MAX_YEARS + 1, 3.5, 3.0, 3.0f,
            4294967299L, Long.MIN_VALUE, Long.MAX_VALUE}) {
            assertFalse(new JsonAdaptedInstrument("\u00a0", years).canIgnoreBlankEntry());
        }
    }

    @Test
    public void toModelType_validInstrumentDetails_returnsInstrument() throws Exception {
        Instrument instrument = new Instrument(VALID_NAME, VALID_YEARS);
        assertEquals(instrument, new JsonAdaptedInstrument(instrument).toModelType());
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedInstrument instrument = new JsonAdaptedInstrument(null, VALID_YEARS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "name");
        assertThrows(IllegalValueException.class, expectedMessage, instrument::toModelType);
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedInstrument instrument = new JsonAdaptedInstrument(" ", VALID_YEARS);
        assertThrows(IllegalValueException.class, Instrument.MESSAGE_NAME_CONSTRAINTS, instrument::toModelType);
    }

    @Test
    public void toModelType_nullYears_throwsIllegalValueException() {
        JsonAdaptedInstrument instrument = new JsonAdaptedInstrument(VALID_NAME, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "years");
        assertThrows(IllegalValueException.class, expectedMessage, instrument::toModelType);
    }

    @Test
    public void toModelType_invalidYears_throwsIllegalValueException() {
        JsonAdaptedInstrument instrument = new JsonAdaptedInstrument(VALID_NAME, -1);
        assertThrows(IllegalValueException.class, Instrument.MESSAGE_YEARS_CONSTRAINTS, instrument::toModelType);
    }

    @Test
    public void toModelType_decimalYearsInJson_throwsIllegalValueException() throws Exception {
        assertYearsRejected("3.9"); // would otherwise be truncated to 3
        assertYearsRejected("-0.5"); // would otherwise be truncated to 0
        assertYearsRejected("3.0");
    }

    @Test
    public void toModelType_yearsTooLargeForIntInJson_throwsIllegalValueException() throws Exception {
        assertYearsRejected("4294967299"); // would otherwise wrap around to 3
    }

    @Test
    public void toModelType_wholeYearsInJson_returnsInstrument() throws Exception {
        assertEquals(new Instrument(VALID_NAME, 0), fromJson("0").toModelType());
        assertEquals(new Instrument(VALID_NAME, Instrument.MAX_YEARS),
                fromJson(String.valueOf(Instrument.MAX_YEARS)).toModelType());
    }

    private static JsonAdaptedInstrument fromJson(String years) throws Exception {
        String json = "{\"name\": \"" + VALID_NAME + "\", \"years\": " + years + "}";
        return JsonUtil.fromJsonString(json, JsonAdaptedInstrument.class);
    }

    private static void assertYearsRejected(String years) throws Exception {
        JsonAdaptedInstrument instrument = fromJson(years);
        assertThrows(IllegalValueException.class, Instrument.MESSAGE_YEARS_CONSTRAINTS, instrument::toModelType);
    }

    @Test
    public void toJsonString_validInstrument_usesNameAndYearsKeys() throws Exception {
        String json = JsonUtil.toJsonString(new JsonAdaptedInstrument(new Instrument(VALID_NAME, VALID_YEARS)));
        assertEquals(new Instrument(VALID_NAME, VALID_YEARS),
                JsonUtil.fromJsonString(json, JsonAdaptedInstrument.class).toModelType());
        assertEquals("{\n  \"name\" : \"Trumpet\",\n  \"years\" : 3\n}",
                json.replace("\r\n", "\n"));
    }
}
