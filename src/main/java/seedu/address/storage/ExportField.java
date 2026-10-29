package seedu.address.storage;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import seedu.address.model.person.Group;
import seedu.address.model.person.Instrument;
import seedu.address.model.person.Person;

/**
 * Supported CSV columns, in the order used by a full export.
 */
public enum ExportField {
    NAME("name", person -> person.getName().toString()),
    PHONE("phone", person -> person.getPhone().toString()),
    EMAIL("email", person -> person.getEmail().toString()),
    STATUS("status", person -> person.getStatus().toString()),
    MEMBER_TYPE("memberType", person -> person.getMemberType().toString()),
    ROLE("role", person -> person.getRole().map(Object::toString).orElse("")),
    INSTRUMENTS("instruments", person -> formatInstruments(person.getInstruments())),
    GROUPS("groups", person -> formatGroups(person.getGroups()));

    public static final List<ExportField> DEFAULT_FIELDS = List.of(values());

    private final String header;
    private final Function<Person, String> valueExtractor;

    ExportField(String header, Function<Person, String> valueExtractor) {
        this.header = header;
        this.valueExtractor = valueExtractor;
    }

    public String getHeader() {
        return header;
    }

    /**
     * Returns the field's value before spreadsheet protection and CSV quoting.
     */
    public String getValue(Person person) {
        return valueExtractor.apply(person);
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
}
