package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CsvCellCodecTest {

    @Test
    public void encode_formulaPrefixes_addsSpreadsheetTextPrefix() {
        assertEquals(" =1+1", CsvCellCodec.encode("=1+1"));
        assertEquals(" +1", CsvCellCodec.encode("+1"));
        assertEquals(" -1", CsvCellCodec.encode("-1"));
        assertEquals(" @SUM(A1)", CsvCellCodec.encode("@SUM(A1)"));
    }

    @Test
    public void encode_safeValue_returnsUnchanged() {
        assertEquals("Conductor", CsvCellCodec.encode("Conductor"));
        assertEquals("", CsvCellCodec.encode(""));
    }

    @Test
    public void decode_encodedValues_restoresOriginalValues() {
        assertEquals("=1+1", CsvCellCodec.decode(" =1+1"));
        assertEquals("+1", CsvCellCodec.decode(" +1"));
        assertEquals("-1", CsvCellCodec.decode(" -1"));
        assertEquals("@SUM(A1)", CsvCellCodec.decode(" @SUM(A1)"));
    }

    @Test
    public void decode_unencodedValue_returnsUnchanged() {
        assertEquals(" Conductor", CsvCellCodec.decode(" Conductor"));
        assertEquals("Conductor", CsvCellCodec.decode("Conductor"));
        assertEquals("", CsvCellCodec.decode(""));
    }
}
