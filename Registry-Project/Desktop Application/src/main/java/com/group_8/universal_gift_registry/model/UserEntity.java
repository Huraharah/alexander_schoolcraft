package com.group_8.universal_gift_registry.model;
/**@application: UniversalGiftRegistry
 * @author: Alexander Schoolcraft, Benjamin King, Brandon King, Gabe Woolums
 * @date: 3/21/2024
 * @version: 3.0
 */
public class UserEntity {

    private String email;
    private String streetAddress;
    private String city;
    private String state;
    private String zip;
    private String firstName;
    private String lastName;
    private String salt;
    private Boolean showAddress;

	public UserEntity() {
	}


	public UserEntity(String email, String streetAddress, String city, String state, String zip, String firstName,
			String lastName, String salt) {
		this.email = email;
		this.streetAddress = streetAddress;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.firstName = firstName;
		this.lastName = lastName;
		this.salt = salt;
	}


	public String getCity() {
		return city;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getState() {
		return state;
	}

	public String getStreetAddress() {
		return streetAddress;
	}

	public String getZip() {
		return zip;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}


	public String getSalt() {
		return salt;
	}


	public void setSalt(String salt) {
		this.salt = salt;
	}
	
	public Boolean getShowAddress() {
		return this.showAddress;
	}
	
	public void setShowAddress(Boolean show) {
		this.showAddress = show;
	}
	
}
