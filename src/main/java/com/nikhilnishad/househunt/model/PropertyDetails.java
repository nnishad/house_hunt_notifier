package com.nikhilnishad.househunt.model;

public class PropertyDetails {
	
	private String address;
	private String price;
	private String contactFormLink;
	private String phoneNumber;
	private String propertyUrl;

	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getContactFormLink() {
		return contactFormLink;
	}
	public void setContactFormLink(String contactFormLink) {
		this.contactFormLink = contactFormLink;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getPropertyUrl() {
		return propertyUrl;
	}
	public void setPropertyUrl(String propertyUrl) {
		this.propertyUrl = propertyUrl;
	}
	public PropertyDetails(String address, String price, String contactFormLink, String phoneNumber,
			String propertyUrl) {
		super();
		this.address = address;
		this.price = price;
		this.contactFormLink = contactFormLink;
		this.phoneNumber = phoneNumber;
		this.propertyUrl = propertyUrl;
	}
	public PropertyDetails() {
		super();
	}
	@Override
	public String toString() {
		return "[\n\n address = " + address + " ,\n price = " + price + " ,\n contactFormLink = "
				+ contactFormLink + " ,\n\n phoneNumber = " + phoneNumber + " ,\n\n propertyUrl = " + propertyUrl + "\n\n]";
	}	
	
}
