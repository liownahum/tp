package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    /** Describes feedback independently of the message wording. */
    public enum FeedbackType {
        INFORMATION, SUCCESS, ERROR
    }

    private static final String FXML = "ResultDisplay.fxml";
    private static final List<String> FEEDBACK_STYLES = List.of(
            "feedback-information", "feedback-success", "feedback-error");

    @FXML
    private TextArea resultDisplay;

    @FXML
    private Label feedbackType;

    public ResultDisplay() {
        super(FXML);
    }

    /** Shows an informational message, such as a startup notice. */
    public void setFeedbackToUser(String feedbackToUser) {
        setFeedbackToUser(feedbackToUser, FeedbackType.INFORMATION);
    }

    /** Shows feedback with a text label and colour identifying its outcome. */
    public void setFeedbackToUser(String feedbackToUser, FeedbackType type) {
        requireNonNull(feedbackToUser);
        requireNonNull(type);
        getRoot().getStyleClass().removeAll(FEEDBACK_STYLES);
        switch (type) {
            case SUCCESS:
                feedbackType.setText("Success");
                getRoot().getStyleClass().add("feedback-success");
                break;
            case ERROR:
                feedbackType.setText("Error");
                getRoot().getStyleClass().add("feedback-error");
                break;
            default:
                feedbackType.setText("Information");
                getRoot().getStyleClass().add("feedback-information");
                break;
        }
        resultDisplay.setText(feedbackToUser);
        resultDisplay.positionCaret(0);
        resultDisplay.setScrollTop(0);
    }

}
