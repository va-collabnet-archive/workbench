/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_cnid.RefexCnidCnidVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * The Class PromotionAndAssignmentRefset.
 */
public class PromotionAndAssignmentRefset extends PromotionRefset {

	/** The default status nid. */
	private int defaultStatusNid;
	
	/** The default user nid. */
	private int defaultUserNid;

	/**
	 * Instantiates a new promotion and assignment refset.
	 *
	 * @param refsetConcept the refset concept
	 * @throws Exception the exception
	 */
	public PromotionAndAssignmentRefset(I_GetConceptData refsetConcept) throws Exception {
		super(refsetConcept);
		// TODO: validate if refsetConcept is promotion refset?
		this.refsetConcept = refsetConcept;
		this.refsetName = refsetConcept.toString();
		this.refsetId = refsetConcept.getConceptNid();
		this.termFactory = Terms.get();
		this.activeValueNid = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid();
		this.defaultStatusNid = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getPrimoridalUid());
		this.defaultUserNid = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.USER.getPrimoridalUid());

	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.PromotionRefset#getLastPromotionTuple(int, org.dwfa.ace.api.I_ConfigAceFrame)
	 */
	public RefexVersionBI getLastPromotionTuple(int componentId, I_ConfigAceFrame config) throws TerminologyException, IOException {
		I_GetConceptData component = termFactory.getConcept(componentId);
		Collection<? extends RefexChronicleBI<?>> members = component.getAnnotations();
		for (RefexChronicleBI<?> promotionMember : members) {
			if (promotionMember.getCollectionNid() == this.refsetId) {
				try {
					RefexVersionBI lastTuple = promotionMember.getVersion(config.getViewCoordinate());
					return lastTuple;
				} catch (Exception e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.PromotionRefset#getPromotionStatus(int, org.dwfa.ace.api.I_ConfigAceFrame)
	 */
	public I_GetConceptData getPromotionStatus(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		RefexVersionBI lastTuple = getLastPromotionTuple(componentId, config);
		if (lastTuple == null) {
			return null;
		} else {
			RefexCnidCnidVersionBI promotionExtensionPart = (RefexCnidCnidVersionBI) lastTuple;
			return termFactory.getConcept(promotionExtensionPart.getCnid1());
		}
	}

	/**
	 * Gets the destination.
	 *
	 * @param componentId the component id
	 * @param config the config
	 * @return the destination
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public I_GetConceptData getDestination(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		RefexVersionBI lastTuple = getLastPromotionTuple(componentId, config);
		if (lastTuple == null) {
			return null;
		} else {
			RefexCnidCnidVersionBI promotionExtensionPart = (RefexCnidCnidVersionBI) lastTuple;
			return termFactory.getConcept(promotionExtensionPart.getCnid2());
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.PromotionRefset#getLastStatusTime(int, org.dwfa.ace.api.I_ConfigAceFrame)
	 */
	public Long getLastStatusTime(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		RefexVersionBI lastTuple = getLastPromotionTuple(componentId, config);
		if (lastTuple == null) {
			return null;
		} else {
			RefexCnidCnidVersionBI promotionExtensionPart = (RefexCnidCnidVersionBI) lastTuple;
			return promotionExtensionPart.getTime();
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.PromotionRefset#getLastPromotionAuthor(int, org.dwfa.ace.api.I_ConfigAceFrame)
	 */
	public I_GetConceptData getLastPromotionAuthor(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		RefexVersionBI lastTuple = getLastPromotionTuple(componentId, config);
		if (lastTuple == null) {
			return null;
		} else {
			RefexCnidCnidVersionBI promotionExtensionPart = (RefexCnidCnidVersionBI) lastTuple;
			return termFactory.getConcept(promotionExtensionPart.getAuthorNid());
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.PromotionRefset#getPreviousPromotionStatus(int, org.dwfa.ace.api.I_ConfigAceFrame)
	 */
	public I_GetConceptData getPreviousPromotionStatus(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		I_GetConceptData component = termFactory.getConcept(componentId);
		Collection<? extends RefexChronicleBI<?>> members = component.getAnnotations();
		Long lastStatusTime = getLastStatusTime(componentId, config);
		for (RefexChronicleBI<?> promotionMember : members) {
			if (promotionMember.getCollectionNid() == this.refsetId) {
				RefexCnidCnidVersionBI promotionExtensionPart = null;
				Collection<? extends RefexVersionBI> versions = promotionMember.getVersions();

				long previousToLastVersion = Long.MIN_VALUE;
				for (RefexVersionBI loopPart : versions) {
					if (loopPart.getTime() >= previousToLastVersion && loopPart.getTime() < lastStatusTime) {
						previousToLastVersion = loopPart.getTime();
						promotionExtensionPart = (RefexCnidCnidVersionBI) loopPart;
					}
				}
				if (promotionExtensionPart != null) {
					return termFactory.getConcept(promotionExtensionPart.getCnid1());
				}
			}
		}
		return null;
	}

	/**
	 * Gets the previous user.
	 *
	 * @param componentId the component id
	 * @param config the config
	 * @return the previous user
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public I_GetConceptData getPreviousUser(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		I_GetConceptData component = termFactory.getConcept(componentId);
		Collection<? extends RefexChronicleBI<?>> members = component.getAnnotations();
		for (RefexChronicleBI<?> promotionMember : members) {
			if (promotionMember.getCollectionNid() == this.refsetId) {
				RefexCnidCnidVersionBI promotionExtensionPart = null;
				Collection<? extends RefexVersionBI> versions = promotionMember.getVersions();

				long last = Long.MIN_VALUE;
				for (RefexVersionBI loopPart : versions) {
					if (loopPart.getTime() > last) {
						last = loopPart.getTime();
						promotionExtensionPart = (RefexCnidCnidVersionBI) loopPart;
					}
				}
				if (promotionExtensionPart != null) {
					return termFactory.getConcept(promotionExtensionPart.getAuthorNid());
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.PromotionRefset#getPreviousStatusTime(int, org.dwfa.ace.api.I_ConfigAceFrame)
	 */
	public Long getPreviousStatusTime(int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		I_GetConceptData component = termFactory.getConcept(componentId);
		Collection<? extends RefexChronicleBI<?>> members = component.getAnnotations();
		Long lastStatusTime = getLastStatusTime(componentId, config);
		for (RefexChronicleBI<?> promotionMember : members) {
			if (promotionMember.getCollectionNid() == this.refsetId) {
				Collection<? extends RefexVersionBI> loopParts = promotionMember.getVersions();
				RefexCnidCnidVersionBI promotionExtensionPart = null;

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

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.PromotionRefset#setPromotionStatus(int, int)
	 */
	public void setPromotionStatus(int componentId, int statusConceptId) throws Exception {
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
		TerminologyBuilderBI tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(), config.getViewCoordinate());

		I_GetConceptData component = termFactory.getConcept(componentId);
		boolean statusAlreadyPresent = false;
		RefexVersionBI oldStatus = getLastPromotionTuple(componentId, config);
		if (oldStatus != null) {
			I_ExtendByRef oldExtension = termFactory.getExtension(oldStatus.getNid());
			long lastVersion = Long.MIN_VALUE;
			I_ExtendByRefPartCidCid promotionStatusExtensionPart = null;
			List<? extends I_ExtendByRefPart> loopParts = oldExtension.getMutableParts();
			for (I_ExtendByRefPart loopPart : loopParts) {
				if (loopPart.getTime() >= lastVersion) {
					lastVersion = loopPart.getTime();
					promotionStatusExtensionPart = (I_ExtendByRefPartCidCid) loopPart;
				}
			}
			if (promotionStatusExtensionPart != null && (promotionStatusExtensionPart.getStatusNid() != activeValueNid || promotionStatusExtensionPart.getC1id() != statusConceptId)) {

				for (PathBI editPath : config.getEditingPathSet()) {
					I_ExtendByRefPartCidCid newPromotionStatusPart = (I_ExtendByRefPartCidCid) promotionStatusExtensionPart.makeAnalog(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid(), config.getDbConfig().getUserConcept().getNid(),
							editPath.getConceptNid(), Long.MAX_VALUE);
					newPromotionStatusPart.setC1id(statusConceptId);
					oldExtension.addVersion(newPromotionStatusPart);
				}
				// termFactory.addUncommittedNoChecks(refsetConcept);
				termFactory.addUncommittedNoChecks(component);
				// termFactory.commit();
			}

			// new proposal by keith and aimee
			// RefexCnidCnidVersionBI oldStatusCnid = (RefexCnidCnidVersionBI)
			// oldStatus;
			// RefexCAB refexBp =
			// oldStatusCnid.makeBlueprint(config.getViewCoordinate());
			// refexBp.put(RefexProperty.CNID1, statusConceptId);
			// tc.construct(refexBp);

			// RefexCnidCnidVersionBI oldStatusCnid = (RefexCnidCnidVersionBI)
			// oldStatus;
			// if (oldStatusCnid.getCnid1() != statusConceptId) {
			// for (PathBI editPath : config.getEditingPathSet()) {
			// RefexCnidCnidAnalogBI newVersion =
			// (RefexCnidCnidAnalogBI) oldStatusCnid.makeAnalog(activeValueNid,
			// config.getDbConfig().getUserConcept().getNid(),
			// editPath.getConceptNid(),
			// Long.MAX_VALUE);
			// newVersion.setCnid1(statusConceptId);
			// oldStatus.getChronicle().getVersions().add(newVersion);
			// }
			// termFactory.addUncommittedNoChecks(component);
			// }
		} else {
			I_GetConceptData newMemberConcept = termFactory.getConcept(componentId);
			I_HelpRefsets refsetHelper = termFactory.getRefsetHelper(config);

			RefsetPropertyMap propMap = new RefsetPropertyMap();
			propMap.put(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, statusConceptId);
			propMap.put(RefsetPropertyMap.REFSET_PROPERTY.CID_TWO, defaultUserNid);

			refsetHelper.newRefsetExtension(this.refsetId, componentId, EConcept.REFSET_TYPES.CID_CID, propMap, config);

			// termFactory.addUncommittedNoChecks(refsetConcept);
			termFactory.addUncommittedNoChecks(newMemberConcept);

			// RefexCAB newSpec = new RefexCAB(
			// TK_REFSET_TYPE.CID_CID,
			// componentId,
			// refsetId);
			// newSpec.put(RefexProperty.CNID1, statusConceptId);
			// newSpec.put(RefexProperty.CNID2, defaultUserNid);
			// RefexChronicleBI<?> newRefex = tc.construct(newSpec);
			// termFactory.addUncommittedNoChecks(component);
		}

		return;
	}

	/**
	 * Sets the destination.
	 *
	 * @param componentId the component id
	 * @param destinationUserConceptId the destination user concept id
	 * @throws Exception the exception
	 */
	public void setDestination(int componentId, int destinationUserConceptId) throws Exception {
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
		TerminologyBuilderBI tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(), config.getViewCoordinate());

		I_GetConceptData component = termFactory.getConcept(componentId);
		boolean statusAlreadyPresent = false;
		RefexVersionBI oldStatus = getLastPromotionTuple(componentId, config);
		if (oldStatus != null) {
			statusAlreadyPresent = true;
			long lastVersion = Long.MIN_VALUE;
			I_ExtendByRef oldExtension = termFactory.getExtension(oldStatus.getNid());
			I_ExtendByRefPartCidCid promotionStatusExtensionPart = null;
			List<? extends I_ExtendByRefPart> loopParts = oldExtension.getMutableParts();
			for (I_ExtendByRefPart loopPart : loopParts) {
				if (loopPart.getTime() >= lastVersion) {
					lastVersion = loopPart.getTime();
					promotionStatusExtensionPart = (I_ExtendByRefPartCidCid) loopPart;
				}
			}
			if (promotionStatusExtensionPart != null && (promotionStatusExtensionPart.getStatusNid() != activeValueNid || promotionStatusExtensionPart.getC2id() != destinationUserConceptId)) {
				for (PathBI editPath : config.getEditingPathSet()) {
					I_ExtendByRefPartCidCid newPromotionStatusPart = (I_ExtendByRefPartCidCid) promotionStatusExtensionPart.makeAnalog(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid(), config.getDbConfig().getUserConcept().getNid(),
							editPath.getConceptNid(), Long.MAX_VALUE);
					newPromotionStatusPart.setC2id(destinationUserConceptId);
					oldExtension.addVersion(newPromotionStatusPart);
				}
				// termFactory.addUncommittedNoChecks(refsetConcept);
				termFactory.addUncommittedNoChecks(component);
				// termFactory.commit();
			}
			// RefexCnidCnidVersionBI oldStatusCnid = (RefexCnidCnidVersionBI)
			// oldStatus;
			// if (oldStatusCnid.getCnid2() != destinationUserConceptId) {
			// for (PathBI editPath : config.getEditingPathSet()) {
			// RefexCnidCnidAnalogBI newVersion =
			// (RefexCnidCnidAnalogBI) oldStatusCnid.makeAnalog(activeValueNid,
			// config.getDbConfig().getUserConcept().getNid(),
			// editPath.getConceptNid(),
			// Long.MAX_VALUE);
			// newVersion.setCnid2(destinationUserConceptId);
			// oldStatus.getChronicle().getVersions().add(newVersion);
			// }
			// termFactory.addUncommittedNoChecks(component);
			// }
		} else {
			I_GetConceptData newMemberConcept = termFactory.getConcept(componentId);
			I_HelpRefsets refsetHelper = termFactory.getRefsetHelper(config);

			RefsetPropertyMap propMap = new RefsetPropertyMap();
			propMap.put(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, defaultStatusNid);
			propMap.put(RefsetPropertyMap.REFSET_PROPERTY.CID_TWO, destinationUserConceptId);

			refsetHelper.newRefsetExtension(this.refsetId, componentId, EConcept.REFSET_TYPES.CID_CID, propMap, config);

			// termFactory.addUncommittedNoChecks(refsetConcept);
			termFactory.addUncommittedNoChecks(newMemberConcept);
			// RefexCAB newSpec = new RefexCAB(
			// TK_REFSET_TYPE.CID_CID,
			// componentId,
			// refsetId);
			// newSpec.put(RefexProperty.CNID1, defaultStatusNid);
			// newSpec.put(RefexProperty.CNID2, destinationUserConceptId);
			// RefexChronicleBI<?> newRefex = tc.construct(newSpec);
			// termFactory.addUncommittedNoChecks(component);
		}

		return;
	}

	/**
	 * Sets the destination and promotion status.
	 *
	 * @param componentId the component id
	 * @param destinationUserConceptId the destination user concept id
	 * @param statusConceptId the status concept id
	 * @throws Exception the exception
	 */
	public void setDestinationAndPromotionStatus(int componentId, int destinationUserConceptId, int statusConceptId) throws Exception {
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
		TerminologyBuilderBI tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(), config.getViewCoordinate());

		I_GetConceptData component = termFactory.getConcept(componentId);
		boolean statusAlreadyPresent = false;
		RefexVersionBI oldStatus = getLastPromotionTuple(componentId, config);
		if (oldStatus != null) {
			statusAlreadyPresent = true;
			long lastVersion = Long.MIN_VALUE;
			I_ExtendByRef oldExtension = termFactory.getExtension(oldStatus.getNid());
			I_ExtendByRefPartCidCid promotionStatusExtensionPart = null;
			List<? extends I_ExtendByRefPart> loopParts = oldExtension.getMutableParts();
			for (I_ExtendByRefPart loopPart : loopParts) {
				if (loopPart.getTime() >= lastVersion) {
					lastVersion = loopPart.getTime();
					promotionStatusExtensionPart = (I_ExtendByRefPartCidCid) loopPart;
				}
			}
			if (promotionStatusExtensionPart != null
					&& (promotionStatusExtensionPart.getStatusNid() != activeValueNid || promotionStatusExtensionPart.getC2id() != destinationUserConceptId || promotionStatusExtensionPart.getC1id() != statusConceptId)) {
				for (PathBI editPath : config.getEditingPathSet()) {
					I_ExtendByRefPartCidCid newPromotionStatusPart = (I_ExtendByRefPartCidCid) promotionStatusExtensionPart.makeAnalog(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid(), config.getDbConfig().getUserConcept().getNid(),
							editPath.getConceptNid(), Long.MAX_VALUE);
					newPromotionStatusPart.setC1id(statusConceptId);
					newPromotionStatusPart.setC2id(destinationUserConceptId);
					oldExtension.addVersion(newPromotionStatusPart);
				}
				// termFactory.addUncommittedNoChecks(refsetConcept);
				termFactory.addUncommittedNoChecks(component);
				// termFactory.commit();
			}
			// RefexCnidCnidVersionBI oldStatusCnid = (RefexCnidCnidVersionBI)
			// oldStatus;
			// if (oldStatusCnid.getCnid2() != destinationUserConceptId) {
			// for (PathBI editPath : config.getEditingPathSet()) {
			// RefexCnidCnidAnalogBI newVersion =
			// (RefexCnidCnidAnalogBI) oldStatusCnid.makeAnalog(activeValueNid,
			// config.getDbConfig().getUserConcept().getNid(),
			// editPath.getConceptNid(),
			// Long.MAX_VALUE);
			// newVersion.setCnid2(destinationUserConceptId);
			// oldStatus.getChronicle().getVersions().add(newVersion);
			// }
			// termFactory.addUncommittedNoChecks(component);
			// }
		} else {
			I_GetConceptData newMemberConcept = termFactory.getConcept(componentId);
			I_HelpRefsets refsetHelper = termFactory.getRefsetHelper(config);

			RefsetPropertyMap propMap = new RefsetPropertyMap();
			propMap.put(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, statusConceptId);
			propMap.put(RefsetPropertyMap.REFSET_PROPERTY.CID_TWO, destinationUserConceptId);

			refsetHelper.newRefsetExtension(this.refsetId, componentId, EConcept.REFSET_TYPES.CID_CID, propMap, config);

			// termFactory.addUncommittedNoChecks(refsetConcept);
			termFactory.addUncommittedNoChecks(newMemberConcept);
			// RefexCAB newSpec = new RefexCAB(
			// TK_REFSET_TYPE.CID_CID,
			// componentId,
			// refsetId);
			// newSpec.put(RefexProperty.CNID1, defaultStatusNid);
			// newSpec.put(RefexProperty.CNID2, destinationUserConceptId);
			// RefexChronicleBI<?> newRefex = tc.construct(newSpec);
			// termFactory.addUncommittedNoChecks(component);
		}

		return;
	}

}
