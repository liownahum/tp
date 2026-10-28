package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;
import java.util.Objects;

/**
 * A participation group that a person belongs to.
 * Group names preserve their spelling, but comparison ignores case and extra whitespace.
 */
public class Group {

    public static final String MESSAGE_CONSTRAINTS = "Group names should not be blank";

    private final String name;

    /**
     * Creates a group from a non-blank name.
     */
    public Group(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        this.name = normalizeName(name);
    }

    /**
     * Returns whether a group name contains non-whitespace characters.
     */
    public static boolean isValidName(String name) {
        requireNonNull(name);
        return !normalizeName(name).isEmpty();
    }

    public String getName() {
        return name;
    }

    /**
     * Returns whether the names match, ignoring case and extra whitespace.
     */
    public boolean hasName(String otherName) {
        requireNonNull(otherName);
        return toKey(name).equals(toKey(normalizeName(otherName)));
    }

    private static String normalizeName(String name) {
        return name.replaceAll("(?U)\\s+", " ").trim();
    }

    private static String toKey(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Group otherGroup)) {
            return false;
        }
        return hasName(otherGroup.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(toKey(name));
    }

    /**
     * Returns true if this group has exactly the same stored name (case-sensitive) as {@code other}.
     * Returns false for {@code null}. Unlike {@link #equals(Object)}, a difference in capitalisation
     * counts as a difference here.
     */
    public boolean isIdenticalTo(Group other) {
        if (other == null) {
            return false;
        }
        return name.equals(other.name);
    }

    @Override
    public String toString() {
        return name;
    }
}
