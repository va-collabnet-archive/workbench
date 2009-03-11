package org.dwfa.tapi;

import java.io.IOException;

public interface I_ExtendUniversally extends I_ManifestUniversally, I_Extend {
	public I_ExtendLocally localize() throws IOException, TerminologyException;

}
