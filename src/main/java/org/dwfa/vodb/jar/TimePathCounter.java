package org.dwfa.vodb.jar;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.vodb.types.I_ProcessTimeBranch;

import com.sleepycat.je.DatabaseEntry;

public class TimePathCounter extends TermCounter implements I_ProcessTimeBranch {

	public void processTimeBranch(DatabaseEntry key, DatabaseEntry value) throws Exception {
		if (canceled) {
			throw new InterruptedException();
		}
		count++;
	}

	public Object call() throws Exception {
		AceConfig.vodb.iterateTimeBranch(this);
		return null;
	}
}