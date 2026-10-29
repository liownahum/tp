package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.address.commons.core.ApplicationPaths;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.storage.CsvExporter;
import seedu.address.storage.ExportField;

/**
 * Exports the people in the current filtered list to a CSV file.
 */
public class ExportCommand extends Command {
    public static final String COMMAND_WORD = "export";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Exports the people currently shown to a CSV file.\n"
            + "Parameters: [f/FIELD]...\n"
            + "Fields: " + CsvExporter.HEADER + ". Each field may be selected once, in the desired column order.\n"
            + "Omit fields to export all eight columns.\n"
            + "Example: " + COMMAND_WORD + " f/name f/phone f/instruments";

    public static final String MESSAGE_EXPORT_SUCCESS = "Exported %d people to %s.";
    public static final String MESSAGE_EXPORT_SUCCESS_SINGULAR = "Exported %d person to %s.";
    public static final String MESSAGE_EXPORT_FAILURE = "Could not export current results to %s. "
            + "Make sure the export location is a writable folder.";

    private static final Path DEFAULT_EXPORT_DIRECTORY = ApplicationPaths.getExportDirectory();
    private static final Logger logger = LogsCenter.getLogger(ExportCommand.class);

    private final Path exportDirectory;
    private final List<ExportField> fields;

    public ExportCommand() {
        this(DEFAULT_EXPORT_DIRECTORY);
    }

    ExportCommand(Path exportDirectory) {
        this(exportDirectory, ExportField.DEFAULT_FIELDS);
    }

    /**
     * Creates a command exporting the selected columns in the requested order.
     *
     * @param fields the non-empty list of distinct columns to export.
     * @throws IllegalArgumentException if the selection is empty or contains repeated fields.
     * @throws NullPointerException if the list or any field is null.
     */
    public ExportCommand(List<ExportField> fields) {
        this(DEFAULT_EXPORT_DIRECTORY, fields);
    }

    /**
     * Creates a command exporting the selected columns to the specified directory.
     *
     * @param exportDirectory the directory in which to create the CSV file.
     * @param fields the non-empty list of distinct columns, in the requested order.
     * @throws IllegalArgumentException if the selection is empty or contains repeated fields.
     * @throws NullPointerException if the directory, list or any field is null.
     */
    ExportCommand(Path exportDirectory, List<ExportField> fields) {
        this.exportDirectory = requireNonNull(exportDirectory);
        this.fields = List.copyOf(fields);
        checkArgument(!this.fields.isEmpty(), "At least one export field must be selected.");
        checkArgument(new HashSet<>(this.fields).size() == this.fields.size(),
                "Each export field may only be selected once.");
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        List<Person> currentResults = model.getFilteredPersonList();

        try {
            Path outputPath = CsvExporter.export(currentResults, exportDirectory, fields);
            logger.info(() -> String.format("Exported %d rows with fields %s to %s.",
                    currentResults.size(), fields.stream().map(ExportField::getHeader).toList(), outputPath));
            String successMessage = currentResults.size() == 1
                    ? MESSAGE_EXPORT_SUCCESS_SINGULAR
                    : MESSAGE_EXPORT_SUCCESS;

            return new CommandResult(
                String.format(successMessage, currentResults.size(), outputPath));
        } catch (IOException ioe) {
            logger.log(Level.WARNING, ioe, () -> String.format("Export failed: %d rows, fields %s, directory %s.",
                    currentResults.size(), fields.stream().map(ExportField::getHeader).toList(), exportDirectory));
            throw new CommandException(
                String.format(MESSAGE_EXPORT_FAILURE, exportDirectory), ioe);
        }
    }

    @Override
    public boolean requiresSave() {
        return false;
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof ExportCommand otherCommand
                && exportDirectory.equals(otherCommand.exportDirectory)
                && fields.equals(otherCommand.fields));
    }

    @Override
    public int hashCode() {
        return Objects.hash(exportDirectory, fields);
    }
}
