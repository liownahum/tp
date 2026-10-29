package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests platform detection used by member-card layout workarounds. */
public class PersonCardPlatformTest {

    @Test
    public void isMacOs_macOsName_returnsTrue() {
        assertTrue(PersonCard.isMacOs("Mac OS X"));
    }

    @Test
    public void isMacOs_otherOsNames_returnsFalse() {
        assertFalse(PersonCard.isMacOs("Windows 11"));
        assertFalse(PersonCard.isMacOs("Linux"));
        assertFalse(PersonCard.isMacOs("FreeBSD"));
        assertFalse(PersonCard.isMacOs(""));
    }
}
