package org.ihtsdo.project.refset;

import java.io.IOException;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.Precedence;

public class PromotionRefset extends Refset {

	public PromotionRefset(I_GetConceptData refsetConcept) throws Exception {
		super();
		//TODO: validate if refsetConcept is promotion refset?
		this.refsetConcept = refsetConcept;
		this.refsetName = refsetConcept.toString();
		this.refsetId = refsetConcept.getConceptNid();
		termFactory = Terms.get();
	}

	public I_ExtendByRefVersion getLastPromotionTuple(int componentId, I_ConfigAceFrame config) throws TerminologyException, IOException {
		List<? extends I_ExtendByRef> members = termFactory.getAllExtensionsForComponent(componentId);
		for (I_ExtendByRef promotionMember : members) {
			if (promotionMember.getRefsetId() == this.refsetId) {
				List<? extends I_ExtendByRefVersion> tuples = promotionMember.getTuples(config.getAllowedStatus(), 
						config.getViewPositionSetReadOnly(), 
						Precedence.TIME, 
						config.getConflictResolutionStrategy());
				if (tuples != null && !tuples.isEmpty()) {
					I_ExtendByRefVersion lastTuple = null;
					for (I_ExtendByRefVersion loopTuple : tuples) {
						if (lastTuple == null || lastTuple.getTime() < loopTuple.getTime()) {
							lastTuple = loopTuple;
						}
					}
					return lastTuple;
				}
			}
		}
		return null;
	}

	public I_GetConceptData getPromotionStatus(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		I_ExtendByRefVersion lastTuple = getLastPromotionTuple(componentId, config);
		if (lastTuple == null) {
			return null;
		} else {
			I_ExtendByRefPartCid promotionExtensionPart = (I_ExtendByRefPartCid) lastTuple.getMutablePart();
			return termFactory.getConcept(promotionExtensionPart.getC1id());
		}
	}

	public Long getLastStatusTime(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		I_ExtendByRefVersion lastTuple = getLastPromotionTuple(componentId, config);
		if (lastTuple == null) {
			return null;
		} else {
			I_ExtendByRefPartCid promotionExtensionPart = (I_ExtendByRefPartCid) lastTuple.getMutablePart();
			return promotionExtensionPart.getTime();
		}
	}
	
	public I_GetConceptData getLastPromotionAuthor(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		I_ExtendByRefVersion lastTuple = getLastPromotionTuple(componentId, config);
		if (lastTuple == null) {
			return null;
		} else {
			I_ExtendByRefPartCid promotionExtensionPart = (I_ExtendByRefPartCid) lastTuple.getMutablePart();
			return termFactory.getConcept(promotionExtensionPart.getAuthorNid());
		}
	}

	public I_GetConceptData getPreviousPromotionStatus(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		for (I_ExtendByRef promotionMember : termFactory.getAllExtensionsForComponent(componentId, true)) {
			if (promotionMember.getRefsetId() == this.refsetId) {
				List<? extends I_ExtendByRefPart> loopParts = promotionMember.getMutableParts();
				long lastVersion = Long.MIN_VALUE;
				I_ExtendByRefPartCid promotionExtensionPart = null;
				List<? extends I_ExtendByRefVersion> tuples = promotionMember.getTuples(config.getAllowedStatus(), 
						config.getViewPositionSetReadOnly(), 
						config.getPrecedence(), 
						config.getConflictResolutionStrategy());
				if (tuples != null && !tuples.isEmpty()) {
					promotionExtensionPart = (I_ExtendByRefPartCid) tuples.iterator().next().getMutablePart();
					lastVersion = promotionExtensionPart.getTime();

					long previousToLastVersion = Long.MIN_VALUE;
					for (I_ExtendByRefPart loopPart : loopParts) {
						if (loopPart.getTime() >= previousToLastVersion && loopPart.getTime() < lastVersion) {
							previousToLastVersion = loopPart.getTime();
							promotionExtensionPart = (I_ExtendByRefPartCid) loopPart;
						}
					}
					if (promotionExtensionPart != null) {
						return termFactory.getConcept(promotionExtensionPart.getC1id());
					}
				}
			}
		}
		return null;
	}

	public Long getPreviousStatusTime(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		for (I_ExtendByRef promotionMember : termFactory.getAllExtensionsForComponent(componentId, true)) {
			if (promotionMember.getRefsetId() == this.refsetId) {
				List<? extends I_ExtendByRefPart> loopParts = promotionMember.getMutableParts();
				long lastVersion = Long.MIN_VALUE;
				I_ExtendByRefPartCid promotionExtensionPart = null;
				List<? extends I_ExtendByRefVersion> tuples = promotionMember.getTuples(config.getAllowedStatus(), 
						config.getViewPositionSetReadOnly(), 
						config.getPrecedence(), 
						config.getConflictResolutionStrategy());
				if (tuples != null && !tuples.isEmpty()) {
					promotionExtensionPart = (I_ExtendByRefPartCid) tuples.iterator().next().getMutablePart();
					lastVersion = promotionExtensionPart.getTime();

					long previousToLastVersion = Long.MIN_VALUE;
					for (I_ExtendByRefPart loopPart : loopParts) {
						if (loopPart.getTime() >= previousToLastVersion && loopPart.getTime() < lastVersion) {
							previousToLastVersion = loopPart.getTime();
							promotionExtensionPart = (I_ExtendByRefPartCid) loopPart;
						}
					}
					if (promotionExtensionPart != null) {
						return promotionExtensionPart.getTime();
					}
				}
			}
		}
		return null;
	}

	public void setPromotionStatus(int componentId, int statusConceptId) throws Exception {
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
		boolean statusAlreadyPresent = false;
		for (I_ExtendByRef promotionStatusMember : termFactory.getAllExtensionsForComponent(componentId, true)) {
			if (promotionStatusMember.getRefsetId() == this.refsetId) {
				statusAlreadyPresent = true;
				long lastVersion = Long.MIN_VALUE;
				I_ExtendByRefPartCid promotionStatusExtensionPart = null;
				List<? extends I_ExtendByRefPart> loopParts = promotionStatusMember.getMutableParts();
				for (I_ExtendByRefPart loopPart : loopParts) {
					if (loopPart.getTime() >= lastVersion) {
						lastVersion = loopPart.getTime();
						promotionStatusExtensionPart = (I_ExtendByRefPartCid) loopPart;
					}
				}
				for (PathBI editPath : config.getEditingPathSet()) {
					I_ExtendByRefPartCid newPromotionStatusPart =(I_ExtendByRefPartCid) 
					promotionStatusExtensionPart.makeAnalog(
							ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(), 
							editPath.getConceptNid(), 
							Long.MAX_VALUE);
					newPromotionStatusPart.setC1id(statusConceptId);
					promotionStatusMember.addVersion(newPromotionStatusPart);
				}
				termFactory.addUncommittedNoChecks(refsetConcept);
				termFactory.addUncommittedNoChecks(promotionStatusMember);
				//				termFactory.commit();
			}
		}

		if (!statusAlreadyPresent) {
			I_GetConceptData newMemberConcept = termFactory.getConcept(componentId);
			I_HelpRefsets refsetHelper = termFactory.getRefsetHelper(config);

			refsetHelper.newRefsetExtension(this.refsetId, 
					componentId, EConcept.REFSET_TYPES.CID, 
					new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, statusConceptId), config);

			termFactory.addUncommittedNoChecks(refsetConcept);
			termFactory.addUncommittedNoChecks(newMemberConcept);
			//			termFactory.commit();
		}

		return;
	}
}
