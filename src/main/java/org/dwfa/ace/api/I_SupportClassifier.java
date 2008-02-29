package org.dwfa.ace.api;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.tapi.TerminologyException;

public interface I_SupportClassifier extends I_TermFactory {

	public void writeRel(I_RelVersioned rel) throws IOException;
	
	public I_RelVersioned newRelationship(UUID relUuid, 
			int uuidType,
			int conceptNid, 
			int relDestinationNid,
			int pathNid,
			int version,
			int relStatusNid,
			int relTypeNid,
			int relCharacteristicNid,
			int relRefinabilityNid, 
			int relGroup) throws TerminologyException, IOException;
}
