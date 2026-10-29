package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests the real member-card FXML on the JavaFX application thread.
 * Requires a graphical session and {@code HIVEMIND_UI_TESTS=true}.
 */
@EnabledIfEnvironmentVariable(named = "HIVEMIND_UI_TESTS", matches = "true")
public class PersonCardTest {

    @BeforeAll
    public static void initializeToolkit() throws Exception {
        CompletableFuture<Void> started = new CompletableFuture<>();
        Runnable initialize = () -> {
            Platform.setImplicitExit(false);
            started.complete(null);
        };
        try {
            Platform.startup(initialize);
        } catch (IllegalStateException e) {
            // Another UI test may already have started the toolkit in this test process.
            Platform.runLater(initialize);
        }
        started.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void constructor_allFieldsPresent_displaysDetails() throws Exception {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withName("Alicia Tan").withPhone("90000002")
                    .withEmail("alicia@example.com").withStatus("active-alumnus").withMemberType("teacher")
                    .withRole("Section Leader").withInstruments("Violin:1", "piano:5")
                    .withGroups("zeta", "Alpha").build();
            PersonCard card = new PersonCard(person, 3);

            assertEquals("3. ", label(card, "id").getText());
            assertEquals("Alicia Tan", label(card, "name").getText());
            assertEquals("90000002", label(card, "phone").getText());
            assertEquals("alicia@example.com", label(card, "email").getText());
            assertEquals("Active alumnus", label(card, "membershipStatus").getText());
            assertEquals("Teacher", label(card, "memberType").getText());
            assertEquals("Role: Section Leader", label(card, "role").getText());
            assertTrue(label(card, "role").isVisible());
            assertTrue(label(card, "role").isManaged());

            FlowPane instruments = (FlowPane) card.getRoot().lookup("#instruments");
            assertNotNull(instruments);
            List<String> instrumentLabels = instruments.getChildren().stream()
                    .map(node -> ((Label) node).getText()).toList();
            assertEquals(List.of("piano (5 yrs)", "Violin (1 yr)"), instrumentLabels);
            assertEquals(List.of("Alpha", "zeta"), groupNames(card));
            assertTrue(card.getRoot().lookup("#groups").isVisible());
            assertTrue(card.getRoot().lookup("#groups").isManaged());
        });
    }

    @Test
    public void constructor_optionalFieldsAbsent_hidesRoleAndHasNoInstruments() throws Exception {
        runOnFxThread(() -> {
            PersonCard card = new PersonCard(new PersonBuilder().build(), 1);

            assertEquals("", label(card, "role").getText());
            assertFalse(label(card, "role").isVisible());
            assertFalse(label(card, "role").isManaged());
            FlowPane instruments = (FlowPane) card.getRoot().lookup("#instruments");
            assertNotNull(instruments);
            assertTrue(instruments.getChildren().isEmpty());
            assertFalse(instruments.isVisible());
            assertFalse(instruments.isManaged());
            assertTrue(groupNames(card).isEmpty());
            assertFalse(card.getRoot().lookup("#groups").isVisible());
            assertFalse(card.getRoot().lookup("#groups").isManaged());
        });
    }

    @Test
    public void constructor_groupNameContainingComma_distinguishesSeparateGroups() throws Exception {
        runOnFxThread(() -> {
            PersonCard oneGroup = new PersonCard(new PersonBuilder().withGroups("Dance, Music").build(), 1);
            PersonCard twoGroups = new PersonCard(new PersonBuilder().withGroups("Dance", "Music").build(), 2);

            assertEquals(List.of("Dance, Music"), groupNames(oneGroup));
            assertEquals(List.of("Dance", "Music"), groupNames(twoGroups));
        });
    }

    @Test
    public void constructor_membershipCombinations_displaysCorrectValues() throws Exception {
        runOnFxThread(() -> {
            String[][] statuses = {{"member", "Member"}, {"active-alumnus", "Active alumnus"},
                {"inactive-alumnus", "Inactive alumnus"}};
            String[][] memberTypes = {{"student", "Student"}, {"teacher", "Teacher"}};
            for (String[] status : statuses) {
                for (String[] memberType : memberTypes) {
                    Person person = new PersonBuilder().withStatus(status[0]).withMemberType(memberType[0]).build();
                    PersonCard card = new PersonCard(person, 1);

                    assertEquals(status[1], label(card, "membershipStatus").getText());
                    assertEquals(memberType[1], label(card, "memberType").getText());
                }
            }
        });
    }

    @Test
    public void layout_longLabelsAtMinimumWidth_keepsAllTextVisibleAfterResizing() throws Exception {
        runOnFxThread(() -> {
            String instrument = "Historical Keyboard and String Instrument Ensemble Performance Specialisation";
            String group = "University Chamber Orchestra Alumni and Student Combined Performance Group";
            Person person = new PersonBuilder().withName("Alexandra Catherine Montgomery Music Coordinator")
                    .withEmail("music.coordinator.long.contact.address@example.com")
                    .withRole("Student Coordinator for the University Chamber Orchestra")
                    .withInstruments(instrument + ":12").withGroups(group, "X".repeat(150)).build();
            Region root = new PersonListPanel(FXCollections.observableArrayList(person)).getRoot();
            Scene scene = new Scene(root, 450, 600);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());

            // The list has less space than the window because of the surrounding padding.
            for (int width : new int[] {400, 450, 800, 400}) {
                root.resize(width, 600);
                root.applyCss();
                root.layout();
                // Settle the label height after the flow pane receives its new viewport width.
                root.layout();
                assertLabelVisible(root, (Label) root.lookup("#name"), person.getName().fullName);
                assertLabelVisible(root, (Label) root.lookup("#id"), "1. ");
                assertLabelVisible(root, (Label) root.lookup("#memberType"), "Student");
                assertLabelVisible(root, (Label) root.lookup("#membershipStatus"), "Member");
                assertLabelVisible(root, (Label) root.lookup("#instruments .label"), instrument + " (12 yrs)");
                assertLabelVisible(root, (Label) root.lookup("#email"), person.getEmail().value);
                assertLabelVisible(root, (Label) root.lookup("#role"), "Role: " + person.getRole().orElseThrow().value);
                FlowPane groups = (FlowPane) root.lookup("#groupLabels");
                for (var node : groups.getChildren()) {
                    Label label = (Label) node;
                    assertLabelVisible(root, label, label.getText());
                }
            }
        });
    }

    /**
     * Checks the laid-out text itself, including absence of truncation, within a real list cell.
     */
    private static void assertLabelVisible(Region root, Label label, String expectedText) {
        assertNotNull(label);
        Bounds bounds = label.localToScene(label.getBoundsInLocal());
        assertTrue(bounds.getMinX() >= 0, "Label starts outside the list");
        assertTrue(bounds.getMaxX() <= root.getWidth(), "Label extends past the list: " + bounds);
        Text text = (Text) label.lookup(".text");
        assertNotNull(text);
        assertEquals(expectedText, text.getText(), "The visible text must not be truncated");
    }

    private static List<String> groupNames(PersonCard card) {
        FlowPane labels = (FlowPane) card.getRoot().lookup("#groupLabels");
        assertNotNull(labels);
        return labels.getChildren().stream().map(node -> ((Label) node).getText()).toList();
    }

    private static Label label(PersonCard card, String id) {
        Label label = (Label) card.getRoot().lookup("#" + id);
        assertNotNull(label, "Missing FXML label: " + id);
        return label;
    }

    /**
     * Runs assertions on the JavaFX thread and propagates failures to JUnit with a bounded wait.
     */
    private static void runOnFxThread(Runnable assertions) throws Exception {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                assertions.run();
                result.complete(null);
            } catch (Throwable error) {
                result.completeExceptionally(error);
            }
        });
        result.get(10, TimeUnit.SECONDS);
    }
}
