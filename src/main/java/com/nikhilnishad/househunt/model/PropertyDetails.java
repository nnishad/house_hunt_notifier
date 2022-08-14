package com.nikhilnishad.househunt.model;

import java.io.Serializable;
import java.util.Objects;

public class PropertyDetails implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */	
	private String id;
	private String address;
	private String price;
	private String contactFormLink;
	private String phoneNumber;
	private String propertyUrl;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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

	@Override
	public int hashCode() {
		return Objects.hash(address, contactFormLink, id, phoneNumber, price, propertyUrl);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyDetails other = (PropertyDetails) obj;
		return Objects.equals(address, other.address) && Objects.equals(contactFormLink, other.contactFormLink)
				&& Objects.equals(id, other.id) && Objects.equals(phoneNumber, other.phoneNumber)
				&& Objects.equals(price, other.price) && Objects.equals(propertyUrl, other.propertyUrl);
	}

	@Override
	public String toString() {
		return "[\n\n address = " + address + 
				" ,\n price = " + price + 
				" ,\n\n phoneNumber = " + phoneNumber + 
				" ,\n\n propertyUrl = " + propertyUrl + 
				" ,\n contactFormLink = " + contactFormLink+
				"\n\n]";
	}

}
