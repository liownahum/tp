package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RoleTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Role(null));
    }

    @Test
    public void constructor_blankRole_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Role(""));
        assertThrows(IllegalArgumentException.class, () -> new Role(" \t "));
        assertThrows(IllegalArgumentException.class, () -> new Role("\u00A0"));
        assertThrows(IllegalArgumentException.class, () -> new Role("\u2007\u202F"));
    }

    @Test
    public void constructor_excessInternalWhitespace_collapsedToSingleSpace() {
        assertEquals("Vice President", new Role("  Vice   President ").value);
    }

    @Test
    public void constructor_unicodeWhitespace_collapsedToSingleSpace() {
        assertEquals("Vice President", new Role("Vice\u2003President").value);
        assertEquals("Vice President", new Role("Vice\u00A0\u202FPresident").value);
        assertEquals("Vice President", new Role("Vice\u0085President").value);
    }

    @Test
    public void normalize_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Role.normalize(null));
    }

    @Test
    public void normalize_whitespaceRuns_replacedBySingleSpace() {
        assertEquals("Vice President", Role.normalize("  Vice\u00A0 \t\u2003 President  "));
        assertEquals("Section Leader", Role.normalize("Section   Leader"));
        assertEquals("", Role.normalize("\u00A0"));
        assertEquals("", Role.normalize(" \t\n "));
    }

    @Test
    public void isValidRole_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Role.isValidRole(null));
    }

    @Test
    public void isValidRole_blankOrWhitespaceOnly_returnsFalse() {
        assertFalse(Role.isValidRole(""));
        assertFalse(Role.isValidRole(" "));
        assertFalse(Role.isValidRole("\u00A0"));
        assertFalse(Role.isValidRole("\u2007"));
        assertFalse(Role.isValidRole("\u202F"));
        assertFalse(Role.isValidRole("\u2003\u00A0 "));
        assertFalse(Role.isValidRole("\t\n"));
        assertFalse(Role.isValidRole("\u0085\u3000"));
    }

    @Test
    public void isValidRole_nonBlank_returnsTrue() {
        assertTrue(Role.isValidRole("a"));
        assertTrue(Role.isValidRole(" President "));
        assertTrue(Role.isValidRole("Vice President"));
        assertTrue(Role.isValidRole("Section Leader"));
        assertTrue(Role.isValidRole("Leader / Conductor (2026)"));
    }

    @Test
    public void toString_returnsNormalizedRoleText() {
        assertEquals("Section Leader", new Role("Section Leader").toString());
        assertEquals("Vice President", new Role("  Vice   President ").toString());
    }

    @Test
    public void equals_sameNormalizedValue_returnsTrue() {
        Role role = new Role("Section Leader");

        assertTrue(role.equals(role));
        assertTrue(role.equals(new Role("Section Leader")));
        assertTrue(role.equals(new Role("  Section   Leader  ")));
        assertTrue(new Role("Vice\u2003President").equals(new Role("Vice President")));
    }

    @Test
    public void equals_differentValue_returnsFalse() {
        Role role = new Role("Section Leader");

        assertFalse(role.equals(null));
        assertFalse(role.equals(5.0f));
        assertFalse(role.equals(new Role("Conductor")));
    }

    @Test
    public void hashCode_equalValues_haveEqualHashCode() {
        assertEquals(new Role("Section Leader").hashCode(), new Role("  Section   Leader  ").hashCode());
        assertEquals(new Role("Vice\u2003President").hashCode(), new Role("Vice President").hashCode());
    }
}
