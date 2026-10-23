package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.commons.util.FileUtil;
import seedu.address.model.person.Group;
import seedu.address.model.person.Instrument;
import seedu.address.model.person.Person;

/**
 * A class that writes supplied member information to a CSV file.
 */
public class CsvExporter {
    public static final String HEADER = "name,phone,email,status,memberType,role,instruments,groups";

    /**
     * Exports the given people to a new CSV file in the specified directory.
     *
     * @param people the people to export.
     * @param exportDirectory the directory in which to create the CSV file.
     * @return the path of the written CSV file.
     * @throws IOException if the file cannot be written.
     */
    public static Path export(List<Person> people, Path exportDirectory) throws IOException {
        requireNonNull(people);
        requireNonNull(exportDirectory);

        Path outputPath = getNextAvailablePath(exportDirectory);
        String csvContent = buildCsvContent(people);

        FileUtil.createIfMissing(outputPath);
        FileUtil.writeToFile(outputPath, csvContent);

        return outputPath;
    }

    private static String buildCsvContent(List<Person> people) {
        StringBuilder csvContent = new StringBuilder(HEADER);

        for (Person person : people) {
            csvContent.append(System.lineSeparator());
            csvContent.append(toCsvRow(person));
        }

        return csvContent.toString();
    }

    private static String toCsvRow(Person person) {
        return String.join(",",
            escape(person.getName().toString()),
            escape(person.getPhone().toString()),
            escape(person.getEmail().toString()),
            escape(person.getStatus().toString()),
            escape(person.getMemberType().toString()),
            escape(person.getRole().map(Object::toString).orElse("")),
            escape(formatInstruments(person.getInstruments())),
            escape(formatGroups(person.getGroups())));
    }

    private static String formatInstruments(Set<Instrument> instruments) {
        return instruments.stream()
            .sorted(Comparator.comparing(Instrument::getName, String.CASE_INSENSITIVE_ORDER))
            .map(Instrument::toString)
            .map(CsvListCodec::escape)
            .collect(Collectors.joining(";"));
    }

    private static String formatGroups(Set<Group> groups) {
        return groups.stream()
            .sorted(Comparator.comparing(Group::getName, String.CASE_INSENSITIVE_ORDER))
            .map(Group::toString)
            .map(CsvListCodec::escape)
            .collect(Collectors.joining(";"));
    }

    private static String escape(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
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
