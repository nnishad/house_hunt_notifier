package com.nikhilnishad.househunt.model;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

public class RequestAddUser {
	
	@Email(message = "Please enter valid email")
	@NotNull(message = "Email Required")
	private String email;
	
	@NotNull(message = "Please enter your location prefernce")
	@Valid
	private LocationFilter locationPref;
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public LocationFilter getLocationPref() {
		return locationPref;
	}
	public void setLocationPref(LocationFilter locationPref) {
		this.locationPref = locationPref;
	}
	public RequestAddUser(String email, LocationFilter locationPref) {
		super();
		this.email = email;
		this.locationPref = locationPref;
	}
	public RequestAddUser() {
		super();
	}
	
	
}
