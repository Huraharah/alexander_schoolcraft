package com.group_8.universal_gift_registry;
/**@application: UniversalGiftRegistry
 * @author: Alexander Schoolcraft, Benjamin King, Brandon King, Gabe Woolums
 * @date: 3/21/2024
 * @version: 3.0
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

import com.group_8.universal_gift_registry.util.GetConnectionUtil;
import com.group_8.universal_gift_registry.util.HashingUtils;
import com.group_8.universal_gift_registry.util.QuestionCodeUtil;
import com.group_8.universal_gift_registry.util.ShowAlertUtil;

/**JFX Controller class for the Answer_Security_Questions scene
 * If the user has forgotten their password, presents the user with the two questions that they set up at registration, then verifies
 * that the answers are correct, then resets the user's password to the new value.
 */
public class AnswerSecurityQuestionsController {

    @FXML
    private Label questionOne;
    @FXML
    private Label questionTwo;
    @FXML
    private PasswordField answerOne;
    @FXML
    private PasswordField answerTwo;
    @FXML
    private PasswordField newPassword;
    @FXML
    private PasswordField confirmPassword;

    private String userEmail;
    private String userSalt;
    
    @FXML
    private void initialize() {
    	Tooltip passwordTooltip = new Tooltip("Password must contain:\n-At least 1 lowercase letter\n-At least 1 uppercase letter\n"
    			+ "-At least 1 number\n-At least 1 special character\n-At least 12 characters");
    	newPassword.setTooltip(passwordTooltip);
    	newPassword.focusedProperty().addListener(new ChangeListener<Boolean>() {
    		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
    			if (newValue) { 
    	            Bounds boundsInScreen = newPassword.localToScreen(newPassword.getBoundsInLocal());
    	            double posX = boundsInScreen.getMinX();
    	            double posY = boundsInScreen.getMinY() - (passwordTooltip.getHeight() - newPassword.getHeight());
    	            passwordTooltip.show(newPassword.getScene().getWindow(), posX, posY);
    	        } else { 
    	            passwordTooltip.hide();
    		}
    	}});
    }

    /**Sets the email of the user that has forgotten their password
     * @param email
     */
    public void setUserEmail(String email) {
        this.userEmail = email;
        fetchUserDetails();
    }

    /**Given the user that has forgotten their password, retrieves their questions to display on the screen. 
     */
    private void fetchUserDetails() {
        try (Connection conn = GetConnectionUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT [SecurityQuestion1Code], [SecurityQuestion2Code], [Salt] FROM [User] WHERE Email = ?")) {
            pstmt.setString(1, userEmail);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    questionOne.setText(QuestionCodeUtil.getQuestionFromCode(rs.getString("SecurityQuestion1Code")));
                    questionTwo.setText(QuestionCodeUtil.getQuestionFromCode(rs.getString("SecurityQuestion2Code")));
                    this.userSalt = rs.getString("Salt");
                }
            }
        } catch (Exception e) {
            ShowAlertUtil.showAlert("Error", "Server error: " + e);
        }
    }

    /**When the user clicks on the submit button, verifies that all of the fields are full, then compares the hashes of the given answers with the hashes stored in the database,
     * and if it all passes, updates the password hash within the database with the new password hash.
     */
    @FXML
    private void handleSubmit() {
        if (!validateInputs()) {
            return;
        }
        try {
            if (verifyAnswers()) {
                updatePassword();
            } else {
                ShowAlertUtil.showAlert("Verification Failed", "Your security answers are incorrect.");
            }
        } catch (Exception e) {
            ShowAlertUtil.showAlert("Error", "Server Error: " + e);
        }
    }

    /**Verifies that all of the fields are filled in, and that the password and confirmation are the same. 
     * @return Boolean if all of the inputs are valid
     */
    private boolean validateInputs() {
        if (answerOne.getText().isEmpty() || answerTwo.getText().isEmpty() ||
            newPassword.getText().isEmpty() || confirmPassword.getText().isEmpty()) {
            ShowAlertUtil.showAlert("Validation Error", "All fields must be filled out.");
            return false;
        }
        if (!newPassword.getText().equals(confirmPassword.getText())) {
            ShowAlertUtil.showAlert("Validation Error", "The new passwords do not match.");
            return false;
        }
        return true;
    }

    /**Verifies that the given answers are correct against the answers on the server,
     * @return Boolean if the answers are correct
     */
    private boolean verifyAnswers() {
	    try (Connection conn = GetConnectionUtil.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement("SELECT [SecurityAnswer1], [SecurityAnswer2] FROM [User] WHERE Email = ?")) {
	        pstmt.setString(1, userEmail);

	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                String storedAnswer1 = rs.getString("SecurityAnswer1");
	                String storedAnswer2 = rs.getString("SecurityAnswer2");

	                String hashedInputAnswer1 = HashingUtils.hashWithExistingSalt(answerOne.getText(), userSalt);
	                String hashedInputAnswer2 = HashingUtils.hashWithExistingSalt(answerTwo.getText(), userSalt);

	                return storedAnswer1.equals(hashedInputAnswer1) && storedAnswer2.equals(hashedInputAnswer2);
	            }
	        }
	    } catch (Exception e) {
	        ShowAlertUtil.showAlert("Database Error", "Error verifying security answers: " + e.getMessage());
	    }
	    return false;
	}

    /**If all of the validation checks are complete and correct, updates the password on the database with the new password. 
     */
    private void updatePassword() {
    	if(verifyPassword()) {
	        try {
	            String newHashedPassword = HashingUtils.hashWithExistingSalt(newPassword.getText(), userSalt);
	
	            try (Connection conn = GetConnectionUtil.getConnection();
	                 PreparedStatement pstmt = conn.prepareStatement("UPDATE [User] SET [Password] = ? WHERE [Email] = ?")) {
	                pstmt.setString(1, newHashedPassword);
	                pstmt.setString(2, userEmail);
	                int affectedRows = pstmt.executeUpdate();
	
	                if (affectedRows > 0) {
	                    ShowAlertUtil.showInformationAlert("Success", "Your password has been updated.");
	                    Stage stage = (Stage) questionOne.getScene().getWindow();
	                    stage.close();
	                } else {
	                    ShowAlertUtil.showAlert("Update Error", "Failed to update password.");
	                }
	            }
	        } catch (Exception e) {
	            ShowAlertUtil.showAlert("Database Error", "Error updating password: " + e.getMessage());
	        }
    	}
    }

	private boolean verifyPassword() {
		if (!newPassword.getText().equals(confirmPassword.getText())) {
            ShowAlertUtil.showAlert("Validation Error", "Passwords do not match");
            return false;
        }
        if (!(newPassword.getText().length() >= 12 && newPassword.getText().matches(".*[A-Z].*") &&
        		newPassword.getText().matches(".*[a-z].*") && newPassword.getText().matches(".*[0-9].*") &&
        		newPassword.getText().matches(".*[^A-Za-z0-9].*"))) {
        	ShowAlertUtil.showAlert("Password Error", "Password must contain at least 1 lowercase, \n1 uppercase, 1 number, and 1 special character, \nand must be at least 12 characters in length");
        	return false;
        }
        return true;
	}
}

