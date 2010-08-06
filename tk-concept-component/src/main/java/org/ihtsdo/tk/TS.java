package org.ihtsdo.tk;

import org.ihtsdo.tk.api.TerminologyStoreDI;


public class TS {
	private static TerminologyStoreDI store;
	
	public static void set(TerminologyStoreDI store) {
		TS.store = store;
	}

	public static TerminologyStoreDI get() {
		return store;
	}
}
