package org.dwfa.ace.api.cs;

import java.io.IOException;
import java.io.Serializable;

import org.dwfa.ace.api.I_GetConceptData;

public interface I_WriteChangeSet extends Serializable {

	public void open() throws IOException;

	public void writeChanges(I_GetConceptData conceptBean, int version)
			throws IOException;

	public void commit() throws IOException;

}
