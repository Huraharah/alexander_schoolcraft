package com.group_8.universal_gift_registry.model;
/**@application: UniversalGiftRegistry
 * @author: Alexander Schoolcraft, Benjamin King, Brandon King, Gabe Woolums
 * @date: 3/21/2024
 * @version: 3.0
 */
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class ItemEntity {

    private int itemID;
    private String itemURL;
    private String itemImage;
    private String itemName;
    private String itemSize;
    private String itemColor;
    private Double itemPrice;
    private Boolean purchased;
    private UserEntity user;
    private ListEntity list;
    private BooleanProperty selected = new SimpleBooleanProperty();

	public ItemEntity(int itemID, String itemURL, String itemName, String itemSize, String itemColor, Double itemPrice,
			Boolean purchased, UserEntity user, ListEntity list) {
		this.itemID = itemID;
		this.itemURL = itemURL;
		this.itemName = itemName;
		this.itemSize = itemSize;
		this.itemColor = itemColor;
		this.itemPrice = itemPrice;
		this.purchased = purchased;
		this.user = user;
		this.list = list;
		setSelected(false);
	}

	public ItemEntity(int itemID, String itemURL, String img, String itemName, String itemSize, String itemColor, Double itemPrice,
			Boolean purchased, UserEntity currentUser, ListEntity currentList) {
		this.itemID = itemID;
		this.itemURL = itemURL;
		this.itemImage = img;
		this.itemName = itemName;
		this.itemSize = itemSize;
		this.itemColor = itemColor;
		this.itemPrice = itemPrice;
		this.purchased = purchased;
		this.user = currentUser;
		this.list = currentList;
		setSelected(false);
	}

	public ItemEntity(String itemURL, String imgURL, String itemName, String itemSize, String itemColor, Double itemPrice,
			Boolean purchased, UserEntity currentUser, ListEntity currentList) {
		this.itemURL = itemURL;
		this.itemImage = imgURL;
		this.itemName = itemName;
		this.itemSize = itemSize;
		this.itemColor = itemColor;
		this.itemPrice = itemPrice;
		this.purchased = purchased;
		this.user = currentUser;
		this.list = currentList;
		setSelected(false);
	}

	public String getImgURL() {
		return itemImage;
	}

	public String getItemColor() {
		return itemColor;
	}

	public int getItemID() {
		return itemID;
	}

	public String getItemName() {
		return itemName;
	}

	public Double getItemPrice() {
		return itemPrice;
	}

	public String getItemSize() {
		return itemSize;
	}

	public String getItemURL() {
		return itemURL;
	}

	public ListEntity getList() {
		return list;
	}

	public Boolean getPurchased() {
		return purchased;
	}

	public BooleanProperty getSelected() {
		return selected;
	}

	public UserEntity getUser() {
		return user;
	}

	public boolean isSelected() {
		return this.selected.get();
	}

	public BooleanProperty selectedProperty() {
		return this.selected;
	}

	public void setImgURL(String imgURL) {
		this.itemImage = imgURL;
	}

	public void setItemColor(String itemColor) {
		this.itemColor = itemColor;
	}

	public void setItemID(int itemID) {
		this.itemID = itemID;
	}


	public void setItemName(String itemName) {
		this.itemName = itemName;
	}


	public void setItemPrice(Double itemPrice) {
		this.itemPrice = itemPrice;
	}

    public void setItemSize(String itemSize) {
		this.itemSize = itemSize;
	}


	public void setItemURL(String itemURL) {
		this.itemURL = itemURL;
	}


	public void setList(ListEntity list) {
		this.list = list;
	}


	public void setPurchased(Boolean purchased) {
		this.purchased = purchased;
	}

	public final void setSelected(Boolean selected) {
        this.selected.set(selected);
    }

	public void setSelected(BooleanProperty selected) {
		this.selected = selected;
	}

    public void setUser(UserEntity user) {
		this.user = user;
	}
}
