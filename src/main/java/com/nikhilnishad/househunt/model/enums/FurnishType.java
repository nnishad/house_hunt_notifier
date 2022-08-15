package com.nikhilnishad.househunt.model.enums;

import java.util.Arrays;

public enum FurnishType {

	furnished("1"),
	partFurnished("0"),
	unfurnished("-1");

	public final String type;
	
	FurnishType(String type) {
		this.type = type;
	}
	
	/**
     * @return the Enum representation for the given string.
     * @throws IllegalArgumentException if unknown string.
     */
    public static FurnishType fromString(String s) throws IllegalArgumentException {
        return Arrays.stream(FurnishType.values())
                .filter(v -> v.type.equals(s))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown furnish type: " + s));
    }
	
}
