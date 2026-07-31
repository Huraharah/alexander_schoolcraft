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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.group_8.universal_gift_registry.model.ItemEntity;
import com.group_8.universal_gift_registry.model.ListEntity;
import com.group_8.universal_gift_registry.model.UserEntity;
import com.group_8.universal_gift_registry.util.GetConnectionUtil;
import com.group_8.universal_gift_registry.util.ShowAlertUtil;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**JFX Controller class for the Edit List scene
 * Contains a WebView element that allows the user to browse the web, much like you can using Chrome or
 * FireFox, to select items to add to their list, or remove items from the list.
 */
public class EditListController {
	@FXML
	private AnchorPane Edits;
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
    private TableColumn<ItemEntity, Boolean> Delete;
    @FXML
    private WebView WebView;
    @FXML
    private Button Remove;
    @FXML
    private Button Add;
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
	private ListEntity currentList;
	private UserEntity currentUser;

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
        Delete.setCellFactory(CheckBoxTableCell.forTableColumn(Delete));
        Delete.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        CurrentList.setEditable(true);
        Delete.setEditable(true);
        WebView.getEngine().load("https://www.amazon.com");
        //Sets up the action for the Go Button, but restricts navigation to Amazon.com (due to how data scraping works)
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
    
    /**This method scrapes the web page data for the specific details needed to fill the item entity in both
     * the database as well as the persistent entity within the front side.
     */
    public void getCurrentProductDetails() {
        Platform.runLater(() -> {
        	Boolean isAddToCartButtonPresent = (Boolean) WebView.getEngine().executeScript(
                    "document.querySelector('.add-to-cart-button') !== null"
                );
        	if(isAddToCartButtonPresent) {
	            String URL = WebView.getEngine().getLocation();
	            String imageUrl = (String) WebView.getEngine().executeScript(
	                "document.getElementById('landingImage') ? document.getElementById('landingImage').src : ''"
	            );
	            String productName = (String) WebView.getEngine().executeScript(
	                "document.getElementById('productTitle') ? document.getElementById('productTitle').innerText.trim() : ''"
	            );
	
	            String priceText = (String) WebView.getEngine().executeScript(
	                "document.querySelector('.a-offscreen') ? document.querySelector('.a-offscreen').textContent.trim() : ''"
	            );
	            String priceWithoutDollar = priceText.replace("$", "").trim();
	            Double price = Double.parseDouble(priceWithoutDollar);
	            String selectedSize = (String) WebView.getEngine().executeScript(
	                "var size = document.querySelector('#variation_size_name .selection'); size ? size.textContent.trim() : ''"
	            );
	            String selectedColor = (String) WebView.getEngine().executeScript(
	                "var color = document.querySelector('#variation_color_name .selection'); color ? color.textContent.trim() : ''"
	            );
	            ItemEntity newItem = new ItemEntity(URL, imageUrl, productName, selectedSize, selectedColor, price, false, currentUser, currentList);
	            try {
					insertNewItem(newItem);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
	            loadListItems();
        	}
        	else {
        		ShowAlertUtil.showAlert("Navigation Error", "Please navigate to an item page.");
        	}
        });
    }

    /**When the Add button is pressed, this method calls the scraper method to gather the details of
     * the item to push to the database.
     * @param event
     */
	@FXML
    private void handleAddAction(ActionEvent event) {
        try {
            getCurrentProductDetails();
        } catch (Exception e) {
            ShowAlertUtil.showAlert("Error", "Failed to add the product: " + e.getMessage());
        }
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

    /**Checks the "Delete" check boxes within the Item table for the list, and removes the checked items
     * from the list.
     * @param event
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @FXML
    private void handleRemoveAction(ActionEvent event) throws SQLException, ClassNotFoundException {
        List<ItemEntity> itemsToRemove = new ArrayList<>();

        for (ItemEntity item : CurrentList.getItems()) {
            if (item.isSelected()) {
                itemsToRemove.add(item);
            }
        }

        if (!itemsToRemove.isEmpty()) {
            removeItemsFromDatabase(itemsToRemove);
            CurrentList.getItems().removeAll(itemsToRemove);
            CurrentList.refresh();
            String listName = currentList.getListName();
            ShowAlertUtil.showInformationAlert("Success", "Items removed from list " + listName);
        }
        else {
        	ShowAlertUtil.showAlert("Removal Error", "Please selecte an item/items to delete");
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

            Stage stage = (Stage) ( Edits.getScene()).getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            ShowAlertUtil.showAlert("Navigation Error", "Error when trying to return to the home page: " + e.getMessage());
        }
    }

	/**After the scraper method pulls all of the needed information from the web page, this method actually
	 * parses the information and then posts it to the database under the current user and list.
	 * @param item
	 * @throws ClassNotFoundException
	 */
    private void insertNewItem(ItemEntity item) throws ClassNotFoundException {
        String query = "INSERT INTO [Item] ([ItemURL], [ItemImage], [ItemName], [ItemSize], [ItemColor], [ItemPrice], [Purchased], [Email], [ListID]) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = GetConnectionUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, item.getItemURL());
            pstmt.setString(2, item.getImgURL());
            pstmt.setString(3, item.getItemName());
            pstmt.setString(4, item.getItemSize());
            pstmt.setString(5, item.getItemColor());
            pstmt.setDouble(6, item.getItemPrice());
            pstmt.setBoolean(7, item.getPurchased());
            pstmt.setString(8, currentUser.getEmail());
            pstmt.setInt(9, currentList.getListID());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating item failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setItemID(generatedKeys.getInt(1));
                    String listName = currentList.getListName();
                    ShowAlertUtil.showInformationAlert("Succsess!", "Item successfully added to list " + listName);
                } else {
                    throw new SQLException("Creating item failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            ShowAlertUtil.showAlert("Database Error", "Error adding new item: " + e.getMessage());
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
	                currentUser,
	                currentList
	            );
	            items.add(item);
	        }

	        CurrentList.setItems(items);
	    } catch (SQLException | ClassNotFoundException e) {

	        ShowAlertUtil.showAlert("Database Error", "Error loading list items: " + e.getMessage());
	    }
	}

    /**When the user decides to remove items from their list, this method actually deletes them out of
     * the database.
     * @param itemsToRemove
     * @throws SQLException
     * @throws ClassNotFoundException
     */
	private void removeItemsFromDatabase(List<ItemEntity> itemsToRemove) throws SQLException, ClassNotFoundException {
        String query = "DELETE FROM [Item] WHERE [ItemID] = ?";
        try (Connection conn = GetConnectionUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            for (ItemEntity item : itemsToRemove) {
                pstmt.setInt(1, item.getItemID());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            ShowAlertUtil.showAlert("Database Error", "Error deleting items: " + e.getMessage());
        }
    }

    public void setCurrentList(ListEntity list) {
		currentList = list;
		loadListItems();
	}

    public void setCurrentUser(UserEntity user) {
		currentUser = user;
	}
}
