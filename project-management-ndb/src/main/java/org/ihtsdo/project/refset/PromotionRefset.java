package org.ihtsdo.project.refset;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

public class PromotionRefset extends Refset {

	int activeValueNid;

	public PromotionRefset(I_GetConceptData refsetConcept) throws Exception {
		super();
		//TODO: validate if refsetConcept is promotion refset?
		this.refsetConcept = refsetConcept;
		this.refsetName = refsetConcept.toString();
		this.refsetId = refsetConcept.getConceptNid();
		this.termFactory = Terms.get();
		this.activeValueNid = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid();
	}

	public RefexVersionBI getLastPromotionTuple(int componentId, I_ConfigAceFrame config) throws TerminologyException, IOException {
		I_GetConceptData component = termFactory.getConcept(componentId);
		Collection<? extends RefexChronicleBI<?>> members = component.getAnnotations();
		for (RefexChronicleBI<?> promotionMember : members) {
			if (promotionMember.getCollectionNid() == this.refsetId) {
				try {
					return promotionMember.getVersion(config.getViewCoordinate());
				} catch (ContradictionException e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			}
		}
		return null;
	}

	public I_GetConceptData getPromotionStatus(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		RefexVersionBI lastTuple = getLastPromotionTuple(componentId, config);
		if (lastTuple == null) {
			return null;
		} else {
			RefexCnidVersionBI promotionExtensionPart = (RefexCnidVersionBI) lastTuple;
			return termFactory.getConcept(promotionExtensionPart.getCnid1());
		}
	}

	public Long getLastStatusTime(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		RefexVersionBI lastTuple = getLastPromotionTuple(componentId, config);
		if (lastTuple == null) {
			return null;
		} else {
			RefexCnidVersionBI promotionExtensionPart = (RefexCnidVersionBI) lastTuple;
			return promotionExtensionPart.getTime();
		}
	}

	public I_GetConceptData getLastPromotionAuthor(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		RefexVersionBI lastTuple = getLastPromotionTuple(componentId, config);
		if (lastTuple == null) {
			return null;
		} else {
			RefexCnidVersionBI promotionExtensionPart = (RefexCnidVersionBI) lastTuple;
			return termFactory.getConcept(promotionExtensionPart.getAuthorNid());
		}
	}

	public I_GetConceptData getPreviousPromotionStatus(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		I_GetConceptData component = termFactory.getConcept(componentId);
		Collection<? extends RefexChronicleBI<?>> members = component.getAnnotations();
		for (RefexChronicleBI<?> promotionMember : members) {
			if (promotionMember.getCollectionNid() == this.refsetId) {
				try {
					RefexCnidVersionBI promotionExtensionPart = (RefexCnidVersionBI) promotionMember.getVersion(config.getViewCoordinate());
					return termFactory.getConcept(promotionExtensionPart.getCnid1());
				} catch (Exception e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			}
		}
		return null;
	}

	public Long getPreviousStatusTime(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		I_GetConceptData component = termFactory.getConcept(componentId);
		Collection<? extends RefexChronicleBI<?>> members = component.getAnnotations();
		Long lastStatusTime = getLastStatusTime(componentId, config);
		for (RefexChronicleBI<?> promotionMember : members) {
			if (promotionMember.getCollectionNid() == this.refsetId) {
				Collection<? extends RefexVersionBI> loopParts = promotionMember.getVersions(config.getViewCoordinate());
				long lastVersion = Long.MIN_VALUE;
				RefexCnidVersionBI promotionExtensionPart = null;
				Collection<? extends RefexVersionBI> versions = promotionMember.getVersions(config.getViewCoordinate());

				long previousToLastVersion = Long.MIN_VALUE;
				for (RefexVersionBI loopPart : loopParts) {
					if (loopPart.getTime() >= previousToLastVersion && loopPart.getTime() < lastStatusTime) {
						previousToLastVersion = loopPart.getTime();
						promotionExtensionPart = (RefexCnidVersionBI) loopPart;
					}
				}
				if (promotionExtensionPart != null) {
					return promotionExtensionPart.getTime();
				}
			}
		}
		return null;
	}

	public void setPromotionStatus(int componentId, int statusConceptId) throws Exception {
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
		TerminologyBuilderBI tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
				config.getViewCoordinate());

		I_GetConceptData component = termFactory.getConcept(componentId);
		boolean statusAlreadyPresent = false;
		RefexVersionBI oldStatus = getLastPromotionTuple(componentId, config);
		if (oldStatus != null) {
			RefexCnidVersionBI oldStatusCnid = (RefexCnidVersionBI) oldStatus;
			if (oldStatusCnid.getCnid1() != statusConceptId) {
				I_ExtendByRef oldExtension = termFactory.getExtension(oldStatus.getNid());
				long lastVersion = Long.MIN_VALUE;
				I_ExtendByRefPartCid promotionStatusExtensionPart = null;
				List<? extends I_ExtendByRefPart> loopParts = oldExtension.getMutableParts();
				for (I_ExtendByRefPart loopPart : loopParts) {
					if (loopPart.getTime() >= lastVersion) {
						lastVersion = loopPart.getTime();
						promotionStatusExtensionPart = (I_ExtendByRefPartCid) loopPart;
					}
				}
				for (PathBI editPath : config.getEditingPathSet()) {
					I_ExtendByRefPartCid newPromotionStatusPart =(I_ExtendByRefPartCid) 
					promotionStatusExtensionPart.makeAnalog(
							SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid(), 
							editPath.getConceptNid(), 
							Long.MAX_VALUE);
					newPromotionStatusPart.setC1id(statusConceptId);
					oldExtension.addVersion(newPromotionStatusPart);
				}
				//termFactory.addUncommittedNoChecks(refsetConcept);
				termFactory.addUncommittedNoChecks(component);
				
				
//				for (PathBI editPath : config.getEditingPathSet()) {
//					RefexCnidAnalogBI newVersion = 
//						(RefexCnidAnalogBI) oldStatusCnid.makeAnalog(activeValueNid,
//								config.getDbConfig().getUserConcept().getNid(),
//								editPath.getConceptNid(), 
//								Long.MAX_VALUE);
//					newVersion.setCnid1(statusConceptId);
//					oldStatus.getChronicle().getVersions().add(newVersion);
//				}
//				termFactory.addUncommittedNoChecks(component);
			}
		} else {
			I_GetConceptData newMemberConcept = termFactory.getConcept(componentId);
			I_HelpRefsets refsetHelper = termFactory.getRefsetHelper(config);

			refsetHelper.newRefsetExtension(this.refsetId, 
					componentId, EConcept.REFSET_TYPES.CID, 
					new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, statusConceptId), config);

			//termFactory.addUncommittedNoChecks(refsetConcept);
			termFactory.addUncommittedNoChecks(newMemberConcept);
//			RefexCAB newSpec = new RefexCAB(
//					TK_REFSET_TYPE.CID,
//					componentId,
//					refsetId);
//			newSpec.put(RefexProperty.CNID1, statusConceptId);
//			RefexChronicleBI<?> newRefex = tc.construct(newSpec);
//			termFactory.addUncommittedNoChecks(component);
		}

		return;
	}
}
