package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class FileUtilTest {

    @Test
    public void isValidWindowsFileName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> FileUtil.isValidWindowsFileName(null));
    }

    @Test
    public void isValidWindowsFileName_invalidCharacters_returnsFalse() {
        String[] invalidFileNames = {
            "bad<name.csv", "bad>name.csv", "bad|name.csv", "bad:name.csv", "bad/name.csv",
            "bad\\name.csv", "bad\"name.csv", "bad?name.csv", "bad*name.csv"
        };

        for (String fileName : invalidFileNames) {
            assertFalse(FileUtil.isValidWindowsFileName(fileName));
        }
    }

    @Test
    public void isValidWindowsFileName_controlCharacter_returnsFalse() {
        String[] invalidFileNames = {
            "bad\0name.csv", "bad\u0001name.csv", "bad\u001Fname.csv"
        };

        for (String fileName : invalidFileNames) {
            assertFalse(FileUtil.isValidWindowsFileName(fileName));
        }
    }

    @Test
    public void isValidWindowsFileName_reservedName_returnsFalse() {
        String[] invalidFileNames = {
            "CON.csv", "prn.csv", "AUX.csv", "nul.csv",
            "COM1.csv", "com9.csv", "COM¹.csv", "LPT1.csv", "lpt9.csv", "LPT³.csv",
            "CON.backup.csv"
        };

        for (String fileName : invalidFileNames) {
            assertFalse(FileUtil.isValidWindowsFileName(fileName));
        }
    }

    @Test
    public void isValidWindowsFileName_emptyOrInvalidEnding_returnsFalse() {
        assertFalse(FileUtil.isValidWindowsFileName(""));
        assertFalse(FileUtil.isValidWindowsFileName("members.csv "));
        assertFalse(FileUtil.isValidWindowsFileName("members.csv."));
    }

    @Test
    public void isValidWindowsFileName_validName_returnsTrue() {
        String[] validFileNames = {
            "members.csv", "CONCERT.csv", "COM10.csv", "LPT0.csv", "my.NUL.csv"
        };

        for (String fileName : validFileNames) {
            assertTrue(FileUtil.isValidWindowsFileName(fileName));
        }
    }
}
