package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests the escaping used for instrument and group lists inside CSV fields.
 */
public class CsvListCodecTest {

    @Test
    public void escape_separatorAndBackslash_preservesBothCharacters() {
        assertEquals("Band\\;Choir", CsvListCodec.escape("Band;Choir"));
        assertEquals("Back\\\\Stage", CsvListCodec.escape("Back\\Stage"));
    }

    @Test
    public void split_escapedAndOrdinaryValues_restoresOriginalNames() {
        assertEquals(List.of("Band;Choir", "Back\\Stage", "Orchestra"),
                CsvListCodec.split("Band\\;Choir;Back\\\\Stage;Orchestra"));
    }

    @Test
    public void split_otherAndTrailingBackslashes_remainLiteral() {
        assertEquals(List.of("Back\\Stage", "End\\"), CsvListCodec.split("Back\\Stage;End\\"));
    }

    @Test
    public void split_emptyField_returnsOneEmptyValue() {
        assertEquals(List.of(""), CsvListCodec.split(""));
    }
}
