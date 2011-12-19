package org.ihtsdo.project.refset;

import java.io.IOException;
import java.util.Collection;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_cnid.RefexCnidCnidAnalogBI;
import org.ihtsdo.tk.api.refex.type_cnid_cnid.RefexCnidCnidVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;

public class PromotionAndAssignmentRefset extends PromotionRefset {
	
	private int defaultStatusNid;
	private int defaultUserNid;

	public PromotionAndAssignmentRefset(I_GetConceptData refsetConcept) throws Exception {
		super(refsetConcept);
		//TODO: validate if refsetConcept is promotion refset?
		this.refsetConcept = refsetConcept;
		this.refsetName = refsetConcept.toString();
		this.refsetId = refsetConcept.getConceptNid();
		this.termFactory = Terms.get();
		this.activeValueNid = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid();
		this.defaultStatusNid = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getPrimoridalUid());
		this.defaultUserNid = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.USER.getPrimoridalUid());
		
	}

	public RefexVersionBI getLastPromotionTuple(int componentId, I_ConfigAceFrame config) throws TerminologyException, IOException {
		I_GetConceptData component = termFactory.getConcept(componentId);
		Collection<? extends RefexChronicleBI<?>> members = component.getAnnotations();
		for (RefexChronicleBI<?> promotionMember : members) {
			if (promotionMember.getCollectionNid() == this.refsetId) {
				try {
					RefexVersionBI lastTuple = promotionMember.getVersion(config.getViewCoordinate());
					return lastTuple;
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
			RefexCnidCnidVersionBI promotionExtensionPart = (RefexCnidCnidVersionBI) lastTuple;
			return termFactory.getConcept(promotionExtensionPart.getCnid1());
		}
	}

	public I_GetConceptData getDestination(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		RefexVersionBI lastTuple = getLastPromotionTuple(componentId, config);
		if (lastTuple == null) {
			return null;
		} else {
			RefexCnidCnidVersionBI promotionExtensionPart = (RefexCnidCnidVersionBI) lastTuple;
			return termFactory.getConcept(promotionExtensionPart.getCnid2());
		}
	}

	public Long getLastStatusTime(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		RefexVersionBI lastTuple = getLastPromotionTuple(componentId, config);
		if (lastTuple == null) {
			return null;
		} else {
			RefexCnidCnidVersionBI promotionExtensionPart = (RefexCnidCnidVersionBI) lastTuple;
			return promotionExtensionPart.getTime();
		}
	}

	public I_GetConceptData getLastPromotionAuthor(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		RefexVersionBI lastTuple = getLastPromotionTuple(componentId, config);
		if (lastTuple == null) {
			return null;
		} else {
			RefexCnidCnidVersionBI promotionExtensionPart = (RefexCnidCnidVersionBI) lastTuple;
			return termFactory.getConcept(promotionExtensionPart.getAuthorNid());
		}
	}

	public I_GetConceptData getPreviousPromotionStatus(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		I_GetConceptData component = termFactory.getConcept(componentId);
		Collection<? extends RefexChronicleBI<?>> members = component.getAnnotations();
		for (RefexChronicleBI<?> promotionMember : members) {
			if (promotionMember.getCollectionNid() == this.refsetId) {
				try {
					RefexCnidCnidVersionBI promotionExtensionPart = (RefexCnidCnidVersionBI) promotionMember.getVersion(config.getViewCoordinate());
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
				RefexCnidCnidVersionBI promotionExtensionPart = null;
				Collection<? extends RefexVersionBI> versions = promotionMember.getVersions(config.getViewCoordinate());

				long previousToLastVersion = Long.MIN_VALUE;
				for (RefexVersionBI loopPart : loopParts) {
					if (loopPart.getTime() >= previousToLastVersion && loopPart.getTime() < lastStatusTime) {
						previousToLastVersion = loopPart.getTime();
						promotionExtensionPart = (RefexCnidCnidVersionBI) loopPart;
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
			RefexCnidCnidVersionBI oldStatusCnid = (RefexCnidCnidVersionBI) oldStatus;
			if (oldStatusCnid.getCnid1() != statusConceptId) {
				for (PathBI editPath : config.getEditingPathSet()) {
					RefexCnidCnidAnalogBI newVersion = 
						(RefexCnidCnidAnalogBI) oldStatusCnid.makeAnalog(activeValueNid,
								config.getDbConfig().getUserConcept().getNid(),
								editPath.getConceptNid(), 
								Long.MAX_VALUE);
					newVersion.setCnid1(statusConceptId);
					oldStatus.getChronicle().getVersions().add(newVersion);
				}
				termFactory.addUncommittedNoChecks(component);
			}
		} else {
			RefexCAB newSpec = new RefexCAB(
					TK_REFSET_TYPE.CID_CID,
					componentId,
					refsetId);
			newSpec.put(RefexProperty.CNID1, statusConceptId);
			newSpec.put(RefexProperty.CNID2, defaultUserNid);
			RefexChronicleBI<?> newRefex = tc.construct(newSpec);
			termFactory.addUncommittedNoChecks(component);
		}

		return;
	}

	public void setDestination(int componentId, int destinationUserConceptId) throws Exception {
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
		TerminologyBuilderBI tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
				config.getViewCoordinate());

		I_GetConceptData component = termFactory.getConcept(componentId);
		boolean statusAlreadyPresent = false;
		RefexVersionBI oldStatus = getLastPromotionTuple(componentId, config);
		if (oldStatus != null) {
			RefexCnidCnidVersionBI oldStatusCnid = (RefexCnidCnidVersionBI) oldStatus;
			if (oldStatusCnid.getCnid2() != destinationUserConceptId) {
				for (PathBI editPath : config.getEditingPathSet()) {
					RefexCnidCnidAnalogBI newVersion = 
						(RefexCnidCnidAnalogBI) oldStatusCnid.makeAnalog(activeValueNid,
								config.getDbConfig().getUserConcept().getNid(),
								editPath.getConceptNid(), 
								Long.MAX_VALUE);
					newVersion.setCnid2(destinationUserConceptId);
					oldStatus.getChronicle().getVersions().add(newVersion);
				}
				termFactory.addUncommittedNoChecks(component);
			}
		} else {
			RefexCAB newSpec = new RefexCAB(
					TK_REFSET_TYPE.CID_CID,
					componentId,
					refsetId);
			newSpec.put(RefexProperty.CNID1, defaultStatusNid);
			newSpec.put(RefexProperty.CNID2, destinationUserConceptId);
			RefexChronicleBI<?> newRefex = tc.construct(newSpec);
			termFactory.addUncommittedNoChecks(component);
		}

		return;
	}
}
