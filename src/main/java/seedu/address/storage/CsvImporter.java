package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Group;
import seedu.address.model.person.Instrument;
import seedu.address.model.person.MemberType;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.person.Status;

/**
 * A class that reads member information from a CSV file written by {@link CsvExporter}.
 */
public class CsvImporter {
    private static final List<String> EXPECTED_HEADER = List.of(
            "name", "phone", "email", "status", "memberType", "role", "instruments", "groups");

    private static final int NAME_INDEX = 0;
    private static final int PHONE_INDEX = 1;
    private static final int EMAIL_INDEX = 2;
    private static final int STATUS_INDEX = 3;
    private static final int MEMBER_TYPE_INDEX = 4;
    private static final int ROLE_INDEX = 5;
    private static final int INSTRUMENTS_INDEX = 6;
    private static final int GROUPS_INDEX = 7;
    private static final char UTF_8_BYTE_ORDER_MARK = '\ufeff';

    /**
     * Reads people from a CSV file in HiveMind's export format.
     *
     * @param csvFile the CSV file to read.
     * @return the people represented by the CSV rows.
     * @throws IOException if the file cannot be read.
     * @throws IllegalValueException if the CSV does not match HiveMind's export format or contains invalid data.
     */
    public static List<Person> importPeople(Path csvFile) throws IOException, IllegalValueException {
        requireNonNull(csvFile);

        List<String> lines = Files.readAllLines(csvFile, StandardCharsets.UTF_8);
        if (lines.isEmpty()) {
            throw new IllegalValueException("CSV file is empty.");
        }

        validateHeader(lines.getFirst());

        List<Person> people = new ArrayList<>();
        for (int lineIndex = 1; lineIndex < lines.size(); lineIndex++) {
            people.add(parsePerson(lines.get(lineIndex), lineIndex + 1));
        }
        return List.copyOf(people);
    }

    private static void validateHeader(String header) throws IllegalValueException {
        if (!header.isEmpty() && header.charAt(0) == UTF_8_BYTE_ORDER_MARK) {
            header = header.substring(1);
        }
        if (!parseCsvLine(header, 1).equals(EXPECTED_HEADER)) {
            throw new IllegalValueException("CSV header must be " + String.join(",", EXPECTED_HEADER) + ".");
        }
    }

    private static Person parsePerson(String line, int lineNumber) throws IllegalValueException {
        List<String> fields = parseCsvLine(line, lineNumber);
        if (fields.size() != EXPECTED_HEADER.size()) {
            throw new IllegalValueException("Expected " + EXPECTED_HEADER.size()
                    + " columns at line " + lineNumber + ".");
        }
        fields.replaceAll(String::trim);

        try {
            return new Person(
                    new Name(fields.get(NAME_INDEX)),
                    new Phone(fields.get(PHONE_INDEX)),
                    new Email(fields.get(EMAIL_INDEX)),
                    Status.fromString(fields.get(STATUS_INDEX)),
                    MemberType.fromString(fields.get(MEMBER_TYPE_INDEX)),
                    parseRole(fields.get(ROLE_INDEX)),
                    parseInstruments(fields.get(INSTRUMENTS_INDEX)),
                    parseGroups(fields.get(GROUPS_INDEX)));
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException("Invalid value at line " + lineNumber + ": " + exception.getMessage(),
                    exception);
        }
    }

    private static Optional<Role> parseRole(String role) {
        return role.isEmpty() ? Optional.empty() : Optional.of(new Role(role));
    }

    private static Set<Instrument> parseInstruments(String instrumentField) {
        Set<Instrument> instruments = new HashSet<>();
        if (instrumentField.isEmpty()) {
            return instruments;
        }

        for (String instrumentFieldValue : CsvListCodec.split(instrumentField)) {
            int separatorIndex = instrumentFieldValue.lastIndexOf(':');
            if (separatorIndex <= 0 || separatorIndex == instrumentFieldValue.length() - 1) {
                throw new IllegalArgumentException(Instrument.MESSAGE_FORMAT_CONSTRAINTS);
            }

            String name = instrumentFieldValue.substring(0, separatorIndex);
            int years;
            try {
                years = Integer.parseInt(instrumentFieldValue.substring(separatorIndex + 1));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(Instrument.MESSAGE_FORMAT_CONSTRAINTS, exception);
            }
            instruments.add(new Instrument(name, years));
        }
        return instruments;
    }

    private static Set<Group> parseGroups(String groupField) {
        Set<Group> groups = new HashSet<>();
        if (groupField.isEmpty()) {
            return groups;
        }

        for (String groupName : CsvListCodec.split(groupField)) {
            groups.add(new Group(groupName));
        }
        return groups;
    }

    private static List<String> parseCsvLine(String line, int lineNumber) throws IllegalValueException {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean isInsideQuotes = false;

        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '"') {
                if (isInsideQuotes && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    field.append(character);
                    index++;
                } else {
                    isInsideQuotes = !isInsideQuotes;
                }
            } else if (character == ',' && !isInsideQuotes) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(character);
            }
        }

        if (isInsideQuotes) {
            throw new IllegalValueException("Unclosed quoted value at line " + lineNumber + ".");
        }

        fields.add(field.toString());
        return fields;
    }
}
