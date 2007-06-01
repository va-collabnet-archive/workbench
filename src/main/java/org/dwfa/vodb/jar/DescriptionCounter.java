package org.dwfa.vodb.jar;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.vodb.types.I_ProcessDescriptionEntries;

import com.sleepycat.je.DatabaseEntry;

public class DescriptionCounter extends TermCounter implements
		I_ProcessDescriptionEntries {

	public void processDesc(DatabaseEntry key, DatabaseEntry value)
			throws Exception {
		if (canceled) {
			throw new InterruptedException();
		}
		count++;
	}

	public Object call() throws Exception {
		AceConfig.getVodb().iterateDescriptionEntries(this);
		return null;
	}

}
