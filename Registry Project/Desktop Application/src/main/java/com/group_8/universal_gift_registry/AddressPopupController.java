package com.group_8.universal_gift_registry;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class AddressPopupController {
	@FXML
	private Text headline;
	@FXML
	private AnchorPane anchor;
	@FXML
	private TextArea addressBox;
	
	@FXML
	public void initialize() {
		String firstName = PurchasesController.searchedUser.getFirstName();
		String lastName = PurchasesController.searchedUser.getLastName();
		String address = PurchasesController.searchedUser.getStreetAddress();
		String city = PurchasesController.searchedUser.getCity();
		String state = PurchasesController.searchedUser.getState();
		String zip = PurchasesController.searchedUser.getZip();
		
		headline.setText(firstName + " " + lastName.charAt(0) + ".'s Address");
		addressBox.setText(firstName + " " + lastName + "\n" + address + "\n" +
				city + ", " + state + ", " + zip);
	}
	@FXML
	public void closeWindow(ActionEvent event) {
        Stage stage = (Stage) anchor.getScene().getWindow();
        stage.close();
	}
}
