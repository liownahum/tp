package seedu.address.storage;

/**
 * Encodes CSV cell values so spreadsheet applications do not interpret user-entered text as formulas.
 */
final class CsvCellCodec {

    private static final String FORMULA_PREFIXES = "=+-@";
    private static final char SPREADSHEET_TEXT_PREFIX = ' ';

    private CsvCellCodec() {}

    /**
     * Prefixes values that spreadsheet applications may interpret as formulas.
     */
    static String encode(String value) {
        if (value.isEmpty()) {
            return value;
        }

        char firstCharacter = value.charAt(0);
        if (FORMULA_PREFIXES.indexOf(firstCharacter) >= 0) {
            return SPREADSHEET_TEXT_PREFIX + value;
        }
        return value;
    }

    /**
     * Restores a value encoded by {@link #encode(String)}.
     */
    static String decode(String value) {
        if (value.length() < 2 || value.charAt(0) != SPREADSHEET_TEXT_PREFIX
                || FORMULA_PREFIXES.indexOf(value.charAt(1)) < 0) {
            return value;
        }
        return value.substring(1);
    }
}
