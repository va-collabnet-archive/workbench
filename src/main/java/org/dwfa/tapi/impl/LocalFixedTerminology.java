package org.dwfa.tapi.impl;

import org.dwfa.tapi.I_StoreLocalFixedTerminology;

public class LocalFixedTerminology {
	
	private static I_StoreLocalFixedTerminology store;
	
	public static I_StoreLocalFixedTerminology getStore() {
		return store;
	}
	
	public static void setStore(I_StoreLocalFixedTerminology store) {
		LocalFixedTerminology.store = store;
	}
}
