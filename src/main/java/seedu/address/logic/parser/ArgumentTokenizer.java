package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import seedu.address.logic.Messages;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Tokenizes arguments string of the form: {@code preamble <prefix>value <prefix>value ...}<br>
 *     e.g. {@code some preamble text t/ 11.00 t/12.00 k/ m/ July}  where prefixes are {@code t/ k/ m/}.<br>
 * 1. An argument's value can be an empty string e.g. the value of {@code k/} in the above example.<br>
 * 2. Leading and trailing whitespaces of an argument value will be discarded.<br>
 * 3. An argument may be repeated and all its values will be accumulated e.g. the value of {@code t/}
 *    in the above example.<br>
 */
public class ArgumentTokenizer {

    /** A token that looks like a prefix: one or two ASCII letters and a slash, e.g. {@code n/} or {@code TY/}. */
    private static final Pattern PREFIX_LIKE_TOKEN = Pattern.compile("[A-Za-z]{1,2}/");

    /**
     * Tokenizes an arguments string and returns an {@code ArgumentMultimap} object that maps prefixes to their
     * respective argument values. Only the given prefixes will be recognized in the arguments string.
     *
     * @param argsString Arguments string of the form: {@code preamble <prefix>value <prefix>value ...}
     * @param prefixes   Prefixes to tokenize the arguments string with
     * @return           ArgumentMultimap object that maps prefixes to their arguments
     */
    public static ArgumentMultimap tokenize(String argsString, Prefix... prefixes) {
        List<PrefixPosition> positions = findAllPrefixPositions(argsString, prefixes);
        return extractArguments(argsString, positions);
    }

    /**
     * Verifies that {@code argsString} does not contain a prefix that the user most likely mistyped.
     * <p>
     * A token is a candidate if it starts the string or follows a space, and is one or two ASCII letters followed by
     * {@code /}. A candidate that is exactly one of {@code prefixes} is accepted. Otherwise it is rejected if
     * <ul>
     *     <li>it is a HiveMind prefix that this command does not accept, or</li>
     *     <li>it appears before the first accepted prefix, where it would otherwise be read as part of the preamble,
     *     and it is an accepted prefix in the wrong case (e.g. {@code S/}) or one of the prefixes removed from the
     *     original AddressBook, {@code t/} or {@code a/}.</li>
     * </ul>
     * Inside values, such tokens are kept as text, as values such as {@code N/A}, {@code a/v} or {@code c/o} are common
     * and cannot be told apart from a mistyped prefix.
     *
     * @param argsString Arguments string of the form: {@code preamble <prefix>value <prefix>value ...}
     * @param prefixes   Prefixes accepted by the command that is parsing {@code argsString}
     * @throws ParseException if an unknown prefix is found, naming the first unknown prefix by position
     */
    public static void verifyNoUnknownPrefixes(String argsString, Prefix... prefixes) throws ParseException {
        requireNonNull(argsString);
        requireNonNull(prefixes);

        Set<String> acceptedPrefixes = Arrays.stream(prefixes)
                .map(Prefix::getPrefix)
                .collect(Collectors.toSet());
        String acceptedPrefixList = Arrays.stream(prefixes)
                .map(Prefix::getPrefix)
                .collect(Collectors.joining(" "));

        boolean isInPreamble = true;
        Matcher matcher = PREFIX_LIKE_TOKEN.matcher(argsString);
        while (matcher.find()) {
            // the tokenizer only recognises a prefix that follows a space, so other characters do not start a token
            boolean isAtStartOfWord = matcher.start() == 0 || argsString.charAt(matcher.start() - 1) == ' ';
            if (!isAtStartOfWord) {
                continue;
            }

            String token = matcher.group();
            if (acceptedPrefixes.contains(token)) {
                isInPreamble = false;
                continue;
            }
            if (isUnknownPrefix(token, isInPreamble)) {
                throw new ParseException(
                        String.format(Messages.MESSAGE_UNKNOWN_PREFIX, token, acceptedPrefixList));
            }
        }
    }

    /**
     * Returns true if {@code token}, which is not exactly an accepted prefix, should be rejected as an unknown prefix.
     *
     * @see #verifyNoUnknownPrefixes(String, Prefix...)
     */
    private static boolean isUnknownPrefix(String token, boolean isInPreamble) {
        if (containsPrefix(CliSyntax.REMOVED_PREFIXES, token)) {
            return isInPreamble;
        }
        String lowercaseToken = token.toLowerCase(Locale.ROOT);
        if (!containsPrefix(CliSyntax.ALL_PREFIXES, lowercaseToken)) {
            return false;
        }
        // a HiveMind prefix typed exactly is the real prefix, so the command does not accept it;
        // a wrong-case one may be ordinary text such as N/A, unless it would be read as part of the preamble
        boolean isExactPrefix = token.equals(lowercaseToken);
        return isExactPrefix || isInPreamble;
    }

    /**
     * Returns true if any of the given {@code prefixes} has the prefix text {@code prefixText}.
     */
    private static boolean containsPrefix(Prefix[] prefixes, String prefixText) {
        return Arrays.stream(prefixes).anyMatch(prefix -> prefix.getPrefix().equals(prefixText));
    }

    /**
     * Finds all zero-based prefix positions in the given arguments string.
     *
     * @param argsString Arguments string of the form: {@code preamble <prefix>value <prefix>value ...}
     * @param prefixes   Prefixes to find in the arguments string
     * @return           List of zero-based prefix positions in the given arguments string
     */
    private static List<PrefixPosition> findAllPrefixPositions(String argsString, Prefix... prefixes) {
        return Arrays.stream(prefixes)
                .flatMap(prefix -> findPrefixPositions(argsString, prefix).stream())
                .collect(Collectors.toList());
    }

    /**
     * @see #findAllPrefixPositions(String, Prefix...)
     */
    private static List<PrefixPosition> findPrefixPositions(String argsString, Prefix prefix) {
        List<PrefixPosition> positions = new ArrayList<>();

        int prefixPosition = findPrefixPosition(argsString, prefix.getPrefix(), 0);
        while (prefixPosition != -1) {
            PrefixPosition extendedPrefix = new PrefixPosition(prefix, prefixPosition);
            positions.add(extendedPrefix);
            prefixPosition = findPrefixPosition(argsString, prefix.getPrefix(), prefixPosition);
        }

        return positions;
    }

    /**
     * Returns the index of the first occurrence of {@code prefix} in
     * {@code argsString} starting from index {@code fromIndex}. An occurrence
     * is valid if there is a whitespace before {@code prefix}. Returns -1 if no
     * such occurrence can be found.
     *
     * E.g if {@code argsString} = "e/hip/900", {@code prefix} = "p/" and
     * {@code fromIndex} = 0, this method returns -1 as there are no valid
     * occurrences of "p/" with whitespace before it. However, if
     * {@code argsString} = "e/hi p/900", {@code prefix} = "p/" and
     * {@code fromIndex} = 0, this method returns 5.
     */
    private static int findPrefixPosition(String argsString, String prefix, int fromIndex) {
        int prefixIndex = argsString.indexOf(" " + prefix, fromIndex);
        return prefixIndex == -1 ? -1
                : prefixIndex + 1; // +1 as offset for whitespace
    }

    /**
     * Extracts prefixes and their argument values, and returns an {@code ArgumentMultimap} object that maps the
     * extracted prefixes to their respective arguments. Prefixes are extracted based on their zero-based positions in
     * {@code argsString}.
     *
     * @param argsString      Arguments string of the form: {@code preamble <prefix>value <prefix>value ...}
     * @param prefixPositions Zero-based positions of all prefixes in {@code argsString}
     * @return                ArgumentMultimap object that maps prefixes to their arguments
     */
    private static ArgumentMultimap extractArguments(String argsString, List<PrefixPosition> prefixPositions) {

        // Sort by start position
        prefixPositions.sort((prefix1, prefix2) -> prefix1.getStartPosition() - prefix2.getStartPosition());

        // Insert a PrefixPosition to represent the preamble
        PrefixPosition preambleMarker = new PrefixPosition(new Prefix(""), 0);
        prefixPositions.addFirst(preambleMarker);

        // Add a dummy PrefixPosition to represent the end of the string
        PrefixPosition endPositionMarker = new PrefixPosition(new Prefix(""), argsString.length());
        prefixPositions.add(endPositionMarker);

        // Map prefixes to their argument values (if any)
        ArgumentMultimap argMultimap = new ArgumentMultimap();
        for (int i = 0; i < prefixPositions.size() - 1; i++) {
            // Extract and store prefixes and their arguments
            Prefix argPrefix = prefixPositions.get(i).getPrefix();
            String argValue = extractArgumentValue(argsString, prefixPositions.get(i), prefixPositions.get(i + 1));
            argMultimap.put(argPrefix, argValue);
        }

        return argMultimap;
    }

    /**
     * Returns the trimmed value of the argument in the arguments string specified by {@code currentPrefixPosition}.
     * The end position of the value is determined by {@code nextPrefixPosition}.
     */
    private static String extractArgumentValue(String argsString,
                                        PrefixPosition currentPrefixPosition,
                                        PrefixPosition nextPrefixPosition) {
        Prefix prefix = currentPrefixPosition.getPrefix();

        int valueStartPos = currentPrefixPosition.getStartPosition() + prefix.getPrefix().length();
        String value = argsString.substring(valueStartPos, nextPrefixPosition.getStartPosition());

        return value.trim();
    }

    /**
     * Represents a prefix's position in an arguments string.
     */
    private static class PrefixPosition {
        private int startPosition;
        private final Prefix prefix;

        PrefixPosition(Prefix prefix, int startPosition) {
            this.prefix = prefix;
            this.startPosition = startPosition;
        }

        int getStartPosition() {
            return startPosition;
        }

        Prefix getPrefix() {
            return prefix;
        }
    }

}
