package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Instrument;

/**
 * Jackson-friendly version of {@link Instrument}.
 */
class JsonAdaptedInstrument {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Instrument's %s field is missing!";

    private final String name;

    /*
     * Kept as a Number rather than an Integer so that Jackson does not silently truncate decimals
     * (e.g. 3.9 to 3, or -0.5 to 0) before they can be rejected in toModelType().
     */
    private final Number years;

    /**
     * Constructs a {@code JsonAdaptedInstrument} with the given instrument details.
     */
    @JsonCreator
    public JsonAdaptedInstrument(@JsonProperty("name") String name, @JsonProperty("years") Number years) {
        this.name = name;
        this.years = years;
    }

    /**
     * Converts a given {@code Instrument} into this class for Jackson use.
     */
    public JsonAdaptedInstrument(Instrument source) {
        name = source.getName();
        years = source.getYears();
    }

    /**
     * Returns whether this entry has valid years and a Unicode-blank name accepted by the previous validation.
     */
    public boolean canIgnoreBlankEntry() {
        return name != null && !name.isBlank() && !name.contains(":") && !Instrument.isValidName(name)
                && years != null && isWholeNumber(years) && Instrument.isValidYears(years.intValue());
    }

    /**
     * Converts this Jackson-friendly adapted instrument object into the model's {@code Instrument} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted instrument.
     */
    public Instrument toModelType() throws IllegalValueException {
        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "name"));
        }
        if (!Instrument.isValidName(name)) {
            throw new IllegalValueException(Instrument.MESSAGE_NAME_CONSTRAINTS);
        }

        if (years == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "years"));
        }
        if (!isWholeNumber(years) || !Instrument.isValidYears(years.intValue())) {
            throw new IllegalValueException(Instrument.MESSAGE_YEARS_CONSTRAINTS);
        }

        return new Instrument(name, years.intValue());
    }

    /**
     * Returns true if the given number is a whole number that fits in an {@code int}.
     */
    private static boolean isWholeNumber(Number number) {
        boolean isIntegerType = number instanceof Integer || number instanceof Long
                || number instanceof Short || number instanceof Byte;
        return isIntegerType && number.longValue() == number.intValue();
    }
}
