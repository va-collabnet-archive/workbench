package org.dwfa.vodb.jar;

import java.io.OutputStream;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.vodb.types.I_ProcessPaths;

import com.sleepycat.je.DatabaseEntry;

public class PathWriter extends TermWriter implements I_ProcessPaths {

	public PathWriter(OutputStream outStream) {
		super(outStream);
	}

	public void processPath(DatabaseEntry key, DatabaseEntry value)
			throws Exception {
		if (canceled) {
			throw new InterruptedException();
		}
		int size = value.getSize();
		dos.writeInt(size);
		dos.write(value.getData(), value.getOffset(), size);
		count++;
	}

	public Object call() throws Exception {
		AceConfig.getVodb().iteratePaths(this);
		dos.close();
		return null;
	}

}
