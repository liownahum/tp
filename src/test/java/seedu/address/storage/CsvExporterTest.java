package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.DANIEL;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class CsvExporterTest {
    private static final int CONCURRENT_EXPORT_TIMEOUT_SECONDS = 10;

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
        assertFalse(Files.exists(testFolder.resolve(".current-results-01.csv.reserve")));
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
    public void export_existingReservation_preservesReservationAndUsesNextNumber() throws Exception {
        Path firstReservation = testFolder.resolve(".current-results-01.csv.reserve");
        Files.createFile(firstReservation);

        Path returnedPath = CsvExporter.export(List.of(DANIEL), testFolder);

        assertEquals(testFolder.resolve("current-results-02.csv"), returnedPath);
        assertTrue(Files.exists(firstReservation));
        assertFalse(Files.exists(testFolder.resolve(".current-results-02.csv.reserve")));
    }

    @Test
    public void export_concurrentExports_useDistinctPathsAndPreserveBothResults() throws Exception {
        CountDownLatch exportsReady = new CountDownLatch(2);
        Person amy = new PersonBuilder().build();
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Path> danielExport = executor.submit(() -> CsvExporter.export(
                    waitForBothExports(DANIEL, exportsReady), testFolder, List.of(ExportField.NAME)));
            Future<Path> amyExport = executor.submit(() -> CsvExporter.export(
                    waitForBothExports(amy, exportsReady), testFolder, List.of(ExportField.NAME)));

            Path danielPath = danielExport.get(CONCURRENT_EXPORT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Path amyPath = amyExport.get(CONCURRENT_EXPORT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            assertNotEquals(danielPath, amyPath);
            assertEquals(Set.of(testFolder.resolve("current-results-01.csv"),
                    testFolder.resolve("current-results-02.csv")), Set.of(danielPath, amyPath));
            assertEquals(Set.of(
                    String.join(System.lineSeparator(), "name", "\"Daniel Meier\""),
                    String.join(System.lineSeparator(), "name", "\"Amy Bee\"")),
                    Set.of(Files.readString(danielPath), Files.readString(amyPath)));
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(CONCURRENT_EXPORT_TIMEOUT_SECONDS, TimeUnit.SECONDS));
        }
    }

    @Test
    public void export_encodingFails_doesNotLeaveOutputFile() throws IOException {
        // An unpaired surrogate cannot be encoded as UTF-8, forcing the write to fail.
        Person person = new PersonBuilder().withRole("Role\uD800").build();

        assertThrows(IOException.class, () -> CsvExporter.export(List.of(person), testFolder));

        try (Stream<Path> files = Files.list(testFolder)) {
            assertTrue(files.findAny().isEmpty());
        }
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

    @Test
    public void export_selectedFields_writesOnlyRequestedColumnsInOrder() throws Exception {
        Path outputPath = CsvExporter.export(List.of(DANIEL), testFolder,
                List.of(ExportField.PHONE, ExportField.NAME, ExportField.INSTRUMENTS));

        assertEquals(String.join(System.lineSeparator(), "phone,name,instruments",
                "\"87652533\",\"Daniel Meier\",\"Violin:20\""), Files.readString(outputPath));
    }

    @Test
    public void export_allFieldsReversed_writesEveryFieldInRequestedOrder() throws Exception {
        Person person = new PersonBuilder(DANIEL).withGroups("Orchestra").build();

        Path outputPath = CsvExporter.export(List.of(person), testFolder, ExportField.DEFAULT_FIELDS.reversed());

        assertEquals(String.join(System.lineSeparator(),
                "groups,instruments,role,memberType,status,email,phone,name",
                "\"Orchestra\",\"Violin:20\",\"Conductor\",\"teacher\",\"member\","
                        + "\"cornelia@example.com\",\"87652533\",\"Daniel Meier\""), Files.readString(outputPath));
    }

    @Test
    public void export_singleField_writesOneColumn() throws Exception {
        Path outputPath = CsvExporter.export(List.of(DANIEL), testFolder, List.of(ExportField.EMAIL));

        assertEquals(String.join(System.lineSeparator(), "email", "\"cornelia@example.com\""),
                Files.readString(outputPath));
    }

    @Test
    public void export_selectedOptionalFields_writesEmptyCells() throws Exception {
        Path outputPath = CsvExporter.export(List.of(new PersonBuilder().build()), testFolder,
                List.of(ExportField.ROLE, ExportField.GROUPS, ExportField.INSTRUMENTS));

        assertEquals(String.join(System.lineSeparator(), "role,groups,instruments", "\"\",\"\",\"\""),
                Files.readString(outputPath));
    }

    @Test
    public void export_selectedLists_preservesSortingAndListEscaping() throws Exception {
        Person person = new PersonBuilder()
                .withInstruments("Drums;Percussion:5", "Bass\\Guitar:2")
                .withGroups("Band;Choir", "Back\\Stage").build();

        Path outputPath = CsvExporter.export(List.of(person), testFolder,
                List.of(ExportField.GROUPS, ExportField.INSTRUMENTS));

        assertEquals(String.join(System.lineSeparator(), "groups,instruments",
                "\"Back\\\\Stage;Band\\;Choir\",\"Bass\\\\Guitar:2;Drums\\;Percussion:5\""),
                Files.readString(outputPath));
    }

    @Test
    public void export_selectedRole_preservesCsvQuoting() throws Exception {
        Person person = new PersonBuilder().withRole("Leader, \"Brass\"").build();

        Path outputPath = CsvExporter.export(List.of(person), testFolder, List.of(ExportField.ROLE));

        assertEquals(String.join(System.lineSeparator(), "role", "\"Leader, \"\"Brass\"\"\""),
                Files.readString(outputPath));
    }

    @Test
    public void export_selectedFields_preservesFormulaProtection() throws Exception {
        for (String prefix : List.of("=", "+", "-", "@")) {
            Person person = new PersonBuilder().withRole(prefix + "1")
                    .withGroups(prefix + "Band").withInstruments(prefix + "Violin:3").build();

            Path outputPath = CsvExporter.export(List.of(person), testFolder,
                    List.of(ExportField.GROUPS, ExportField.ROLE, ExportField.INSTRUMENTS));

            assertEquals(String.join(System.lineSeparator(), "groups,role,instruments",
                    "\" " + prefix + "Band\",\" " + prefix + "1\",\" " + prefix + "Violin:3\""),
                    Files.readString(outputPath));
        }
    }

    @Test
    public void export_emptyListWithSelectedFields_writesSelectedHeaderOnly() throws Exception {
        Path outputPath = CsvExporter.export(List.of(), testFolder,
                List.of(ExportField.GROUPS, ExportField.NAME));

        assertEquals("groups,name", Files.readString(outputPath));
    }

    @Test
    public void export_invalidSelection_doesNotCreateDirectoryOrFile() {
        Path exportDirectory = testFolder.resolve("exports");
        assertThrows(IllegalArgumentException.class, () ->
                CsvExporter.export(List.of(DANIEL), exportDirectory, List.of()));
        assertThrows(IllegalArgumentException.class, () ->
                CsvExporter.export(List.of(DANIEL), exportDirectory,
                        List.of(ExportField.NAME, ExportField.NAME)));

        assertFalse(Files.exists(exportDirectory));
    }

    @Test
    public void export_selectedFieldsWithExistingExport_preservesEarlierFileAndNumbering() throws Exception {
        Path firstExport = testFolder.resolve("current-results-01.csv");
        Files.writeString(firstExport, "existing export");

        Path outputPath = CsvExporter.export(List.of(DANIEL), testFolder, List.of(ExportField.NAME));

        assertEquals(testFolder.resolve("current-results-02.csv"), outputPath);
        assertEquals("existing export", Files.readString(firstExport));
        assertEquals(String.join(System.lineSeparator(), "name", "\"Daniel Meier\""), Files.readString(outputPath));
    }

    private List<Person> waitForBothExports(Person person, CountDownLatch exportsReady) {
        return new AbstractList<>() {
            @Override
            public Person get(int index) {
                exportsReady.countDown();
                try {
                    assertTrue(exportsReady.await(CONCURRENT_EXPORT_TIMEOUT_SECONDS, TimeUnit.SECONDS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(e);
                }
                return List.of(person).get(index);
            }

            @Override
            public int size() {
                return 1;
            }
        };
    }
}
