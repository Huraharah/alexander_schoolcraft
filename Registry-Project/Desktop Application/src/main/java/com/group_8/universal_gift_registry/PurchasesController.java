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

import com.group_8.universal_gift_registry.model.ItemEntity;
import com.group_8.universal_gift_registry.model.ListEntity;
import com.group_8.universal_gift_registry.model.UserEntity;
import com.group_8.universal_gift_registry.util.GetConnectionUtil;
import com.group_8.universal_gift_registry.util.ShowAlertUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**JFX Controller class for the Purchases scene
 * Enables the user to navigate through the Amazon.com web store, or even select an item off of the list
 * and directly navigate to it, then purchase it and mark it as purchased on the list.
 */
public class PurchasesController {
	@FXML
	private AnchorPane Purchase;
    @FXML
    private TableView<ItemEntity> CurrentList;
    @FXML
    private TableColumn<ItemEntity, String> ItemName;
    @FXML
    private TableColumn<ItemEntity, String> URL;
    @FXML
    private TableColumn<ItemEntity, String> ItemColor;
    @FXML
    private TableColumn<ItemEntity, String> ItemSize;
    @FXML
    private TableColumn<ItemEntity, Double> ItemPrice;
    @FXML
    private TableColumn<ItemEntity, Boolean> Purchased;
    @FXML
    private WebView WebView;
    @FXML
    private Button GoTo;
    @FXML
    private Button PurchasedItem;
    @FXML
    private TextField AddressBar;
    @FXML
    private Button GoButton;
    @FXML
    private Button Back;
    @FXML
    private Button Forward;
    @FXML
    private Button ReturnToHome;
    @FXML
    private Button showAddress;
	private ListEntity currentList;
	private UserEntity currentUser;
	protected static UserEntity searchedUser;
	
    /**When 'loader.load()' is called from the prior screen, initializes the page, and does the initial
     * setup for proper display
     */
	@FXML
    private void initialize() {
        ItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        URL.setCellValueFactory(new PropertyValueFactory<>("itemURL"));
        ItemColor.setCellValueFactory(new PropertyValueFactory<>("itemColor"));
        ItemSize.setCellValueFactory(new PropertyValueFactory<>("itemSize"));
        ItemPrice.setCellValueFactory(new PropertyValueFactory<>("itemPrice"));
        Purchased.setCellValueFactory(new PropertyValueFactory<>("purchased"));
        WebView.getEngine().load("https://www.amazon.com");
        CurrentList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        GoButton.setOnAction(e -> {
            if (AddressBar.getText().isEmpty()) {
            	ShowAlertUtil.showAlert("Navigation Error", "Please provide a URL");
            	return;
            }
        	
            String url = AddressBar.getText();
  
            boolean isAmazonUrl = url.contains("amazon.com");

            if (!isAmazonUrl) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Restricted Access");
                alert.setHeaderText(null);
                alert.setContentText("You are only allowed to visit Amazon.com");
                alert.showAndWait();
                return;
            }

            if (!url.startsWith("http") && isAmazonUrl) {
                url = "http://" + url;
            }

            WebView.getEngine().load(url);
        });
    }

	/**The next two methods enable to Back and Forward buttons on the scene to act like they would
	 * in a real browser.
	 * @param event
	 */
	@FXML
    private void handleBackAction(ActionEvent event) {
        final WebHistory history = WebView.getEngine().getHistory();
        if (history.getCurrentIndex() > 0) {
            history.go(-1);
        }
    }

    @FXML
    private void handleForwardAction(ActionEvent event) {
        final WebHistory history = WebView.getEngine().getHistory();
        if (history.getCurrentIndex() < history.getEntries().size() - 1) {
            history.go(1);
        }
    }

    /**When the user selects an item from the list, this action handler will make the WebView pane load
     * the URL of the item selected.
     * @param event
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @FXML
    private void handleGoToAction(ActionEvent event) throws SQLException, ClassNotFoundException {
        ItemEntity selectedItem = CurrentList.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            WebView.getEngine().load(selectedItem.getItemURL());
        } else {
            ShowAlertUtil.showAlert("Selection Error", "Please select an item.");
        }
    }

    /**After the user has purchased an item from the list, this action handler will update the item information
     * to mark it as purchased.
     * @param event
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @FXML
    private void handlePurchaseAction(ActionEvent event) throws SQLException, ClassNotFoundException {
        ItemEntity selectedItem = CurrentList.getSelectionModel().getSelectedItem();
        if (selectedItem != null && !selectedItem.getPurchased()) {
            String updateQuery = "UPDATE [Item] SET [Purchased] = 1 WHERE [ItemID] = ?";
            try (Connection conn = GetConnectionUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                pstmt.setInt(1, selectedItem.getItemID());
                pstmt.executeUpdate();
                selectedItem.setPurchased(true);
                CurrentList.refresh();
                ShowAlertUtil.showInformationAlert("Success!", "Item information updated to \"Purchased\"");
            } catch (SQLException e) {
                ShowAlertUtil.showAlert("Database Error", "Error updating item status: " + e.getMessage());
            }
        } else {
            ShowAlertUtil.showAlert("Selection Error", "Please select an unpurchased item.");
        }
    }

    /**Returns the user to the home screen.
     * @param event
     */
    @FXML
    private void handleReturnToHome(ActionEvent event) {
        try {
            currentList = null;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Landing_Page.fxml"));
            Parent root = loader.load();

            LandingPageController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = (Stage) ( Purchase.getScene()).getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            ShowAlertUtil.showAlert("Navigation Error", "Error when trying to return to the home page: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleShowAddress(ActionEvent event) throws IOException {
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Address_Popup.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initModality(Modality.NONE);
        stage.initOwner(Purchase.getScene().getWindow());
        stage.show();
    }

    @FXML
    private void handleBackToLists (ActionEvent event) throws IOException {
    	try {
    		currentList = null;
    		
	    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Searched_Lists.fxml"));
	    	Parent root = loader.load();
	    	
	    	SearchedListsController controller = loader.getController();
	    	controller.setCurrentUser(currentUser);
	    	controller.setSearchedUser(searchedUser);
	    	
	    	Stage stage = (Stage) (Purchase.getScene()).getWindow();
	    	stage.setScene(new Scene(root));
	    	stage.show();
    	}catch (IOException e) {
    		ShowAlertUtil.showAlert("Navigation Error", "Error when trying to return to User's Lists: " + e.getMessage());
    	}
    }
    /**A part of the initialization of the page, this method is called by the prior scene before switching
     * over to this one, so that when it does load, the items on the currently selected list are displayed
     * properly.
     */
    public void loadListItems() {
	    ObservableList<ItemEntity> items = FXCollections.observableArrayList();
	    String query = "SELECT * FROM [Item] WHERE [ListID] = ?";

	    try (Connection conn = GetConnectionUtil.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(query)) {

	        pstmt.setInt(1, currentList.getListID());
	        ResultSet rs = pstmt.executeQuery();

	        while (rs.next()) {
	            ItemEntity item = new ItemEntity(
	                rs.getInt("ItemID"),
	                rs.getString("ItemURL"),
	                rs.getString("ItemImage"),
	                rs.getString("ItemName"),
	                rs.getString("ItemSize"),
	                rs.getString("ItemColor"),
	                rs.getDouble("ItemPrice"),
	                rs.getBoolean("Purchased"),
	                searchedUser,
	                currentList
	            );
	            items.add(item);
	        }

	        CurrentList.setItems(items);
	    } catch (SQLException | ClassNotFoundException e) {

	        ShowAlertUtil.showAlert("Database Error", "Error loading list items: " + e.getMessage());
	    }
	}

	public void setCurrentList(ListEntity list) {
		currentList = list;
		loadListItems();
	}

    public void setCurrentUser(UserEntity user) {
		currentUser = user;
	}

    public void setSearchedUser(UserEntity user) {
		searchedUser = user;
        showAddress.setVisible(searchedUser.getShowAddress());
	}
}
