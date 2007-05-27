package org.dwfa.ace.api;


public class LocalVersionedTerminology {
	private static I_TermFactory factory;
	
	public static I_TermFactory get() {
		return factory;
	}
	
	public static void set(I_TermFactory factory) {
		if (LocalVersionedTerminology.factory == null ||
				LocalVersionedTerminology.factory == factory) {
			LocalVersionedTerminology.factory = factory;
		} else {
			throw new RuntimeException("LocalVersionedTerminology.factory is already set to: " + LocalVersionedTerminology.factory);
		}
	}
}

