package org.dwfa.vodb.jar;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.vodb.types.I_ProcessImages;

import com.sleepycat.je.DatabaseEntry;

public class ImageCounter extends TermCounter implements I_ProcessImages {

	public void processImages(DatabaseEntry key, DatabaseEntry value)
			throws Exception {
		if (canceled) {
			throw new InterruptedException();
		}
		count++;
	}

	public Object call() throws Exception {
		AceConfig.vodb.iterateImages(this);
		return null;
	}

}
