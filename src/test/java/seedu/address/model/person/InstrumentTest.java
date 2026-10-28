package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class InstrumentTest {

    private static final List<String> UNICODE_WHITESPACE = List.of(
            "\u0085", "\u00a0", "\u1680", "\u2000", "\u2001", "\u2002", "\u2003", "\u2004", "\u2005", "\u2006",
            "\u2007", "\u2008", "\u2009", "\u200a", "\u2028", "\u2029", "\u202f", "\u205f", "\u3000");

    @Test
    public void constructor_unicodeWhitespace_normalizesNameAndPreservesYears() {
        for (String whitespace : UNICODE_WHITESPACE) {
            Instrument instrument = new Instrument(
                    whitespace + "French" + whitespace + " \t" + whitespace + "Horn" + whitespace, 3);
            assertEquals("French Horn", instrument.getName());
            assertEquals(3, instrument.getYears());
        }
    }

    @Test
    public void constructor_unicodeWhitespaceOnly_throwsIllegalArgumentException() {
        for (String whitespace : UNICODE_WHITESPACE) {
            assertFalse(Instrument.isValidName(whitespace));
            assertThrows(IllegalArgumentException.class,
                    Instrument.MESSAGE_NAME_CONSTRAINTS, () -> new Instrument(" " + whitespace + "\t", 3));
        }
    }

    @Test
    public void equals_unicodeWhitespace_hasSameHashCodeAndDetectsDuplicateNames() {
        Instrument instrument = new Instrument("French Horn", 3);
        for (String whitespace : UNICODE_WHITESPACE) {
            String equivalentName = whitespace + "french" + whitespace + "horn" + whitespace;
            Instrument equivalent = new Instrument(equivalentName, 3);
            assertTrue(instrument.hasName(equivalentName));
            assertEquals(instrument, equivalent);
            assertEquals(equivalent, instrument);
            assertEquals(instrument.hashCode(), equivalent.hashCode());
            assertTrue(Instrument.hasDuplicateNames(List.of(instrument, new Instrument(equivalentName, 5))));
        }
    }

    @Test
    public void constructor_nullName_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Instrument(null, 3));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Instrument("", 3));
        assertThrows(IllegalArgumentException.class, () -> new Instrument("Trumpet:3", 3));
    }

    @Test
    public void constructor_invalidYears_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Instrument("Trumpet", -1));
        assertThrows(IllegalArgumentException.class, () -> new Instrument("Trumpet", Instrument.MAX_YEARS + 1));
    }

    @Test
    public void constructor_extraWhitespaceInName_normalizesName() {
        assertEquals("French Horn", new Instrument("  French   Horn ", 2).getName());
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Instrument.isValidName(null));

        // invalid names
        assertFalse(Instrument.isValidName("")); // empty string
        assertFalse(Instrument.isValidName(" \t ")); // whitespace only
        assertFalse(Instrument.isValidName("Trumpet:3")); // contains the years separator

        // valid names
        assertTrue(Instrument.isValidName("Trumpet"));
        assertTrue(Instrument.isValidName("French Horn")); // with space
        assertTrue(Instrument.isValidName("Bb Clarinet (2nd)")); // with digits and symbols
        assertTrue(Instrument.isValidName("Er-hu"));
    }

    @Test
    public void isValidYears() {
        assertFalse(Instrument.isValidYears(-1));
        assertFalse(Instrument.isValidYears(Instrument.MAX_YEARS + 1));

        assertTrue(Instrument.isValidYears(0)); // boundary
        assertTrue(Instrument.isValidYears(5));
        assertTrue(Instrument.isValidYears(Instrument.MAX_YEARS)); // boundary
    }

    @Test
    public void fromString_invalidValue_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Instrument.MESSAGE_FORMAT_CONSTRAINTS, ()
            -> Instrument.fromString("Violin"));
        assertThrows(IllegalArgumentException.class, Instrument.MESSAGE_NAME_CONSTRAINTS, ()
            -> Instrument.fromString(":5"));
        assertThrows(IllegalArgumentException.class, Instrument.MESSAGE_YEARS_CONSTRAINTS, ()
            -> Instrument.fromString("Violin:+5"));
        assertThrows(IllegalArgumentException.class, Instrument.MESSAGE_YEARS_CONSTRAINTS, ()
            -> Instrument.fromString("Violin:100"));
    }

    @Test
    public void fromString_validValue_returnsInstrument() {
        assertEquals(new Instrument("Violin", 5), Instrument.fromString(" Violin : 5 "));
    }

    @Test
    public void hasName() {
        Instrument instrument = new Instrument("French Horn", 3);

        assertTrue(instrument.hasName("French Horn"));
        assertTrue(instrument.hasName("french horn")); // different case
        assertTrue(instrument.hasName("  FRENCH   horn ")); // extra whitespace

        assertFalse(instrument.hasName("Horn")); // partial name
        assertFalse(instrument.hasName("Trumpet"));
        assertThrows(NullPointerException.class, () -> instrument.hasName(null));
    }

    @Test
    public void equals() {
        Instrument instrument = new Instrument("Trumpet", 3);

        // same values -> returns true
        assertTrue(instrument.equals(new Instrument("Trumpet", 3)));

        // same name in different case -> returns true
        assertTrue(instrument.equals(new Instrument("TRUMPET", 3)));

        // same object -> returns true
        assertTrue(instrument.equals(instrument));

        // null -> returns false
        assertFalse(instrument.equals(null));

        // different types -> returns false
        assertFalse(instrument.equals(5.0f));

        // different name -> returns false
        assertFalse(instrument.equals(new Instrument("Cornet", 3)));

        // different years -> returns false
        assertFalse(instrument.equals(new Instrument("Trumpet", 4)));
    }

    @Test
    public void hashCode_equalInstruments_haveEqualHashCode() {
        assertEquals(new Instrument("Trumpet", 3).hashCode(), new Instrument("trumpet", 3).hashCode());
    }

    @Test
    public void hasDuplicateNames() {
        // null -> throws
        assertThrows(NullPointerException.class, () -> Instrument.hasDuplicateNames(null));

        // no repeated names -> returns false
        assertFalse(Instrument.hasDuplicateNames(List.of()));
        assertFalse(Instrument.hasDuplicateNames(List.of(new Instrument("Trumpet", 3),
                new Instrument("Trombone", 3))));

        // same name with different case, spacing or years -> returns true
        assertTrue(Instrument.hasDuplicateNames(List.of(new Instrument("French Horn", 3),
                new Instrument("french  horn", 5))));
        assertTrue(Instrument.hasDuplicateNames(List.of(new Instrument("Trumpet", 3),
                new Instrument("Trumpet", 3))));
    }

    @Test
    public void toDisplayString_returnsNameAndYearsPlayed() {
        assertEquals("French Horn (3 yrs)", new Instrument("French Horn", 3).toDisplayString());
        assertEquals("Flute (1 yr)", new Instrument("Flute", 1).toDisplayString());
        assertEquals("Cello (0 yrs)", new Instrument("Cello", 0).toDisplayString());
    }

    @Test
    public void toString_returnsNameAndYears() {
        assertEquals("French Horn:3", new Instrument("French Horn", 3).toString());
    }

    @Test
    public void isIdenticalTo_sameNameAndYears_returnsTrue() {
        assertTrue(new Instrument("Violin", 8).isIdenticalTo(new Instrument("Violin", 8)));
    }

    @Test
    public void isIdenticalTo_differentCase_returnsFalse() {
        Instrument violin = new Instrument("Violin", 8);
        Instrument lowercaseViolin = new Instrument("violin", 8);

        // equals remains case-insensitive
        assertTrue(violin.equals(lowercaseViolin));
        assertFalse(violin.isIdenticalTo(lowercaseViolin));
    }

    @Test
    public void isIdenticalTo_differentYears_returnsFalse() {
        assertFalse(new Instrument("Violin", 8).isIdenticalTo(new Instrument("Violin", 9)));
    }

    @Test
    public void isIdenticalTo_null_returnsFalse() {
        assertFalse(new Instrument("Violin", 8).isIdenticalTo(null));
    }
}
