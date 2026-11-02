package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import seedu.address.model.person.Person;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.testutil.PersonBuilder;

/** Checks member rows through resizing, selection and reuse under the macOS platform name. */
@EnabledIfEnvironmentVariable(named = "HIVEMIND_UI_TESTS", matches = "true")
public class PersonListPanelSizingTest {
    @BeforeAll
    public static void initializeToolkit() throws Exception {
        FxTestUtil.initializeToolkit();
    }

    @Test
    public void layout_scrollSelectAddAndResize_keepsDetailsInsideRows() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            String originalOsName = System.getProperty("os.name");
            try {
                System.setProperty("os.name", "Mac OS X");
                checkListTransitions();
            } finally {
                System.setProperty("os.name", originalOsName);
            }
        });
    }

    private void checkListTransitions() {
        ObservableList<Person> persons = FXCollections.observableArrayList(SampleDataUtil.getSamplePersons());
        for (int index = 0; index < 24; index++) {
            PersonBuilder builder = new PersonBuilder().withName("Member " + index);
            if (index % 2 == 0) {
                builder.withRole("Student Coordinator for the University Chamber Orchestra")
                        .withInstruments("Historical Keyboard and String Instrument Ensemble:12", "Violin:1")
                        .withGroups("University Chamber Orchestra Alumni", "Leadership Team");
            }
            persons.add(builder.build());
        }
        Region root = new PersonListPanel(persons).getRoot();
        Scene scene = new Scene(root, 450, 720);
        scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
        @SuppressWarnings("unchecked")
        ListView<Person> list = (ListView<Person>) root.lookup("#personListView");
        assertNotNull(list);

        for (int width : new int[] {400, 450, 1050, 1450, 400}) {
            root.resize(width, 720);
            settle(root);
            for (int index : new int[] {0, 7, 24, 12, 0}) {
                list.getSelectionModel().clearSelection();
                list.scrollTo(index);
                settle(root);
                assertRowsFit(root, persons);
                list.getSelectionModel().select(index);
                settle(root);
                assertRowsFit(root, persons);
            }
        }

        persons.add(new PersonBuilder().withName("New Member").withInstruments("Drums:7").build());
        list.scrollTo(persons.size() - 1);
        settle(root);
        assertRowsFit(root, persons);

        // Simulate find/filter results and list restoring the full directory.
        list.setItems(FXCollections.observableArrayList(persons.get(1), persons.get(6)));
        list.scrollTo(0);
        settle(root);
        assertRowsFit(root, list.getItems());
        list.setItems(persons);
        list.scrollTo(0);
        settle(root);
        assertRowsFit(root, persons);
    }

    private static void settle(Region root) {
        for (int pass = 0; pass < 3; pass++) {
            root.applyCss();
            root.layout();
        }
    }

    static void assertRowsFit(Region root, List<Person> persons) {
        Bounds viewportBounds = root.localToScene(root.getLayoutBounds());
        List<ListCell<?>> cells = root.lookupAll(".list-cell").stream()
                .filter(ListCell.class::isInstance).<ListCell<?>>map(node -> (ListCell<?>) node)
                .filter(cell -> !cell.isEmpty() && cell.getGraphic() != null && cell.getHeight() > 0)
                .filter(cell -> isDisplayed(cell)
                        && cell.localToScene(cell.getLayoutBounds()).intersects(viewportBounds))
                .sorted(Comparator.comparingInt(ListCell::getIndex))
                .toList();
        assertFalse(cells.isEmpty());
        Bounds previousRow = null;
        for (ListCell<?> cell : cells) {
            Region card = (Region) cell.getGraphic();
            Region content = (Region) card.lookup("#cardContent");
            assertNotNull(content);
            double verticalInsets = card.getInsets().getTop() + card.getInsets().getBottom()
                    + cell.getInsets().getTop() + cell.getInsets().getBottom();
            assertTrue(cell.getHeight() >= content.prefHeight(content.getWidth()) + verticalInsets - 1.0,
                    "Row " + cell.getIndex() + " must fit its content's required height");
            assertEquals(content.getHeight() + verticalInsets, cell.getHeight(), 1.0,
                    "Rows must not reserve excess blank space");
            assertEquals((cell.getIndex() + 1) + ". ", ((Label) card.lookup("#id")).getText());
            Label name = (Label) card.lookup("#name");
            assertEquals(persons.get(cell.getIndex()).getName().fullName, name.getText());
            Bounds rowBounds = cell.localToScene(cell.getBoundsInLocal());
            if (previousRow != null) {
                assertTrue(previousRow.getMaxY() <= rowBounds.getMinY() + 1.0,
                        "Adjacent visible rows must not overlap");
            }
            previousRow = rowBounds;
            for (var node : card.lookupAll(".label")) {
                Label label = (Label) node;
                if (!isDisplayed(label) || label.getText().isEmpty()) {
                    continue;
                }
                Text text = (Text) label.lookup(".text");
                assertNotNull(text, "Missing text in row " + cell.getIndex() + ": " + label.getText());
                assertEquals(label.getText(), text.getText(), "Visible text must not be truncated");
                Bounds textBounds = text.localToScene(text.getBoundsInLocal());
                assertTrue(textBounds.getMinY() >= rowBounds.getMinY() - 1.0,
                        "Text must not overlap the previous row: " + label.getText());
                assertTrue(textBounds.getMaxY() <= rowBounds.getMaxY() + 1.0,
                        "Text must not overlap the next row: " + label.getText());
            }
        }
    }

    private static boolean isDisplayed(Node node) {
        for (Node ancestor = node; ancestor != null; ancestor = ancestor.getParent()) {
            if (!ancestor.isVisible()) {
                return false;
            }
        }
        return true;
    }
}
