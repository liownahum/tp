package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import seedu.address.commons.util.FileUtil;
import seedu.address.model.person.Person;

/**
 * A class that writes supplied member information to a CSV file.
 */
public class CsvExporter {
    public static final String HEADER = ExportField.DEFAULT_FIELDS.stream()
            .map(ExportField::getHeader)
            .collect(Collectors.joining(","));

    /**
     * Exports the given people to a new CSV file in the specified directory.
     *
     * @param people the people to export.
     * @param exportDirectory the directory in which to create the CSV file.
     * @return the path of the written CSV file.
     * @throws IOException if the file cannot be written.
     */
    public static Path export(List<Person> people, Path exportDirectory) throws IOException {
        return export(people, exportDirectory, ExportField.DEFAULT_FIELDS);
    }

    /**
     * Exports the selected columns in the given order to a new CSV file.
     *
     * @param people the people to export, in displayed order.
     * @param exportDirectory the directory in which to create the CSV file.
     * @param fields the non-empty list of distinct columns to export.
     * @return the path of the written CSV file.
     * @throws IllegalArgumentException if the selection is empty or contains repeated fields.
     * @throws IOException if the file cannot be written.
     */
    public static Path export(List<Person> people, Path exportDirectory, List<ExportField> fields) throws IOException {
        requireNonNull(people);
        requireNonNull(exportDirectory);
        List<ExportField> selectedFields = List.copyOf(fields);
        if (selectedFields.isEmpty() || new HashSet<>(selectedFields).size() != selectedFields.size()) {
            throw new IllegalArgumentException("Select at least one export field without repetitions.");
        }

        Path outputPath = getNextAvailablePath(exportDirectory);
        String csvContent = buildCsvContent(people, selectedFields);

        FileUtil.createIfMissing(outputPath);
        FileUtil.writeToFile(outputPath, csvContent);

        return outputPath;
    }

    private static String buildCsvContent(List<Person> people, List<ExportField> fields) {
        StringBuilder csvContent = new StringBuilder(fields.stream()
                .map(ExportField::getHeader)
                .collect(Collectors.joining(",")));

        for (Person person : people) {
            csvContent.append(System.lineSeparator());
            csvContent.append(toCsvRow(person, fields));
        }

        return csvContent.toString();
    }

    private static String toCsvRow(Person person, List<ExportField> fields) {
        return fields.stream()
            .map(field -> escape(field.getValue(person)))
            .collect(Collectors.joining(","));
    }

    private static String escape(String value) {
        String encodedValue = CsvCellCodec.encode(value);
        return "\"" + encodedValue.replace("\"", "\"\"") + "\"";
    }

    private static Path getNextAvailablePath(Path exportDirectory) {
        int exportNumber = 1;

        while (true) {
            Path outputPath = exportDirectory.resolve(
                    String.format("current-results-%02d.csv", exportNumber));

            if (!Files.exists(outputPath)) {
                return outputPath;
            }

            exportNumber++;
        }
    }
}
