package com.smart181.jibs.service;

import static org.junit.Assert.*;

import org.junit.Test;

public class NameValidatorTest {

	@Test
	public void testAlphaName() {
		assertTrue(NameValidator.isValid("thisIsMyName"));
	}

	@Test
	public void testNumericalName() {
		assertTrue(NameValidator.isValid("12345"));
	}
	
	@Test
	public void testAlphaNumericalName() {
		assertTrue(NameValidator.isValid("thisIsMyName2"));
	}
	
	@Test
	public void testFullCompatibleName1() {
		assertTrue(NameValidator.isValid("thisIsMyName_2"));
	}
	
	@Test
	public void testFullCompatibleName2() {
		assertTrue(NameValidator.isValid("this-is-my-name_2"));
	}
	
	@Test
	public void testFullCompatibleName3() {
		assertTrue(NameValidator.isValid("this-is-my-name.2"));
	}
	
	@Test
	public void testNullName() {
		assertFalse(NameValidator.isValid(null));
	}
	
	@Test
	public void testEmptyName() {
		assertFalse(NameValidator.isValid(""));
	}
	
	@Test
	public void testInvalidName1() {
		assertFalse(NameValidator.isValid("#356"));
	}
	
	@Test
	public void testInvalidName2() {
		assertFalse(NameValidator.isValid("&356"));
	}
	
	@Test
	public void testInvalidName3() {
		assertFalse(NameValidator.isValid("&356()"));
	}
}
