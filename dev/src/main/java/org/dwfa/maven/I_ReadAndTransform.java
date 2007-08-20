package org.dwfa.maven;

import java.io.IOException;

public interface I_ReadAndTransform {
	public void setup(Transform transformer) throws IOException, ClassNotFoundException;
   public void cleanup(Transform transformer) throws Exception;
	public String transform(String input) throws Exception;
	public I_ReadAndTransform getChainedTransform();
	public String getLastTransform();
	public String getName();
	public void setName(String name);
	public void setColumnId(int id);
	public int getColumnId();
}
