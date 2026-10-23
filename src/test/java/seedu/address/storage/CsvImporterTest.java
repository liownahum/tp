package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Instrument;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class CsvImporterTest {

    private static final String HEADER = "name,phone,email,status,memberType,role,instruments,groups";

    @TempDir
    public Path temporaryFolder;

    @Test
    public void importPeople_nullPath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> CsvImporter.importPeople(null));
    }

    @Test
    public void importPeople_emptyFile_throwsIllegalValueException() throws Exception {
        Path csvFile = writeCsv("");

        assertThrows(IllegalValueException.class, "CSV file is empty.", () -> CsvImporter.importPeople(csvFile));
    }

    @Test
    public void importPeople_invalidHeader_throwsIllegalValueException() throws Exception {
        Path csvFile = writeCsv("name,phone,email");
        String expectedMessage = "CSV header must be name,phone,email,status,memberType,role,instruments,groups.";

        assertThrows(IllegalValueException.class, expectedMessage, () -> CsvImporter.importPeople(csvFile));
    }

    @Test
    public void importPeople_headerOnly_returnsEmptyList() throws Exception {
        Path csvFile = writeCsv(HEADER);

        assertEquals(List.of(), CsvImporter.importPeople(csvFile));
    }

    @Test
    public void importPeople_utf8ByteOrderMarkBeforeHeader_returnsEmptyList() throws Exception {
        Path csvFile = writeCsv("\ufeff" + HEADER);

        assertEquals(List.of(), CsvImporter.importPeople(csvFile));
    }

    @Test
    public void importPeople_quotedCommaAndEscapedQuote_parsesRole() throws Exception {
        Path csvFile = writeCsv(
                HEADER,
                "Alice Tan,91234567,alice@example.com,member,student,\"Assistant, \"\"Conductor\"\"\",Violin:1,");

        List<Person> people = CsvImporter.importPeople(csvFile);

        assertEquals(1, people.size());
        assertEquals("Assistant, \"Conductor\"", people.getFirst().getRole().orElseThrow().toString());
    }

    @Test
    public void importPeople_exportedNamesWithSemicolonsAndBackslashes_roundTrips() throws Exception {
        Person original = new PersonBuilder()
                .withInstruments("Drums;Percussion:5", "Bass\\Guitar:2")
                .withGroups("Band;Choir", "Back\\Stage")
                .build();
        Path exportedFile = CsvExporter.export(List.of(original), temporaryFolder);

        assertEquals(List.of(original), CsvImporter.importPeople(exportedFile));
    }

    @Test
    public void importPeople_valuesWithSurroundingWhitespace_parsesTrimmedValues() throws Exception {
        Path csvFile = writeCsv(HEADER,
                " Cara Tan , 93456789 , cara@example.com , member , student , Leader , Piano:3 , Band ");

        Person person = CsvImporter.importPeople(csvFile).getFirst();

        assertEquals("Cara Tan", person.getName().toString());
        assertEquals("93456789", person.getPhone().toString());
        assertEquals("cara@example.com", person.getEmail().toString());
        assertEquals("Leader", person.getRole().orElseThrow().toString());
        assertEquals("Piano:3", person.getInstruments().iterator().next().toString());
        assertEquals("Band", person.getGroups().iterator().next().toString());
    }

    @Test
    public void importPeople_unclosedQuotedValue_throwsIllegalValueException() throws Exception {
        Path csvFile = writeCsv(HEADER,
                "Alice Tan,91234567,alice@example.com,member,student,\"Section Leader,Violin:1,");
        String expectedMessage = "Unclosed quoted value at line 2.";

        assertThrows(IllegalValueException.class, expectedMessage, () -> CsvImporter.importPeople(csvFile));
    }

    @Test
    public void importPeople_invalidInstrumentFormat_throwsIllegalValueException() throws Exception {
        Path csvFile = writeCsv(HEADER, "Alice Tan,91234567,alice@example.com,member,student,,Violin,");
        String expectedMessage = "Invalid value at line 2: " + Instrument.MESSAGE_FORMAT_CONSTRAINTS;

        assertThrows(IllegalValueException.class, expectedMessage, () -> CsvImporter.importPeople(csvFile));
    }

    @Test
    public void importPeople_instrumentWithoutYears_throwsIllegalValueException() throws Exception {
        Path csvFile = writeCsv(HEADER, "Alice Tan,91234567,alice@example.com,member,student,,Violin:,");
        String expectedMessage = "Invalid value at line 2: " + Instrument.MESSAGE_FORMAT_CONSTRAINTS;

        assertThrows(IllegalValueException.class, expectedMessage, () -> CsvImporter.importPeople(csvFile));
    }

    @Test
    public void importPeople_nonNumericInstrumentYears_throwsIllegalValueException() throws Exception {
        Path csvFile = writeCsv(HEADER, "Alice Tan,91234567,alice@example.com,member,student,,Violin:many,");
        String expectedMessage = "Invalid value at line 2: " + Instrument.MESSAGE_FORMAT_CONSTRAINTS;

        assertThrows(IllegalValueException.class, expectedMessage, () -> CsvImporter.importPeople(csvFile));
    }

    @Test
    public void importPeople_outOfRangeInstrumentYears_throwsIllegalValueException() throws Exception {
        Path csvFile = writeCsv(HEADER, "Alice Tan,91234567,alice@example.com,member,student,,Violin:100,");
        String expectedMessage = "Invalid value at line 2: " + Instrument.MESSAGE_YEARS_CONSTRAINTS;

        assertThrows(IllegalValueException.class, expectedMessage, () -> CsvImporter.importPeople(csvFile));
    }

    @Test
    public void importPeople_whitespaceOnlyGroup_isTreatedAsEmptyGroupField() throws Exception {
        Path csvFile = writeCsv(HEADER, "Alice Tan,91234567,alice@example.com,member,student,,Violin:1,\" \"");

        Person person = CsvImporter.importPeople(csvFile).getFirst();

        assertEquals(0, person.getGroups().size());
    }

    private Path writeCsv(String... lines) throws Exception {
        Path csvFile = temporaryFolder.resolve("members.csv");
        Files.writeString(csvFile, String.join(System.lineSeparator(), lines));
        return csvFile;
    }
}
