package org.dwfa.vodb.jar;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.vodb.types.I_ProcessConceptAttributeEntries;

import com.sleepycat.je.DatabaseEntry;

public class ConceptCounter extends TermCounter implements I_ProcessConceptAttributeEntries {

	public void processConceptAttributeEntry(DatabaseEntry key, DatabaseEntry value) throws Exception {
		if (canceled) {
			throw new InterruptedException();
		}
		count++;
	}

	public Object call() throws Exception {
		AceConfig.getVodb().iterateConceptAttributeEntries(this);
		return null;
	}

}
