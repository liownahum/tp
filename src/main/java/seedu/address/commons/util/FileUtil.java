package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

/**
 * Utility methods for working with files.
 */
public class FileUtil {

    private static final String CHARSET = "UTF-8";
    private static final String INVALID_WINDOWS_FILENAME_CHARS = "\\/:*?\"<>|";
    private static final Set<String> RESERVED_WINDOWS_FILENAMES = Set.of(
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "COM¹", "COM²", "COM³",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9",
            "LPT¹", "LPT²", "LPT³");

    /**
     * Returns whether {@code fileName} is a valid Windows filename.
     */
    public static boolean isValidWindowsFileName(String fileName) {
        requireNonNull(fileName);
        if (fileName.isEmpty() || fileName.endsWith(" ") || fileName.endsWith(".")
                || fileName.chars().anyMatch(character -> character < 32)) {
            return false;
        }

        if (fileName.chars().anyMatch(character -> INVALID_WINDOWS_FILENAME_CHARS.indexOf(character) >= 0)) {
            return false;
        }

        int extensionSeparatorIndex = fileName.indexOf('.');
        String baseName = extensionSeparatorIndex == -1
                ? fileName
                : fileName.substring(0, extensionSeparatorIndex);
        return !RESERVED_WINDOWS_FILENAMES.contains(baseName.toUpperCase(Locale.ROOT));
    }

    /**
     * Creates a file if it does not exist along with its missing parent directories.
     * @throws IOException if the file or directory cannot be created.
     */
    public static void createIfMissing(Path file) throws IOException {
        if (Files.exists(file)) {
            return;
        }

        createParentDirsOfFile(file);

        Files.createFile(file);
    }

    /**
     * Creates parent directories of file if it has a parent directory
     */
    private static void createParentDirsOfFile(Path file) throws IOException {
        Path parentDir = file.getParent();

        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
    }

    /**
     * Assumes file exists
     */
    public static String readFromFile(Path file) throws IOException {
        return new String(Files.readAllBytes(file), CHARSET);
    }

    /**
     * Writes given string to a file.
     * Will create the file if it does not exist yet.
     */
    public static void writeToFile(Path file, String content) throws IOException {
        Files.write(file, content.getBytes(CHARSET));
    }

}
