package org.ihtsdo.db.bdb.concept;

import java.io.IOException;

import org.ihtsdo.db.bdb.Bdb;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class ReadWriteDataVersion {
	public static int get(int nid) throws IOException {
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();
		data.setPartial(ConceptData.OFFSETS.DATA_VERSION.getOffset(), ConceptData.OFFSETS.DATA_VERSION.getBytes(), true);
		IntegerBinding.intToEntry(nid, key);
		try {
			if (Bdb.getConceptDb().getReadWrite().get(null, key, data, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
				return IntegerBinding.entryToInt(data);
			} else {
				return Integer.MIN_VALUE;
			}
		} catch (DatabaseException e) {
			throw new IOException(e);
		}
	}
}
