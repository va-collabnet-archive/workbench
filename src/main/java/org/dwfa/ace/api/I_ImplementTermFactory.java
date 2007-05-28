package org.dwfa.ace.api;

import java.io.File;
import java.io.IOException;

public interface I_ImplementTermFactory extends I_TermFactory {
	public void setup(File envHome, boolean readOnly, Long cacheSize) throws IOException;
}
