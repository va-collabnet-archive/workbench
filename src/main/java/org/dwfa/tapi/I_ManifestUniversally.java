package org.dwfa.tapi;

import java.io.IOException;
import java.io.Serializable;

public interface I_ManifestUniversally extends I_Manifest, Serializable {
	public I_ManifestLocally localize() throws IOException, TerminologyException;
	I_ManifestUniversally getExtension(I_ConceptualizeUniversally extensionType, I_StoreUniversalFixedTerminology extensionServer) throws IOException, TerminologyException;
}
