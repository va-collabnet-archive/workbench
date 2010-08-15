package org.ihtsdo.tk;

import org.ihtsdo.tk.api.TerminologyDI;


public class TS {
	private static TerminologyDI store;
	
	public static void set(TerminologyDI store) {
		TS.store = store;
	}

	public static TerminologyDI get() {
		return store;
	}
}
