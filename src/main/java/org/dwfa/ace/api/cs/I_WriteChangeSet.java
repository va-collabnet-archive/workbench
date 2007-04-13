package org.dwfa.ace.api.cs;

import java.io.IOException;

import org.dwfa.ace.api.I_GetConceptData;

public interface I_WriteChangeSet {
	
	public void writeChanges(I_GetConceptData conceptBean, int version) throws IOException;

}
