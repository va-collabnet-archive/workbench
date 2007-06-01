package org.dwfa.vodb.jar;

import java.io.OutputStream;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.vodb.types.I_ProcessRelationshipEntries;

import com.sleepycat.je.DatabaseEntry;

public class RelationshipWriter extends TermWriter implements I_ProcessRelationshipEntries {

	public RelationshipWriter(OutputStream outStream) {
		super(outStream);
	}

	public void processRel(DatabaseEntry key, DatabaseEntry value) throws Exception {
		if (canceled) {
			throw new InterruptedException();
		}
		int size = value.getSize();
		dos.writeInt(size);
		dos.write(value.getData(), value.getOffset(), size);
		count++;
	}

	public Object call() throws Exception {
		AceConfig.getVodb().iterateRelationships(this);
		dos.close();
		return null;
	}

}
