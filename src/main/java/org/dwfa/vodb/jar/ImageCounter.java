package org.dwfa.vodb.jar;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.vodb.types.I_ProcessImageEntries;

import com.sleepycat.je.DatabaseEntry;

public class ImageCounter extends TermCounter implements I_ProcessImageEntries {

	public void processImages(DatabaseEntry key, DatabaseEntry value)
			throws Exception {
		if (canceled) {
			throw new InterruptedException();
		}
		count++;
	}

	public Object call() throws Exception {
		AceConfig.getVodb().iterateImages(this);
		return null;
	}

}
