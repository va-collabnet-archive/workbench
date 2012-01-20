package org.dwfa.ace.task.reporting;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.PositionSet;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;

public class DescriptionsDiffComputer {

	I_GetConceptData addedConceptsRefset;
	I_GetConceptData addedDescriptionsRefset;
	I_GetConceptData changedDescriptionsRefset;
	String refsetNamePrefix;
	I_ConfigAceFrame config;
	TerminologyBuilderBI tb;
	I_TermFactory tf;
	TerminologyStoreDI ts;
	UUID path1Uuid;
	UUID path2Uuid;
	String time1;
	String time2;
	ViewCoordinate v1;
	ViewCoordinate v2;
	I_GetConceptData snomedRoot;
	int activeNid;
	NidSet relTypeNids;

	public DescriptionsDiffComputer(I_ConfigAceFrame config, String refsetNamePrefix,
			String initialTime1, String laterTime2, UUID path1Uuid, UUID path2Uuid) throws Exception {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");
		
		if (TimeHelper.getTimeFromString(laterTime2, dateFormat) <= 
			TimeHelper.getTimeFromString(initialTime1, dateFormat)) throw new Exception("Later time should be greater than initial time.");
		
		this.config = config;

		tf = Terms.get();
		ts = Ts.get();
		tb = ts.getTerminologyBuilder(config.getEditCoordinate(), config.getViewCoordinate());

		this.refsetNamePrefix = refsetNamePrefix + UUID.randomUUID().toString();
		this.time1 = initialTime1;
		this.time2 = laterTime2;
		this.path1Uuid = path1Uuid;
		this.path2Uuid = path2Uuid;
		v1 = new ViewCoordinate(config.getViewCoordinate());
		PositionSet posSet1 = new PositionSet(ts.newPosition(ts.getPath(ts.getNidForUuids(path1Uuid)), TimeHelper.getTimeFromString(initialTime1, dateFormat)));
		v1.setPositionSet(posSet1);
		v2 = new ViewCoordinate(config.getViewCoordinate());
		PositionSet posSet2 = new PositionSet(ts.newPosition(ts.getPath(ts.getNidForUuids(path2Uuid)), TimeHelper.getTimeFromString(laterTime2, dateFormat)));
		v2.setPositionSet(posSet2);
		snomedRoot = tf.getConcept(SNOMED.Concept.ROOT.getUids());
		activeNid = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid();
		relTypeNids = new NidSet();
		relTypeNids.add(SNOMED.Concept.IS_A.localize().getNid());
	}


	private void addToRefset(I_GetConceptData member, I_GetConceptData refset) {
		try {
			RefexCAB newSpec = new RefexCAB(
					TK_REFSET_TYPE.CID,
					member.getNid(),
					refset.getNid());
			newSpec.put(RefexProperty.CNID1, activeNid);
			RefexChronicleBI<?> newRefex = tb.construct(newSpec);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidCAB e) {
			e.printStackTrace();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
	}

	public I_GetConceptData getAddedConceptsRefset() {
		return addedConceptsRefset;
	}

	public void setAddedConceptsRefset(I_GetConceptData addedConceptsRefset) {
		this.addedConceptsRefset = addedConceptsRefset;
	}

	public I_GetConceptData getAddedDescriptionsRefset() {
		return addedDescriptionsRefset;
	}

	public void setAddedDescriptionsRefset(I_GetConceptData addedDescriptionsRefset) {
		this.addedDescriptionsRefset = addedDescriptionsRefset;
	}

	public I_GetConceptData getChangedDescriptionsRefset() {
		return changedDescriptionsRefset;
	}

	public void setChangedDescriptionsRefset(
			I_GetConceptData changedDescriptionsRefset) {
		this.changedDescriptionsRefset = changedDescriptionsRefset;
	}

	public String getRefsetNamePrefix() {
		return refsetNamePrefix;
	}

	public void setRefsetNamePrefix(String refsetNamePrefix) {
		this.refsetNamePrefix = refsetNamePrefix;
	}

	public void setup() throws IOException, InvalidCAB, ContradictionException, TerminologyException {
		ConceptCB addedConceptsCB = new ConceptCB(refsetNamePrefix + " Added Concepts (refset)", 
				refsetNamePrefix + " Added Concepts",
				LANG_CODE.EN, ArchitectonicAuxiliary.Concept.IS_A_REL.getPrimoridalUid(),
				RefsetAuxiliary.Concept.REFSET_IDENTITY.getPrimoridalUid());
		ConceptChronicleBI ac = tb.constructIfNotCurrent(addedConceptsCB);
		addedConceptsRefset = (I_GetConceptData) ac;
		tf.addUncommittedNoChecks(addedConceptsRefset);

		ConceptCB addedDescriptionsCB = new ConceptCB(refsetNamePrefix + " Added Descriptions (refset)", 
				refsetNamePrefix + " Added Descriptions",
				LANG_CODE.EN, ArchitectonicAuxiliary.Concept.IS_A_REL.getPrimoridalUid(),
				RefsetAuxiliary.Concept.REFSET_IDENTITY.getPrimoridalUid());
		ConceptChronicleBI ad = tb.constructIfNotCurrent(addedDescriptionsCB);
		addedDescriptionsRefset = (I_GetConceptData) ad;
		tf.addUncommittedNoChecks(addedDescriptionsRefset);

		ConceptCB changedDescriptionsCB = new ConceptCB(refsetNamePrefix + " Changed Descriptions (refset)", 
				refsetNamePrefix + " Changed Descriptions",
				LANG_CODE.EN, ArchitectonicAuxiliary.Concept.IS_A_REL.getPrimoridalUid(),
				RefsetAuxiliary.Concept.REFSET_IDENTITY.getPrimoridalUid());
		ConceptChronicleBI cd = tb.constructIfNotCurrent(changedDescriptionsCB);
		changedDescriptionsRefset = (I_GetConceptData) cd;
		tf.addUncommittedNoChecks(changedDescriptionsRefset);
	}

	public void run() throws Exception {
		Processor p = new Processor();
		ts.iterateConceptDataInSequence(p);
		Terms.get().addUncommittedNoChecks(addedConceptsRefset);
		Terms.get().addUncommittedNoChecks(addedDescriptionsRefset);
		Terms.get().addUncommittedNoChecks(changedDescriptionsRefset);
		tf.commit();
	}

	private class Processor implements ProcessUnfetchedConceptDataBI {
		int countIterated;
		int countCompared;
		int countDiff;

		public Processor() {
			super();
			countIterated=0;
			countCompared=0;
			countDiff=0;
		}

		@Override
		public boolean continueWork() {
			return true;
		}

		@Override
		public NidBitSetBI getNidSet() throws IOException {
			return Ts.get().getAllConceptNids();
		}

		@Override
		public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher)
		throws Exception {
			countIterated++;
			ConceptChronicleBI concept = fetcher.fetch();

			if (snomedRoot.isParentOfOrEqualTo((I_GetConceptData) concept, 
					config.getAllowedStatus(), 
					config.getDestRelTypes(), config.getViewPositionSetReadOnly(), 
					config.getPrecedence(), 
					config.getConflictResolutionStrategy())) {
				
				countCompared++;
				
				boolean diff = false;
				boolean changeDiffFound = false;

				ConceptVersionBI version1 = concept.getVersion(v1);
				ConceptVersionBI version2 = concept.getVersion(v2);

				if (version1.getConAttrs().getVersion(v1) == null &&
						version2.getConAttrs().getVersion(v2) != null) {
					// New concept
					diff = true;
					addToRefset((I_GetConceptData) concept, addedConceptsRefset);
				}
				
				HashMap<UUID, DescriptionVersionBI> v1Descs = new HashMap<UUID,DescriptionVersionBI>();
				for (DescriptionVersionBI loopDesc : version1.getDescsActive()) {
					v1Descs.put(loopDesc.getPrimUuid(), loopDesc);
				}
				
				HashMap<UUID, DescriptionVersionBI> v2Descs = new HashMap<UUID,DescriptionVersionBI>();
				for (DescriptionVersionBI loopDesc : version2.getDescsActive()) {
					v2Descs.put(loopDesc.getPrimUuid(), loopDesc);
				}
				
				for (UUID loopDescUuid : v1Descs.keySet()) {
					if (!v2Descs.keySet().contains(loopDescUuid)) {
						// Retired desc
						changeDiffFound = true;
					}
				}
				
				for (UUID loopDescUuid : v2Descs.keySet()) {
					if (!v1Descs.keySet().contains(loopDescUuid)) {
						// New desc
						diff = true;
						addToRefset((I_GetConceptData) concept, addedDescriptionsRefset);
					}
				}
				
				for (UUID loopDescUuid : v1Descs.keySet()) {
					if (v2Descs.keySet().contains(loopDescUuid)) {
						DescriptionVersionBI d1 = v1Descs.get(loopDescUuid);
						DescriptionVersionBI d2 = v2Descs.get(loopDescUuid);
						
						if (d1.getStatusNid() != d2.getStatusNid()) {
							// Changed Status
							changeDiffFound = true;
						}
						
						if (d1.getTypeNid() != d2.getTypeNid()) {
							// Changed Type
							changeDiffFound = true;
						}
						
						if (d1.getText() != d2.getText()) {
							// Changed Text
							changeDiffFound = true;
						}
						
						if (d1.getLang() != d2.getLang()) {
							// Changed Lang
							changeDiffFound = true;
						}
						
						if (d1.isInitialCaseSignificant() != d2.isInitialCaseSignificant()) {
							// Changed ics
							changeDiffFound = true;
						}
					}
				}
				
				if (changeDiffFound) {
					diff = true;
					addToRefset((I_GetConceptData) concept, changedDescriptionsRefset);
				}
				
				if (diff) {
					countDiff++;
				}
			}

			if (countIterated % 10000 == 0) {
				System.out.println("Iterated: " + countIterated + " Compared: " + countCompared + " Diffs found: " + countDiff);
			}

		}

	}

}
