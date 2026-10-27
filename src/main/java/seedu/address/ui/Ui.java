package seedu.address.ui;

import javafx.stage.Stage;

/**
 * API of the UI component
 */
public interface Ui {

    /** Starts the UI (and the App).  */
    void start(Stage primaryStage);

    /** Shows a message that was generated while the application was starting. */
    void showStartupMessage(String message);

}
