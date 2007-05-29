package org.dwfa.vodb.jar;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.vodb.types.I_ProcessRelationships;

import com.sleepycat.je.DatabaseEntry;

public class RelationshipCounter extends TermCounter implements
		I_ProcessRelationships {

	public Object call() throws Exception {
		AceConfig.getVodb().iterateRelationships(this);
		return null;
	}

	public void processRel(DatabaseEntry key, DatabaseEntry value)
			throws Exception {
		if (canceled) {
			throw new InterruptedException();
		}
		count++;
	}

}
