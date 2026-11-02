package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.geometry.Rectangle2D;
import seedu.address.commons.core.GuiSettings;

/** Tests display changes without starting the JavaFX toolkit or requiring multiple monitors. */
public class WindowGeometryTest {
    private static final WindowGeometry.ScreenBounds PRIMARY = screen(0, 0, 1920, 1080);
    private static final WindowGeometry.ScreenBounds SMALL = screen(0, 0, 853, 480);
    private static final WindowGeometry.ScreenBounds LEFT = screen(-1280, 0, 1280, 1024);

    @Test
    public void fit_defaultSettingsOnSmallScreen_centersAndLimitsHeight() {
        assertEquals(new Rectangle2D(56.5, 0, 740, 480),
                fit(new GuiSettings(), SMALL, List.of(SMALL)));
    }

    @Test
    public void fit_oversizedStaleSettings_limitsBothDimensionsAndPosition() {
        assertEquals(new Rectangle2D(0, 0, 853, 480),
                fit(new GuiSettings(2000, 1200, 3000, 2000), SMALL, List.of(SMALL)));
    }

    @Test
    public void fit_undersizedSettings_enforcesMinimumDimensions() {
        assertEquals(new Rectangle2D(10, 20, 450, 400),
                fit(new GuiSettings(100, 100, 10, 20), PRIMARY, List.of(PRIMARY)));
    }

    @Test
    public void fit_validSettings_preservesSizeAndPosition() {
        assertEquals(new Rectangle2D(50, 60, 740, 600),
                fit(new GuiSettings(740, 600, 50, 60), PRIMARY, List.of(PRIMARY)));
    }

    @Test
    public void fit_negativeSecondaryCoordinates_preservesPosition() {
        assertEquals(new Rectangle2D(-1100, 100, 740, 600),
                fit(new GuiSettings(740, 600, -1100, 100), PRIMARY, List.of(PRIMARY, LEFT)));
    }

    @Test
    public void fit_removedMonitor_restoresOnPrimary() {
        assertEquals(new Rectangle2D(0, 0, 740, 600),
                fit(new GuiSettings(740, 600, -2000, -1000), PRIMARY, List.of(PRIMARY)));
    }

    @Test
    public void fit_straddlingDisplays_usesLargestOverlapAndKeepsWindowInside() {
        assertEquals(new Rectangle2D(-740, 100, 740, 600),
                fit(new GuiSettings(740, 600, -600, 100), PRIMARY, List.of(PRIMARY, LEFT)));
    }

    @Test
    public void fit_defaultPosition_usesPrimaryEvenIfSecondaryIsFirst() {
        assertEquals(new Rectangle2D(590, 240, 740, 600),
                fit(new GuiSettings(), PRIMARY, List.of(LEFT, PRIMARY)));
    }

    @Test
    public void fit_taskbarInsets_keepsWindowWithinVisualArea() {
        WindowGeometry.ScreenBounds display = new WindowGeometry.ScreenBounds(
                new Rectangle2D(0, 0, 1920, 1080), new Rectangle2D(40, 30, 1880, 1000));
        assertEquals(new Rectangle2D(40, 30, 1880, 1000),
                fit(new GuiSettings(2000, 1200, 0, 0), display, List.of(display)));
    }

    @Test
    public void fit_savedPositionOnlyOverlapsSecondaryTaskbar_retainsSecondaryDisplay() {
        WindowGeometry.ScreenBounds secondary = new WindowGeometry.ScreenBounds(
                new Rectangle2D(-1280, 0, 1280, 1024), new Rectangle2D(-1280, 0, 1280, 984));
        assertEquals(new Rectangle2D(-1000, 584, 450, 400),
                fit(new GuiSettings(450, 400, -1000, 1000), PRIMARY, List.of(PRIMARY, secondary)));
    }

    @Test
    public void fit_displayBelowMinimumSize_stillFitsAvailableSpace() {
        WindowGeometry.ScreenBounds display = screen(0, 0, 400, 350);
        assertEquals(new Rectangle2D(0, 0, 400, 350),
                fit(new GuiSettings(), display, List.of(display)));
    }

    @Test
    public void fit_wideShortScreen_clampsEachDimensionIndependently() {
        WindowGeometry.ScreenBounds display = screen(0, 0, 1000, 500);
        assertEquals(new Rectangle2D(0, 0, 900, 500),
                fit(new GuiSettings(900, 700, 0, 0), display, List.of(display)));
    }

    private static WindowGeometry.ScreenBounds screen(double x, double y, double width, double height) {
        Rectangle2D bounds = new Rectangle2D(x, y, width, height);
        return new WindowGeometry.ScreenBounds(bounds, bounds);
    }

    private static Rectangle2D fit(GuiSettings settings, WindowGeometry.ScreenBounds primary,
            List<WindowGeometry.ScreenBounds> screens) {
        return WindowGeometry.fit(settings, screens, primary, 450, 400);
    }
}
