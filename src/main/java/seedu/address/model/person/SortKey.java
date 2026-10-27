package seedu.address.model.person;

import java.util.Comparator;

/**
 * Supported display ordering keys and their comparators.
 */
public enum SortKey {
    NAME("name", Comparator.comparing(person -> person.getName().fullName.replaceAll("\\s+", " "),
            String.CASE_INSENSITIVE_ORDER)),
    STATUS("status", Comparator.comparingInt(person -> statusRank(person.getStatus())));

    private final String keyword;
    private final Comparator<Person> comparator;

    SortKey(String keyword, Comparator<Person> comparator) {
        this.keyword = keyword;
        this.comparator = comparator;
    }

    public String getKeyword() {
        return keyword;
    }

    public Comparator<Person> getComparator() {
        return comparator;
    }

    /**
     * Returns the display rank independently of the Status enum's declaration order.
     */
    private static int statusRank(Status status) {
        return switch (status) {
            case MEMBER -> 0;
            case ACTIVE_ALUMNUS -> 1;
            case INACTIVE_ALUMNUS -> 2;
        };
    }
}
