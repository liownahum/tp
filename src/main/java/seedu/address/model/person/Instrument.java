package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an instrument a person plays, with the number of years they have played it.
 * Guarantees: immutable; name is valid as declared in {@link #isValidName(String)};
 * years is valid as declared in {@link #isValidYears(int)}.
 */
public class Instrument {

    public static final int MAX_YEARS = 99;

    public static final String MESSAGE_NAME_CONSTRAINTS =
            "Instrument names should not be blank and should not contain ':'";

    public static final String MESSAGE_YEARS_CONSTRAINTS =
            "Years played should be a whole number from 0 to " + MAX_YEARS;

    public static final String MESSAGE_FORMAT_CONSTRAINTS =
            "Instruments should be in the format NAME:YEARS, e.g. Piano:5";

    public static final String MESSAGE_DUPLICATE_NAMES =
            "Each instrument should only be listed once per person";

    private static final String YEARS_SEPARATOR = ":";

    private final String name;
    private final int years;

    /**
     * Constructs an {@code Instrument}.
     * Surrounding whitespace in the name is removed and inner whitespace is collapsed to single spaces.
     *
     * @param name A valid instrument name.
     * @param years A valid number of years played.
     */
    public Instrument(String name, int years) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_NAME_CONSTRAINTS);
        checkArgument(isValidYears(years), MESSAGE_YEARS_CONSTRAINTS);
        this.name = normalizeName(name);
        this.years = years;
    }

    /**
     * Returns true if the given string is a valid instrument name.
     */
    public static boolean isValidName(String test) {
        requireNonNull(test);
        return !normalizeName(test).isEmpty() && !test.contains(":");
    }

    /**
     * Returns true if the given number is a valid number of years played.
     */
    public static boolean isValidYears(int test) {
        return test >= 0 && test <= MAX_YEARS;
    }

    /**
     * Parses an instrument in {@code NAME:YEARS} form.
     * Whitespace around the name and years is ignored.
     *
     * @throws IllegalArgumentException if the value is not in the expected form,
     *     or its name or years are invalid.
     */
    public static Instrument fromString(String value) {
        requireNonNull(value);
        int separatorIndex = value.indexOf(YEARS_SEPARATOR);
        if (separatorIndex == -1) {
            throw new IllegalArgumentException(MESSAGE_FORMAT_CONSTRAINTS);
        }

        String name = value.substring(0, separatorIndex).trim();
        if (!isValidName(name)) {
            throw new IllegalArgumentException(MESSAGE_NAME_CONSTRAINTS);
        }

        String yearsValue = value.substring(separatorIndex + 1).trim();
        if (!yearsValue.matches("\\d+")) {
            throw new IllegalArgumentException(MESSAGE_YEARS_CONSTRAINTS);
        }

        int years;
        try {
            years = Integer.parseInt(yearsValue);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(MESSAGE_YEARS_CONSTRAINTS, exception);
        }
        if (!isValidYears(years)) {
            throw new IllegalArgumentException(MESSAGE_YEARS_CONSTRAINTS);
        }
        return new Instrument(name, years);
    }

    public String getName() {
        return name;
    }

    public int getYears() {
        return years;
    }

    /**
     * Returns true if this instrument has the given name, ignoring case and extra whitespace.
     */
    public boolean hasName(String otherName) {
        requireNonNull(otherName);
        return toKey(name).equals(toKey(normalizeName(otherName)));
    }

    /**
     * Returns true if two of the given instruments share a name, ignoring case and extra whitespace.
     */
    public static boolean hasDuplicateNames(Collection<Instrument> instruments) {
        requireNonNull(instruments);
        Set<String> seenKeys = new HashSet<>();
        for (Instrument instrument : instruments) {
            if (!seenKeys.add(toKey(instrument.name))) {
                return true;
            }
        }
        return false;
    }

    static String normalizeName(String name) {
        return name.replaceAll("(?U)\\s+", " ").trim();
    }

    private static String toKey(String normalizedName) {
        return normalizedName.toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Instrument otherInstrument)) {
            return false;
        }

        return hasName(otherInstrument.name) && years == otherInstrument.years;
    }

    @Override
    public int hashCode() {
        return Objects.hash(toKey(name), years);
    }

    /**
     * Returns true if this instrument has exactly the same stored name (case-sensitive) and the same
     * number of years as {@code other}. Returns false for {@code null}.
     * Unlike {@link #equals(Object)}, a difference in capitalisation counts as a difference here.
     */
    public boolean isIdenticalTo(Instrument other) {
        if (other == null) {
            return false;
        }
        return name.equals(other.name) && years == other.years;
    }

    /**
     * Formats the instrument for display as its name followed by the years played,
     * e.g. {@code Violin (5 yrs)} or {@code Flute (1 yr)}.
     */
    public String toDisplayString() {
        return name + " (" + years + (years == 1 ? " yr)" : " yrs)");
    }

    /**
     * Formats the instrument in the same {@code NAME:YEARS} form that users type.
     */
    @Override
    public String toString() {
        return name + ":" + years;
    }
}
