package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.FxTestUtil.runOnFxThread;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Tests screen fitting through the actual main-window FXML and startup wiring. */
@EnabledIfEnvironmentVariable(named = "HIVEMIND_UI_TESTS", matches = "true")
public class MainWindowScreenFitTest {
    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void initializeToolkit() throws Exception {
        FxTestUtil.initializeToolkit();
    }

    @Test
    public void startup_oversizedSettingsBeyondAllScreens_fitsPrimaryVisualBounds() throws Exception {
        runOnFxThread(() -> {
            Rectangle2D visual = Screen.getPrimary().getVisualBounds();
            double rightmost = Screen.getScreens().stream()
                    .mapToDouble(screen -> screen.getBounds().getMaxX()).max().orElseThrow();
            Stage stage = createWindow(new GuiSettings(visual.getWidth() * 2, visual.getHeight() * 2,
                    (int) rightmost + 100, (int) visual.getMaxY() + 100));
            try {
                stage.show();
                assertTrue(stage.getX() >= visual.getMinX() - 1);
                assertTrue(stage.getY() >= visual.getMinY() - 1);
                assertTrue(stage.getX() + stage.getWidth() <= visual.getMaxX() + 1);
                assertTrue(stage.getY() + stage.getHeight() <= visual.getMaxY() + 1);
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void resize_to400Pixels_keepsCoreControlsVisible() throws Exception {
        runOnFxThread(() -> {
            Stage stage = createWindow(new GuiSettings());
            try {
                assertTrue(stage.getMinHeight() <= 400, "The stage minimum must fit a small logical screen");
                stage.setWidth(740);
                stage.setHeight(400);
                stage.show();
                assertTrue(stage.getHeight() <= 401, "The window must shrink below 480 logical pixels");
                Parent root = stage.getScene().getRoot();
                root.applyCss();
                root.layout();
                for (String selector : new String[] {"#commandTextField", "#resultDisplay",
                    "#personListPanelPlaceholder", "#saveLocationStatus"}) {
                    Node node = root.lookup(selector);
                    Bounds bounds = node.localToScene(node.getBoundsInLocal());
                    assertTrue(bounds.getMinY() >= -1, selector + " extends above the scene");
                    assertTrue(bounds.getMaxY() <= stage.getScene().getHeight() + 1,
                            selector + " extends below the scene");
                    assertTrue(bounds.getHeight() > 0, selector + " has no visible height");
                }
            } finally {
                stage.close();
            }
        });
    }

    private Stage createWindow(GuiSettings settings) {
        UserPrefs prefs = new UserPrefs();
        prefs.setGuiSettings(settings);
        ModelManager model = new ModelManager(SampleDataUtil.getSampleAddressBook(), prefs);
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("members.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
        Stage stage = new Stage();
        MainWindow window = new MainWindow(stage, new LogicManager(model, storage),
                temporaryFolder.resolve("members.json"));
        window.fillInnerParts();
        return stage;
    }
}
