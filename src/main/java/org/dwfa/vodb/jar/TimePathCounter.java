package org.dwfa.vodb.jar;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.vodb.types.I_ProcessTimeBranchEntries;

import com.sleepycat.je.DatabaseEntry;

public class TimePathCounter extends TermCounter implements I_ProcessTimeBranchEntries {

	public void processTimeBranch(DatabaseEntry key, DatabaseEntry value) throws Exception {
		if (canceled) {
			throw new InterruptedException();
		}
		count++;
	}

	public Object call() throws Exception {
		AceConfig.getVodb().iterateTimeBranch(this);
		return null;
	}
}