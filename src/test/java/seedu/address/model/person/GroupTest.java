package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Verifies group validation and the comparison rules used by membership sets and filters. */
public class GroupTest {

    private static final List<String> UNICODE_WHITESPACE = List.of(
            "\u0085", "\u00a0", "\u1680", "\u2000", "\u2001", "\u2002", "\u2003", "\u2004", "\u2005", "\u2006",
            "\u2007", "\u2008", "\u2009", "\u200a", "\u2028", "\u2029", "\u202f", "\u205f", "\u3000");

    private static final List<String> FORMAT_CHARACTERS = List.of(
            "\u200b", "\u200c", "\u200d", "\u2060", "\ufeff", "\udb40\udc01");

    @Test
    public void constructor_invisibleOnlyName_rejectsBlankGroup() {
        for (String invisible : FORMAT_CHARACTERS) {
            assertFalse(Group.isValidName(" " + invisible + "\u00a0"));
            assertThrows(IllegalArgumentException.class, Group.MESSAGE_CONSTRAINTS, ()
                -> new Group(" " + invisible + "\u00a0"));
        }
    }

    @Test
    public void equals_invisibleCharacters_normalizesAndDeduplicates() {
        Group expected = new Group("Jazz Band");
        for (String invisible : FORMAT_CHARACTERS) {
            String pastedName = "Ja" + invisible + "zz\u00a0" + invisible + " Band";
            Group actual = new Group(pastedName);
            assertEquals("Jazz Band", actual.getName());
            assertTrue(expected.hasName(pastedName));
            assertEquals(expected, actual);
            assertEquals(expected.hashCode(), actual.hashCode());
            assertEquals(1, new HashSet<>(List.of(expected, actual)).size());
        }
    }

    @Test
    public void constructor_blankName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Group.MESSAGE_CONSTRAINTS, () -> new Group(" \t "));
    }

    @Test
    public void constructor_unicodeWhitespaceName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Group.MESSAGE_CONSTRAINTS, ()
            -> new Group("\u00A0\u2007\u202F"));
    }

    @Test
    public void constructor_extraWhitespace_normalizesName() {
        assertEquals("Chamber Orchestra", new Group("  Chamber   Orchestra  ").getName());
        assertEquals("Chamber Orchestra", new Group("\u00A0Chamber\u2007Orchestra\u202F").getName());
    }

    @Test
    public void constructor_unicodeWhitespace_normalizesName() {
        for (String whitespace : UNICODE_WHITESPACE) {
            assertEquals("Chamber Orchestra",
                    new Group(whitespace + "Chamber" + whitespace + " \t" + whitespace + "Orchestra" + whitespace)
                            .getName());
        }
    }

    @Test
    public void constructor_unicodeWhitespaceOnly_throwsIllegalArgumentException() {
        for (String whitespace : UNICODE_WHITESPACE) {
            assertFalse(Group.isValidName(whitespace));
            assertThrows(IllegalArgumentException.class,
                    Group.MESSAGE_CONSTRAINTS, () -> new Group(" " + whitespace + "\t"));
        }
    }

    @Test
    public void equals_unicodeWhitespace_hasSameHashCodeAndDeduplicates() {
        Group group = new Group("Chamber Orchestra");
        for (String whitespace : UNICODE_WHITESPACE) {
            String equivalentName = whitespace + "chamber" + whitespace + "orchestra" + whitespace;
            Group equivalent = new Group(equivalentName);
            assertTrue(group.hasName(equivalentName));
            assertEquals(group, equivalent);
            assertEquals(equivalent, group);
            assertEquals(group.hashCode(), equivalent.hashCode());
            assertEquals(1, new HashSet<>(List.of(group, equivalent)).size());
        }
    }

    @Test
    public void equals_sameNameIgnoringCaseAndWhitespace_returnsTrue() {
        Group group = new Group("Chamber Orchestra");
        Group equivalent = new Group(" chamber   orchestra ");
        assertTrue(group.equals(group));
        assertTrue(group.equals(equivalent));
        assertEquals(group.hashCode(), equivalent.hashCode());
        assertFalse(group.equals(new Group("Training")));
        assertFalse(group.equals(null));
        assertFalse(group.equals("Chamber Orchestra"));
    }

    @Test
    public void isIdenticalTo_sameName_returnsTrue() {
        assertTrue(new Group("Band").isIdenticalTo(new Group("Band")));
    }

    @Test
    public void isIdenticalTo_differentCase_returnsFalse() {
        Group band = new Group("Band");
        Group lowercaseBand = new Group("band");

        // equals remains case-insensitive
        assertTrue(band.equals(lowercaseBand));
        assertFalse(band.isIdenticalTo(lowercaseBand));
    }

    @Test
    public void isIdenticalTo_null_returnsFalse() {
        assertFalse(new Group("Band").isIdenticalTo(null));
    }
}
