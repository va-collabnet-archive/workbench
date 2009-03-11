package org.dwfa.tapi;

import java.io.IOException;

public interface I_ManifestLocally extends I_Manifest {
	public I_ManifestUniversally universalize() throws IOException, TerminologyException;
	public I_ManifestLocally getExtension(I_ConceptualizeLocally extensionType) throws IOException, TerminologyException;
	public int getNid();
}
