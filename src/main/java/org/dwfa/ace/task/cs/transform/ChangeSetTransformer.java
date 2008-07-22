package org.dwfa.ace.task.cs.transform;

import java.beans.Encoder;
import java.beans.Expression;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.UUID;
import java.util.logging.Logger;

public abstract class ChangeSetTransformer {

	protected String outputSuffix = ".xml"; 
	
	protected XMLEncoder getEncoder(File changeset) throws FileNotFoundException {
		XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(
				new FileOutputStream(new File(changeset.getParent(), changeset.getName() + outputSuffix))));

		encoder.setPersistenceDelegate(UUID.class,
				new java.beans.PersistenceDelegate() {
					protected Expression instantiate(Object oldInstance, Encoder out) {
						return new Expression(oldInstance, oldInstance.getClass(), "fromString", new Object[] { oldInstance.toString() });
					}
				});
		
		return encoder;
	}

	public String getOutputSuffix() {
		return outputSuffix;
	}
	
	public void setOutputSuffix(String outputSuffix) {
		this.outputSuffix = outputSuffix;
	}

	public abstract void transform(Logger logger, File changeset) throws Exception;

}
