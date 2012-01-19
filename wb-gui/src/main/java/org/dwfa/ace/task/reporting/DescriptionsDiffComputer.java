package org.dwfa.ace.task.reporting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.reporting.DiffBase;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;

public class DescriptionsDiffComputer extends DiffBase {

	I_GetConceptData addedConceptsRefset;
	I_GetConceptData addedDescriptionsRefset;
	I_GetConceptData changedDescriptionsRefset;
	String refsetNamePrefix;
	I_ConfigAceFrame config;
	TerminologyBuilderBI tb;
	I_TermFactory tf;
	TerminologyStoreDI ts;

	public DescriptionsDiffComputer(String v1, String v2, String path1_uuid,
			String path2_uuid,
			List<Integer> v1_relationship_characteristic_filter_int,
			List<Integer> v2_relationship_characteristic_filter_int,
			List<Integer> v1_concept_status_filter_int,
			List<Integer> v2_concept_status_filter_int,
			List<Integer> v1_description_status_filter_int,
			List<Integer> v2_description_status_filter_int,
			List<Integer> v1_relationship_status_filter_int,
			List<Integer> v2_relationship_status_filter_int,
			boolean added_concepts, boolean deleted_concepts,
			boolean added_concepts_refex, boolean changed_concepts_refex,
			boolean changed_concept_status, boolean changed_concept_author,
			boolean changed_description_author, boolean changed_rel_author,
			boolean changed_refex_author, List<Integer> author1,
			List<Integer> author2, boolean changed_defined,
			boolean added_descriptions, boolean deleted_descriptions,
			boolean changed_description_status,
			boolean changed_description_term, boolean changed_description_type,
			boolean changed_description_language,
			boolean changed_description_case, boolean added_relationships,
			boolean deleted_relationships, boolean changed_relationship_status,
			boolean changed_relationship_characteristic,
			boolean changed_relationship_refinability,
			boolean changed_relationship_type,
			boolean changed_relationship_group, I_ConfigAceFrame config,
			boolean noDescendantsV1, boolean noDescendantsV2) {
		super(v1, v2, path1_uuid, path2_uuid,
				v1_relationship_characteristic_filter_int,
				v2_relationship_characteristic_filter_int,
				v1_concept_status_filter_int, v2_concept_status_filter_int,
				v1_description_status_filter_int, v2_description_status_filter_int,
				v1_relationship_status_filter_int, v2_relationship_status_filter_int,
				added_concepts, deleted_concepts, added_concepts_refex,
				changed_concepts_refex, changed_concept_status, changed_concept_author,
				changed_description_author, changed_rel_author, changed_refex_author,
				author1, author2, changed_defined, added_descriptions,
				deleted_descriptions, changed_description_status,
				changed_description_term, changed_description_type,
				changed_description_language, changed_description_case,
				added_relationships, deleted_relationships,
				changed_relationship_status, changed_relationship_characteristic,
				changed_relationship_refinability, changed_relationship_type,
				changed_relationship_group, config, noDescendantsV1, noDescendantsV2);
		this.config = config;
	}

	@Override
	protected void changedDescriptionCase(I_GetConceptData c,
			I_DescriptionTuple d1, I_DescriptionTuple d2) throws Exception {
		super.changedDescriptionCase(c, d1, d2);
		addToRefset(c,changedDescriptionsRefset, this.description_case_change);
	}

	protected void changedDescriptionLang(I_GetConceptData c,
			I_DescriptionTuple d1, I_DescriptionTuple d2) throws Exception {
		super.changedDescriptionLang(c, d1, d2);
		addToRefset(c, changedDescriptionsRefset, this.description_language_change);
	}

	@Override
	protected void changedDescriptionStatus(I_GetConceptData c,
			I_DescriptionTuple d1, I_DescriptionTuple d2) throws Exception {
		super.changedDescriptionStatus(c, d1, d2);
		addToRefset(c, changedDescriptionsRefset, this.description_status_change);
	}

	@Override
	protected void changedDescriptionTerm(I_GetConceptData c,
			I_DescriptionTuple d1, I_DescriptionTuple d2) throws Exception {
		super.changedDescriptionTerm(c, d1, d2);
		addToRefset(c, changedDescriptionsRefset, this.description_term_change);
	}

	@Override
	protected void changedDescriptionType(I_GetConceptData c,
			I_DescriptionTuple d1, I_DescriptionTuple d2) throws Exception {
		super.changedDescriptionType(c, d1, d2);
		addToRefset(c, changedDescriptionsRefset, this.description_type_change);
	}

	@Override
	protected void addedDescription(I_GetConceptData c, I_DescriptionTuple d)
	throws Exception {
		addToRefset(c, addedDescriptionsRefset, this.added_description_change);
	}

	@Override
	protected void addedConcept(I_GetConceptData c) throws Exception {
		addToRefset(c, addedConceptsRefset, this.added_concept_change);

	}

	private void addToRefset(I_GetConceptData member, I_GetConceptData refset, int changeType) {
		try {
			RefexCAB newSpec = new RefexCAB(
					TK_REFSET_TYPE.CID,
					member.getNid(),
					refset.getNid());
			newSpec.put(RefexProperty.CNID1, changeType);
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

	public void setup(String refsetNamePrefix) throws IOException, InvalidCAB, ContradictionException, TerminologyException {
		this.refsetNamePrefix = refsetNamePrefix;
		tf = Terms.get();
		ts = Ts.get();
		tb = ts.getTerminologyBuilder(config.getEditCoordinate(), config.getViewCoordinate());

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

	protected void processConcepts() throws Exception {
		I_TermFactory tf = Terms.get();
		AceLog.getAppLog().info("Getting concepts in DFS order.");
		//ArrayList<Integer> all_concepts = getAllConcepts();
		ArrayList<Integer> all_concepts = 
			getAllConceptsForParent(tf.uuidToNative(SNOMED.Concept.ROOT.getPrimoridalUid()));
		AceLog.getAppLog().info("Processing: " + all_concepts.size());
		long beg = System.currentTimeMillis();
		int i = 0;
		for (int id : all_concepts) {
			I_GetConceptData c = tf.getConcept(id);
			i++;
			processConcept(c, i, beg);
		}
		Terms.get().addUncommittedNoChecks(this.addedConceptsRefset);
		Terms.get().addUncommittedNoChecks(this.addedDescriptionsRefset);
		Terms.get().addUncommittedNoChecks(this.changedDescriptionsRefset);
		tf.commit();
	}
	
	protected ArrayList<Integer> getAllConceptsForParent(int parentConceptNid) throws Exception {
        ArrayList<Integer> all_concepts;
        I_TermFactory tf = Terms.get();
        if (!test_p) {
            all_concepts = getDescendants(
                    parentConceptNid,
                    this.allowed_position2);
            AceLog.getAppLog().info("Retrieved hierarchical: " + all_concepts.size());
            return all_concepts;
        } else {
            all_concepts = super.getAllConcepts();
            // for (int id : test_concepts) {
            // UUID uuid = Type3UuidFactory.fromSNOMED(id);
            // I_GetConceptData c = tf.getConcept(uuid);
            // all_concepts.add(0, c.getConceptNid());
            // }
        }
        return all_concepts;
    }

	protected void processConcept(I_GetConceptData c, int i, long beg)
	throws Exception {
		compareDescriptions(c);
		if (i % 10000 == 0) {
			System.out.println("Processed: " + i + " "
					+ ((System.currentTimeMillis() - beg) / 1000));
		}
	}

}
