package org.ihtsdo.project;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.refset.PromotionRefset;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.spec.ValidationException;

public class WorksetInitializerProcessor implements ProcessUnfetchedConceptDataBI {

	NidBitSetBI membersNidSet;
	ViewCoordinate vc;
	PromotionRefset promRef;
	int activeNid;
	UUID activeUuid;
	int inactiveNid;
	UUID inactiveUuid;
	ActivityUpdater updater;
	TerminologyBuilderBI tc;
	int workSetNid;

	NidBitSetBI excludedConcepts;
	NidBitSetBI currentMembersConcepts;
	NidBitSetBI includedConcepts;

	public WorksetInitializerProcessor(WorkSet workSet, I_GetConceptData sourceRefset, I_ConfigAceFrame config, ActivityUpdater updater) {
		super();
		try {
			vc = config.getViewCoordinate();
			tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
					config.getViewCoordinate());
			this.updater = updater;
			this.workSetNid = workSet.getId();
			promRef = workSet.getPromotionRefset(config);
			TerminologyStoreDI ts = Ts.get();
			I_TermFactory termFactory = Terms.get();

			excludedConcepts = ts.getEmptyNidSet();
			currentMembersConcepts = ts.getEmptyNidSet();
			includedConcepts = ts.getEmptyNidSet();
			membersNidSet = ts.getEmptyNidSet();

			updater.setTaskMessage("Processing sourceMembers");
			ConceptChronicleBI sourceRefsetChronicle = (ConceptChronicleBI) sourceRefset;
			Collection<? extends RefexVersionBI<?>> sourceRefsetMembersList = sourceRefsetChronicle.getCurrentRefsetMembers(config.getViewCoordinate());
			for (RefexVersionBI<?> loopMember : sourceRefsetMembersList) {
				includedConcepts.setMember(loopMember.getReferencedComponentNid());
				membersNidSet.setMember(loopMember.getReferencedComponentNid());
			}

			updater.setTaskMessage("Processing oldMembers");
			ConceptChronicleBI worksetConceptChronicle = (ConceptChronicleBI) workSet.getConcept();
			Collection<? extends RefexVersionBI<?>> worksetMembersList = worksetConceptChronicle.getCurrentRefsetMembers(config.getViewCoordinate());
			for (RefexVersionBI<?> loopMember : worksetMembersList) {
				currentMembersConcepts.setMember(loopMember.getReferencedComponentNid());
				membersNidSet.setMember(loopMember.getReferencedComponentNid());
			}

			List<I_GetConceptData> exclusionRefsets = workSet.getExclusionRefsets();

			updater.setTaskMessage("Processing exclusions");
			for (I_GetConceptData loopRefset : exclusionRefsets) {
				ConceptChronicleBI loopRefsetConcept = (ConceptChronicleBI) loopRefset;
				Collection<? extends RefexVersionBI<?>> exclusionMembersList = loopRefset.getCurrentRefsetMembers(config.getViewCoordinate());
				for (RefexVersionBI<?> loopMember : exclusionMembersList) {
					excludedConcepts.setMember(loopMember.getReferencedComponentNid());
					membersNidSet.setMember(loopMember.getReferencedComponentNid());
				}
			}

			activeNid = SnomedMetadataRf1.CURRENT_RF1.getLenient().getNid();
			inactiveNid = SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getNid();
			activeUuid = SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid();
			inactiveUuid = SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid();
			updater.setTaskMessage("Initializing WorkSet");
			updater.startCount(membersNidSet.cardinality());
		} catch (ValidationException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	@Override
	public boolean continueWork() {
		return true;
	}

	@Override
	public NidBitSetBI getNidSet() throws IOException {
		return membersNidSet;
	}

	@Override
	public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher)
	throws Exception {
		if (membersNidSet.isMember(cNid)) {
			ConceptVersionBI c = fetcher.fetch(vc);
			if (processConcept(c)) {
				//fetcher.update(c.getChronicle());
				Terms.get().addUncommittedNoChecks((I_GetConceptData) c.getChronicle());
			}
		}

	}

	private boolean processConcept(ConceptVersionBI concept) {
		boolean update = false;
		updater.incrementCount();
		
		if (currentMembersConcepts.isMember(concept.getNid()) && 
				includedConcepts.isMember(concept.getNid()) && 
				!excludedConcepts.isMember(concept.getNid())) {
			// already a member, do nothing
		} else if (!currentMembersConcepts.isMember(concept.getNid()) && 
				includedConcepts.isMember(concept.getNid()) && 
				!excludedConcepts.isMember(concept.getNid())) {
			// should add
			update = true;
			try {
				RefexCAB newSpec = new RefexCAB(
						TK_REFSET_TYPE.CID,
						concept.getNid(),
						workSetNid);
				newSpec.put(RefexProperty.CNID1, activeNid);
				RefexChronicleBI<?> newRefex = tc.constructIfNotCurrent(newSpec);
				
				RefexCAB newSpecForProm = new RefexCAB(
						TK_REFSET_TYPE.CID,
						concept.getNid(),
						promRef.getRefsetId());
				newSpecForProm.put(RefexProperty.CNID1, activeNid);
				RefexChronicleBI<?> newRefexForProm = tc.constructIfNotCurrent(newSpecForProm);
				concept.addAnnotation(newRefexForProm);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (Exception e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		} else if (currentMembersConcepts.isMember(concept.getNid()) && 
				(!includedConcepts.isMember(concept.getNid()) || 
				excludedConcepts.isMember(concept.getNid()))) {
			// should remove
			update = true;
			try {
				RefexCAB newSpec = new RefexCAB(
						TK_REFSET_TYPE.CID,
						concept.getNid(),
						workSetNid);
				newSpec.put(RefexProperty.CNID1, inactiveNid);
				newSpec.setStatusUuid(inactiveUuid);
				RefexChronicleBI<?> newRefex = tc.constructIfNotCurrent(newSpec);
				
				RefexCAB newSpecForProm = new RefexCAB(
						TK_REFSET_TYPE.CID,
						concept.getNid(),
						promRef.getRefsetId());
				newSpecForProm.put(RefexProperty.CNID1, inactiveNid);
				newSpecForProm.setStatusUuid(inactiveUuid);
				RefexChronicleBI<?> newRefexForProm = tc.constructIfNotCurrent(newSpecForProm);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (Exception e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		} else {
			// do nothing
		}
		return update;
		
	}

}
