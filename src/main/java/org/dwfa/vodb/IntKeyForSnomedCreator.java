package org.dwfa.vodb;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

public class IntKeyForSnomedCreator implements SecondaryKeyCreator {
	EntryBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);
			
	public IntKeyForSnomedCreator() {
		super();
	}


	public boolean createSecondaryKey(SecondaryDatabase secDb,
            DatabaseEntry keyEntry, 
            DatabaseEntry dataEntry,
            DatabaseEntry resultEntry)
			throws DatabaseException {
		Integer id = (Integer) intBinder.entryToObject(dataEntry);
		intBinder.objectToEntry(id, resultEntry);
		return true;
	}
	
}
