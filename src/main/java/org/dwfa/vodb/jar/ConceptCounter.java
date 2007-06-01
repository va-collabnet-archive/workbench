package org.dwfa.vodb.jar;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.vodb.types.I_ProcessConceptEntries;

import com.sleepycat.je.DatabaseEntry;

public class ConceptCounter extends TermCounter implements I_ProcessConceptEntries {

	public void processConcept(DatabaseEntry key, DatabaseEntry value) throws Exception {
		if (canceled) {
			throw new InterruptedException();
		}
		count++;
	}

	public Object call() throws Exception {
		AceConfig.getVodb().iterateConcepts(this);
		return null;
	}

}
