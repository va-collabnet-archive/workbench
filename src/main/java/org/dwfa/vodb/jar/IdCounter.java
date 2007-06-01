package org.dwfa.vodb.jar;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.vodb.types.I_ProcessIdEntries;

import com.sleepycat.je.DatabaseEntry;

public class IdCounter extends TermCounter implements I_ProcessIdEntries {

	public Object call() throws Exception {
		AceConfig.getVodb().iterateIdEntries(this);
		return null;
	}

	public void processId(DatabaseEntry key, DatabaseEntry value) throws Exception {
		if (canceled) {
			throw new InterruptedException();
		}
		count++;
	}

}
