package com.group_8.universal_gift_registry;
/**@application: UniversalGiftRegistry
 * @author: Alexander Schoolcraft, Benjamin King, Brandon King, Gabe Woolums
 * @date: 3/21/2024
 * @version: 3.0
 */

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import com.group_8.universal_gift_registry.model.ListEntity;
import com.group_8.universal_gift_registry.model.UserEntity;
import com.group_8.universal_gift_registry.util.GetConnectionUtil;
import com.group_8.universal_gift_registry.util.OccasionUtil;
import com.group_8.universal_gift_registry.util.ShowAlertUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**JFX Controller class for the Searched Lists scene
 * After the user has selected another user to purchase items for, this controller allows the user to
 * choose one of the selected user's lists to purchase off of.
 */
public class SearchedListsController {
	@FXML
	private TableView<ListEntity> ListTable;
	@FXML
	private TableColumn<ListEntity, String> ListName;
	@FXML
	private TableColumn<ListEntity, String> Occasion;
	@FXML
	private TableColumn<ListEntity, LocalDate> EventDate;
	@FXML
	private Text ListHeader;
	@FXML
	private Label noListsFound;
	@FXML
	private AnchorPane anchor;
	private UserEntity currentUser;
	private UserEntity searchedUser;

    /**When 'loader.load()' is called from the prior screen, initializes the page, and does the initial
     * setup for proper display
     */
    @FXML
    private void initialize() {
        ListName.setCellValueFactory(new PropertyValueFactory<>("listName"));
        Occasion.setCellValueFactory(new PropertyValueFactory<>("occasion"));
        EventDate.setCellValueFactory(new PropertyValueFactory<>("eventDate"));
    }

    /**Identifies which list the user has selected to purchase from, and then calls the method to set up
     * the next scene.
     * @param event
     * @throws IOException
     */
    @FXML
    private void handlePurchaseList(ActionEvent event) throws IOException {
    	ListEntity selectedList = ListTable.getSelectionModel().getSelectedItem();
        if (selectedList != null) {
            switchToPurchaseScene(selectedList);
        } else {
            ShowAlertUtil.showAlert("Selection Error", "Please select a list to purchase from.");
        }
    }
    
    /**Returns the user to the home screen.
     * @param event
     */
    @FXML
    private void handleBackToHome(ActionEvent event) {
        try {
            searchedUser = null;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Landing_Page.fxml"));
            Parent root = loader.load();

            LandingPageController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = (Stage) (anchor.getScene()).getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            ShowAlertUtil.showAlert("Navigation Error", "Error when trying to return to the home page: " + e.getMessage());
        }
    }

	/**Populates the table of lists with registry lists owned by the searched user.
	 */
    void loadUserLists() {
	    ObservableList<ListEntity> userLists = FXCollections.observableArrayList();
	    String query = "SELECT [ListID], [ListName], [Occasion], [Email], [eventDate] FROM [List] WHERE [Email] = ?";

	    try (Connection conn = GetConnectionUtil.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(query)) {

	        pstmt.setString(1, searchedUser.getEmail());
	        ResultSet rs = pstmt.executeQuery();

	        while (rs.next()) {
	            int listID = rs.getInt("ListID");
	            String listName = rs.getString("ListName");
	            String occasionCode = rs.getString("Occasion");
	            String fullOccasionName = OccasionUtil.getOccasionForCode(occasionCode);
	            LocalDate eventDate = null;
	            java.sql.Date dbDate = rs.getDate("eventDate");
	            if (dbDate != null) {
	                eventDate = dbDate.toLocalDate();
	            }
	            ListEntity newList = new ListEntity(listID, fullOccasionName, listName, searchedUser);
	            newList.setEventDate(eventDate);
	            userLists.add(newList);
	        }

	        ListTable.setItems(userLists);

	        if (userLists.isEmpty()) {
	            noListsFound.setText("No lists found for user.");
	            noListsFound.setVisible(true);
	        } else {
	            noListsFound.setVisible(false);
	        }

	    } catch (SQLException | ClassNotFoundException e) {
	        ShowAlertUtil.showAlert("Database Error", "Error loading user lists: " + e.getMessage());
	    }
	}

    /**Sets up the scene for the purchase screen, so that the user can purchase items off of the selected
     * list.
     * @param selectedList
     * @throws IOException
     */
    private void switchToPurchaseScene(ListEntity selectedList) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Purchases.fxml"));
        Parent root = loader.load();

        PurchasesController purchasesController = loader.getController();
        purchasesController.setCurrentList(selectedList);
        purchasesController.setCurrentUser(currentUser);
        purchasesController.setSearchedUser(searchedUser);


        Stage newStage = new Stage();
        newStage.setScene(new Scene(root));
        newStage.show();

        Stage stage = (Stage) anchor.getScene().getWindow();
        stage.close();
    }
    
    void setCurrentUser(UserEntity user) {
    	currentUser = user;
    }

    void setSearchedUser(UserEntity user) {
    	searchedUser = user;
        ListHeader.setText(searchedUser.getFirstName() + " " + searchedUser.getLastName().charAt(0) + ".'s Registry Lists");
        loadUserLists();
    }
}
