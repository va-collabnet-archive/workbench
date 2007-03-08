package org.dwfa.vodb;

import org.dwfa.vodb.bind.ThinImageBinder;
import org.dwfa.vodb.types.ThinImageVersioned;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

public class ConceptKeyForImageCreator implements SecondaryKeyCreator {
	ThinImageBinder imageBinder = new ThinImageBinder();
	EntryBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);

	public boolean createSecondaryKey(SecondaryDatabase secDb,
            DatabaseEntry keyEntry, 
            DatabaseEntry dataEntry,
            DatabaseEntry resultEntry)
			throws DatabaseException {
		ThinImageVersioned image = (ThinImageVersioned) imageBinder.entryToObject(dataEntry);
		intBinder.objectToEntry(image.getConceptId(), resultEntry);
		return true;
	}

}
