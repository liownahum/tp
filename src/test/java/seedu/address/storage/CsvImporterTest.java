package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Group;
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
    public void importPeople_headerWithSurroundingWhitespace_importsPerson() throws Exception {
        Path csvFile = writeCsv(
                " name , phone , email , status , memberType , role , instruments , groups ",
                "Alice Tan,91234567,alice@example.com,member,student,,Violin:1,");

        List<Person> people = CsvImporter.importPeople(csvFile);

        assertEquals(1, people.size());
        assertEquals("Alice Tan", people.getFirst().getName().toString());
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
    public void importPeople_multilineQuotedRole_normalizesNewline() throws Exception {
        Path csvFile = writeCsv(
                HEADER,
                "Alice Tan,91234567,alice@example.com,member,student,\"Section Leader",
                "Conductor\",Violin:1,");

        Person person = CsvImporter.importPeople(csvFile).getFirst();

        assertEquals("Section Leader Conductor", person.getRole().orElseThrow().toString());
    }

    @Test
    public void importPeople_multilineQuotedRoleWithBlankLine_normalizesWhitespace() throws Exception {
        Path csvFile = writeCsv(
                HEADER,
                "Alice Tan,91234567,alice@example.com,member,student,\"Section Leader",
                "",
                "Conductor\",Violin:1,");

        Person person = CsvImporter.importPeople(csvFile).getFirst();
        assertEquals("Section Leader Conductor", person.getRole().orElseThrow().toString());
    }

    @Test
    public void importPeople_invalidRowAfterMultilineRecord_reportsPhysicalLineNumber() throws Exception {
        Path csvFile = writeCsv(
                HEADER,
                "Alice Tan,91234567,alice@example.com,member,student,\"Section Leader",
                "Conductor\",Violin:1,",
                "Bob Lim,98765432,bob@example.com,member,student,,Violin,");
        String expectedMessage = "Invalid value at line 4: " + Instrument.MESSAGE_FORMAT_CONSTRAINTS;

        assertThrows(IllegalValueException.class, expectedMessage, () -> CsvImporter.importPeople(csvFile));
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
    public void importPeople_exportedFormulaPrefixesAndTextPrefixes_roundTrips() throws Exception {
        Person original = new PersonBuilder()
                .withRole("=1+1")
                .withInstruments("+Violin:3", "-Cello:2")
                .withGroups("@SUM(A1)", "=Original")
                .build();
        Path exportedFile = CsvExporter.export(List.of(original), temporaryFolder);

        assertEquals(List.of(original), CsvImporter.importPeople(exportedFile));
    }

    @Test
    public void importPeople_partialExport_rejectsMissingColumns() throws Exception {
        Path exportedFile = CsvExporter.export(List.of(new PersonBuilder().build()), temporaryFolder,
                List.of(ExportField.NAME, ExportField.PHONE, ExportField.INSTRUMENTS));

        assertThrows(IllegalValueException.class, "CSV header must be " + HEADER + ".", () ->
                CsvImporter.importPeople(exportedFile));
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
    public void importPeople_invalidQuotePlacement_throwsIllegalValueException() throws Exception {
        String[] malformedRoles = {
            "\"Con\"ductor", "Con\"ductor", "\"Conductor\" "
        };
        String expectedMessage = "Invalid quote placement at line 2.";

        for (String malformedRole : malformedRoles) {
            Path csvFile = writeCsv(HEADER,
                    "Alice Tan,91234567,alice@example.com,member,student," + malformedRole + ",Violin:1,");
            assertThrows(IllegalValueException.class, expectedMessage, () -> CsvImporter.importPeople(csvFile));
        }
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
        String expectedMessage = "Invalid value at line 2: " + Instrument.MESSAGE_YEARS_CONSTRAINTS;

        assertThrows(IllegalValueException.class, expectedMessage, () -> CsvImporter.importPeople(csvFile));
    }

    @Test
    public void importPeople_nonNumericInstrumentYears_throwsIllegalValueException() throws Exception {
        Path csvFile = writeCsv(HEADER, "Alice Tan,91234567,alice@example.com,member,student,,Violin:many,");
        String expectedMessage = "Invalid value at line 2: " + Instrument.MESSAGE_YEARS_CONSTRAINTS;

        assertThrows(IllegalValueException.class, expectedMessage, () -> CsvImporter.importPeople(csvFile));
    }

    @Test
    public void importPeople_instrumentWithWhitespaceAroundSeparator_importsPerson() throws Exception {
        Path csvFile = writeCsv(HEADER, "Alice Tan,91234567,alice@example.com,member,student,,Violin : 5,");

        Person person = CsvImporter.importPeople(csvFile).getFirst();

        assertEquals(Set.of(new Instrument("Violin", 5)), person.getInstruments());
    }

    @Test
    public void importPeople_instrumentWithSignedYears_throwsIllegalValueException() throws Exception {
        Path csvFile = writeCsv(HEADER, "Alice Tan,91234567,alice@example.com,member,student,,Violin:+5,");
        String expectedMessage = "Invalid value at line 2: " + Instrument.MESSAGE_YEARS_CONSTRAINTS;

        assertThrows(IllegalValueException.class, expectedMessage, () -> CsvImporter.importPeople(csvFile));
    }

    @Test
    public void importPeople_duplicateInstrumentNames_throwsIllegalValueException() throws Exception {
        Path csvFile = writeCsv(HEADER,
                "Alice Tan,91234567,alice@example.com,member,student,,Violin:8;violin:8,");
        String expectedMessage = "Invalid value at line 2: " + Instrument.MESSAGE_DUPLICATE_NAMES;

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

    @Test
    public void importPeople_nonBreakingSpaceOnlyGroup_throwsIllegalValueException() throws Exception {
        Path csvFile = writeCsv(HEADER, "Alice Tan,91234567,alice@example.com,member,student,,Violin:1,\u00A0");
        String expectedMessage = "Invalid value at line 2: " + Group.MESSAGE_CONSTRAINTS;

        assertThrows(IllegalValueException.class, expectedMessage, () -> CsvImporter.importPeople(csvFile));
    }

    @Test
    public void importPeople_blankLineAtEnd_isSkipped() throws Exception {
        Path csvFile = writeCsv(HEADER, "Alice Tan,91234567,alice@example.com,member,student,,Violin:1,", "");

        assertEquals(List.of(new PersonBuilder().withName("Alice Tan").withPhone("91234567")
                .withEmail("alice@example.com").withInstruments("Violin:1").build()),
                CsvImporter.importPeople(csvFile));
    }

    @Test
    public void importPeople_rowOfOnlyCommas_isSkipped() throws Exception {
        Path csvFile = writeCsv(HEADER, "Alice Tan,91234567,alice@example.com,member,student,,Violin:1,", ",,,,,,,,");

        assertEquals(List.of(new PersonBuilder().withName("Alice Tan").withPhone("91234567")
                .withEmail("alice@example.com").withInstruments("Violin:1").build()),
                CsvImporter.importPeople(csvFile));
    }

    @Test
    public void importPeople_blankAndEmptyRows_mixedWithValidData() throws Exception {
        Path csvFile = writeCsv(HEADER,
                "Alice Tan,91234567,alice@example.com,member,student,,Violin:1,",
                "",
                ",,,,,,,",
                "Bob Lim,98765432,bob@example.com,member,student,,Drums:2,",
                "Carol Ng,91112222,carol@example.com,member,teacher,,Cello:5,Choir");

        List<Person> people = CsvImporter.importPeople(csvFile);

        assertEquals(3, people.size());
        assertEquals("Alice Tan", people.get(0).getName().toString());
        assertEquals("Bob Lim", people.get(1).getName().toString());
        assertEquals("Carol Ng", people.get(2).getName().toString());
    }

    @Test
    public void importPeople_nonUtf8File_throwsIllegalValueException() throws Exception {
        Path csvFile = temporaryFolder.resolve("non_utf8.csv");
        String content = HEADER + System.lineSeparator() + "Zoé,12345678,zoe@example.com,member,student,,Violin:1,";

        // Write file using ISO-8859-1 encoding (like Excel's default CSV)
        try (OutputStreamWriter writer = new OutputStreamWriter(
                Files.newOutputStream(csvFile, StandardOpenOption.CREATE),
                java.nio.charset.StandardCharsets.ISO_8859_1)) {
            writer.write(content);
        }

        String expectedMessage = "The file could not be read. Please save it as CSV UTF-8 (not plain CSV).";
        assertThrows(IllegalValueException.class, expectedMessage, () -> CsvImporter.importPeople(csvFile));
    }

    private Path writeCsv(String... lines) throws Exception {
        Path csvFile = temporaryFolder.resolve("members.csv");
        Files.writeString(csvFile, String.join(System.lineSeparator(), lines));
        return csvFile;
    }
}
