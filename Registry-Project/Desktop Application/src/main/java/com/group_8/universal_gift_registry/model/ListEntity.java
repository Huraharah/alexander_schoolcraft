package com.group_8.universal_gift_registry.model;

import java.time.LocalDate;

/**@application: UniversalGiftRegistry
 * @author: Alexander Schoolcraft, Benjamin King, Brandon King, Gabe Woolums
 * @date: 3/21/2024
 * @version: 3.0
 */
public class ListEntity {

    private int listID;
    private String occasion;
    private String listName;
    private UserEntity user;
    private LocalDate eventDate;

    public ListEntity() {
    }

	public ListEntity(int listID, String occasion, String listName, UserEntity user) {
		this.listID = listID;
		this.occasion = occasion;
		this.listName = listName;
		this.user = user;
	}

	public ListEntity(String listName, String occasion) {
		this.occasion = occasion;
		this.listName = listName;
	}

	public int getListID() {
		return listID;
	}

	public String getListName() {
		return listName;
	}

	public String getOccasion() {
		return occasion;
	}

	public UserEntity getUser() {
		return user;
	}
	
	public LocalDate getEventDate() {
		return this.eventDate;
	}

	public void setListID(int listID) {
		this.listID = listID;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}

	public void setOccasion(String occasion) {
		this.occasion = occasion;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}
	
	public void setEventDate(LocalDate date) {
		this.eventDate = date;
	}
}
