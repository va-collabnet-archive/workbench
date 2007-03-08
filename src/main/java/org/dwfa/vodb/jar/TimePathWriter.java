package org.dwfa.vodb.jar;

import java.io.OutputStream;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.vodb.types.I_ProcessTimeBranch;

import com.sleepycat.je.DatabaseEntry;

public class TimePathWriter extends TermWriter implements I_ProcessTimeBranch {

	public TimePathWriter(OutputStream outStream) {
		super(outStream);
	}

	public void processTimeBranch(DatabaseEntry key, DatabaseEntry value) throws Exception {
		if (canceled) {
			throw new InterruptedException();
		}
		int size = value.getSize();
		dos.writeInt(size);
		dos.write(value.getData(), value.getOffset(), size);
		count++;
	}

	public Object call() throws Exception {
		AceConfig.vodb.iterateTimeBranch(this);
		dos.close();
		return null;
	}
}