package com.group_8.universal_gift_registry;
/**@application: UniversalGiftRegistry
 * @author: Alexander Schoolcraft, Benjamin King, Brandon King, Gabe Woolums
 * @date: 3/21/2024
 * @version: 3.0
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.group_8.universal_gift_registry.util.HashingUtils;
import com.group_8.universal_gift_registry.util.QuestionCodeUtil;
import com.group_8.universal_gift_registry.util.ShowAlertUtil;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

/**JFX Controller class for the Set_Security_Questions scene
 * Called when the user registers for an account within the system, and prompts them to pick two of the security questions,
 * and provide answers for them, to provide a secure method to reset a forgotten password.
 */
public class SetSecurityQuestionsController {

	    @FXML
	    private ComboBox<String> secQuestionOne;
	    @FXML
	    private ComboBox<String> secQuestionTwo;
	    @FXML
	    private PasswordField secAnswerOne;
	    @FXML
	    private PasswordField secAnswerTwo;
	    private String salt;
	    private ArrayList<String> hashedAnswers = new ArrayList<String>();

	    /**When the stage.initModality and stage.initOwner methods are called in the Login_Register scene, this sets up the 
	     * scene for the user to select and answer their two security questions. 
	     */
	    @FXML
	    public void initialize() {
	        List<String> questions = Arrays.asList(
	            "What was your childhood nickname?",
	            "In what city did your parents meet?",
	            "What is the name of your favorite childhood friend?",
	            "What is your favorite team?",
	            "What was your dream job as a child?",
	            "What is your favorite movie?",
	            "In what town was your first job?",
	            "What was the make and model of your first car?",
	            "Where did you vacation last year?",
	            "What is the name of your pet?"
	        );

	        secQuestionOne.setItems(FXCollections.observableArrayList(questions));
	        secQuestionTwo.setItems(FXCollections.observableArrayList(questions));
	    }
	    
	    /**When the user clicks on the submit button, this will verify that all fields are filled out properly,
	     * then parses the data from each field, and encodes it according to what it is, and prepares it to transmit
	     * to the database.
	     * @param event
	     */
	    @FXML
	    private void handleSubmit(ActionEvent event) {
	    	if (!verifyFields()) {
	    		return;
	    	}
	    	
	    	try {
	             String questionCode1 = QuestionCodeUtil.getCodeFromQuestion(secQuestionOne.getValue());
	             String questionCode2 = QuestionCodeUtil.getCodeFromQuestion(secQuestionTwo.getValue());
	             String hashedAnswer1 = HashingUtils.hashWithExistingSalt(secAnswerOne.getText(), salt);
	             String hashedAnswer2 = HashingUtils.hashWithExistingSalt(secAnswerTwo.getText(), salt);
	             hashedAnswers.add(questionCode1);
	             hashedAnswers.add(hashedAnswer1);
	             hashedAnswers.add(questionCode2);
	             hashedAnswers.add(hashedAnswer2);

	             Stage stage = (Stage) secQuestionOne.getScene().getWindow();
	             ShowAlertUtil.showInformationAlert("Success", "Questions and answers set");
	             stage.close();
	         } catch (Exception e) {
	             System.err.println("Error processing security questions: " + e.getMessage());
	             e.printStackTrace();
	         }
	    }
	    
	    /**After the user clicks on the submit button, verifies that all of the fields are filled in or selected,
	     * and that the two questions selected are different questions, the allows the submit button method to finish.
	     * @return True if the fields pass all checks, false otherwise.
	     */
	    private boolean verifyFields() {
	        if (secQuestionOne.getValue() == null || secQuestionOne.getValue().trim().isEmpty() ||
	            secQuestionTwo.getValue() == null || secQuestionTwo.getValue().trim().isEmpty() ||
	            secAnswerOne.getText().trim().isEmpty() || secAnswerTwo.getText().trim().isEmpty()) {
	            ShowAlertUtil.showAlert("Error:", "Please fill out all fields");
	            return false;
	        }

	        if (secQuestionOne.getValue().equals(secQuestionTwo.getValue()) ||
	            secAnswerOne.getText().equalsIgnoreCase(secAnswerTwo.getText())) {
	            ShowAlertUtil.showAlert("Error", "Questions and answers must be distinct.");
	            return false;
	        }

	        return true;
	    }

	    
	    public void setSalt(String salt) {
	    	this.salt = salt;
	    }

		public ArrayList<String> getHashedAnswers() {
			return hashedAnswers;
		}

}
