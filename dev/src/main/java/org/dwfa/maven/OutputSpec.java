package org.dwfa.maven;

public class OutputSpec {
	private InputFileSpec[] inputSpecs;
	private I_TransformAndWrite[] writers;
	private I_ReadAndTransform[] constantSpecs;
	
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("\nInputFileSpec: \n");
		for (InputFileSpec is: inputSpecs) {
			b.append(is);
		}
		b.append("\nWriters: \n");
		for (I_TransformAndWrite w: writers) {
			b.append(w);
		}
		return b.toString();
	}
	public InputFileSpec[] getInputSpecs() {
		return inputSpecs;
	}
	public void setInputSpecs(InputFileSpec[] inputSpecs) {
		this.inputSpecs = inputSpecs;
	}
	public I_TransformAndWrite[] getWriters() {
		return writers;
	}
	public void setWriters(I_TransformAndWrite[] writers) {
		this.writers = writers;
	}
	public I_ReadAndTransform[] getConstantSpecs() {
		return constantSpecs;
	}
	public void setConstantSpecs(I_ReadAndTransform[] constantSpecs) {
		this.constantSpecs = constantSpecs;
	}
	
}
