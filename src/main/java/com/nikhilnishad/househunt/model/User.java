package com.nikhilnishad.househunt.model;

import java.util.List;

public class User {
	private String userId;
	private String emailId;
	private List<LocationFilter> localtionFilterList;
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getEmailId() {
		return emailId;
	}
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	public List<LocationFilter> getLocaltionFilterList() {
		return localtionFilterList;
	}
	public void setLocaltionFilterList(List<LocationFilter> localtionFilterList) {
		this.localtionFilterList = localtionFilterList;
	}
	public User(String userId, String emailId, List<LocationFilter> localtionFilterList) {
		super();
		this.userId = userId;
		this.emailId = emailId;
		this.localtionFilterList = localtionFilterList;
	}
}
