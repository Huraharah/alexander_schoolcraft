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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**JFX Controller class for the Landing Page scene
 * Enables the user to view their current lists, select one to edit, or create a new list.
 * Also allows for the user to search for another user's lists to purchase off of it.
 * Uses the currentUser persistent UserEntity to keep track of who is logged in.
 * Creates either the searchedUser UserEntity or currentList ListEntity, depending on the path
 * that the user takes.
 */
public class LandingPageController {
	@FXML
	private AnchorPane anchor;
    @FXML
    private TableView<ListEntity> listOfLists;
    @FXML
    private TableColumn<ListEntity, String> listName;
    @FXML
    private TableColumn<ListEntity, String> Occasion;
    @FXML
    private TableColumn<ListEntity, LocalDate> EventDate;
    @FXML
    private Label noListsFound;
    @FXML
    private Button newList;
    @FXML
    private Button searchLists;
    @FXML
    private TextField searchEmail;
    @FXML
    private Button editList;
	private UserEntity currentUser;
    
    /**When 'loader.load()' is called from the prior screen, initializes the page, and does the initial
     * setup for proper display
     */
	@FXML
    private void initialize() {
        listName.setCellValueFactory(new PropertyValueFactory<>("listName"));
        Occasion.setCellValueFactory(new PropertyValueFactory<>("occasion"));
        EventDate.setCellValueFactory(new PropertyValueFactory<>("eventDate"));
    }

    /**Checks for a selected list from the table of lists, then, if found, moves onto the Edit List
     * page, creating a persistent ListEntity of the list selected to edit
     * @param event
     * @throws IOException
     */
    @FXML
    private void handleEditListAction(ActionEvent event) throws IOException {
        ListEntity selectedList = listOfLists.getSelectionModel().getSelectedItem();
        if (selectedList != null) {
            switchToEditListPage(selectedList);
        } else {
            ShowAlertUtil.showAlert("Selection Error", "Please select a list to edit.");
        }
    }
    
    /**Allows the user to delete a List from the server, so that it no longer shows up when people search for their lists to
     * purchase from
     * @param event
     * @throws IOException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @FXML
    private void handleDeleteListAction(ActionEvent event) throws IOException, SQLException, ClassNotFoundException {
        ListEntity selectedList = listOfLists.getSelectionModel().getSelectedItem();
        if (selectedList != null) {
            String query1 = "DELETE FROM [Item] WHERE [ListID] = ?";
            String query2 = "DELETE FROM [List] WHERE [ListID] = ?";
            
            try (Connection conn = GetConnectionUtil.getConnection();
   	             PreparedStatement pstmt = conn.prepareStatement(query1)) {
            		pstmt.setInt(1, selectedList.getListID());
            		pstmt.executeUpdate();
   	             }
            try (Connection conn = GetConnectionUtil.getConnection();
      	         PreparedStatement pstmt = conn.prepareStatement(query2)) {
      		        pstmt.setInt(1, selectedList.getListID());
      		        pstmt.executeUpdate();
      	         }
            listOfLists.getItems().remove(selectedList);
            listOfLists.refresh();
            ShowAlertUtil.showInformationAlert("Success", "List deleted from server");
        } else {
            ShowAlertUtil.showAlert("Selection Error", "Please select a list to delete.");
        }
    }

    /**Moves the user to the New List page to generate a new list.
     * @param event
     * @throws IOException
     */
    @FXML
    private void handleNewListAction(ActionEvent event) throws IOException {
       Stage stage = (Stage) anchor.getScene().getWindow();
	stage.close();
	
	FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/New_List.fxml"));
	Parent root = loader.load();
	
	NewListController newListController = loader.getController();
	newListController.setCurrentUser(currentUser);
	
	Stage newStage = new Stage();
	newStage.setScene(new Scene(root));
	newStage.show();
    }

    /**Checks the search box for an email address, and then connects to the database to pull that user, if found.
     * Then, generates a new UserEntity to keep track of which user was searched for, and moves to the Search List page.
     * @param event
     * @throws IOException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @FXML
    private void handleSearchListAction(ActionEvent event) throws IOException, SQLException, ClassNotFoundException {
    	UserEntity searchedUser = new UserEntity();
    	String query = "SELECT * FROM [User] WHERE Email = ?";
    	
    	if(searchEmail.getText() == null) {
    		ShowAlertUtil.showAlert("Search Error", "Please provide an email to search for");
    		return;
    	}
        String email = searchEmail.getText();
        
        if(!(email.contains("@") && (email.contains(".com") || email.contains(".org") || email.contains(".edu") || email.contains(".gov") || email.contains(".net") 
        		|| email.contains(".biz") || email.contains(".mil") || email.contains(".info") || email.contains(".co")))) {
        	ShowAlertUtil.showAlert("Error", "Please provide a valid email");
        	return;
        }
        
        try (Connection conn = GetConnectionUtil.getConnection();
		     PreparedStatement pstmt = conn.prepareStatement(query)) {

		    pstmt.setString(1, email);
		    ResultSet rs = pstmt.executeQuery();
		    
		    if (!rs.next()) {
		    	ShowAlertUtil.showAlert("Search Error", "No account with that email");
		    	return;
		    }
		    else {
		        searchedUser.setEmail(rs.getString("Email"));
		        searchedUser.setFirstName(rs.getString("First_Name"));
		        searchedUser.setLastName(rs.getString("Last_Name"));
		        searchedUser.setStreetAddress(rs.getString("Street_Address"));
		        searchedUser.setCity(rs.getString("City"));
		        searchedUser.setState(rs.getString("State"));
		        searchedUser.setZip(rs.getString("Zip"));
		        searchedUser.setShowAddress(rs.getBoolean("ShowAddress"));
		    }
		    switchToSearchLists(searchedUser);
			}
        
    }

	/**Populates the table of lists with registry lists owned by the current user.
	 */
    private void loadUserLists() {
	    ObservableList<ListEntity> userLists = FXCollections.observableArrayList();
	    String query = "SELECT [ListID], [ListName], [Occasion], [Email], [eventDate] FROM [List] WHERE [Email] = ?";

	    try (Connection conn = GetConnectionUtil.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(query)) {

	        pstmt.setString(1, currentUser.getEmail());
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
	            ListEntity newList = new ListEntity(listID, fullOccasionName, listName, currentUser);
            	newList.setEventDate(eventDate);
	            
	            userLists.add(newList);
	        }

	        listOfLists.setItems(userLists);

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

    /**Called from the prior scene to pass the currently logged in user as a persistent UserEntity.
     * @param user: Current logged on user
     */
    public void setCurrentUser(UserEntity user) {
		currentUser = user;
        loadUserLists();
	}

    /**If the user selects a list of their own to edit, this method will switch the scene over to the
     * edit list scene, passing both the selected list, as well as the current user, to add or remove
     * items from the list
     * @param selectedList
     * @throws IOException
     */
    private void switchToEditListPage(ListEntity selectedList) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Edit_Lists.fxml"));
        Parent root = loader.load();

        EditListController editListController = loader.getController();
        editListController.setCurrentList(selectedList);
        editListController.setCurrentUser(currentUser);

        Stage newStage = new Stage();
        newStage.setScene(new Scene(root));
        newStage.show();

        Stage stage = (Stage) anchor.getScene().getWindow();
        stage.close();
    }

    /**If the user is looking to purchase for another user, this method will switch over to the searched
     * user's lists, allowing for the logged-in user to select a list to purchase from.
     * @param searchedUser
     * @throws IOException
     */
    private void switchToSearchLists(UserEntity searchedUser) throws IOException {
        Stage stage = (Stage) anchor.getScene().getWindow();
        stage.close();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Searched_Lists.fxml"));
        Parent root = loader.load();

        SearchedListsController searchedListsController = loader.getController();
        searchedListsController.setCurrentUser(currentUser);
        searchedListsController.setSearchedUser(searchedUser);
        searchedListsController.loadUserLists();

        Stage newStage = new Stage();
        newStage.setScene(new Scene(root));
        newStage.show();
    }
    
    /**When the user chooses to log out of the system, will remove the current user, and return the user to the login screen.
     * @throws IOException
     */
    @FXML
    private void handleLogOut() throws IOException {
        Stage stage = (Stage) anchor.getScene().getWindow();
        stage.close();
        
        currentUser = null;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Login_Register.fxml"));
        Parent root = loader.load();

        Stage newStage = new Stage();
        newStage.setScene(new Scene(root));
        newStage.show();
    }
}
