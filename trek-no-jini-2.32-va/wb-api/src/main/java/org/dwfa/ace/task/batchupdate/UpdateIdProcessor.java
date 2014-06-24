package org.dwfa.ace.task.batchupdate;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeAnalogBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.relationship.RelationshipAnalogBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

public class UpdateIdProcessor implements ProcessUnfetchedConceptDataBI { 
	I_ConfigAceFrame config;
	int count;
	int updated;
	long start;
	private I_ShowActivity activity;
	NidBitSetBI nidSet;
	Map<UUID, String> sctidsMap;
	Map<UUID, String> snomedIdsMap;
	Map<UUID, String> ctv3IdsMap;
	Integer snomedIntIdNid;
	Integer snomedUuidNid;
	Integer snomedAuxIntId;
	Integer snomedAuxRtId;
	Integer snomedAuxCtv3Id;

	public UpdateIdProcessor(Map<UUID, String> sctidsMap, 
			Map<UUID, String> snomedIdsMap, 
			Map<UUID, String> ctv3IdsMap,
			I_ShowActivity activity) throws Exception {
		super();
		this.activity = activity;
		this.sctidsMap = sctidsMap;
		this.snomedIdsMap = snomedIdsMap;
		this.ctv3IdsMap = ctv3IdsMap;
		nidSet = Ts.get().getAllConceptNids();
		activity.setValue(0);
		activity.setMaximum(nidSet.cardinality());
		activity.setIndeterminate(false);
		count = 0;
		updated = 0;
		snomedIntIdNid = Terms.get().uuidToNative(UUID.fromString("87360947-e603-3397-804b-efd0fcc509b9"));
		snomedUuidNid = Terms.get().uuidToNative(UUID.fromString("680f3f6c-7a2a-365d-b527-8c9a96dd1a94"));
		snomedAuxIntId = Ts.get().getNidForUuids(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids());
		snomedAuxRtId = Ts.get().getNidForUuids(ArchitectonicAuxiliary.Concept.SNOMED_RT_ID.getUids());
		snomedAuxCtv3Id = Ts.get().getNidForUuids(ArchitectonicAuxiliary.Concept.CTV3_ID.getUids());
	}

	@Override
	public boolean continueWork() {
		return true;
	}

	@Override
	public NidBitSetBI getNidSet() throws IOException {
		return nidSet;
	}

	@Override
	public void processUnfetchedConceptData(int arg0, ConceptFetcherBI fetcher)
			throws Exception {
		count++;
		boolean changes = false;
		ConceptVersionBI concept = fetcher.fetch(Terms.get().getActiveAceFrameConfig().getViewCoordinate());

		// add sctid
		if (sctidsMap.containsKey(concept.getPrimUuid())) {
			// verify and update
			String foundId = sctidsMap.get(concept.getPrimUuid());
			boolean addId = true;
			for (IdBI id : concept.getConceptAttributes().getAllIds()) {
				if (id.getAuthorityNid() == snomedAuxIntId) {
					Long denotation = (Long) id.getDenotation();
					if (!denotation.equals(Long.parseLong(foundId))) {
						// Error, it has a different id!
						AceLog.getAppLog().info(denotation + " already assigned to " + foundId);
					}
					addId = false;
				}
			}
			if (addId) {
				ConceptAttributeAnalogBI analog = (ConceptAttributeAnalogBI) concept.getConceptAttributes();
				analog.addLongId(Long.parseLong(foundId),
						snomedAuxIntId,
						SnomedMetadataRfx.getSTATUS_CURRENT_NID(),
						Terms.get().getActiveAceFrameConfig().getEditCoordinate(),
						Long.MAX_VALUE);
				changes = true;
				updated++;
			}
		}
		
		// add snomedid
		if (snomedIdsMap.containsKey(concept.getPrimUuid())) {
			// verify and update
			String foundId = snomedIdsMap.get(concept.getPrimUuid());
			boolean addId = true;
			for (IdBI id : concept.getConceptAttributes().getAllIds()) {
				if (id.getAuthorityNid() == snomedAuxRtId) {
					String denotation = (String) id.getDenotation();
					if (!denotation.equals(foundId)) {
						// Error, it has a different id!
						AceLog.getAppLog().info(denotation + " already assigned to " + foundId);
					}
					addId = false;
				}
			}
			if (addId) {
				I_Identify i_Identify = Terms.get().getId(concept.getNid());
				i_Identify.addStringId(foundId, 
						snomedAuxRtId, 
						SnomedMetadataRfx.getSTATUS_CURRENT_NID(), 
						Long.MAX_VALUE,
						Terms.get().getActiveAceFrameConfig().getEditCoordinate().getAuthorNid(),
						Terms.get().getActiveAceFrameConfig().getEditCoordinate().getModuleNid(),
						Terms.get().getActiveAceFrameConfig().getEditingPathSet().iterator().next().getConceptNid());
				changes = true;
				updated++;
			}
		}
		
		// add ctv3id
		if (ctv3IdsMap.containsKey(concept.getPrimUuid())) {
			// verify and update
			String foundId = ctv3IdsMap.get(concept.getPrimUuid());
			boolean addId = true;
			for (IdBI id : concept.getConceptAttributes().getAllIds()) {
				if (id.getAuthorityNid() == snomedAuxCtv3Id) {
					String denotation = (String) id.getDenotation();
					if (!denotation.equals(foundId)) {
						// Error, it has a different id!
						AceLog.getAppLog().info(denotation + " already assigned to " + foundId);
					}
					addId = false;
				}
			}
			if (addId) {
				I_Identify i_Identify = Terms.get().getId(concept.getNid());
				i_Identify.addStringId(foundId, 
						snomedAuxCtv3Id, 
						SnomedMetadataRfx.getSTATUS_CURRENT_NID(), 
						Long.MAX_VALUE,
						Terms.get().getActiveAceFrameConfig().getEditCoordinate().getAuthorNid(),
						Terms.get().getActiveAceFrameConfig().getEditCoordinate().getModuleNid(),
						Terms.get().getActiveAceFrameConfig().getEditingPathSet().iterator().next().getConceptNid());
				changes = true;
				updated++;
			}
		}

		for (DescriptionChronicleBI description : concept.getDescriptions()) {
			if (sctidsMap.containsKey(description.getPrimUuid())) {
				// verify and update
				String foundId = sctidsMap.get(description.getPrimUuid());
				boolean addId = true;
				for (IdBI id : description.getAllIds()) {
					if (id.getAuthorityNid() == snomedAuxIntId) {
						Long denotation = (Long) id.getDenotation();
						if (!denotation.equals(Long.parseLong(foundId))) {
							// Error, it has a different id!
							AceLog.getAppLog().info(denotation + " already assigned to " + foundId);
						}
						addId = false;
					}
				}
				if (addId) {
					I_Identify i_Identify = Terms.get().getId(description.getNid());
					i_Identify.addLongId(Long.parseLong(foundId), 
							snomedAuxIntId, 
							SnomedMetadataRfx.getSTATUS_CURRENT_NID(), 
							Long.MAX_VALUE,
							Terms.get().getActiveAceFrameConfig().getEditCoordinate().getAuthorNid(),
							Terms.get().getActiveAceFrameConfig().getEditCoordinate().getModuleNid(),
							Terms.get().getActiveAceFrameConfig().getEditingPathSet().iterator().next().getConceptNid());
					changes = true;
					updated++;
//					DescriptionAnalogBI analog = (DescriptionAnalogBI) description;
//					analog.addLongId(Long.parseLong(foundId),
//							snomedIntIdNid,
//							SnomedMetadataRfx.getSTATUS_CURRENT_NID(),
//							Terms.get().getActiveAceFrameConfig().getEditCoordinate(),
//							Long.MAX_VALUE);
//					Ts.get().addUncommittedNoChecks(concept);
//					updated++;
				}
			}
		}

		for (RelationshipChronicleBI relationship : concept.getRelationshipsOutgoing()) {
			if (sctidsMap.containsKey(relationship.getPrimUuid())) {
				// verify and update
				String foundId = sctidsMap.get(relationship.getPrimUuid());
				boolean addId = true;
				for (IdBI id : relationship.getAllIds()) {
					if (id.getAuthorityNid() == snomedAuxIntId) {
						Long denotation = (Long) id.getDenotation();
						if (!denotation.equals(Long.parseLong(foundId))) {
							// Error, it has a different id!
							AceLog.getAppLog().info(denotation + " already assigned to " + foundId);
						}
						addId = false;
					}
				}
				if (addId) {
					I_Identify i_Identify = Terms.get().getId(relationship.getNid());
					i_Identify.addLongId(Long.parseLong(foundId), 
							snomedAuxIntId, 
							SnomedMetadataRfx.getSTATUS_CURRENT_NID(), 
							Long.MAX_VALUE,
							Terms.get().getActiveAceFrameConfig().getEditCoordinate().getAuthorNid(),
							Terms.get().getActiveAceFrameConfig().getEditCoordinate().getModuleNid(),
							Terms.get().getActiveAceFrameConfig().getEditingPathSet().iterator().next().getConceptNid());
					changes = true;
					updated++;
//					RelationshipAnalogBI analog = (RelationshipAnalogBI) relationship;
//					analog.addLongId(Long.parseLong(foundId),
//							Ts.get().getNidForUuids(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids()),
//							SnomedMetadataRfx.getSTATUS_CURRENT_NID(),
//							Terms.get().getActiveAceFrameConfig().getEditCoordinate(),
//							Long.MAX_VALUE);
//					Ts.get().addUncommittedNoChecks(concept);
//					updated++;
				}
			}
		}
		if (changes) {
			Ts.get().addUncommittedNoChecks(concept);
		}
		activity.setValue(count);
		activity.setProgressInfoLower("Looking for concept to add ids... Update Count: " + updated);
	}
}
