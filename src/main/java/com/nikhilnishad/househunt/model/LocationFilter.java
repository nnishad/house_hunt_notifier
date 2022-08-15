package com.nikhilnishad.househunt.model;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;

import com.nikhilnishad.househunt.model.enums.FurnishType;

public class LocationFilter {
	
	@Id
	private String id;
	@NotNull(message = "Location Identifier required")
	private String locationIdentifier;
	@NotNull(message = "Mininum bed quantity required")
	private int minBedrooms;
	@NotNull(message="Maximum price required")
	private int maxPrice;
	@NotNull(message = "Radius required")
	private int radius;
	@NotNull(message = "Furnish type required")
	private String furnishTypes;
	public String getLocationIdentifier() {
		return locationIdentifier;
	}
	public void setLocationIdentifier(String locationIdentifier) {
		this.locationIdentifier = locationIdentifier;
	}
	public int getMinBedrooms() {
		return minBedrooms;
	}
	public void setMinBedrooms(int minBedrooms) {
		this.minBedrooms = minBedrooms;
	}
	public int getMaxPrice() {
		return maxPrice;
	}
	public void setMaxPrice(int maxPrice) {
		this.maxPrice = maxPrice;
	}
	public int getRadius() {
		return radius;
	}
	public void setRadius(int radius) {
		this.radius = radius;
	}
	public String getFurnishTypes() {
		return furnishTypes;
	}
	public void setFurnishTypes(String furnishTypes) {
		if(furnishTypes.contains(",")) {
			this.furnishTypes=Arrays.asList(furnishTypes.split(",")).stream()
			.map(type->
				FurnishType.fromString(type).name())
			.collect(Collectors.joining(","));
		}
		else {
			this.furnishTypes=FurnishType.fromString(furnishTypes).name();
		}
	}
	public LocationFilter(String locationIdentifier, int minBedrooms, int maxPrice, int radius, String furnishTypes) {
		super();
		this.locationIdentifier = locationIdentifier;
		this.minBedrooms = minBedrooms;
		this.maxPrice = maxPrice;
		this.radius = radius;
		this.furnishTypes = furnishTypes;
	}
	@Override
	public int hashCode() {
		return Objects.hash(furnishTypes, locationIdentifier, maxPrice, minBedrooms, radius);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LocationFilter other = (LocationFilter) obj;
		return Objects.equals(furnishTypes, other.furnishTypes)
				&& Objects.equals(locationIdentifier, other.locationIdentifier) && maxPrice == other.maxPrice
				&& minBedrooms == other.minBedrooms && radius == other.radius;
	}
	public LocationFilter() {
		super();
	}
	
}
