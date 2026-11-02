package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import seedu.address.testutil.PersonBuilder;

/** Checks card sizing after CSS changes under the macOS platform name. */
@EnabledIfEnvironmentVariable(named = "HIVEMIND_UI_TESTS", matches = "true")
public class PersonCardMacSizingTest {
    @BeforeAll
    public static void initializeToolkit() throws Exception {
        FxTestUtil.initializeToolkit();
    }

    @Test
    public void layout_cssChangesAtSameWidth_updatesPreferredHeight() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            Region card = createMacCard();
            card.resize(450, 300);
            // VirtualFlow can query the height before the card receives its final CSS.
            card.prefHeight(450);
            StackPane root = new StackPane(card);
            Scene scene = new Scene(root, 450, 600);
            scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
            root.applyCss();
            root.layout();
            root.layout();

            Region content = (Region) card.lookup("#cardContent");
            assertEquals(content.prefHeight(content.getWidth()), card.prefHeight(card.getWidth()), 1.0);

            Label name = (Label) card.lookup("#name");
            name.setStyle("-fx-font-size: 40px;");
            root.applyCss();
            root.layout();
            root.layout();
            assertTrue(card.prefHeight(card.getWidth()) >= content.prefHeight(content.getWidth()) - 1.0,
                    "Card height must include text after CSS changes at an unchanged width");
        });
    }

    private static Region createMacCard() {
        String originalOsName = System.getProperty("os.name");
        try {
            // The native toolkit is already running; exercise any macOS-specific card behavior.
            System.setProperty("os.name", "Mac OS X");
            return new PersonCard(new PersonBuilder().withName("Bernice Yu")
                    .withInstruments("Cello:6").build(), 2).getRoot();
        } finally {
            System.setProperty("os.name", originalOsName);
        }
    }
}
