package com.group_8.universal_gift_registry.util;
/**@application: UniversalGiftRegistry
 * @author: Alexander Schoolcraft, Benjamin King, Brandon King, Gabe Woolums
 * @date: 3/21/2024
 * @version: 3.0
 */

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * 
 */
public class ShowAlertUtil {

    /**showAlert method builds and displays a warning-type alert message to the user
     * This alert is specifically a scrolling-type alert box, in case the message is
     * too long to fit into the standardized window size (such as a file path).
     * @param title: The header of the alert
     * @param content: The content of the alert box
     */
    public static void showAlert(String title, String content) {
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(false);

        textArea.setFont(javafx.scene.text.Font.font("Monospaced", 12));

        textArea.setPrefRowCount(10);
        textArea.setPrefColumnCount(50);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 1);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(expContent);

        alert.showAndWait();
    }
    
    /**showInformationAlert shows an information-type alert (A blue circle icon) to the user
     * This method specifically is used to show an action has been completed successfully
     * @param title: The header of the alert
     * @param content: The content of the alert box
     */
    public static void showInformationAlert(String title, String content) {
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(false);

        textArea.setFont(javafx.scene.text.Font.font("Monospaced", 12));

        textArea.setPrefRowCount(10);
        textArea.setPrefColumnCount(50);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 1);
    	
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(expContent);
        
        alert.showAndWait();
    }
}
