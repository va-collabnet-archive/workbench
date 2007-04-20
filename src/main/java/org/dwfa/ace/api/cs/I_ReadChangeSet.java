package org.dwfa.ace.api.cs;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public interface I_ReadChangeSet extends Serializable {
	
	public void read() throws IOException, ClassNotFoundException;

	public void setChangeSetFile(File changeSetFile);
	

}
