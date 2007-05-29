package org.dwfa.ace.api;

import java.io.File;
import java.io.IOException;


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
	
	public static void open(Class<I_ImplementTermFactory> factoryClass, File envHome, boolean readOnly, Long cacheSize) throws InstantiationException, IllegalAccessException, IOException {
		I_ImplementTermFactory factory = factoryClass.newInstance();
		factory.setup(envHome, readOnly, cacheSize);
		set(factory);
	}
	
	@SuppressWarnings("unchecked")
	public static void openDefaultFactory(File envHome, boolean readOnly, Long cacheSize) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		open((Class<I_ImplementTermFactory>) Class.forName("org.dwfa.vodb.VodbEnv"), envHome, readOnly, cacheSize);
	}
	
}

