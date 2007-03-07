package org.dwfa.maven;

import java.io.IOException;
import java.io.Writer;


public interface I_TransformAndWrite {
	public String getOutputEncoding();
	public String getFileName();
	public void init(Writer w, Transform t) throws Exception;
	public void addTransform(I_ReadAndTransform t);
	public void processRec() throws IOException;
	public void close() throws IOException;
	public boolean append();
}
