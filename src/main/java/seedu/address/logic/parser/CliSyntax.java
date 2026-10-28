package seedu.address.logic.parser;

/**
 * Contains Command Line Interface (CLI) syntax definitions common to multiple commands
 */
public class CliSyntax {

    /* Prefix definitions */
    public static final Prefix PREFIX_NAME = new Prefix("n/");
    public static final Prefix PREFIX_PHONE = new Prefix("p/");
    public static final Prefix PREFIX_EMAIL = new Prefix("e/");
    public static final Prefix PREFIX_STATUS = new Prefix("s/");
    public static final Prefix PREFIX_MEMBER_TYPE = new Prefix("ty/");
    public static final Prefix PREFIX_ROLE = new Prefix("r/");
    public static final Prefix PREFIX_INSTRUMENT = new Prefix("i/");
    public static final Prefix PREFIX_GROUP = new Prefix("g/");

    /** All prefixes supported by HiveMind, in declaration order. */
    public static final Prefix[] ALL_PREFIXES = {PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_STATUS,
        PREFIX_MEMBER_TYPE, PREFIX_ROLE, PREFIX_INSTRUMENT, PREFIX_GROUP};

    /** Prefixes removed from the original AddressBook app and no longer accepted by HiveMind. */
    public static final Prefix[] REMOVED_PREFIXES = {new Prefix("t/"), new Prefix("a/")};

}
