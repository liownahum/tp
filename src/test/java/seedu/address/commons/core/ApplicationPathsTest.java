package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ApplicationPathsTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    public void resolveApplicationHome_packagedJar_returnsCanonicalJarDirectory() throws Exception {
        Path jarDirectory = Files.createDirectories(temporaryFolder.resolve("JAR home 子"));
        Path jarFile = Files.createFile(jarDirectory.resolve("Hive Mind"));
        Path workingDirectory = Files.createDirectory(temporaryFolder.resolve("launch folder"));

        Path applicationHome = ApplicationPaths.resolveApplicationHome(jarFile.toUri(), workingDirectory);

        assertEquals(jarDirectory.toRealPath(), applicationHome);
    }

    @Test
    public void resolveApplicationHome_developmentClasses_returnsWorkingDirectory() throws Exception {
        Path classesDirectory = Files.createDirectory(temporaryFolder.resolve("classes"));
        Path workingDirectory = Files.createDirectory(temporaryFolder.resolve("working directory"));

        Path applicationHome = ApplicationPaths.resolveApplicationHome(classesDirectory.toUri(), workingDirectory);

        assertEquals(workingDirectory.toAbsolutePath().normalize(), applicationHome);
    }

    @Test
    public void applicationPaths_allPathsAreInsideApplicationHome() {
        Path applicationHome = ApplicationPaths.getApplicationHome();

        assertTrue(ApplicationPaths.getUserPrefsFile().startsWith(applicationHome));
        assertTrue(ApplicationPaths.getAddressBookFile().startsWith(applicationHome));
        assertTrue(ApplicationPaths.getImportDirectory().startsWith(applicationHome));
        assertTrue(ApplicationPaths.getExportDirectory().startsWith(applicationHome));
        assertTrue(ApplicationPaths.getLogFile().startsWith(applicationHome));
    }
}
