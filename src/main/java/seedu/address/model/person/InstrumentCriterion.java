package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;
import java.util.Objects;

/**
 * An immutable filter criterion matching an instrument name and an inclusive minimum experience.
 */
public final class InstrumentCriterion {

    public static final String MESSAGE_MINIMUM_YEARS_CONSTRAINTS =
            "Minimum years should be a whole number from 0 to " + Instrument.MAX_YEARS;

    private final String name;
    private final int minimumYears;

    /**
     * Creates a criterion with a valid instrument name and minimum years from zero to {@link Instrument#MAX_YEARS}.
     * Surrounding whitespace is removed and inner whitespace in the name is collapsed to single spaces.
     */
    public InstrumentCriterion(String name, int minimumYears) {
        requireNonNull(name);
        checkArgument(Instrument.isValidName(name), Instrument.MESSAGE_NAME_CONSTRAINTS);
        checkArgument(isValidMinimumYears(minimumYears), MESSAGE_MINIMUM_YEARS_CONSTRAINTS);
        this.name = Instrument.normalizeName(name);
        this.minimumYears = minimumYears;
    }

    public String getName() {
        return name;
    }

    public int getMinimumYears() {
        return minimumYears;
    }

    /**
     * Returns whether the minimum years are within the supported range of instrument experience.
     */
    public static boolean isValidMinimumYears(int minimumYears) {
        return Instrument.isValidYears(minimumYears);
    }

    /**
     * Returns whether the instrument matches the whole name, ignoring case and extra whitespace,
     * and has at least the required experience.
     */
    public boolean matches(Instrument instrument) {
        requireNonNull(instrument);
        return instrument.hasName(name) && instrument.getYears() >= minimumYears;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof InstrumentCriterion otherCriterion)) {
            return false;
        }
        return name.toLowerCase(Locale.ROOT).equals(otherCriterion.name.toLowerCase(Locale.ROOT))
                && minimumYears == otherCriterion.minimumYears;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase(Locale.ROOT), minimumYears);
    }
}
