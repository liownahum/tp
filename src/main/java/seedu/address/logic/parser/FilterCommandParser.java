package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INSTRUMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBER_TYPE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;

import java.util.HashSet;
import java.util.Set;

import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Group;
import seedu.address.model.person.InstrumentCriterion;
import seedu.address.model.person.MemberType;
import seedu.address.model.person.Role;
import seedu.address.model.person.Status;

/**
 * Parses input arguments and creates a FilterCommand.
 */
public class FilterCommandParser implements Parser<FilterCommand> {

    /**
     * Prefixes accepted by the {@code filter} command.
     */
    private static final Prefix[] FILTER_PREFIXES = {
        PREFIX_STATUS, PREFIX_INSTRUMENT, PREFIX_MEMBER_TYPE, PREFIX_ROLE, PREFIX_GROUP
    };

    /**
     * Parses member criteria, including optional minimum instrument years, and returns a FilterCommand.
     *
     * @throws ParseException if the arguments do not conform to the expected format.
     */
    @Override
    public FilterCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String normalizedArgs = " " + args.trim();
        ArgumentTokenizer.verifyNoUnknownPrefixes(normalizedArgs, FILTER_PREFIXES);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(normalizedArgs, FILTER_PREFIXES);

        boolean hasCriteria = argMultimap.getValue(PREFIX_STATUS).isPresent()
                || argMultimap.getValue(PREFIX_INSTRUMENT).isPresent()
                || argMultimap.getValue(PREFIX_MEMBER_TYPE).isPresent()
                || argMultimap.getValue(PREFIX_ROLE).isPresent()
                || argMultimap.getValue(PREFIX_GROUP).isPresent();
        if (!argMultimap.getPreamble().isEmpty() || !hasCriteria) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
        }

        Set<Status> statuses = new HashSet<>();
        for (String status : argMultimap.getAllValues(PREFIX_STATUS)) {
            statuses.add(ParserUtil.parseStatus(status));
        }
        Set<InstrumentCriterion> instrumentCriteria = new HashSet<>();
        for (String instrument : argMultimap.getAllValues(PREFIX_INSTRUMENT)) {
            instrumentCriteria.add(parseInstrumentCriterion(instrument));
        }
        Set<MemberType> memberTypes = new HashSet<>();
        for (String memberType : argMultimap.getAllValues(PREFIX_MEMBER_TYPE)) {
            memberTypes.add(ParserUtil.parseMemberType(memberType));
        }
        Set<Role> roles = new HashSet<>();
        for (String role : argMultimap.getAllValues(PREFIX_ROLE)) {
            roles.add(ParserUtil.parseRole(role));
        }
        Set<Group> groups = ParserUtil.parseGroups(argMultimap.getAllValues(PREFIX_GROUP));
        return new FilterCommand(statuses, instrumentCriteria, memberTypes, roles, groups);
    }

    /**
     * Parses a name with optional minimum years, defaulting to zero when years are omitted.
     */
    private InstrumentCriterion parseInstrumentCriterion(String instrument) throws ParseException {
        int separatorIndex = instrument.indexOf(":");
        if (separatorIndex == -1) {
            return new InstrumentCriterion(ParserUtil.parseInstrumentName(instrument), 0);
        }

        String name = ParserUtil.parseInstrumentName(instrument.substring(0, separatorIndex));
        String minimumYearsText = instrument.substring(separatorIndex + 1).trim();
        if (!minimumYearsText.matches("\\d+")) {
            throw new ParseException(InstrumentCriterion.MESSAGE_MINIMUM_YEARS_CONSTRAINTS);
        }
        int minimumYears;
        try {
            minimumYears = Integer.parseInt(minimumYearsText);
        } catch (NumberFormatException e) {
            throw new ParseException(InstrumentCriterion.MESSAGE_MINIMUM_YEARS_CONSTRAINTS, e);
        }
        if (!InstrumentCriterion.isValidMinimumYears(minimumYears)) {
            throw new ParseException(InstrumentCriterion.MESSAGE_MINIMUM_YEARS_CONSTRAINTS);
        }
        return new InstrumentCriterion(name, minimumYears);
    }
}
