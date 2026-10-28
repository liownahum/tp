package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INSTRUMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBER_TYPE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Group;
import seedu.address.model.person.Instrument;
import seedu.address.model.person.Role;

/**
 * Parses input arguments and creates a new EditCommand object
 */
public class EditCommandParser implements Parser<EditCommand> {

    /** Prefixes accepted by the {@code edit} command, which accepts every HiveMind prefix. */
    private static final Prefix[] EDIT_PREFIXES = CliSyntax.ALL_PREFIXES;

    /**
     * Matches an optionally signed integer, e.g. {@code 1}, {@code -3} or {@code +1}.
     * Any Unicode decimal digit is allowed, as {@link ParserUtil#parseIndex(String)} accepts them too.
     */
    private static final String SIGNED_INTEGER_REGEX = "[+-]?\\p{Nd}+";

    /**
     * Parses the given {@code String} of arguments in the context of the EditCommand
     * and returns an EditCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public EditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentTokenizer.verifyNoUnknownPrefixes(args, EDIT_PREFIXES);

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, EDIT_PREFIXES);
        Index index = parseIndexForEdit(argMultimap.getPreamble());

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_STATUS,
                PREFIX_MEMBER_TYPE, PREFIX_ROLE);

        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();

        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            editPersonDescriptor.setName(ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get()));
        }
        if (argMultimap.getValue(PREFIX_PHONE).isPresent()) {
            editPersonDescriptor.setPhone(ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get()));
        }
        if (argMultimap.getValue(PREFIX_EMAIL).isPresent()) {
            editPersonDescriptor.setEmail(ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get()));
        }
        if (argMultimap.getValue(PREFIX_STATUS).isPresent()) {
            editPersonDescriptor.setStatus(ParserUtil.parseStatus(argMultimap.getValue(PREFIX_STATUS).get()));
        }
        if (argMultimap.getValue(PREFIX_MEMBER_TYPE).isPresent()) {
            editPersonDescriptor.setMemberType(
                    ParserUtil.parseMemberType(argMultimap.getValue(PREFIX_MEMBER_TYPE).get()));
        }
        if (argMultimap.getValue(PREFIX_ROLE).isPresent()) {
            editPersonDescriptor.setRole(parseRoleForEdit(argMultimap.getValue(PREFIX_ROLE).get()));
        }
        parseInstrumentsForEdit(argMultimap.getAllValues(PREFIX_INSTRUMENT))
                .ifPresent(editPersonDescriptor::setInstruments);
        parseGroupsForEdit(argMultimap.getAllValues(PREFIX_GROUP))
                .ifPresent(editPersonDescriptor::setGroups);

        if (!editPersonDescriptor.isAnyFieldEdited()) {
            throw new ParseException(EditCommand.MESSAGE_NOT_EDITED);
        }

        return new EditCommand(index, editPersonDescriptor);
    }

    /**
     * Parses the {@code preamble} of the {@code edit} command into the index of the person to edit.
     *
     * @throws ParseException with the generic invalid-format message if {@code preamble} is not an integer at all,
     *     with {@code MESSAGE_INVALID_PERSON_DISPLAYED_INDEX} if it is a positive integer too large to be an index,
     *     or with {@code ParserUtil.MESSAGE_INVALID_INDEX} if it is any other integer that is not a positive index.
     */
    private Index parseIndexForEdit(String preamble) throws ParseException {
        String trimmedPreamble = preamble.trim();
        if (!trimmedPreamble.matches(SIGNED_INTEGER_REGEX)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        }
        if (isTooLargeForInt(trimmedPreamble) && !trimmedPreamble.startsWith("-")) {
            throw new ParseException(MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        return ParserUtil.parseIndex(trimmedPreamble);
    }

    /**
     * Returns true if the integer {@code number} does not fit in an {@code int}.
     */
    private static boolean isTooLargeForInt(String number) {
        try {
            Integer.parseInt(number);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    /**
     * Parses {@code role} into the role to edit to. An empty {@code role} clears the person's role.
     *
     * @throws ParseException if the given {@code role} is invalid.
     */
    private Optional<Role> parseRoleForEdit(String role) throws ParseException {
        requireNonNull(role);
        if (role.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ParserUtil.parseRole(role));
    }

    /**
     * Parses {@code Collection<String> instruments} into a {@code Set<Instrument>} if {@code instruments}
     * is non-empty.
     * If {@code instruments} contains only one element which is an empty string, it will be parsed into a
     * {@code Set<Instrument>} containing zero instruments.
     */
    private Optional<Set<Instrument>> parseInstrumentsForEdit(Collection<String> instruments)
            throws ParseException {
        requireNonNull(instruments);

        if (instruments.isEmpty()) {
            return Optional.empty();
        }
        Collection<String> instrumentSet = instruments.size() == 1 && instruments.contains("")
                ? Collections.emptySet()
                : instruments;
        return Optional.of(ParserUtil.parseInstruments(instrumentSet));
    }

    /**
     * Parses replacement groups. One empty {@code g/} clears all groups; a blank group
     * mixed with named groups is invalid.
     */
    private Optional<Set<Group>> parseGroupsForEdit(Collection<String> groups) throws ParseException {
        requireNonNull(groups);
        if (groups.isEmpty()) {
            return Optional.empty();
        }
        Collection<String> replacementGroups = groups.size() == 1 && groups.contains("")
                ? Collections.emptySet() : groups;
        return Optional.of(ParserUtil.parseGroups(replacementGroups));
    }

}
