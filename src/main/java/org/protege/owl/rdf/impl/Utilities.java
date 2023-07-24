package org.protege.owl.rdf.impl;

public class Utilities {

	private Utilities() {
		
	}
	
	public static boolean isInteger(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch( NumberFormatException ex ) {
			return false;
		}
	}
}
