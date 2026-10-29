package seedu.address.ui;

import java.util.Comparator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Group;
import seedu.address.model.person.Instrument;
import seedu.address.model.person.Person;

/**
 * A UI component that displays a person's name, contact details, membership status,
 * member type, optional role, instruments with years of experience, and participation groups.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label email;
    @FXML
    private Label membershipStatus;
    @FXML
    private Label memberType;
    @FXML
    private Label role;
    @FXML
    private FlowPane instruments;
    @FXML
    private VBox groups;
    @FXML
    private FlowPane groupLabels;

    /**
     * Creates a card for the given person at the specified displayed index.
     * Instruments are sorted by name, ignoring case. If no role is recorded, the role
     * label is hidden and excluded from the layout so it does not occupy space.
     *
     * @param person The person whose details are displayed.
     * @param displayedIndex The one-based index in the currently displayed person list.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        email.setText(person.getEmail().value);
        membershipStatus.setText(toDisplayText(person.getStatus().toString()));
        memberType.setText(toDisplayText(person.getMemberType().toString()));

        boolean hasRole = person.getRole().isPresent();
        role.setText(person.getRole()
                .map(value -> "Role: " + value.value)
                .orElse(""));

        role.setVisible(hasRole);
        role.setManaged(hasRole);

        person.getInstruments().stream()
                .sorted(Comparator.comparing(Instrument::getName, String.CASE_INSENSITIVE_ORDER))
                .forEach(instrument -> addWrappingLabel(instruments, instrument.toDisplayString()));
        boolean hasInstruments = !person.getInstruments().isEmpty();
        instruments.setVisible(hasInstruments);
        instruments.setManaged(hasInstruments);

        boolean hasGroups = !person.getGroups().isEmpty();
        person.getGroups().stream()
                .sorted(Comparator.comparing(Group::getName, String.CASE_INSENSITIVE_ORDER))
                .forEach(group -> addWrappingLabel(groupLabels, group.getName()));
        groups.setVisible(hasGroups);
        groups.setManaged(hasGroups);
    }

    /**
     * Returns a readable label for a validated membership keyword without changing its stored value.
     */
    private static String toDisplayText(String keyword) {
        String words = keyword.replace('-', ' ');
        return Character.toUpperCase(words.charAt(0)) + words.substring(1);
    }

    /**
     * Keeps each badge within its flow pane, including long words, as the window is resized.
     */
    private static void addWrappingLabel(FlowPane pane, String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinWidth(0);
        // Font metrics differ between platforms; do not squeeze wrapped lines below their preferred height.
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.maxWidthProperty().bind(pane.widthProperty());
        pane.getChildren().add(label);
    }
}
