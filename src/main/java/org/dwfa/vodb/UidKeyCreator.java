package org.dwfa.vodb;

import java.util.Set;
import java.util.UUID;

import org.dwfa.vodb.bind.ThinIdVersionedBinding;
import org.dwfa.vodb.bind.UuidBinding;
import org.dwfa.vodb.types.ThinIdPart;
import org.dwfa.vodb.types.ThinIdVersioned;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryMultiKeyCreator;

public class UidKeyCreator implements SecondaryMultiKeyCreator {

	private UuidBinding uuidBinding;
	private ThinIdVersionedBinding idBinding;
	
	public UidKeyCreator(UuidBinding uuidBinding, ThinIdVersionedBinding idBinding) {
		super();
		this.uuidBinding = uuidBinding;
		this.idBinding = idBinding;
	}


	@SuppressWarnings("unchecked")
	public void createSecondaryKeys(SecondaryDatabase secDb, 
			DatabaseEntry keyEntry, DatabaseEntry dataEntry, 
			Set results) throws DatabaseException {
		Set<DatabaseEntry> keySet = results;
		ThinIdVersioned id = (ThinIdVersioned) idBinding.entryToObject(dataEntry);
		 for (ThinIdPart p: id.getVersions()) {
			 if (UUID.class.isAssignableFrom(p.getSourceId().getClass())) {
				 UUID secondaryId = (UUID) p.getSourceId();
				 DatabaseEntry entry = new DatabaseEntry();
				 uuidBinding.objectToEntry(secondaryId, entry);
				 keySet.add(entry);
			 }
		 }
	}
}	