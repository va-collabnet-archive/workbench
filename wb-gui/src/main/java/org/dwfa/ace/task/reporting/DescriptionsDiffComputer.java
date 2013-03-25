package org.dwfa.ace.task.reporting;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.apache.commons.beanutils.converters.CalendarConverter;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.tapi.I_ConceptualizeUniversally;
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
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.PositionSet;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

/**
 * Version diff computes, generates the following refsets: 1- Conceptos nuevos
 * 2- Conceptos retirados 3- Conceptos reactivados 4- Conceptos que cambiaron
 * semantic tag (con termino igual) 5- Coneptos que cambiaron el FSN 6- Coneptos
 * que cambiaron el Preferred Term (EN-US) 7- Conceptos Activos al que se le
 * retiraron descripciones (desc status active->inactive, o en lang refset EN-US
 * member status active -> inactive) 8- Coneptos con terminos nuevos 9- Concepts
 * con terminos con acceptabilidad EN-US cambiado 10- Conceptos con
 * descripciones que pasaron de (desc status inactive->active, o en lang refset
 * EN-US member status inactive->active)
 */
public class DescriptionsDiffComputer {

	I_GetConceptData addedConceptsRefset;
	I_GetConceptData retiredConceptsRefset;
	I_GetConceptData reactivatedConceptsRefset;
	I_GetConceptData semtagChangesRefset;
	I_GetConceptData fsnChangesRefset;
	I_GetConceptData prefChangesRefset;
	I_GetConceptData retiredDescriptionsRefset;
	I_GetConceptData addedDescriptionsRefset;
	I_GetConceptData changedAcceptabilityRefset;
	I_GetConceptData reactivatedDescriptionsRefset;
	String refsetNamePrefix;
	I_ConfigAceFrame config;
	TerminologyBuilderBI tb;
	I_TermFactory tf;
	TerminologyStoreDI ts;
	UUID path1Uuid;
	UUID path2Uuid;
	String time1;
	String time2;
	long time1Long;
	long time2Long;
	ViewCoordinate v1;
	ViewCoordinate v2;
	I_GetConceptData snomedRoot;
	int activeNid;
	NidSet relTypeNids;
	int fsnTypeNid;
	int preferredAccepabilityNid;
	int descriptionTypeNid;
	int usLangRefNid;
	int gbLangRefNid;
	long startTime;
	I_ShowActivity activity;
	int count = 0;
	NidSet allowedStatus;
	private SimpleDateFormat dateFormat;

	public DescriptionsDiffComputer(I_ConfigAceFrame config, String refsetNamePrefix, String initialTime1, String laterTime2, UUID path1Uuid, UUID path2Uuid) throws Exception {

		dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");
		time2Long = TimeHelper.getTimeFromString(laterTime2, dateFormat);
		time1Long = TimeHelper.getTimeFromString(initialTime1, dateFormat);
		if (time2Long <= time1Long)
			throw new Exception("Later time should be greater than initial time.");

		this.config = config;

		tf = Terms.get();
		ts = Ts.get();
		tb = ts.getTerminologyBuilder(config.getEditCoordinate(), config.getViewCoordinate());

		this.refsetNamePrefix = refsetNamePrefix;
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
		fsnTypeNid = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid();
		relTypeNids = new NidSet();
		relTypeNids.add(SNOMED.Concept.IS_A.localize().getNid());
		usLangRefNid = SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getLenient().getNid();
		gbLangRefNid = SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2.getLenient().getNid();
		preferredAccepabilityNid = SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid();
		descriptionTypeNid = SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid();

		allowedStatus = new NidSet();
		allowedStatus.addAll(config.getAllowedStatus().getSetValues());

	}

	private void addToRefset(I_GetConceptData member, I_GetConceptData refset) {
		try {
			RefexCAB newSpec = new RefexCAB(TK_REFEX_TYPE.CID, member.getNid(), refset.getNid());
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
		return changedAcceptabilityRefset;
	}

	public void setChangedDescriptionsRefset(I_GetConceptData changedDescriptionsRefset) {
		this.changedAcceptabilityRefset = changedDescriptionsRefset;
	}

	public String getRefsetNamePrefix() {
		return refsetNamePrefix;
	}

	public void setRefsetNamePrefix(String refsetNamePrefix) {
		this.refsetNamePrefix = refsetNamePrefix;
	}

	public void setup() throws IOException, InvalidCAB, ContradictionException, TerminologyException {
		HashSet<I_ShowActivity> activities = new HashSet<I_ShowActivity>();
		activity = Terms.get().newActivityPanel(true, config, "<html>Creating Diff refsets: " + refsetNamePrefix, true);
		activities.add(activity);
		activity.setValue(0);
		activity.setIndeterminate(true);
		activity.setProgressInfoLower("Preparing refsets...");
		startTime = System.currentTimeMillis();

		Calendar init = new GregorianCalendar();
		init.setTimeInMillis(time1Long);
		Calendar end = new GregorianCalendar();
		end.setTimeInMillis(time2Long);
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.mm.dd");
		
		
		String fsnPostFix = "";		
		
		Collection<I_ConceptualizeUniversally> types;
		ConceptVersionBI refsetIdentity = ts.getConceptVersion(config.getViewCoordinate(),RefsetAuxiliary.Concept.REFSET_IDENTITY.getPrimoridalUid());
		Collection<? extends ConceptVersionBI> relationships = refsetIdentity.getRelationshipsIncomingSourceConceptsActive();
		Integer num = 0;
		for (ConceptVersionBI conceptVersionBI : relationships) {
			Collection<? extends DescriptionVersionBI> fsns = conceptVersionBI.getDescriptionsFullySpecifiedActive();
			for (DescriptionVersionBI descriptionVersionBI : fsns) {
				String desc = descriptionVersionBI.toUserString();
				if(desc.contains(refsetNamePrefix) && desc.contains("_")){
					String[] splited = desc.split("_");
					String[] splited2 = splited[1].split("\\(");
					Integer currentNum = Integer.parseInt(splited2[0].trim());
					if(currentNum >=  num){
						num = currentNum + 1;
					}
				}else if(desc.contains(refsetNamePrefix) && !desc.contains("_")){
					num = 1;
				}
			}
		}
		
		if(num > 0 ){
			fsnPostFix = "_" + num ;
		}
		
		String preferredNamePrefix = "Comparison "+ formatter.format(init.getTime())  +  " to " + formatter.format(init.getTime()) +  " (Created on "  + dateFormat.format(new Date()) + ")";
		ConceptCB addedConceptsRefsetCB = new ConceptCB(refsetNamePrefix + " Added Concepts" + fsnPostFix + " (refset)", preferredNamePrefix + " Added Concepts" + fsnPostFix, LANG_CODE.EN, ArchitectonicAuxiliary.Concept.IS_A_REL.getPrimoridalUid(), RefsetAuxiliary.Concept.REFSET_IDENTITY.getPrimoridalUid());
		ConceptChronicleBI ac = tb.constructIfNotCurrent(addedConceptsRefsetCB);
		addedConceptsRefset = (I_GetConceptData) ac;
		tf.addUncommittedNoChecks(addedConceptsRefset);

		ConceptCB retiredConceptsRefsetCB = new ConceptCB(refsetNamePrefix + " Retired Concepts" + fsnPostFix + " (refset)", preferredNamePrefix + " Retired Concepts"+ fsnPostFix, LANG_CODE.EN, ArchitectonicAuxiliary.Concept.IS_A_REL.getPrimoridalUid(), RefsetAuxiliary.Concept.REFSET_IDENTITY.getPrimoridalUid());
		ConceptChronicleBI rcd = tb.constructIfNotCurrent(retiredConceptsRefsetCB);
		retiredConceptsRefset = (I_GetConceptData) rcd;
		tf.addUncommittedNoChecks(retiredConceptsRefset);

		ConceptCB reactivatedConceptsRefsetCB = new ConceptCB(refsetNamePrefix + " Reactivated Concepts" + fsnPostFix + " (refset)",  preferredNamePrefix + " Reactivated Concepts"+ fsnPostFix, LANG_CODE.EN, ArchitectonicAuxiliary.Concept.IS_A_REL.getPrimoridalUid(), RefsetAuxiliary.Concept.REFSET_IDENTITY.getPrimoridalUid());
		ConceptChronicleBI reacs = tb.constructIfNotCurrent(reactivatedConceptsRefsetCB);
		
		reactivatedConceptsRefset = (I_GetConceptData) reacs;
		tf.addUncommittedNoChecks(reactivatedConceptsRefset);

		ConceptCB semtagChangesRefsetCB = new ConceptCB(refsetNamePrefix + " SemTag Changes" + fsnPostFix + " (refset)", preferredNamePrefix + " SemTag Changes"+ fsnPostFix, LANG_CODE.EN, ArchitectonicAuxiliary.Concept.IS_A_REL.getPrimoridalUid(), RefsetAuxiliary.Concept.REFSET_IDENTITY.getPrimoridalUid());
		ConceptChronicleBI stc = tb.constructIfNotCurrent(semtagChangesRefsetCB);
		semtagChangesRefset = (I_GetConceptData) stc;
		tf.addUncommittedNoChecks(semtagChangesRefset);

		ConceptCB fsnChangesRefsetCB = new ConceptCB(refsetNamePrefix + " FSN Changes" + fsnPostFix + " (refset)", preferredNamePrefix + " FSN Changes"+ fsnPostFix, LANG_CODE.EN, ArchitectonicAuxiliary.Concept.IS_A_REL.getPrimoridalUid(), RefsetAuxiliary.Concept.REFSET_IDENTITY.getPrimoridalUid());
		ConceptChronicleBI fsnc = tb.constructIfNotCurrent(fsnChangesRefsetCB);
		fsnChangesRefset = (I_GetConceptData) fsnc;
		tf.addUncommittedNoChecks(fsnChangesRefset);

		ConceptCB prefChangesRefsetCB = new ConceptCB(refsetNamePrefix + " Pref Changes" + fsnPostFix + " (refset)", preferredNamePrefix + " Pref Changes"+ fsnPostFix, LANG_CODE.EN, ArchitectonicAuxiliary.Concept.IS_A_REL.getPrimoridalUid(), RefsetAuxiliary.Concept.REFSET_IDENTITY.getPrimoridalUid());
		ConceptChronicleBI prc = tb.constructIfNotCurrent(prefChangesRefsetCB);
		prefChangesRefset = (I_GetConceptData) prc;
		tf.addUncommittedNoChecks(prefChangesRefset);

		ConceptCB retiredDescriptionsRefsetCB = new ConceptCB(refsetNamePrefix + " Retired Descriptions" + fsnPostFix + " (refset)", preferredNamePrefix + " Retired Descriptions"+ fsnPostFix, LANG_CODE.EN, ArchitectonicAuxiliary.Concept.IS_A_REL.getPrimoridalUid(), RefsetAuxiliary.Concept.REFSET_IDENTITY.getPrimoridalUid());
		ConceptChronicleBI retd = tb.constructIfNotCurrent(retiredDescriptionsRefsetCB);
		retiredDescriptionsRefset = (I_GetConceptData) retd;
		tf.addUncommittedNoChecks(retiredDescriptionsRefset);

		ConceptCB addedDescriptionsRefsetCB = new ConceptCB(refsetNamePrefix + " New Descriptions" + fsnPostFix + " (refset)", preferredNamePrefix + " New Descriptions"+ fsnPostFix, LANG_CODE.EN, ArchitectonicAuxiliary.Concept.IS_A_REL.getPrimoridalUid(), RefsetAuxiliary.Concept.REFSET_IDENTITY.getPrimoridalUid());
		ConceptChronicleBI addd = tb.constructIfNotCurrent(addedDescriptionsRefsetCB);
		addedDescriptionsRefset = (I_GetConceptData) addd;
		tf.addUncommittedNoChecks(addedDescriptionsRefset);

		ConceptCB changedAcceptabilityRefsetCB = new ConceptCB(refsetNamePrefix + " Changed Acceptability" + fsnPostFix + " (refset)", preferredNamePrefix + " Changed Acceptability"+ fsnPostFix, LANG_CODE.EN, ArchitectonicAuxiliary.Concept.IS_A_REL.getPrimoridalUid(), RefsetAuxiliary.Concept.REFSET_IDENTITY.getPrimoridalUid());
		ConceptChronicleBI cha = tb.constructIfNotCurrent(changedAcceptabilityRefsetCB);
		changedAcceptabilityRefset = (I_GetConceptData) cha;
		tf.addUncommittedNoChecks(changedAcceptabilityRefset);

		ConceptCB reactivatedDescriptionsRefsetCB = new ConceptCB(refsetNamePrefix + " Reactivated Descriptions" + fsnPostFix + " (refset)", preferredNamePrefix + " Reactivated Descriptions"+ fsnPostFix, LANG_CODE.EN, ArchitectonicAuxiliary.Concept.IS_A_REL.getPrimoridalUid(),
				RefsetAuxiliary.Concept.REFSET_IDENTITY.getPrimoridalUid());
		ConceptChronicleBI read = tb.constructIfNotCurrent(reactivatedDescriptionsRefsetCB);
		reactivatedDescriptionsRefset = (I_GetConceptData) read;
		tf.addUncommittedNoChecks(reactivatedDescriptionsRefset);
	}

	public void run() throws Exception {
		activity.setValue(0);
		activity.setMaximum(ts.getAllConceptNids().cardinality());
		activity.setIndeterminate(false);
		count = 0;
		Processor p = new Processor();
		ts.iterateConceptDataInSequence(p);
		tf.addUncommittedNoChecks(addedConceptsRefset);
		tf.addUncommittedNoChecks(retiredConceptsRefset);
		tf.addUncommittedNoChecks(addedDescriptionsRefset);
		tf.addUncommittedNoChecks(changedAcceptabilityRefset);
		tf.commit();
		long endTime = System.currentTimeMillis();
		long elapsed = endTime - startTime;
		String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);
		activity.setProgressInfoUpper("<html>Refset creation ended.");
		activity.setProgressInfoLower("Elapsed: " + elapsedStr);
		try {
			activity.complete();
		} catch (ComputationCanceled e1) {
			// Nothing to do
		}
	}

	private class Processor implements ProcessUnfetchedConceptDataBI {
		int countIterated;
		int countCompared;
		int countDiff;
		List<Integer> monitorList;

		public Processor() throws TerminologyException, IOException {
			super();
			countIterated = 0;
			countCompared = 0;
			countDiff = 0;
			monitorList = new ArrayList<Integer>();
			monitorList.add(tf.uuidToNative(UUID.fromString("d47b4cbe-76c0-3266-8b0c-26eba8b09aa4")));
			monitorList.add(tf.uuidToNative(UUID.fromString("31c346d2-8ff7-371b-89d3-5a7121e18a86")));
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
		public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
			if (monitorList.contains(cNid)) {
				System.out.println("Monitored concept!");
			}
			countIterated++;
			ConceptChronicleBI concept = fetcher.fetch();

			if (snomedRoot.isParentOfOrEqualTo((I_GetConceptData) concept, allowedStatus, config.getDestRelTypes(), config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy())) {

				countCompared++;

				boolean diff = false;
				boolean changeDiffFound = false;

				ConceptVersionBI version1 = concept.getVersion(v1);
				ConceptVersionBI version2 = concept.getVersion(v2);

				ConceptAttributeVersionBI attrV1 = version1.getConceptAttributes().getVersion(v1);
				ConceptAttributeVersionBI attrV2 = version2.getConceptAttributes().getVersion(v2);

				if (attrV1 == null && attrV2 != null) {
					if (attrV2.getPrimordialVersion().getTime() <= time1Long) {
						// Reactivated concept
						diff = true;
						addToRefset((I_GetConceptData) concept, reactivatedConceptsRefset);
					} else {
						// New concept
						diff = true;
						addToRefset((I_GetConceptData) concept, addedConceptsRefset);
					}

				} else if (attrV1 != null && attrV2 == null) {
					// retired concept
					diff = true;
					addToRefset((I_GetConceptData) concept, retiredConceptsRefset);
				}

				String v1Fsn = "";
				String v2Fsn = "";
				String v1SemTag = "";
				String v2SemTag = "";
				String v1Pref = "";
				String v2Pref = "";
				HashMap<UUID, DescriptionVersionBI> v1Descs = new HashMap<UUID, DescriptionVersionBI>();
				for (DescriptionVersionBI loopDescPre : version1.getDescriptionsActive()) {
					DescriptionVersionBI loopDesc = (DescriptionVersionBI) loopDescPre.getVersion(v1);
					v1Descs.put(loopDesc.getPrimUuid(), loopDesc);
					for (RefexVersionBI<?> loopAnnot : loopDesc.getAnnotationMembersActive(v1, usLangRefNid)) {
						RefexNidVersionBI loopAnnotC = (RefexNidVersionBI) loopAnnot;
						if (loopDesc.getTypeNid() == fsnTypeNid && loopAnnotC.getNid1() == preferredAccepabilityNid) {
							v1Fsn = loopDesc.getText();
							try {
								v1SemTag = v1Fsn.substring(v1Fsn.lastIndexOf('(') + 1, v1Fsn.lastIndexOf(')'));
							} catch (Exception e) {
								// bad semtag, will be detected in QA;
							}
						}
						if (loopDesc.getTypeNid() == descriptionTypeNid && loopAnnotC.getNid1() == preferredAccepabilityNid) {
							v1Pref = loopDesc.getText();
						}
					}
				}

				HashMap<UUID, DescriptionVersionBI> v2Descs = new HashMap<UUID, DescriptionVersionBI>();
				for (DescriptionVersionBI loopDescPre : version2.getDescriptionsActive()) {
					DescriptionVersionBI loopDesc = (DescriptionVersionBI) loopDescPre.getVersion(v2);
					v2Descs.put(loopDesc.getPrimUuid(), loopDesc);
					for (RefexVersionBI<?> loopAnnot : loopDesc.getAnnotationMembersActive(v2, usLangRefNid)) {
						RefexNidVersionBI loopAnnotC = (RefexNidVersionBI) loopAnnot;
						if (loopDesc.getTypeNid() == fsnTypeNid && loopAnnotC.getNid1() == preferredAccepabilityNid) {
							v2Fsn = loopDesc.getText();
							try {
								v2SemTag = v2Fsn.substring(v2Fsn.lastIndexOf('(') + 1, v2Fsn.lastIndexOf(')'));
							} catch (Exception e) {
								// bad semtag, will be detected in QA;
							}
						}
						if (loopDesc.getTypeNid() == descriptionTypeNid && loopAnnotC.getNid1() == preferredAccepabilityNid) {
							v2Pref = loopDesc.getText();
						}
					}
				}

				if (attrV1 != null && attrV2 != null) { // if the concept is not
														// new
					if (!v1Fsn.equals(v2Fsn)) {
						// Changed FSN
						changeDiffFound = true;
						addToRefset((I_GetConceptData) concept, fsnChangesRefset);
					}

					if (!v1SemTag.equals(v2SemTag)) {
						// Changed Sem Tag
						System.out.println("semtagChangesRefset: " + v1SemTag + " > " + v2SemTag);
						changeDiffFound = true;
						addToRefset((I_GetConceptData) concept, semtagChangesRefset);
					}

					if (!v1Pref.equals(v2Pref)) {
						// Changed Pref
						changeDiffFound = true;
						addToRefset((I_GetConceptData) concept, prefChangesRefset);
					}

					for (UUID loopDescUuid : v1Descs.keySet()) {
						if (!v2Descs.keySet().contains(loopDescUuid)) {
							// Retired desc
							changeDiffFound = true;
							addToRefset((I_GetConceptData) concept, retiredDescriptionsRefset);
						}
					}

					for (UUID loopDescUuid : v2Descs.keySet()) {
						if (!v1Descs.keySet().contains(loopDescUuid)) {
							if (v2Descs.get(loopDescUuid).getPrimordialVersion().getTime() > time1Long) {
								// New desc
								diff = true;
								addToRefset((I_GetConceptData) concept, addedDescriptionsRefset);
							} else {
								// Reactivated desc
								diff = true;
								addToRefset((I_GetConceptData) concept, reactivatedDescriptionsRefset);
							}
						}
					}
				}

				for (UUID loopDescUuid : v1Descs.keySet()) {
					if (v2Descs.keySet().contains(loopDescUuid)) {
						DescriptionVersionBI d1 = v1Descs.get(loopDescUuid);
						DescriptionVersionBI d2 = v2Descs.get(loopDescUuid);

						int v1PreferenceUS = Integer.MIN_VALUE;
						int v2PreferenceUS = Integer.MIN_VALUE;

						for (RefexVersionBI<?> annot : d1.getAnnotationMembersActive(v1, usLangRefNid)) {
							RefexNidVersionBI annotCnid = (RefexNidVersionBI) annot;
							v1PreferenceUS = annotCnid.getNid1();
						}

						for (RefexVersionBI<?> annot : d2.getAnnotationMembersActive(v2, usLangRefNid)) {
							RefexNidVersionBI annotCnid = (RefexNidVersionBI) annot;
							v2PreferenceUS = annotCnid.getNid1();
						}

						if (v1PreferenceUS != v2PreferenceUS) {
							// Changed US pref
							changeDiffFound = true;
							addToRefset((I_GetConceptData) concept, changedAcceptabilityRefset);
						}
					}
				}

				if (changeDiffFound) {
					diff = true;
				}

				if (diff) {
					countDiff++;
				}
			}
			activity.setValue(activity.getValue() + 1);
			if (countIterated % 10000 == 0) {
				System.out.println("Iterated: " + countIterated + " Compared: " + countCompared + " Diffs found: " + countDiff);
				activity.setProgressInfoLower("Iterated: " + countIterated + " Compared: " + countCompared + " Diffs found: " + countDiff);
			}

		}

	}

}
