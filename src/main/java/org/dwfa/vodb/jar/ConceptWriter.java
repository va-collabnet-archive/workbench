package org.dwfa.vodb.jar;

import java.io.OutputStream;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.vodb.types.I_ProcessConceptEntries;

import com.sleepycat.je.DatabaseEntry;

public class ConceptWriter extends TermWriter implements I_ProcessConceptEntries {

	public ConceptWriter(OutputStream outStream) {
		super(outStream);
	}

	public void processConcept(DatabaseEntry key, DatabaseEntry value) throws Exception {
		if (canceled) {
			throw new InterruptedException();
		}
		int size = value.getSize();
		dos.writeInt(size);
		dos.write(value.getData(), value.getOffset(), size);
		count++;
	}

	public Object call() throws Exception {
		AceConfig.getVodb().iterateConcepts(this);
		dos.close();
		return null;
	}

}
