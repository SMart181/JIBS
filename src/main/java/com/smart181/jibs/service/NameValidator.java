package com.smart181.jibs.service;

public class NameValidator {
	private final static String LOGICAL_NAME_VALIDATOR = "^([a-z_A-Z_0-9]|_|-|\\.)+$";
	
	public static boolean isValid(String name) {
		if((name == null) || (name.isEmpty()))
			return false;
		
		return name.matches(LOGICAL_NAME_VALIDATOR);
	}
}
