package seedu.address.commons.core;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;

/**
 * Provides paths to files and directories owned by the application.
 */
public final class ApplicationPaths {

    private static final Path APPLICATION_HOME = determineApplicationHome();

    private ApplicationPaths() {}

    /**
     * Returns the application home directory.
     * For a packaged launch, this is the directory containing the JAR file. For a development launch, this is the
     * working directory.
     */
    public static Path getApplicationHome() {
        return APPLICATION_HOME;
    }

    public static Path getUserPrefsFile() {
        return APPLICATION_HOME.resolve("preferences.json");
    }

    public static Path getAddressBookFile() {
        return APPLICATION_HOME.resolve("data").resolve("addressbook.json");
    }

    public static Path getImportDirectory() {
        return APPLICATION_HOME.resolve("imports");
    }

    public static Path getExportDirectory() {
        return APPLICATION_HOME.resolve("exports");
    }

    public static Path getLogFile() {
        return APPLICATION_HOME.resolve("addressbook.log");
    }

    private static Path determineApplicationHome() {
        Path workingDirectory = Path.of("");
        CodeSource codeSource = ApplicationPaths.class.getProtectionDomain().getCodeSource();
        if (codeSource == null) {
            return normalizeWorkingDirectory(workingDirectory);
        }

        try {
            return resolveApplicationHome(codeSource.getLocation().toURI(), workingDirectory);
        } catch (URISyntaxException exception) {
            return normalizeWorkingDirectory(workingDirectory);
        }
    }

    /**
     * Resolves the application home from a code-source location.
     *
     * @param codeSourceLocation location from which the application classes were loaded.
     * @param workingDirectory working directory to use for a development launch or if the code source is unavailable.
     */
    static Path resolveApplicationHome(URI codeSourceLocation, Path workingDirectory) {
        requireNonNull(codeSourceLocation);
        requireNonNull(workingDirectory);

        Path normalizedWorkingDirectory = normalizeWorkingDirectory(workingDirectory);
        if (!"file".equalsIgnoreCase(codeSourceLocation.getScheme())) {
            return normalizedWorkingDirectory;
        }

        try {
            Path codeSourcePath = Path.of(codeSourceLocation);
            if (!Files.isRegularFile(codeSourcePath)) {
                return normalizedWorkingDirectory;
            }

            Path canonicalCodeSource = codeSourcePath.toRealPath();
            Path parentDirectory = canonicalCodeSource.getParent();
            return parentDirectory == null ? normalizedWorkingDirectory : parentDirectory;
        } catch (IOException | IllegalArgumentException exception) {
            return normalizedWorkingDirectory;
        }
    }

    private static Path normalizeWorkingDirectory(Path workingDirectory) {
        return workingDirectory.toAbsolutePath().normalize();
    }
}
