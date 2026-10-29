package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Comparator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/** Verifies the macOS-specific workaround for oversized member list cells. */
@EnabledOnOs(OS.MAC)
@EnabledIfEnvironmentVariable(named = "HIVEMIND_UI_TESTS", matches = "true")
public class PersonListPanelMacSpacingTest {

    @BeforeAll
    public static void initializeToolkit() throws Exception {
        FxTestUtil.initializeToolkit();
    }

    @Test
    public void layout_commonWindowWidths_cellHeightMatchesContent() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Person person = new PersonBuilder().withName("Alexandra Catherine Montgomery Music Coordinator")
                    .withPhone("91234567").withEmail("music.coordinator.long.contact.address@example.com")
                    .withRole("Student Coordinator for the University Chamber Orchestra")
                    .withInstruments("Historical Keyboard and String Instrument Ensemble:12", "Violin:8")
                    .withGroups("University Chamber Orchestra Alumni", "Leadership Team").build();
            Region root = new PersonListPanel(FXCollections.observableArrayList(person)).getRoot();
            Scene scene = new Scene(root, 450, 600);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());

            for (int width : new int[] {400, 450, 1050, 1450}) {
                root.resize(width, 600);
                root.applyCss();
                root.layout();
                // Settle the cell height after its content receives the resized viewport width.
                root.layout();

                ListCell<?> cell = root.lookupAll(".list-cell").stream()
                        .filter(ListCell.class::isInstance)
                        .map(ListCell.class::cast)
                        .filter(item -> !item.isEmpty())
                        .min(Comparator.comparingInt(ListCell::getIndex))
                        .orElseThrow();
                Region card = (Region) cell.getGraphic();
                Region content = (Region) card.lookup("#cardContent");
                assertNotNull(content);
                assertEquals(content.getHeight(), cell.getHeight(), 1.0,
                        "macOS list cell should not reserve blank space below its card content");
            }
        });
    }
}
