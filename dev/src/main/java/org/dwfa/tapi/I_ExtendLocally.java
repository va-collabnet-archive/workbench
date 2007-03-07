package org.dwfa.tapi;

import java.io.IOException;

public interface I_ExtendLocally extends I_ManifestLocally, I_Extend {
	public I_ExtendUniversally universalize() throws IOException, TerminologyException;

}
