package org.dwfa.vodb.jar;

import java.io.OutputStream;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.vodb.types.I_ProcessDescriptions;

import com.sleepycat.je.DatabaseEntry;

public class DescritionWriter extends TermWriter implements I_ProcessDescriptions {

	public DescritionWriter(OutputStream outStream) {
		super(outStream);
	}
	public void processDesc(DatabaseEntry key, DatabaseEntry value) throws Exception {
		if (canceled) {
			throw new InterruptedException();
		}
		int size = value.getSize();
		dos.writeInt(size);
		dos.write(value.getData(), value.getOffset(), size);
		count++;
	}
	public Object call() throws Exception {
		AceConfig.getVodb().iterateDescriptions(this);
		dos.close();
		return null;
	}


}
