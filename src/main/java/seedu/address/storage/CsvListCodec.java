package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;

/**
 * Encodes lists stored inside a single CSV field without confusing a semicolon in a name with a separator.
 */
final class CsvListCodec {

    private CsvListCodec() {}

    /**
     * Escapes the list separator and escape character in one list value.
     */
    static String escape(String value) {
        return value.replace("\\", "\\\\").replace(";", "\\;");
    }

    /**
     * Splits a list field, restoring escaped semicolons and backslashes in each value.
     * A backslash before any other character remains literal for manually written CSV files.
     */
    static List<String> split(String field) {
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();

        for (int index = 0; index < field.length(); index++) {
            char current = field.charAt(index);
            if (current == '\\' && index + 1 < field.length()
                    && (field.charAt(index + 1) == '\\' || field.charAt(index + 1) == ';')) {
                value.append(field.charAt(++index));
            } else if (current == ';') {
                values.add(value.toString());
                value.setLength(0);
            } else {
                value.append(current);
            }
        }

        values.add(value.toString());
        return values;
    }
}
