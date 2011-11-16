package org.ihtsdo.idgeneration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ihtsdo.idgen.ws.data.SCTIDRequest;
import org.ihtsdo.idgen.ws.data.SNOMEDIDRequest;

import com.termmed.genid.SctIdGen;
import com.termmed.genid.SctIdGenImpl;


public class IdAssignmentDerbyImpl implements IdAssignmentBI {

	SctIdGen idGenStub = new SctIdGenImpl();

	static {
		IdAssignmentFactory.getInstance().registerProduct(IdAssignmentFactory.DERBY_DB_IMPL, IdAssignmentDerbyImpl.class);
	}

	public IdAssignmentDerbyImpl() {
		super();
	}

	@Override
	public Long getSCTID(UUID componentUuid) throws Exception {
		SCTIDRequest request = new SCTIDRequest();
		request.setComponentUuid(componentUuid.toString());

		Long sctid = idGenStub.getSCTID(componentUuid.toString());

		return sctid;
	}

	@Override
	public String getSNOMEDID(UUID componentUuid) throws Exception {
		SNOMEDIDRequest request = new SNOMEDIDRequest();
		request.setComponentUuid(componentUuid.toString());
		String resonse = idGenStub.getSNOMEDID(componentUuid.toString());
		return resonse;
	}

	@Override
	public String getCTV3ID(UUID componentUuid) throws Exception {
		String ctv3id = idGenStub.getCTV3ID(componentUuid.toString());
		return ctv3id;
	}

	@Override
	public HashMap<UUID, Long> getSCTIDList(List<UUID> componentUuidList) throws Exception {
		List<String> requestList = new ArrayList<String>();
		for (UUID componentUuid : componentUuidList) {
			requestList.add(componentUuid.toString());
		}

		HashMap<String, Long> sctidList = idGenStub.getSCTIDList(requestList);
		HashMap<UUID, Long> result = new HashMap<UUID, Long>();
		Set<String> keys = sctidList.keySet();
		for (String string : keys) {
			result.put(UUID.fromString(string), sctidList.get(string));
			System.out.println("[componentUUID: " + string + " SCTID " + sctidList.get(string) + "]");
		}

		return result;
	}

	@Override
	public Long createSCTID(UUID componentUuid, Integer namespaceId, String partitionId, String releaseId, String executionId, String moduleId) throws Exception {
		Long response = idGenStub.createSCTID(componentUuid.toString(), namespaceId, partitionId, releaseId, executionId,moduleId);
		return response;
	}

	@Override
	public String createSNOMEDID(UUID componentUuid, String parentSnomedId) throws Exception {
		String response = idGenStub.createSNOMEDID(componentUuid.toString(), parentSnomedId);

		return response;
	}

	@Override
	public String createCTV3ID(UUID componentUuid) throws Exception {
		String response = idGenStub.createCTV3ID(componentUuid.toString());
		return response;
	}

	@Override
	public HashMap<UUID, Long> createSCTIDList(List<UUID> componentUuidList, Integer namespaceId, String partitionId, String releaseId, String executionId, String moduleId) throws Exception {
		List<String> reqList = new ArrayList<String>();
		for (UUID componentUuid : componentUuidList) {
			reqList.add(componentUuid.toString());
		}
		HashMap<String, Long> response = idGenStub.createSCTIDList(reqList,namespaceId,partitionId,releaseId,executionId,moduleId);
		HashMap<UUID, Long> result = new HashMap<UUID, Long>();
		
		Set<String> keys = response.keySet();
		for (String string : keys) {
			result.put(UUID.fromString(string), response.get(string));
		}
		return result;
	}

	@Override
	public HashMap<UUID, HashMap<IDENTIFIER, String>> createConceptIDList(HashMap<UUID, String> componentUuidParentSnoId, Integer namespaceId, String partitionId, String releaseId,
			String executionId, String moduleId) throws Exception {
		HashMap<String, String> componentUuidStrParentSnoId = new HashMap<String, String>();
		for (UUID componentUuid : componentUuidParentSnoId.keySet()) {
			componentUuidStrParentSnoId.put(componentUuid.toString(), componentUuidParentSnoId.get(componentUuid));
		}
		HashMap<String, HashMap<com.termmed.genid.SctIdGen.IDENTIFIER, String>> response = idGenStub.createConceptIDList(componentUuidStrParentSnoId, namespaceId, partitionId, 
				releaseId, executionId, moduleId);

		HashMap<UUID, HashMap<IDENTIFIER, String>> result = new HashMap<UUID, HashMap<IDENTIFIER, String>>();
		
		Set<String> keys = response.keySet();
		for (String conceptIds2 : keys) {
			
			HashMap<IDENTIFIER, String> resIdenHashMap = new HashMap<IdAssignmentBI.IDENTIFIER, String>();
			HashMap<com.termmed.genid.SctIdGen.IDENTIFIER, String> inMap = response.get(conceptIds2);
			Set<com.termmed.genid.SctIdGen.IDENTIFIER> inMapKeys = inMap.keySet();
			
			
			for (com.termmed.genid.SctIdGen.IDENTIFIER identifier : inMapKeys) {
				IDENTIFIER[] responseIdentifiers = IDENTIFIER.values();
				for (IDENTIFIER identifier2 : responseIdentifiers) {
					if(identifier2.getIdNumber() == identifier.getIdNumber()){
						resIdenHashMap.put(identifier2, inMap.get(identifier2));
					}
				}
			}
			
			result.put(UUID.fromString(conceptIds2), resIdenHashMap);
		}

		return result;
	}

	@Override
	public HashMap<IDENTIFIER, String> createConceptIds(UUID componentUuid, String parentSnomedId, Integer namespaceId, String partitionId, String releaseId, String executionId, String moduleId)
			throws Exception {
		HashMap<com.termmed.genid.SctIdGen.IDENTIFIER, String> response = idGenStub.createConceptIds(componentUuid.toString(),parentSnomedId,namespaceId,partitionId,releaseId,executionId,moduleId);
		HashMap<IDENTIFIER, String> result = new HashMap<IDENTIFIER, String>();
		Set<com.termmed.genid.SctIdGen.IDENTIFIER> keyset = response.keySet();
		for (com.termmed.genid.SctIdGen.IDENTIFIER idString : keyset) {
			for (IDENTIFIER identifier : IDENTIFIER.values()) {
				if (identifier.getIdNumber() == idString.getIdNumber()) {
					result.put(identifier, response.get(identifier));
				}
			}
		}
		return result;
	}
}
