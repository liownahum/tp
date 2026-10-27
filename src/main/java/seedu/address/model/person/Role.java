package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.regex.Pattern;

/**
 * Represents a person's free-text role in HiveMind.
 * Surrounding whitespace is removed and runs of internal whitespace are collapsed to a single space,
 * so e.g. "Vice   President" is stored and compared as "Vice President".
 * Guarantees: immutable; is valid as declared in {@link #isValidRole(String)}.
 */
public class Role {

    public static final String MESSAGE_CONSTRAINTS = "Roles should not be blank";

    /** Matches runs of Unicode whitespace, including ASCII whitespace, U+0085, U+00A0, U+2003 and U+202F. */
    private static final Pattern WHITESPACE = Pattern.compile("\\p{IsWhite_Space}+");

    public final String value;

    /**
     * Constructs a {@code Role}.
     * Surrounding whitespace is removed and repeated internal whitespace is collapsed to a single space.
     *
     * @param role A non-blank role.
     */
    public Role(String role) {
        requireNonNull(role);
        checkArgument(isValidRole(role), MESSAGE_CONSTRAINTS);
        value = normalize(role);
    }

    /**
     * Returns the given role with every run of Unicode whitespace replaced by a single ASCII space,
     * and any leading or trailing spaces stripped.
     */
    public static String normalize(String role) {
        requireNonNull(role);
        return WHITESPACE.matcher(role).replaceAll(" ").trim();
    }

    /**
     * Returns true if the given string contains a non-whitespace character once it is normalized.
     */
    public static boolean isValidRole(String test) {
        requireNonNull(test);
        return !normalize(test).isEmpty();
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Role otherRole)) {
            return false;
        }
        return value.equals(otherRole.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
