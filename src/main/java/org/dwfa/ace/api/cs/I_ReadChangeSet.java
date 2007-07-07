package org.dwfa.ace.api.cs;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public interface I_ReadChangeSet extends Serializable {
	
	public long nextCommitTime() throws IOException, ClassNotFoundException;

	public void readUntil(long time) throws IOException, ClassNotFoundException;

	public void read() throws IOException, ClassNotFoundException;

	public void setChangeSetFile(File changeSetFile);
	
	public void setCounter(I_Count counter);
	

}
