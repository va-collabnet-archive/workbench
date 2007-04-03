package org.dwfa.vodb;

import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.vodb.bind.ThinImageBinder;

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
		I_ImageVersioned image = (I_ImageVersioned) imageBinder.entryToObject(dataEntry);
		intBinder.objectToEntry(image.getConceptId(), resultEntry);
		return true;
	}

}
