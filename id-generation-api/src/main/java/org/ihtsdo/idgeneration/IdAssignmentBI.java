package org.ihtsdo.idgeneration;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.omg.CORBA.IdentifierHelper;

public interface IdAssignmentBI {

	public enum IDENTIFIER {
		TDEUID(1), SCTID(2), SNOMEDID(3), CTV3ID(4), WBUID(5);
		
		private int idNumber;
		
		IDENTIFIER(int idNumber){
			this.setIdNumber(idNumber);
		}

		public void setIdNumber(int idNumber) {
			this.idNumber = idNumber;
		}

		public int getIdNumber() {
			return idNumber;
		}
		
	}

	public Long getSCTID(UUID componentUuid) throws Exception;

	public String getSNOMEDID(UUID componentUuid) throws Exception;

	public String getCTV3ID(UUID componentUuid) throws Exception;

	public HashMap<UUID, Long> getSCTIDList(List<UUID> componentUuidList) throws Exception;

	public Long createSCTID(UUID componentUuid, Integer namespaceId, String partitionId, 
			String releaseId, String executionId, String moduleId) throws Exception;

	public String createSNOMEDID(UUID componentUuid, String parentSnomedId) throws Exception;

	public String createCTV3ID(UUID componentUuid) throws Exception;

	public HashMap<IDENTIFIER, String> createConceptIds(UUID componentUuid, String parentSnomedId, 
			Integer namespaceId, String partitionId, String releaseId, String executionId, 
			String moduleId) throws Exception;

	public HashMap<UUID, Long> createSCTIDList(List<UUID> componentUuidList, Integer namespaceId, String partitionId, 
			String releaseId, String executionId, String moduleId) throws Exception;

	public HashMap<UUID, HashMap<IDENTIFIER, String>> createConceptIDList(HashMap<UUID, String> componentUUIDandParentSnomedId, 
			Integer namespaceId, String partitionId, String releaseId,
			String executionId, String moduleId) throws Exception;

}
