package seedu.address.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import seedu.address.model.person.Person;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.testutil.PersonBuilder;

/** Checks recycled rows in a shown window, allowing deferred layout checks between actions. */
@EnabledIfEnvironmentVariable(named = "HIVEMIND_UI_TESTS", matches = "true")
public class PersonListPanelPulseSizingTest {
    private Stage stage;
    private AnimationTimer timer;

    @BeforeAll
    public static void initializeToolkit() throws Exception {
        FxTestUtil.initializeToolkit();
    }

    @AfterEach
    public void closeWindow() throws Exception {
        FxTestUtil.runOnFxThread(() -> {
            if (timer != null) {
                timer.stop();
            }
            if (stage != null) {
                stage.close();
            }
        });
    }

    @Test
    public void layout_addAfterScrolling200Members_keepsWrappedRowsInsideCells() throws Exception {
        CompletableFuture<Void> completed = new CompletableFuture<>();
        FxTestUtil.runOnFxThread(() -> startScenario(completed));
        completed.get(30, TimeUnit.SECONDS);
    }

    /**
     * Creates a real window and checks each transition after its layout pulses settle.
     */
    private void startScenario(CompletableFuture<Void> completed) {
        ObservableList<Person> persons = createPersons();
        Region root = new PersonListPanel(persons).getRoot();
        Scene scene = new Scene(root, 400, 720);
        scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
        stage = new Stage();
        stage.setScene(scene);
        stage.show();
        @SuppressWarnings("unchecked")
        ListView<Person> list = (ListView<Person>) root.lookup("#personListView");
        List<Runnable> actions = createActions(persons, list);
        timer = new AnimationTimer() {
            private int actionIndex = -1;
            private int frames;

            @Override
            public void handle(long now) {
                // Give CSS, layout and the deferred size check several real pulses to settle.
                if (++frames < 8) {
                    return;
                }
                frames = 0;
                try {
                    if (actionIndex >= 0) {
                        PersonListPanelSizingTest.assertRowsFit(root, persons);
                    }
                    actionIndex++;
                    if (actionIndex == actions.size()) {
                        stop();
                        completed.complete(null);
                    } else {
                        actions.get(actionIndex).run();
                    }
                } catch (Throwable failure) {
                    stop();
                    completed.completeExceptionally(failure);
                }
            }
        };
        timer.start();
    }

    /**
     * Returns scrolling, selection, resizing and addition steps that exercise recycled cells.
     */
    private List<Runnable> createActions(ObservableList<Person> persons, ListView<Person> list) {
        List<Runnable> actions = new ArrayList<>();
        for (int width : new int[] {400, 450, 1050, 1450, 400}) {
            actions.add(() -> stage.setWidth(width));
            for (int index : new int[] {0, 24, 99, 199, 0}) {
                actions.add(() -> {
                    list.getSelectionModel().clearSelection();
                    list.scrollTo(index);
                });
                actions.add(() -> list.getSelectionModel().select(index));
            }
        }
        actions.add(() -> {
            persons.add(new PersonBuilder().withName("New Member").withInstruments("Drums:7").build());
            list.scrollTo(persons.size() - 1);
        });
        return actions;
    }

    /**
     * Builds a full-size directory with both short and wrapped member cards.
     */
    private static ObservableList<Person> createPersons() {
        ObservableList<Person> persons = FXCollections.observableArrayList(SampleDataUtil.getSamplePersons());
        int index = 0;
        while (persons.size() < 200) {
            PersonBuilder builder = new PersonBuilder().withName("Member " + index);
            if (index % 2 == 0) {
                builder.withRole("Student Coordinator for the University Chamber Orchestra")
                        .withInstruments("Historical Keyboard and String Instrument Ensemble:12", "Violin:1")
                        .withGroups("University Chamber Orchestra Alumni", "Leadership Team");
            }
            persons.add(builder.build());
            index++;
        }
        return persons;
    }
}
