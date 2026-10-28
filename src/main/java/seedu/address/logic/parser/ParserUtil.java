package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Group;
import seedu.address.model.person.Instrument;
import seedu.address.model.person.MemberType;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.person.Status;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";
    public static final String MESSAGE_INVALID_COUNT = "Count must be a positive integer.";

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses {@code count} into a positive number of steps. Leading and trailing whitespaces will be trimmed.
     * A count too large for an {@code int} is returned as {@link Integer#MAX_VALUE}, as it exceeds any history.
     *
     * @throws ParseException if the given {@code count} is not a positive integer.
     */
    public static int parseCount(String count) throws ParseException {
        requireNonNull(count);
        String trimmedCount = count.trim();
        if (!trimmedCount.matches("\\d+") || trimmedCount.matches("0+")) {
            throw new ParseException(MESSAGE_INVALID_COUNT);
        }
        try {
            return Integer.parseInt(trimmedCount);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String instrument} in the form {@code NAME:YEARS} into an {@code Instrument}.
     * Whitespace around the name and the years is ignored.
     *
     * @throws ParseException if the given {@code instrument} is not in the expected form,
     *     or its name or years are invalid.
     */
    public static Instrument parseInstrument(String instrument) throws ParseException {
        requireNonNull(instrument);
        try {
            return Instrument.fromString(instrument);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }

    /**
     * Parses {@code Collection<String> instruments} into a {@code Set<Instrument>}.
     *
     * @throws ParseException if any instrument is invalid, or two instruments share a name.
     */
    public static Set<Instrument> parseInstruments(Collection<String> instruments) throws ParseException {
        requireNonNull(instruments);
        final List<Instrument> instrumentList = new ArrayList<>();
        for (String instrument : instruments) {
            instrumentList.add(parseInstrument(instrument));
        }
        if (Instrument.hasDuplicateNames(instrumentList)) {
            throw new ParseException(Instrument.MESSAGE_DUPLICATE_NAMES);
        }
        return new HashSet<>(instrumentList);
    }

    /**
     * Parses an instrument name without years, trimming surrounding whitespace.
     *
     * @throws ParseException if the name is blank or contains the years separator.
     */
    public static String parseInstrumentName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!Instrument.isValidName(trimmedName)) {
            throw new ParseException(Instrument.MESSAGE_NAME_CONSTRAINTS);
        }
        return trimmedName;
    }

    /**
     * Parses a membership status, ignoring case and surrounding whitespace.
     *
     * @throws ParseException if the status is invalid.
     */
    public static Status parseStatus(String status) throws ParseException {
        requireNonNull(status);
        if (!Status.isValidStatus(status)) {
            throw new ParseException(Status.MESSAGE_CONSTRAINTS);
        }
        return Status.fromString(status);
    }

    /**
     * Parses a member type, ignoring case and surrounding whitespace.
     *
     * @throws ParseException if the member type is invalid.
     */
    public static MemberType parseMemberType(String memberType) throws ParseException {
        requireNonNull(memberType);
        if (!MemberType.isValidMemberType(memberType)) {
            throw new ParseException(MemberType.MESSAGE_CONSTRAINTS);
        }
        return MemberType.fromString(memberType);
    }

    /**
     * Parses a non-blank free-text role.
     * Surrounding and repeated whitespace, including Unicode space separators, is normalized by {@code Role}.
     *
     * @throws ParseException if the role is blank.
     */
    public static Role parseRole(String role) throws ParseException {
        requireNonNull(role);
        if (!Role.isValidRole(role)) {
            throw new ParseException(Role.MESSAGE_CONSTRAINTS);
        }
        return new Role(role);
    }

    /**
     * Parses a non-blank participation group name.
     * Surrounding and repeated whitespace is normalized by {@code Group}.
     */
    public static Group parseGroup(String group) throws ParseException {
        requireNonNull(group);
        if (!Group.isValidName(group)) {
            throw new ParseException(Group.MESSAGE_CONSTRAINTS);
        }
        return new Group(group);
    }

    /**
     * Parses repeated group names, keeping each distinct name only once.
     */
    public static Set<Group> parseGroups(Collection<String> groups) throws ParseException {
        requireNonNull(groups);
        Set<Group> parsedGroups = new HashSet<>();
        for (String group : groups) {
            // Repeated group names represent the same membership, unlike duplicate instrument entries.
            parsedGroups.add(parseGroup(group));
        }
        return parsedGroups;
    }
}
