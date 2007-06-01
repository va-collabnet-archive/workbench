package org.dwfa.vodb.jar;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.vodb.types.I_ProcessPathEntries;

import com.sleepycat.je.DatabaseEntry;

public class PathCounter extends TermCounter implements I_ProcessPathEntries {

	public void processPath(DatabaseEntry key, DatabaseEntry value)
			throws Exception {
		if (canceled) {
			throw new InterruptedException();
		}
		count++;
	}

	public Object call() throws Exception {
		AceConfig.getVodb().iteratePaths(this);
		return null;
	}

}
