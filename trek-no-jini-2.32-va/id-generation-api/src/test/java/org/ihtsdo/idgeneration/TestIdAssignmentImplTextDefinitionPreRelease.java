package org.ihtsdo.idgeneration;

import java.util.UUID;


import junit.framework.TestCase;
import org.ihtsdo.idgeneration.IdAssignmentBI;
import org.ihtsdo.idgeneration.IdAssignmentImpl;

public class TestIdAssignmentImplTextDefinitionPreRelease extends TestCase {
	public void testApi() {
		try {
			IdAssignmentBI idAssignment = new IdAssignmentImpl();
			
			//Individual creation for any component
			UUID componentUuid = UUID.randomUUID();
			Long newSctId = idAssignment.createSCTID(componentUuid, null, "01", null, null, null);
			System.out.println("New SCTID: " + newSctId); 
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
