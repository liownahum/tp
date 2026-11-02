package seedu.address.ui;

import java.awt.Point;
import java.util.List;

import javafx.geometry.Rectangle2D;
import seedu.address.commons.core.GuiSettings;

/** Fits saved window geometry to a current display without requiring the JavaFX toolkit. */
final class WindowGeometry {
    private WindowGeometry() {
    }

    /** Describes a display's full bounds and the area available outside system panels. */
    record ScreenBounds(Rectangle2D bounds, Rectangle2D visualBounds) {
    }

    /** Preserves the best-overlapping display, or uses the primary display for stale positions. */
    static Rectangle2D fit(GuiSettings settings, List<ScreenBounds> screens, ScreenBounds primary,
            double minimumWidth, double minimumHeight) {
        Point position = settings.getWindowCoordinates();
        ScreenBounds selected = primary;
        if (position != null) {
            Rectangle2D saved = new Rectangle2D(position.getX(), position.getY(),
                    Math.max(settings.getWindowWidth(), minimumWidth),
                    Math.max(settings.getWindowHeight(), minimumHeight));
            double largestOverlap = overlap(saved, primary.bounds());
            for (ScreenBounds screen : screens) {
                double overlap = overlap(saved, screen.bounds());
                if (overlap > largestOverlap) {
                    selected = screen;
                    largestOverlap = overlap;
                }
            }
        }
        Rectangle2D visual = selected.visualBounds();
        double width = Math.min(Math.max(settings.getWindowWidth(), minimumWidth), visual.getWidth());
        double height = Math.min(Math.max(settings.getWindowHeight(), minimumHeight), visual.getHeight());
        double x = position == null ? visual.getMinX() + (visual.getWidth() - width) / 2 : position.getX();
        double y = position == null ? visual.getMinY() + (visual.getHeight() - height) / 2 : position.getY();
        x = Math.max(visual.getMinX(), Math.min(x, visual.getMaxX() - width));
        y = Math.max(visual.getMinY(), Math.min(y, visual.getMaxY() - height));
        return new Rectangle2D(x, y, width, height);
    }

    /** Returns the shared area, or zero when the window and display do not overlap. */
    private static double overlap(Rectangle2D window, Rectangle2D screen) {
        double width = Math.max(0, Math.min(window.getMaxX(), screen.getMaxX())
                - Math.max(window.getMinX(), screen.getMinX()));
        double height = Math.max(0, Math.min(window.getMaxY(), screen.getMaxY())
                - Math.max(window.getMinY(), screen.getMinY()));
        return width * height;
    }
}
