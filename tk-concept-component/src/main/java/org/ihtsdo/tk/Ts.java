package org.ihtsdo.tk;

import org.ihtsdo.tk.api.TerminologyStoreDI;

/**
 * Ts is short for Terminology store...
 * 
 * @author kec
 *
 */
public class Ts {
	private static TerminologyStoreDI store;
	
	public static void set(TerminologyStoreDI store) {
		Ts.store = store;
	}

	public static TerminologyStoreDI get() {
		return store;
	}
}
