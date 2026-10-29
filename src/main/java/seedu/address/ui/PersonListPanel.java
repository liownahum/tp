package seedu.address.ui;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.logging.Logger;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    @FXML
    private ListView<Person> personListView;

    @FXML
    private Label emptyState;

    @FXML
    private Label resultCount;

    /**
     * Creates a panel displaying the complete directory.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        this(personList, personList);
    }

    /**
     * Creates a panel observing the displayed persons and the complete directory independently.
     */
    public PersonListPanel(ObservableList<Person> personList, ObservableList<Person> allPersons) {
        super(FXML);
        requireAllNonNull(personList, allPersons);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        emptyState.textProperty().bind(Bindings.when(Bindings.isEmpty(allPersons))
                .then("No members yet. Enter add with member details to start your directory. "
                        + "Enter help for command examples.")
                .otherwise("No matching members. Enter list to show everyone."));
        emptyState.maxWidthProperty().bind(personListView.widthProperty().subtract(40));
        resultCount.textProperty().bind(Bindings.format("Showing %d of %d %s",
                Bindings.size(personList), Bindings.size(allPersons),
                Bindings.when(Bindings.size(allPersons).isEqualTo(1)).then("member").otherwise("members")));
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        /**
         * Lets the list viewport determine cell width instead of the longest member detail.
         */
        PersonListViewCell() {
            setPrefWidth(0);
        }

        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                Region card = new PersonCard(person, getIndex() + 1).getRoot();
                // The cell knows the viewport width; the card must not size itself from long content.
                card.prefWidthProperty().bind(widthProperty());
                setGraphic(card);
            }
        }
    }

}
