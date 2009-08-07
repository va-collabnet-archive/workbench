package org.dwfa.mojo.diff;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.tapi.TerminologyException;

/**
 * Generate a Refset for all concepts in the Concept enum
 * 
 * @goal version-diff
 * 
 * @phase generate-resources
 * 
 * @requiresDependencyResolution compile
 */
public class VersionDiff extends AbstractMojo {

	/**
	 * The uuid of the path.
	 * 
	 * @parameter
	 */
	private String path_uuid;

	/**
	 * The id of v1.
	 * 
	 * @parameter
	 */
	private Integer v1_id;

	/**
	 * The id of v2.
	 * 
	 * @parameter
	 */
	private Integer v2_id;

	/**
	 * Set to true to include added concepts.
	 * 
	 * @parameter default-value=true
	 */
	private boolean added_concepts;

	/**
	 * Set to true to include deleted concepts.
	 * 
	 * @parameter default-value=true
	 */
	private boolean deleted_concepts;

	/**
	 * Set to true to include concepts with changed status.
	 * 
	 * @parameter default-value=true
	 */
	private boolean changed_concept_status;

	/**
	 * Optional list of concept status to filter v1
	 * 
	 * @parameter
	 */
	private List<String> v1_concept_status= new ArrayList<String>();

	private List<Integer> v1_concept_status_int;

	/**
	 * Optional list of concept status to filter v2
	 * 
	 * @parameter
	 */
	private List<String> v2_concept_status= new ArrayList<String>();

	private List<Integer> v2_concept_status_int;

	/**
	 * Set to true to include concepts with changed defined
	 * 
	 * @parameter default-value=true
	 */
	private boolean changed_defined;

	/**
	 * Set to true to include added descriptions.
	 * 
	 * @parameter default-value=true
	 */
	private boolean added_descriptions;

	/**
	 * Set to true to include deleted descriptions.
	 * 
	 * @parameter default-value=true
	 */
	private boolean deleted_descriptions;

	/**
	 * Set to true to include descriptions with changed status.
	 * 
	 * @parameter default-value=true
	 */
	private boolean changed_description_status;

	/**
	 * Optional list of description status to filter v1
	 * 
	 * @parameter
	 */
	private List<String> v1_description_status= new ArrayList<String>();

	private List<Integer> v1_description_status_int;

	/**
	 * Optional list of description status to filter v2
	 * 
	 * @parameter
	 */
	private List<String> v2_description_status= new ArrayList<String>();

	private List<Integer> v2_description_status_int;

	/**
	 * Set to true to include descriptions with changed term.
	 * 
	 * @parameter default-value=true
	 */
	private boolean changed_description_term;

	/**
	 * Set to true to include descriptions with changed type.
	 * 
	 * @parameter default-value=true
	 */
	private boolean changed_description_type;

	/**
	 * Set to true to include descriptions with changed language.
	 * 
	 * @parameter default-value=true
	 */
	private boolean changed_description_language;

	/**
	 * Set to true to include descriptions with changed case.
	 * 
	 * @parameter default-value=true
	 */
	private boolean changed_description_case;

	/**
	 * Set to true to include added relationships.
	 * 
	 * @parameter default-value=true
	 */
	private boolean added_relationships;

	/**
	 * Set to true to include deleted relationships.
	 * 
	 * @parameter default-value=true
	 */
	private boolean deleted_relationships;

	/**
	 * Set to true to include relationships with changed status.
	 * 
	 * @parameter default-value=true
	 */
	private boolean changed_relationship_status;

	/**
	 * Optional list of relationship status to filter v1
	 * 
	 * @parameter
	 */
	private List<String> v1_relationship_status = new ArrayList<String>();

	private List<Integer> v1_relationship_status_int;

	/**
	 * Optional list of relationship status to filter v2
	 * 
	 * @parameter
	 */
	private List<String> v2_relationship_status = new ArrayList<String>();

	private List<Integer> v2_relationship_status_int;

	/**
	 * Set to true to include relationships with changed characteristic.
	 * 
	 * @parameter default-value=true
	 */
	private boolean changed_relationship_characteristic;

	/**
	 * Set to true to include relationships with changed refinability.
	 * 
	 * @parameter default-value=true
	 */
	private boolean changed_relationship_refinability;

	/**
	 * Set to true to include relationships with changed type.
	 * 
	 * @parameter default-value=true
	 */
	private boolean changed_relationship_type;

	/**
	 * Set to true to include relationships with changed group.
	 * 
	 * @parameter default-value=true
	 */
	private boolean changed_relationship_group;

	private I_GetConceptData refset;

	private int config;

	private int added_concept_change;

	private int deleted_concept_change;

	private int concept_status_change;

	private int defined_change;

	private int added_description_change;

	private int deleted_description_change;

	private int description_status_change;

	private int description_term_change;

	private int description_type_change;

	private int description_language_change;

	private int description_case_change;

	private int added_relationship_change;

	private int deleted_relationship_change;

	private int relationship_status_change;

	private int relationship_characteristic_change;

	private int relationship_refinability_change;

	private int relationship_type_change;

	private int relationship_group_change;

	/*
	 * Creates the refset concept
	 */
	private void createRefsetConcept() throws Exception {
		I_TermFactory tf = LocalVersionedTerminology.get();
		I_GetConceptData fully_specified_description_type = tf
				.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
						.getUids());
		I_GetConceptData preferred_description_type = tf
				.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
						.getUids());
		I_ConfigAceFrame config = tf.newAceFrameConfig();
		I_Path path = tf
				.getPath(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH
						.getUids());
		config.addEditingPath(path);
		config.setDefaultStatus(tf
				.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()));
		UUID uuid = UUID.randomUUID();
		refset = tf.newConcept(uuid, false, config);
		String name = "Compare of " + v1_id + " " + v2_id + " @ "
				+ System.currentTimeMillis();
		// Install the FSN
		tf.newDescription(UUID.randomUUID(), refset, "en", name,
				fully_specified_description_type, config);
		// Install the preferred term
		tf.newDescription(UUID.randomUUID(), refset, "en", name,
				preferred_description_type, config);
		tf
				.newRelationship(
						UUID.randomUUID(),
						refset,
						tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL
								.getUids()),
						tf.getConcept(RefsetAuxiliary.Concept.REFSET_IDENTITY
								.getUids()),
						tf
								.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC
										.getUids()),
						tf
								.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE
										.getUids()),
						tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE
								.getUids()), 0, config);
		tf
				.newRelationship(
						UUID.randomUUID(),
						refset,
						tf.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE_REL
								.getUids()),
						// tf.getConcept(RefsetAuxiliary.Concept.CONCEPT_EXTENSION
						// .getUids()),
						tf
								.getConcept(RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION
										.getUids()),
						tf
								.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC
										.getUids()),
						tf
								.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE
										.getUids()),
						tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE
								.getUids()), 0, config);
		tf.commit();
	}

	protected I_GetConceptData createChangeTypeConcept(String name)
			throws Exception {
		I_TermFactory tf = LocalVersionedTerminology.get();
		I_GetConceptData fully_specified_description_type = tf
				.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
						.getUids());
		I_GetConceptData preferred_description_type = tf
				.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
						.getUids());
		I_ConfigAceFrame config = tf.newAceFrameConfig();
		I_Path path = tf
				.getPath(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH
						.getUids());
		config.addEditingPath(path);
		config.setDefaultStatus(tf
				.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()));
		UUID uuid = UUID.randomUUID();
		I_GetConceptData newConcept = tf.newConcept(uuid, false, config);
		// Install the FSN
		tf.newDescription(UUID.randomUUID(), newConcept, "en", name,
				fully_specified_description_type, config);
		// Install the preferred term
		tf.newDescription(UUID.randomUUID(), newConcept, "en", name,
				preferred_description_type, config);
		tf
				.newRelationship(
						UUID.randomUUID(),
						newConcept,
						tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL
								.getUids()),
						tf
								.getConcept(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT
										.getUids()),
						tf
								.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC
										.getUids()),
						tf
								.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE
										.getUids()),
						tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE
								.getUids()), 0, config);
		tf.commit();
		return newConcept;
	}

	/*
	 * Adds a concept to a refset
	 */
	private void addToRefset(I_TermFactory tf, int refsetId, int conceptId,
			int changeId, String comment) throws Exception {
		I_GetConceptData include_individual = tf
				.getConcept(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL
						.getUids());
		// System.out.println("Include: " +
		// include_individual.getUids().get(0));
		int typeId = include_individual.getConceptId();
		I_GetConceptData active_status = tf
				.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());
		int statusId = active_status.getConceptId();
		int memberId = tf.uuidToNativeWithGeneration(UUID.randomUUID(),
				ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize()
						.getNid(), tf.getPaths(), Integer.MAX_VALUE);
		I_ThinExtByRefVersioned newExtension = tf.newExtension(refsetId,
				memberId, conceptId, typeId);
		I_ThinExtByRefPartConceptConceptString ext = tf
				.newConceptConceptStringExtensionPart();
		ext.setC1id(conceptId);
		ext.setC2id(changeId);
		ext.setStr(comment);
		I_GetConceptData path = tf
				.getConcept(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH
						.getUids());
		// System.out.println("Path: " + path.getUids().get(0));
		ext.setPathId(path.getConceptId());
		ext.setStatusId(statusId);
		ext.setVersion(Integer.MAX_VALUE);
		newExtension.addVersion(ext);
		tf.addUncommitted(newExtension);
	}

	private List<Integer> buildStatus(List<String> status, String tag)
			throws Exception {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		I_TermFactory tf = LocalVersionedTerminology.get();
		for (String str : status) {
			I_GetConceptData con = tf.getConcept(Arrays.asList(UUID
					.fromString(str)));
			if (con == null) {
				getLog().info("Can't find " + str);
				continue;
			}
			ret.add(con.getConceptId());
			addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
					this.config, tag + " = " + str + " " + con.getInitialText());
		}
		return ret;
	}

	private void compareAttributes(I_GetConceptData c, I_Path path)
			throws Exception {
		I_TermFactory tf = LocalVersionedTerminology.get();
		I_ConceptAttributePart a1 = null;
		I_ConceptAttributePart a2 = null;
		for (I_ConceptAttributePart a : c.getConceptAttributes().getVersions()) {
			// Must be on the path
			if (a.getPathId() != path.getConceptId())
				continue;
			// Find the greatest version <= the one of interest
			if (a.getVersion() <= v1_id
					&& (a1 == null || a1.getVersion() < a.getVersion()))
				a1 = a;
			if (a.getVersion() <= v2_id
					&& (a2 == null || a2.getVersion() < a.getVersion()))
				a2 = a;
		}
		if (this.added_concepts && a1 == null && a2 != null) {
			addToRefset(tf, refset.getConceptId(), c.getConceptId(),
					this.added_concept_change, c.toString());
			incr(this.added_concept_change);
		}
		if (this.deleted_concepts && a1 != null && a2 == null) {
			addToRefset(tf, refset.getConceptId(), c.getConceptId(),
					this.deleted_concept_change, c.toString());
			incr(this.deleted_concept_change);
		}
		if (a1 != null && a2 != null && a1.getVersion() != a2.getVersion()) {
			// Something changed
			if (this.changed_concept_status
					&& a1.getStatusId() != a2.getStatusId()
					&& (this.v1_concept_status.size() == 0 || this.v1_concept_status_int
							.contains(a1.getStatusId()))
					&& (this.v2_concept_status.size() == 0 || this.v2_concept_status_int
							.contains(a2.getStatusId()))) {
				addToRefset(tf, refset.getConceptId(), c.getConceptId(),
						this.concept_status_change, tf.getConcept(
								a1.getStatusId()).getInitialText()
								+ " -> "
								+ tf.getConcept(a2.getStatusId())
										.getInitialText());
				incr(this.concept_status_change);
			}
			if (this.changed_defined && a1.isDefined() != a2.isDefined()) {
				addToRefset(tf, refset.getConceptId(), c.getConceptId(),
						this.defined_change, a1.isDefined() + " -> "
								+ a2.isDefined());
				incr(this.defined_change);
			}
		}
	}

	private void compareDescriptions(I_GetConceptData c, I_Path path)
			throws Exception {
		I_TermFactory tf = LocalVersionedTerminology.get();
		for (I_DescriptionVersioned d : c.getDescriptions()) {
			I_DescriptionPart d1 = null;
			I_DescriptionPart d2 = null;
			for (I_DescriptionPart dd : d.getVersions()) {
				if (dd.getPathId() != path.getConceptId())
					continue;
				// Find the greatest version <= the one of interest
				if (dd.getVersion() <= v1_id
						&& (d1 == null || d1.getVersion() < dd.getVersion()))
					d1 = dd;
				if (dd.getVersion() <= v2_id
						&& (d2 == null || d2.getVersion() < dd.getVersion()))
					d2 = dd;
			}
			if (this.added_descriptions && d1 == null && d2 != null) {
				addToRefset(tf, refset.getConceptId(), c.getConceptId(),
						this.added_description_change, String.valueOf(d
								.getDescId())
								+ "\t" + d2);
				incr(this.added_description_change);
			}
			if (this.deleted_descriptions && d1 != null && d2 == null) {
				addToRefset(tf, refset.getConceptId(), c.getConceptId(),
						this.deleted_description_change, String.valueOf(d
								.getDescId())
								+ "\t" + d1);
				incr(this.deleted_description_change);
			}
			if (d1 != null && d2 != null && d1.getVersion() != d2.getVersion()) {
				// Something changed
				if (this.changed_description_status
						&& d1.getStatusId() != d2.getStatusId()
						&& (this.v1_description_status.size() == 0 || this.v1_description_status_int
								.contains(d1.getStatusId()))
						&& (this.v2_description_status.size() == 0 || this.v2_description_status_int
								.contains(d2.getStatusId()))) {
					addToRefset(tf, refset.getConceptId(), c.getConceptId(),
							this.description_status_change, String.valueOf(d
									.getDescId())
									+ "\t"
									+ d2.getText()
									+ ": "
									+ tf.getConcept(d1.getStatusId())
											.getInitialText()
									+ " -> "
									+ tf.getConcept(d2.getStatusId())
											.getInitialText());
					incr(this.description_status_change);
				}
				// term
				if (this.changed_description_term
						&& !d1.getText().equals(d2.getText())) {
					addToRefset(tf, refset.getConceptId(), c.getConceptId(),
							this.description_term_change, String.valueOf(d
									.getDescId())
									+ "\t"
									+ d1.getText()
									+ " -> "
									+ d2.getText());
					incr(this.description_term_change);
				}
				// type
				if (this.changed_description_type
						&& d1.getTypeId() != d2.getTypeId()) {
					addToRefset(tf, refset.getConceptId(), c.getConceptId(),
							this.description_type_change, String.valueOf(d
									.getDescId())
									+ "\t"
									+ d2.getText()
									+ ": "
									+ tf.getConcept(d1.getTypeId())
											.getInitialText()
									+ " -> "
									+ tf.getConcept(d2.getTypeId())
											.getInitialText());
					incr(this.description_type_change);
				}
				// lang
				if (this.changed_description_language
						&& !d1.getLang().equals(d2.getLang())) {
					addToRefset(tf, refset.getConceptId(), c.getConceptId(),
							this.description_language_change, String.valueOf(d
									.getDescId())
									+ "\t"
									+ d2.getText()
									+ ": "
									+ d1.getLang()
									+ " -> " + d2.getLang());
					incr(this.description_language_change);
				}
				// case
				if (this.changed_description_case
						&& d1.getInitialCaseSignificant() != d2
								.getInitialCaseSignificant()) {
					addToRefset(tf, refset.getConceptId(), c.getConceptId(),
							this.description_case_change, String.valueOf(d
									.getDescId())
									+ "\t"
									+ d2.getText()
									+ ": "
									+ d1.getInitialCaseSignificant()
									+ " -> "
									+ d2.getInitialCaseSignificant());
					incr(this.description_case_change);
				}
			}
		}
	}

	private void compareRelationships(I_GetConceptData c, I_Path path)
			throws Exception {
		I_TermFactory tf = LocalVersionedTerminology.get();
		for (I_RelVersioned d : c.getSourceRels()) {
			I_RelPart d1 = null;
			I_RelPart d2 = null;
			for (I_RelPart dd : d.getVersions()) {
				if (dd.getPathId() != path.getConceptId())
					continue;
				// Find the greatest version <= the one of interest
				if (dd.getVersion() <= v1_id
						&& (d1 == null || d1.getVersion() < dd.getVersion()))
					d1 = dd;
				if (dd.getVersion() <= v2_id
						&& (d2 == null || d2.getVersion() < dd.getVersion()))
					d2 = dd;
			}
			if (this.added_relationships && d1 == null && d2 != null) {
				addToRefset(tf, refset.getConceptId(), c.getConceptId(),
						this.added_relationship_change, String.valueOf(d
								.getRelId())
								+ "\t" + d2);
				incr(this.added_relationship_change);
			}
			if (this.deleted_relationships && d1 != null && d2 == null) {
				addToRefset(tf, refset.getConceptId(), c.getConceptId(),
						this.deleted_relationship_change, String.valueOf(d
								.getRelId())
								+ "\t" + d1);
				incr(this.deleted_relationship_change);
			}
			if (d1 != null && d2 != null && d1.getVersion() != d2.getVersion()) {
				// Something changed
				if (this.changed_relationship_status
						&& d1.getStatusId() != d2.getStatusId()
						&& (this.v1_relationship_status.size() == 0 || this.v1_relationship_status_int
								.contains(d1.getStatusId()))
						&& (this.v2_relationship_status.size() == 0 || this.v2_relationship_status_int
								.contains(d2.getStatusId()))) {
					addToRefset(tf, refset.getConceptId(), c.getConceptId(),
							this.relationship_status_change, String.valueOf(d
									.getRelId())
									+ "\t"
									+ d2
									+ ": "
									+ tf.getConcept(d1.getStatusId())
											.getInitialText()
									+ " -> "
									+ tf.getConcept(d2.getStatusId())
											.getInitialText());
					incr(this.relationship_status_change);
				}
				// characteristic
				if (this.changed_relationship_characteristic
						&& d1.getCharacteristicId() != d2.getCharacteristicId()) {
					addToRefset(tf, refset.getConceptId(), c.getConceptId(),
							this.relationship_characteristic_change, String
									.valueOf(d.getRelId())
									+ "\t"
									+ d2
									+ ": "
									+ tf.getConcept(d1.getCharacteristicId())
											.getInitialText()
									+ " -> "
									+ tf.getConcept(d2.getCharacteristicId())
											.getInitialText());
					incr(this.relationship_characteristic_change);
				}
				// refinability
				if (this.changed_relationship_refinability
						&& d1.getRefinabilityId() != d2.getRefinabilityId()) {
					addToRefset(tf, refset.getConceptId(), c.getConceptId(),
							this.relationship_refinability_change, String
									.valueOf(d.getRelId())
									+ "\t"
									+ d2
									+ ": "
									+ tf.getConcept(d1.getCharacteristicId())
											.getInitialText()
									+ " -> "
									+ tf.getConcept(d2.getCharacteristicId())
											.getInitialText());
					incr(this.relationship_refinability_change);
				}
				// type
				if (this.changed_relationship_type
						&& d1.getTypeId() != d2.getTypeId()) {
					addToRefset(tf, refset.getConceptId(), c.getConceptId(),
							this.relationship_type_change, String.valueOf(d
									.getRelId())
									+ "\t"
									+ d2
									+ ": "
									+ tf.getConcept(d1.getTypeId())
											.getInitialText()
									+ " -> "
									+ tf.getConcept(d2.getTypeId())
											.getInitialText());
					incr(this.relationship_type_change);
				}
				// group
				if (this.changed_relationship_group
						&& d1.getGroup() != d2.getGroup()) {
					addToRefset(tf, refset.getConceptId(), c.getConceptId(),
							this.relationship_group_change, String.valueOf(d
									.getRelId())
									+ "\t"
									+ d2
									+ ": "
									+ d1.getGroup()
									+ " -> "
									+ d2.getGroup());
					incr(this.relationship_group_change);
				}
			}
		}
	}

	// int only1 = 0;
	// int only2 = 0;
	// int changed = 0;
	// int only1_descr = 0;
	// int only2_descr = 0;
	// int changed_descr = 0;

	private HashMap<Integer, Integer> diff_count = new HashMap<Integer, Integer>();

	private void incr(int id) {
		this.diff_count.put(id, this.diff_count.get(id) + 1);
	}

	private void diff() throws Exception {
		I_TermFactory tf = LocalVersionedTerminology.get();
		this.createRefsetConcept();
		getLog().info("Refset: " + refset.getInitialText());
		getLog().info("Refset: " + refset.getUids().get(0));
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "v1_id = " + this.v1_id);
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "v2_id = " + this.v2_id);
		I_Path path = tf.getPath(Arrays.asList(UUID.fromString(path_uuid)));
		I_Position pos1 = tf.newPosition(path, v1_id);
		I_Position pos2 = tf.newPosition(path, v2_id);
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "v1: " + pos1.toString());
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "v2: " + pos2.toString());
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "added_concepts = " + this.added_concepts);
		this.diff_count.put(this.added_concept_change, 0);
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "deleted_concepts = " + this.deleted_concepts);
		this.diff_count.put(this.deleted_concept_change, 0);
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "changed_concept_status = "
						+ this.changed_concept_status);
		this.diff_count.put(this.concept_status_change, 0);
		this.v1_concept_status_int = buildStatus(this.v1_concept_status,
				"v1_concept_status");
		this.v2_concept_status_int = buildStatus(this.v2_concept_status,
				"v2_concept_status");
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "changed_defined = " + this.changed_defined);
		this.diff_count.put(this.defined_change, 0);
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "added_descriptions = " + this.added_descriptions);
		this.diff_count.put(this.added_description_change, 0);
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "deleted_descriptions = "
						+ this.deleted_descriptions);
		this.diff_count.put(this.deleted_description_change, 0);
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "changed_description_status = "
						+ this.changed_description_status);
		this.diff_count.put(this.description_status_change, 0);
		this.v1_description_status_int = buildStatus(
				this.v1_description_status, "v1_description_status");
		this.v2_description_status_int = buildStatus(
				this.v2_description_status, "v2_description_status");
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "changed_description_term = "
						+ this.changed_description_term);
		this.diff_count.put(this.description_term_change, 0);
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "changed_description_type = "
						+ this.changed_description_type);
		this.diff_count.put(this.description_type_change, 0);
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "changed_description_language = "
						+ this.changed_description_language);
		this.diff_count.put(this.description_language_change, 0);
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "changed_description_case = "
						+ this.changed_description_case);
		this.diff_count.put(this.description_case_change, 0);
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "added_relationships = "
						+ this.added_relationships);
		this.diff_count.put(this.added_relationship_change, 0);
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "deleted_relationships = "
						+ this.deleted_relationships);
		this.diff_count.put(this.deleted_relationship_change, 0);
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "changed_relationship_status = "
						+ this.changed_relationship_status);
		this.diff_count.put(this.relationship_status_change, 0);
		this.v1_relationship_status_int = buildStatus(
				this.v1_relationship_status, "v1_relationship_status");
		this.v2_relationship_status_int = buildStatus(
				this.v2_relationship_status, "v2_relationship_status");
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "changed_relationship_characteristic = "
						+ this.changed_relationship_characteristic);
		this.diff_count.put(this.relationship_characteristic_change, 0);
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "changed_relationship_refinability = "
						+ this.changed_relationship_refinability);
		this.diff_count.put(this.relationship_refinability_change, 0);
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "changed_relationship_type = "
						+ this.changed_relationship_type);
		this.diff_count.put(this.relationship_type_change, 0);
		addToRefset(tf, refset.getConceptId(), refset.getConceptId(),
				this.config, "changed_relationship_group = "
						+ this.changed_relationship_group);
		this.diff_count.put(this.relationship_group_change, 0);
		int concepts = 0;

		for (Iterator<I_GetConceptData> i = tf.getConceptIterator(); i
				.hasNext();) {
			concepts++;
			if (concepts % 10000 == 0)
				getLog().info("concepts " + concepts);
			I_GetConceptData c = i.next();
			compareAttributes(c, path);
			compareDescriptions(c, path);
			compareRelationships(c, path);
		}

		getLog().info("concepts " + concepts);
		for (Entry<Integer, Integer> e : this.diff_count.entrySet()) {
			getLog().info(
					tf.getConcept(e.getKey()).getInitialText() + " "
							+ e.getValue());
		}
		// getLog().info("only1 " + only1);
		// getLog().info("only2 " + only2);
		// getLog().info("changed " + changed);
		// getLog().info("only1 descr " + only1_descr);
		// getLog().info("only2 descr " + only2_descr);
		// getLog().info("changed descr " + changed_descr);
		tf.commit();

		this.listDiff();
	}

	private void listDiff() throws Exception {
		getLog().info("diff list");
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
				"diff.txt")));
		I_TermFactory tf = LocalVersionedTerminology.get();
		List<Integer> description_changes = Arrays.asList(
				this.added_description_change, this.deleted_description_change,
				this.description_status_change, this.description_term_change,
				this.description_type_change, this.description_language_change,
				this.description_case_change);
		int diffs = 0;
		for (I_ThinExtByRefVersioned mem : tf.getRefsetExtensionMembers(refset
				.getConceptId())) {
			diffs++;
			if (diffs % 1000 == 0)
				getLog().info("diffs " + diffs);
			I_GetConceptData mem_con = tf.getConcept(mem.getComponentId());
			I_ThinExtByRefPart p = mem.getVersions().get(0);
			if (p instanceof I_ThinExtByRefPartConceptConceptString) {
				I_ThinExtByRefPartConceptConceptString pccs = (I_ThinExtByRefPartConceptConceptString) p;
				if (description_changes.contains(pccs.getC2id())) {
					String[] comments = pccs.getStr().split("\t");
					int id = Integer.parseInt(comments[0]);
					I_DescriptionVersioned descr = tf.getDescription(id, pccs
							.getC1id());
					out.println(tf.getConcept(pccs.getC2id()).getInitialText()
							+ "\t" + "<" + id + ">" + comments[1] + "\t"
							+ tf.getConcept(pccs.getC1id()).getInitialText());
					continue;
				}
				out.println(tf.getConcept(pccs.getC2id()).getInitialText()
						+ "\t" + pccs.getStr() + "\t"
						+ tf.getConcept(pccs.getC1id()).getInitialText());
			} else {
				getLog().info("Wrong type: " + mem_con.getInitialText());
			}
		}
		getLog().info("diffs " + diffs);
		out.close();
	}

	// Status: 2faa9261-8fb2-11db-b606-0800200c9a66 current (active status type)

	// Status: 4bc081d8-9f64-3a89-a668-d11ca031979b limited

	// Status: 11c24184-4d8a-3cd3-bc30-bb0aa4c76e93 pending move (active status
	// type)

	// Status: e1956e7b-08b4-3ad0-ab02-b411869f1c09 retired

	// Status: cbe19851-49f7-32f7-bb27-2d24be0e77e8 duplicate

	// Status: ad6b6532-0cb7-35d0-be04-47a9e4634ed8 outdated (inactive status
	// type)

	// Status: 3b397a0a-b510-391b-bd2e-5dd168c092ba ambiguous (inactive status
	// type)

	// Status: a09cc39f-2c01-3563-84cc-bad7d7fb597f erroneous (inactive status
	// type)

	// Status: 76367831-522f-3250-83a4-8609ab298436 moved elsewhere (inactive
	// status type)

	private void listStatus() throws Exception {
		I_TermFactory tf = LocalVersionedTerminology.get();
		for (Concept c : Arrays.asList(ArchitectonicAuxiliary.Concept.CURRENT,
				ArchitectonicAuxiliary.Concept.LIMITED,
				ArchitectonicAuxiliary.Concept.PENDING_MOVE,
				ArchitectonicAuxiliary.Concept.RETIRED,
				ArchitectonicAuxiliary.Concept.DUPLICATE,
				ArchitectonicAuxiliary.Concept.OUTDATED,
				ArchitectonicAuxiliary.Concept.AMBIGUOUS,
				ArchitectonicAuxiliary.Concept.ERRONEOUS,
				ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE)) {
			getLog()
					.info(
							"Status: "
									+ tf.getConcept(c.getUids()).getUids().get(
											0)
									+ " "
									+ tf.getConcept(c.getUids())
											.getInitialText());
		}
	}

	// SNOMED Clinical Terms version: 20080731 [R] (July 2008 Release)

	// -612920153

	// SNOMED Clinical Terms version: 20090131 [R] (January 2009 Release)

	// -597018953

	private void listVersions() throws Exception {
		I_TermFactory tf = LocalVersionedTerminology.get();
		I_GetConceptData c = tf.getConcept(SNOMED.Concept.ROOT.getUids());
		getLog().info(c.getInitialText());
		I_ConceptAttributeVersioned cv = c.getConceptAttributes();
		for (I_ConceptAttributePart cvp : cv.getVersions()) {
			getLog().info("Attr: " + cvp.getVersion());
		}
		I_GetConceptData syn_type = tf
				.getConcept(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE
						.getUids());
		for (I_DescriptionVersioned cd : c.getDescriptions()) {
			for (I_DescriptionPart cvp : cd.getVersions()) {
				if (cvp.getTypeId() == syn_type.getConceptId()
						&& cvp.getText().contains("version")) {
					getLog().info("Version: " + cvp.getText());
					getLog().info("         " + cvp.getVersion());
				}
			}
		}
	}

	private void listPaths() throws Exception {
		I_TermFactory tf = LocalVersionedTerminology.get();
		for (I_Path path : tf.getPaths()) {
			I_GetConceptData path_con = tf.getConcept(path.getConceptId());
			getLog().info("Path: " + path_con.getInitialText());
			getLog().info("      " + path_con.getUids().get(0));
			for (I_Position position : path.getOrigins()) {
				getLog().info("Position: " + position);
				getLog().info("Version: " + position.getVersion());
				I_GetConceptData position_con = tf.getConcept(position
						.getPath().getConceptId());
				getLog().info("         " + position_con.getInitialText());
				getLog().info("         " + position_con.getUids().get(0));
			}
		}
	}

	/*
	 * 
	 * Example use:
	 * 
	 * <br>&lt;execution&gt;
	 * 
	 * <br>&lt;id&gt;version-diff&lt;/id&gt;
	 * 
	 * <br>&lt;phase&gt;generate-sources&lt;/phase&gt;
	 * 
	 * <br>&lt;goals&gt;
	 * 
	 * <br>&lt;goal&gt;version-diff&lt;/goal&gt;
	 * 
	 * <br>&lt;/goals&gt;
	 * 
	 * <br>&lt;configuration&gt;
	 * 
	 * <br>&lt;v1&gt;v1-id&lt;v1&gt;
	 * 
	 * <br>&lt;v2&gt;v2-id&lt;v2&gt;
	 * 
	 * <br>&lt;/configuration&gt;
	 * 
	 * <br>&lt;/execution&gt;
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			this.listVersions();

			this.listPaths();

			this.listStatus();

			this.config = this.createChangeTypeConcept("Configuration")
					.getConceptId();

			this.added_concept_change = this.createChangeTypeConcept(
					"Added Concept").getConceptId();
			this.deleted_concept_change = this.createChangeTypeConcept(
					"Deleted Concept").getConceptId();
			this.concept_status_change = this.createChangeTypeConcept(
					"Changed Concept Status").getConceptId();
			this.defined_change = this.createChangeTypeConcept(
					"Changed Defined").getConceptId();

			this.added_description_change = this.createChangeTypeConcept(
					"Added Description").getConceptId();
			this.deleted_description_change = this.createChangeTypeConcept(
					"Deleted Description").getConceptId();
			this.description_status_change = this.createChangeTypeConcept(
					"Changed Description Status").getConceptId();
			this.description_term_change = this.createChangeTypeConcept(
					"Changed Description Term").getConceptId();
			this.description_type_change = this.createChangeTypeConcept(
					"Changed Description Type").getConceptId();
			this.description_language_change = this.createChangeTypeConcept(
					"Changed Description Language").getConceptId();
			this.description_case_change = this.createChangeTypeConcept(
					"Changed Description Case").getConceptId();

			this.added_relationship_change = this.createChangeTypeConcept(
					"Added Relationship").getConceptId();
			this.deleted_relationship_change = this.createChangeTypeConcept(
					"Deleted Relationship").getConceptId();
			this.relationship_status_change = this.createChangeTypeConcept(
					"Changed Relationship Status").getConceptId();
			this.relationship_characteristic_change = this
					.createChangeTypeConcept(
							"Changed Relationship Characteristic")
					.getConceptId();
			this.relationship_refinability_change = this
					.createChangeTypeConcept(
							"Changed Relationship Refinability").getConceptId();
			this.relationship_type_change = this.createChangeTypeConcept(
					"Changed Relationship Type").getConceptId();
			this.relationship_group_change = this.createChangeTypeConcept(
					"Changed Relationship Group").getConceptId();

			this.diff();
		} catch (Exception e) {
			// e.printStackTrace();
			throw new MojoFailureException(e.getLocalizedMessage(), e);
		}
	}
}
