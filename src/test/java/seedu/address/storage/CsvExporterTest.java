package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.TypicalPersons.DANIEL;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class CsvExporterTest {
    @TempDir
    public Path testFolder;

    @Test
    public void export_noExistingExport_createsFirstExport() throws Exception {
        Path outputPath = testFolder.resolve("current-results-01.csv");
        Path exportDirectory = testFolder;
        Path returnedPath = CsvExporter.export(List.of(DANIEL), exportDirectory);

        assertEquals(outputPath, returnedPath);
        assertEquals(String.join(System.lineSeparator(),
                CsvExporter.HEADER,
                "\"Daniel Meier\",\"87652533\",\"cornelia@example.com\","
                        + "\"member\",\"teacher\",\"Conductor\",\"Violin:20\",\"\""),
                Files.readString(outputPath));
    }

    @Test
    public void export_memberWithGroups_writesGroupsAsEighthColumn() throws Exception {
        Person person = new PersonBuilder().withGroups("Training", "Brass Band").build();
        Path outputPath = testFolder.resolve("current-results-01.csv");

        CsvExporter.export(List.of(person), testFolder);

        assertEquals("name,phone,email,status,memberType,role,instruments,groups", CsvExporter.HEADER);
        assertEquals(String.join(System.lineSeparator(),
                CsvExporter.HEADER,
                "\"Amy Bee\",\"85355255\",\"amy@gmail.com\",\"member\",\"student\",\"\",\"\","
                        + "\"Brass Band;Training\""), Files.readString(outputPath));
    }

    @Test
    public void export_roleWithCommaAndQuotes_escapesCsvValue() throws Exception {
        Person person = new PersonBuilder().withRole("Leader, \"Brass\"").build();
        Path outputPath = testFolder.resolve("current-results-01.csv");

        CsvExporter.export(List.of(person), testFolder);

        assertEquals(String.join(System.lineSeparator(),
                CsvExporter.HEADER,
                "\"Amy Bee\",\"85355255\",\"amy@gmail.com\",\"member\",\"student\","
                        + "\"Leader, \"\"Brass\"\"\",\"\",\"\""), Files.readString(outputPath));
    }

    @Test
    public void export_roleWithFormulaPrefix_addsSpreadsheetTextPrefix() throws Exception {
        Person person = new PersonBuilder().withRole("=1+1").build();
        Path outputPath = testFolder.resolve("current-results-01.csv");

        CsvExporter.export(List.of(person), testFolder);

        assertEquals(String.join(System.lineSeparator(),
                CsvExporter.HEADER,
                "\"Amy Bee\",\"85355255\",\"amy@gmail.com\",\"member\",\"student\","
                        + "\" =1+1\",\"\",\"\""), Files.readString(outputPath));
    }

    @Test
    public void export_emptyList_writesHeaderOnly() throws Exception {
        Path outputPath = testFolder.resolve("current-results-01.csv");

        Path returnedPath = CsvExporter.export(List.of(), testFolder);

        assertEquals(outputPath, returnedPath);
        assertEquals(CsvExporter.HEADER, Files.readString(outputPath));
    }

    @Test
    public void export_existingExport_preservesEarlierExport() throws Exception {
        Path firstExport = testFolder.resolve("current-results-01.csv");
        Files.writeString(firstExport, "existing export");

        Path returnedPath = CsvExporter.export(List.of(DANIEL), testFolder);
        Path secondExport = testFolder.resolve("current-results-02.csv");

        assertEquals(secondExport, returnedPath);
        assertEquals("existing export", Files.readString(firstExport));
    }

    @Test
    public void export_numberingGap_usesFirstAvailableNumber() throws Exception {
        Path firstExport = testFolder.resolve("current-results-01.csv");
        Path thirdExport = testFolder.resolve("current-results-03.csv");
        Files.writeString(firstExport, "first export");
        Files.writeString(thirdExport, "third export");

        Path returnedPath = CsvExporter.export(List.of(DANIEL), testFolder);
        Path secondExport = testFolder.resolve("current-results-02.csv");

        assertEquals(secondExport, returnedPath);
        assertEquals("first export", Files.readString(firstExport));
        assertEquals("third export", Files.readString(thirdExport));
    }
}
