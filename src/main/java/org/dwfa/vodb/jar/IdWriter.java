package org.dwfa.vodb.jar;

import java.io.OutputStream;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.vodb.types.I_ProcessIds;

import com.sleepycat.je.DatabaseEntry;

public class IdWriter extends TermWriter implements I_ProcessIds {

	public IdWriter(OutputStream outStream) {
		super(outStream);
	}
	public void processId(DatabaseEntry key, DatabaseEntry value) throws Exception {
		if (canceled) {
			throw new InterruptedException();
		}
		int size = value.getSize();
		dos.writeInt(size);
		dos.write(value.getData(), value.getOffset(), size);
		count++;
	}
	public Object call() throws Exception {
		AceConfig.vodb.iterateIds(this);
		dos.close();
		return null;
	}

}
