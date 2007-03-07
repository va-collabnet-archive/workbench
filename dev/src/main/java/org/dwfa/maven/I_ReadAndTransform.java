package org.dwfa.maven;

public interface I_ReadAndTransform {
	public void setup(Transform transformer);
	public String transform(String input) throws Exception;
	public String getLastTransform();
	public String getName();
	public void setName(String name);
}
