package org.ihtsdo.tk;

import org.ihtsdo.tk.api.TerminologyDI;

/**
 * Ts is short for Terminology store...
 * 
 * @author kec
 *
 */
public class Ts {
	private static TerminologyDI store;
	
	public static void set(TerminologyDI store) {
		Ts.store = store;
	}

	public static TerminologyDI get() {
		return store;
	}
}
