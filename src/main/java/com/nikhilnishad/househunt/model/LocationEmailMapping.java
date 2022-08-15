package com.nikhilnishad.househunt.model;

import java.util.List;

import org.springframework.data.annotation.Id;

public class LocationEmailMapping {
	
	@Id
	private String id;
	private LocationFilter locationPreference;
	private List<String> userEmailList;
	public LocationFilter getLocationPreference() {
		return locationPreference;
	}
	public void setLocationPreference(LocationFilter locationPreference) {
		this.locationPreference = locationPreference;
	}
	public List<String> getUserEmailList() {
		return userEmailList;
	}
	public void setUserEmailList(List<String> userEmailList) {
		this.userEmailList = userEmailList;
	}
}
