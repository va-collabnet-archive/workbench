/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.ihtsdo.mojo.mojo.diff;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_IterateIds;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidString;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

/**
 * Compare two version of SNOMED
 * 
 * <p>
 * For each component in SNOMED (concepts, descriptions, and relationships), the
 * parameters permit the configuration of the types of change to include in the
 * refset. Some of these change inclusion parameters also include the ability to
 * further qualify the change. The concept, description, and relationship status
 * types have a status type parameter which allow for the specification of a
 * "reactivation" change refset, by specifying the "v1" status to be the
 * inactive status types and the "v2" status to be the active status types.
 * 
 * <p>
 * There is some commonality among the parameters for all components. Each
 * component type has the ability to specify addition, deletion (but should not
 * happen if v2 &gt; v1), and change in status type along with qualification of
 * the status type.
 * 
 * <p>
 * For concepts, a change in "defined" status may be specified.
 * 
 * <p>
 * For descriptions, a change in term, type, language, or case sensitivity may
 * be specified.
 * 
 * <p>
 * For relationships, a change in characteristic, type, refinability, and group
 * may be specified.
 * 
 * <p>
 * There is also the ability to specify "filters" on components to focus on
 * specific aspects of SNOMED. Note that filters restrict the components on
 * which comparisons are performed, which the change type specification limits
 * the changes which are reported.
 * 
 * <p>
 * The ISA filter restricts the concepts to one or more sub-hierarchies. To
 * focus on a status change in the clinical findings section, specify the uuid
 * for "clinical finding" in the v1 ISA filter. Note that this is version
 * specific, since finding a clinical finding concept that has been retired
 * requires using the previous ISA relationship, not the current one.
 * 
 * <p>
 * Each components allows for the specification of a status filter.
 * 
 * <p>
 * Descriptions have filters on term, type, language, and case. The language
 * filter utilizes and regular expression. Thus changes to English language
 * descriptions (including dialects) are "(?i)en.*". Similarly, the terms may be
 * filtered using regular expressions.
 * 
 * <p>
 * Relationships have filters on characteristic, refinability, and type.
 * 
 * <p>
 * The created refset is a concept-concept-string refset. <br>
 * concept1 is the changed concept (inclding changed description and
 * relationships) <br>
 * concept2 is the change type (e.g. added concept, changed concept status). The
 * configuration information is included in the refset as a config type.
 * Statistic on the counts of each type of change are recorded as a stats type.
 * The components counts (concepts, descriptions, relationships) are included as
 * a stat type, along with the counts of the components post-filtering. <br>
 * The string is a comment further describing the specifics of the change.
 * 
 * <p>
 * If the list_file parameter is specified, the contents of the created refset
 * are written to the file in a more-or-less human readable format.
 * 
 * @goal version-diff
 * 
 * @phase generate-resources
 * 
 * @requiresDependencyResolution compile
 */
public class VersionDiff extends AbstractMojo {

	/**
	 * The name to identify this comparison
	 * 
	 * @parameter
	 */
	private String name;

	/**
	 * The uuid of the path.
	 * 
	 * @parameter
	 */
	private String path_uuid;

	private I_Path path;

	/**
	 * The time for v1 in yyyy.mm.dd hh:mm:ss zzz format
	 * 
	 * @parameter
	 */
	private String v1;

	/**
	 * The id of v1.
	 */
	private Integer v1_id;

	/**
	 * The time for v2 in yyyy.mm.dd hh:mm:ss zzz format
	 * 
	 * @parameter
	 */
	private String v2;

	/**
	 * The id of v2.
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
	 * Optional list of concept status to filter v1 concept status changes
	 * 
	 * @parameter
	 */
	private List<String> v1_concept_status = new ArrayList<String>();

	private List<Integer> v1_concept_status_int;

	/**
	 * Optional list of concept status to filter v2 concept status changes
	 * 
	 * @parameter
	 */
	private List<String> v2_concept_status = new ArrayList<String>();

	private List<Integer> v2_concept_status_int;

	/**
	 * Set to true to include concepts with changed defined
	 * 
	 * @parameter default-value=true
	 */
	private boolean changed_defined;

	/**
	 * Optional list of isa to filter v1
	 * 
	 * @parameter
	 */
	private List<String> v1_isa_filter = new ArrayList<String>();

	private List<Integer> v1_isa_filter_int;

	/**
	 * Optional list of isa to filter v2
	 * 
	 * @parameter
	 */
	private List<String> v2_isa_filter = new ArrayList<String>();

	private List<Integer> v2_isa_filter_int;

	/**
	 * Optional list of concept status to filter v1 concept status
	 * 
	 * @parameter
	 */
	private List<String> v1_concept_status_filter = new ArrayList<String>();

	private List<Integer> v1_concept_status_filter_int;

	/**
	 * Optional list of concept status to filter v2 concept status
	 * 
	 * @parameter
	 */
	private List<String> v2_concept_status_filter = new ArrayList<String>();

	private List<Integer> v2_concept_status_filter_int;

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
	 * Optional list of description status to filter v1 changed status
	 * 
	 * @parameter
	 */
	private List<String> v1_description_status = new ArrayList<String>();

	private List<Integer> v1_description_status_int;

	/**
	 * Optional list of description status to filter v2 changed status
	 * 
	 * @parameter
	 */
	private List<String> v2_description_status = new ArrayList<String>();

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
	 * Optional list of description status to filter v1 status
	 * 
	 * @parameter
	 */
	private List<String> v1_description_status_filter = new ArrayList<String>();

	private List<Integer> v1_description_status_filter_int;

	/**
	 * Optional list of description status to filter v2 status
	 * 
	 * @parameter
	 */
	private List<String> v2_description_status_filter = new ArrayList<String>();

	private List<Integer> v2_description_status_filter_int;

	/**
	 * Optional list of description type to filter v1 type
	 * 
	 * @parameter
	 */
	private List<String> v1_description_type_filter = new ArrayList<String>();

	private List<Integer> v1_description_type_filter_int;

	/**
	 * Optional list of description type to filter v2 type
	 * 
	 * @parameter
	 */
	private List<String> v2_description_type_filter = new ArrayList<String>();

	private List<Integer> v2_description_type_filter_int;

	/**
	 * Optional regex to filter v1 term
	 * 
	 * @parameter
	 */
	private String v1_description_term_filter;

	/**
	 * Optional regex to filter v2 term
	 * 
	 * @parameter
	 */
	private String v2_description_term_filter;

	/**
	 * Optional regex to filter v1 lang
	 * 
	 * @parameter
	 */
	private String v1_description_lang_filter;

	/**
	 * Optional regex to filter v2 lang
	 * 
	 * @parameter
	 */
	private String v2_description_lang_filter;

	/**
	 * Optional filter on v1 case
	 * 
	 * @parameter
	 */
	private Boolean v1_description_case_filter;

	/**
	 * Optional filter on v2 case
	 * 
	 * @parameter
	 */
	private Boolean v2_description_case_filter;

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

	/**
	 * Optional list of relationship status to filter v1
	 * 
	 * @parameter
	 */
	private List<String> v1_relationship_status_filter = new ArrayList<String>();

	private List<Integer> v1_relationship_status_filter_int;

	/**
	 * Optional list of relationship status to filter v2
	 * 
	 * @parameter
	 */
	private List<String> v2_relationship_status_filter = new ArrayList<String>();

	private List<Integer> v2_relationship_status_filter_int;

	/**
	 * Optional list of relationship characteristic to filter v1 characteristic
	 * 
	 * @parameter
	 */
	private List<String> v1_relationship_characteristic_filter = new ArrayList<String>();

	private List<Integer> v1_relationship_characteristic_filter_int;

	/**
	 * Optional list of relationship characteristic to filter v2
	 * 
	 * @parameter
	 */
	private List<String> v2_relationship_characteristic_filter = new ArrayList<String>();

	private List<Integer> v2_relationship_characteristic_filter_int;

	/**
	 * Optional list of relationship refinability to filter v1
	 * 
	 * @parameter
	 */
	private List<String> v1_relationship_refinability_filter = new ArrayList<String>();

	private List<Integer> v1_relationship_refinability_filter_int;

	/**
	 * Optional list of relationship refinability to filter v2
	 * 
	 * @parameter
	 */
	private List<String> v2_relationship_refinability_filter = new ArrayList<String>();

	private List<Integer> v2_relationship_refinability_filter_int;

	/**
	 * Optional list of relationship type to filter v1
	 * 
	 * @parameter
	 */
	private List<String> v1_relationship_type_filter = new ArrayList<String>();

	private List<Integer> v1_relationship_type_filter_int;

	/**
	 * Optional list of relationship type to filter v2
	 * 
	 * @parameter
	 */
	private List<String> v2_relationship_type_filter = new ArrayList<String>();

	private List<Integer> v2_relationship_type_filter_int;

	/**
	 * List refset to file if present
	 * 
	 * @parameter
	 */
	private File list_file;

	private I_GetConceptData refset;

	private HashMap<Integer, I_GetConceptData> refsets = new HashMap<Integer, I_GetConceptData>();

	private int config;

	private int stats;

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

	private I_ConfigAceFrame config_ace_frame;

	private RefsetPropertyMap refset_map;

	private RefsetPropertyMap refset_map_member;

	/*
	 * Creates the refset concept, if member_refset != null then create a member
	 * refset
	 */
	private void createRefsetConcept(I_Position pos1, I_Position pos2,
			Integer member_refset) throws Exception {
		I_TermFactory tf = Terms.get();
		I_GetConceptData fully_specified_description_type = tf
				.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
						.getUids());
		I_GetConceptData preferred_description_type = tf
				.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
						.getUids());
		I_GetConceptData member_refset_con = null;
		if (member_refset != null) {
			member_refset_con = tf.getConcept(member_refset);
		}
		// I_ConfigAceFrame config = tf.newAceFrameConfig();
		// I_Path path = tf
		// .getPath(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH
		// .getUids());
		config_ace_frame.addEditingPath(path);
		config_ace_frame.setDefaultStatus(tf
				.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()));
		UUID uuid = UUID.randomUUID();
		I_GetConceptData new_refset = tf.newConcept(uuid, false,
				config_ace_frame);
		String refset_name = "Compare "
				+ this.name
				+ " "
				+ (member_refset != null ? member_refset_con.getInitialText()
						+ " " : "") + "of " + pos1 + " and " + pos2 + " @ "
				+ new Date();
		// Install the FSN
		tf.newDescription(UUID.randomUUID(), new_refset, "en", refset_name,
				fully_specified_description_type, config_ace_frame);
		// Install the preferred term
		tf.newDescription(UUID.randomUUID(), new_refset, "en", refset_name,
				preferred_description_type, config_ace_frame);
		tf
				.newRelationship(
						UUID.randomUUID(),
						new_refset,
						tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL
								.getUids()),
						(member_refset != null ? refset
								: tf
										.getConcept(RefsetAuxiliary.Concept.REFSET_IDENTITY
												.getUids())),
						tf
								.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC
										.getUids()),
						tf
								.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE
										.getUids()),
						tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE
								.getUids()), 0, config_ace_frame);
		tf
				.newRelationship(
						UUID.randomUUID(),
						new_refset,
						tf.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE_REL
								.getUids()),
						(member_refset != null ? tf
								.getConcept(RefsetAuxiliary.Concept.CONCEPT_EXTENSION
										.getUids())
								: tf
										.getConcept(RefsetAuxiliary.Concept.CONCEPT_STRING_EXTENSION
												.getUids())),
						tf
								.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC
										.getUids()),
						tf
								.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE
										.getUids()),
						tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE
								.getUids()), 0, config_ace_frame);
		tf.commit();
		if (member_refset != null) {
			refsets.put(member_refset, new_refset);
		} else {
			refset = new_refset;
		}
	}

	@Deprecated
	protected I_GetConceptData createChangeTypeConcept(String name)
			throws Exception {
		I_TermFactory tf = Terms.get();
		I_GetConceptData fully_specified_description_type = tf
				.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
						.getUids());
		I_GetConceptData preferred_description_type = tf
				.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
						.getUids());
		I_ConfigAceFrame config = tf.newAceFrameConfig();
		// I_Path path = tf
		// .getPath(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH
		// .getUids());
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

	private boolean noop = false;

	private boolean noop_member = false;

	/*
	 * Adds a concept to the refset
	 */
	private void addToRefset(int concept_id, int change_id, String comment)
			throws Exception {
		I_TermFactory tf = Terms.get();
		I_HelpRefsets refsetHelper = tf.getRefsetHelper(config_ace_frame);
		if (!noop) {
			// refset_map.put(REFSET_PROPERTY.CID_ONE, concept_id);
			// refset_map.put(REFSET_PROPERTY.CID_TWO, change_id);
			refset_map.put(REFSET_PROPERTY.CID_ONE, change_id);
			refset_map.put(REFSET_PROPERTY.STRING_VALUE, comment);
			refsetHelper.getOrCreateRefsetExtension(refset.getConceptId(),
					concept_id, REFSET_TYPES.CID_STR, refset_map, UUID
							.randomUUID());
		}
		//
		if (change_id == this.config || change_id == this.stats)
			return;
		if (!noop_member) {
			I_GetConceptData member_refset = refsets.get(change_id);
			if (member_refset == null) {
				System.out.println("None for " + change_id);
				return;
			}
			refset_map_member.put(REFSET_PROPERTY.CID_ONE, concept_id);
			refsetHelper.getOrCreateRefsetExtension(member_refset
					.getConceptId(), concept_id, REFSET_TYPES.CID,
					refset_map_member, UUID.randomUUID());
		}
	}

	private void setupConcepts() throws Exception {
		// this.config = this.createChangeTypeConcept("Configuration")
		// .getConceptId();
		this.config = RefsetAuxiliary.Concept.DIFFERENCE_CONFIGURATION
				.localize().getNid();
		// this.stats = this.createChangeTypeConcept("Statistics")
		// .getConceptId();
		this.stats = RefsetAuxiliary.Concept.DIFFERENCE_STATISTICS.localize()
				.getNid();

		// this.added_concept_change = this.createChangeTypeConcept(
		// "Added Concept").getConceptId();
		this.added_concept_change = RefsetAuxiliary.Concept.ADDED_CONCEPT
				.localize().getNid();
		// this.deleted_concept_change = this.createChangeTypeConcept(
		// "Deleted Concept").getConceptId();
		this.deleted_concept_change = RefsetAuxiliary.Concept.DELETED_CONCEPT
				.localize().getNid();
		// this.concept_status_change = this.createChangeTypeConcept(
		// "Changed Concept Status").getConceptId();
		this.concept_status_change = RefsetAuxiliary.Concept.CHANGED_CONCEPT_STATUS
				.localize().getNid();
		// this.defined_change = this.createChangeTypeConcept(
		// "Changed Defined").getConceptId();
		this.defined_change = RefsetAuxiliary.Concept.CHANGED_DEFINED
				.localize().getNid();

		// this.added_description_change = this.createChangeTypeConcept(
		// "Added Description").getConceptId();
		this.added_description_change = RefsetAuxiliary.Concept.ADDED_DESCRIPTION
				.localize().getNid();
		// this.deleted_description_change = this.createChangeTypeConcept(
		// "Deleted Description").getConceptId();
		this.deleted_description_change = RefsetAuxiliary.Concept.DELETED_DESCRIPTION
				.localize().getNid();
		// this.description_status_change = this.createChangeTypeConcept(
		// "Changed Description Status").getConceptId();
		this.description_status_change = RefsetAuxiliary.Concept.CHANGED_DESCRIPTION_STATUS
				.localize().getNid();
		// this.description_term_change = this.createChangeTypeConcept(
		// "Changed Description Term").getConceptId();
		this.description_term_change = RefsetAuxiliary.Concept.CHANGED_DESCRIPTION_TERM
				.localize().getNid();
		// this.description_type_change = this.createChangeTypeConcept(
		// "Changed Description Type").getConceptId();
		this.description_type_change = RefsetAuxiliary.Concept.CHANGED_DESCRIPTION_TYPE
				.localize().getNid();
		// this.description_language_change = this.createChangeTypeConcept(
		// "Changed Description Language").getConceptId();
		this.description_language_change = RefsetAuxiliary.Concept.CHANGED_DESCRIPTION_LANGUAGE
				.localize().getNid();
		// this.description_case_change = this.createChangeTypeConcept(
		// "Changed Description Case").getConceptId();
		this.description_case_change = RefsetAuxiliary.Concept.CHANGED_DESCRIPTION_CASE
				.localize().getNid();

		// this.added_relationship_change = this.createChangeTypeConcept(
		// "Added Relationship").getConceptId();
		this.added_relationship_change = RefsetAuxiliary.Concept.ADDED_RELATIONSHIP
				.localize().getNid();
		// this.deleted_relationship_change = this.createChangeTypeConcept(
		// "Deleted Relationship").getConceptId();
		this.deleted_relationship_change = RefsetAuxiliary.Concept.DELETED_RELATIONSHIP
				.localize().getNid();
		// this.relationship_status_change = this.createChangeTypeConcept(
		// "Changed Relationship Status").getConceptId();
		this.relationship_status_change = RefsetAuxiliary.Concept.CHANGED_RELATIONSHIP_STATUS
				.localize().getNid();
		// this.relationship_characteristic_change = this
		// .createChangeTypeConcept(
		// "Changed Relationship Characteristic")
		// .getConceptId();
		this.relationship_characteristic_change = RefsetAuxiliary.Concept.CHANGED_RELATIONSHIP_CHARACTERISTIC
				.localize().getNid();
		// this.relationship_refinability_change = this
		// .createChangeTypeConcept(
		// "Changed Relationship Refinability").getConceptId();
		this.relationship_refinability_change = RefsetAuxiliary.Concept.CHANGED_RELATIONSHIP_REFINABILITY
				.localize().getNid();
		// this.relationship_type_change = this.createChangeTypeConcept(
		// "Changed Relationship Type").getConceptId();
		this.relationship_type_change = RefsetAuxiliary.Concept.CHANGED_RELATIONSHIP_TYPE
				.localize().getNid();
		// this.relationship_group_change = this.createChangeTypeConcept(
		// "Changed Relationship Group").getConceptId();
		this.relationship_group_change = RefsetAuxiliary.Concept.CHANGED_RELATIONSHIP_GROUP
				.localize().getNid();
	}

	private List<Integer> buildConceptEnum(List<String> concepts, String tag)
			throws Exception {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		logConfig(tag + " size = " + concepts.size());
		I_TermFactory tf = Terms.get();
		for (String str : concepts) {
			I_GetConceptData con = tf.getConcept(Arrays.asList(UUID
					.fromString(str)));
			if (con == null) {
				logConfig(tag + " ERROR - Can't find " + str);
				continue;
			}
			ret.add(con.getConceptId());
			logConfig(tag + " = " + str + " " + con.getInitialText());
		}
		return ret;
	}

	private void compareAttributes(I_GetConceptData c, I_Path path)
			throws Exception {
		I_TermFactory tf = Terms.get();
		I_ConceptAttributePart a1 = null;
		I_ConceptAttributePart a2 = null;
		for (I_ConceptAttributePart a : c.getConceptAttributes()
				.getMutableParts()) {
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
			addToRefset(c.getConceptId(), this.added_concept_change, c
					.toString());
			incr(this.added_concept_change);
		}
		if (this.deleted_concepts && a1 != null && a2 == null) {
			addToRefset(c.getConceptId(), this.deleted_concept_change, c
					.toString());
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
				addToRefset(c.getConceptId(), this.concept_status_change, tf
						.getConcept(a1.getStatusId()).getInitialText()
						+ " -> "
						+ tf.getConcept(a2.getStatusId()).getInitialText());
				incr(this.concept_status_change);
			}
			if (this.changed_defined && a1.isDefined() != a2.isDefined()) {
				addToRefset(c.getConceptId(), this.defined_change, a1
						.isDefined()
						+ " -> " + a2.isDefined());
				incr(this.defined_change);
			}
		}
	}

	int descriptions = 0;

	int descriptions_filtered = 0;

	private void compareDescriptions(I_GetConceptData c, I_Path path)
			throws Exception {
		I_TermFactory tf = Terms.get();
		for (I_DescriptionVersioned d : c.getDescriptions()) {
			descriptions++;
			I_DescriptionPart d1 = null;
			I_DescriptionPart d2 = null;
			for (I_DescriptionPart dd : d.getMutableParts()) {
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
			if (v1_description_status_filter_int.size() > 0
					&& d1 != null
					&& !v1_description_status_filter_int.contains(d1
							.getStatusId()))
				continue;
			if (v2_description_status_filter_int.size() > 0
					&& d2 != null
					&& !v2_description_status_filter_int.contains(d2
							.getStatusId()))
				continue;
			if (v1_description_type_filter_int.size() > 0 && d1 != null
					&& !v1_description_type_filter_int.contains(d1.getTypeId()))
				continue;
			if (v2_description_type_filter_int.size() > 0 && d2 != null
					&& !v2_description_type_filter_int.contains(d2.getTypeId()))
				continue;
			if (v1_description_term_filter != null
					&& !v1_description_term_filter.equals("") && d1 != null
					&& !d1.getText().matches(v1_description_term_filter))
				continue;
			if (v2_description_term_filter != null
					&& !v2_description_term_filter.equals("") && d2 != null
					&& !d2.getText().matches(v2_description_term_filter))
				continue;
			if (v1_description_lang_filter != null
					&& !v1_description_lang_filter.equals("") && d1 != null
					&& !d1.getLang().matches(v1_description_lang_filter))
				continue;
			if (v2_description_lang_filter != null
					&& !v2_description_lang_filter.equals("") && d2 != null
					&& !d2.getLang().matches(v2_description_lang_filter))
				continue;
			if (v1_description_case_filter != null
					&& d1 != null
					&& !d1.isInitialCaseSignificant() == v1_description_case_filter)
				continue;
			if (v2_description_case_filter != null
					&& d1 != null
					&& !d1.isInitialCaseSignificant() == v2_description_case_filter)
				continue;
			descriptions_filtered++;
			if (this.added_descriptions && d1 == null && d2 != null) {
				addToRefset(c.getConceptId(), this.added_description_change,
						String.valueOf(d.getDescId()) + "\t" + d2);
				incr(this.added_description_change);
			}
			if (this.deleted_descriptions && d1 != null && d2 == null) {
				addToRefset(c.getConceptId(), this.deleted_description_change,
						String.valueOf(d.getDescId()) + "\t" + d1);
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
					addToRefset(c.getConceptId(),
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
					addToRefset(c.getConceptId(), this.description_term_change,
							String.valueOf(d.getDescId()) + "\t" + d1.getText()
									+ " -> " + d2.getText());
					incr(this.description_term_change);
				}
				// type
				if (this.changed_description_type
						&& d1.getTypeId() != d2.getTypeId()) {
					addToRefset(c.getConceptId(), this.description_type_change,
							String.valueOf(d.getDescId())
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
					addToRefset(c.getConceptId(),
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
						&& d1.isInitialCaseSignificant() != d2
								.isInitialCaseSignificant()) {
					addToRefset(c.getConceptId(), this.description_case_change,
							String.valueOf(d.getDescId()) + "\t" + d2.getText()
									+ ": " + d1.isInitialCaseSignificant()
									+ " -> " + d2.isInitialCaseSignificant());
					incr(this.description_case_change);
				}
			}
		}
	}

	int relationships = 0;

	int relationships_filtered = 0;

	private void compareRelationships(I_GetConceptData c, I_Path path)
			throws Exception {
		I_TermFactory tf = Terms.get();
		for (I_RelVersioned d : c.getSourceRels()) {
			relationships++;
			I_RelPart r1 = null;
			I_RelPart r2 = null;
			for (I_RelPart dd : d.getMutableParts()) {
				if (dd.getPathId() != path.getConceptId())
					continue;
				// Find the greatest version <= the one of interest
				if (dd.getVersion() <= v1_id
						&& (r1 == null || r1.getVersion() < dd.getVersion()))
					r1 = dd;
				if (dd.getVersion() <= v2_id
						&& (r2 == null || r2.getVersion() < dd.getVersion()))
					r2 = dd;
			}
			if (v1_relationship_status_filter_int.size() > 0
					&& r1 != null
					&& !v1_relationship_status_filter_int.contains(r1
							.getStatusId()))
				continue;
			if (v2_relationship_status_filter_int.size() > 0
					&& r2 != null
					&& !v2_relationship_status_filter_int.contains(r2
							.getStatusId()))
				continue;
			if (v1_relationship_characteristic_filter_int.size() > 0
					&& r1 != null
					&& !v1_relationship_characteristic_filter_int.contains(r1
							.getCharacteristicId()))
				continue;
			if (v2_relationship_characteristic_filter_int.size() > 0
					&& r2 != null
					&& !v2_relationship_characteristic_filter_int.contains(r2
							.getCharacteristicId()))
				continue;
			if (v1_relationship_refinability_filter_int.size() > 0
					&& r1 != null
					&& !v1_relationship_refinability_filter_int.contains(r1
							.getRefinabilityId()))
				continue;
			if (v2_relationship_refinability_filter_int.size() > 0
					&& r2 != null
					&& !v2_relationship_refinability_filter_int.contains(r2
							.getRefinabilityId()))
				continue;
			if (v1_relationship_type_filter_int.size() > 0
					&& r1 != null
					&& !v1_relationship_type_filter_int
							.contains(r1.getTypeId()))
				continue;
			if (v2_relationship_type_filter_int.size() > 0
					&& r2 != null
					&& !v2_relationship_type_filter_int
							.contains(r2.getTypeId()))
				continue;
			relationships_filtered++;
			if (this.added_relationships && r1 == null && r2 != null) {
				addToRefset(c.getConceptId(), this.added_relationship_change,
						String.valueOf(d.getRelId()) + "\t" + r2);
				incr(this.added_relationship_change);
			}
			if (this.deleted_relationships && r1 != null && r2 == null) {
				addToRefset(c.getConceptId(), this.deleted_relationship_change,
						String.valueOf(d.getRelId()) + "\t" + r1);
				incr(this.deleted_relationship_change);
			}
			if (r1 != null && r2 != null && r1.getVersion() != r2.getVersion()) {
				// Something changed
				if (this.changed_relationship_status
						&& r1.getStatusId() != r2.getStatusId()
						&& (this.v1_relationship_status.size() == 0 || this.v1_relationship_status_int
								.contains(r1.getStatusId()))
						&& (this.v2_relationship_status.size() == 0 || this.v2_relationship_status_int
								.contains(r2.getStatusId()))) {
					addToRefset(c.getConceptId(),
							this.relationship_status_change, String.valueOf(d
									.getRelId())
									+ "\t"
									+ r2
									+ ": "
									+ tf.getConcept(r1.getStatusId())
											.getInitialText()
									+ " -> "
									+ tf.getConcept(r2.getStatusId())
											.getInitialText());
					incr(this.relationship_status_change);
				}
				// characteristic
				if (this.changed_relationship_characteristic
						&& r1.getCharacteristicId() != r2.getCharacteristicId()) {
					addToRefset(c.getConceptId(),
							this.relationship_characteristic_change, String
									.valueOf(d.getRelId())
									+ "\t"
									+ r2
									+ ": "
									+ tf.getConcept(r1.getCharacteristicId())
											.getInitialText()
									+ " -> "
									+ tf.getConcept(r2.getCharacteristicId())
											.getInitialText());
					incr(this.relationship_characteristic_change);
				}
				// refinability
				if (this.changed_relationship_refinability
						&& r1.getRefinabilityId() != r2.getRefinabilityId()) {
					addToRefset(c.getConceptId(),
							this.relationship_refinability_change, String
									.valueOf(d.getRelId())
									+ "\t"
									+ r2
									+ ": "
									+ tf.getConcept(r1.getCharacteristicId())
											.getInitialText()
									+ " -> "
									+ tf.getConcept(r2.getCharacteristicId())
											.getInitialText());
					incr(this.relationship_refinability_change);
				}
				// type
				if (this.changed_relationship_type
						&& r1.getTypeId() != r2.getTypeId()) {
					addToRefset(c.getConceptId(),
							this.relationship_type_change, String.valueOf(d
									.getRelId())
									+ "\t"
									+ r2
									+ ": "
									+ tf.getConcept(r1.getTypeId())
											.getInitialText()
									+ " -> "
									+ tf.getConcept(r2.getTypeId())
											.getInitialText());
					incr(this.relationship_type_change);
				}
				// group
				if (this.changed_relationship_group
						&& r1.getGroup() != r2.getGroup()) {
					addToRefset(c.getConceptId(),
							this.relationship_group_change, String.valueOf(d
									.getRelId())
									+ "\t"
									+ r2
									+ ": "
									+ r1.getGroup()
									+ " -> "
									+ r2.getGroup());
					incr(this.relationship_group_change);
				}
			}
		}
	}

	private HashMap<Integer, Integer> diff_count = new HashMap<Integer, Integer>();

	private void incr(int id) {
		this.diff_count.put(id, this.diff_count.get(id) + 1);
	}

	private void logConfig(String str) throws Exception {
		addToRefset(refset.getConceptId(), this.config, str);
		getLog().info(str);
	}

	private void logStats(String str) throws Exception {
		addToRefset(refset.getConceptId(), this.stats, str);
		getLog().info(str);
	}

	private void processConfig() throws Exception {
		I_TermFactory tf = Terms.get();
		this.config_ace_frame = tf.newAceFrameConfig();
		// I_Path path = tf.getPath(Arrays.asList(UUID.fromString(path_uuid)));
		this.path = tf.getPath(Arrays.asList(UUID.fromString(path_uuid)));
		this.v1_id = ThinVersionHelper.convertTz(this.v1);
		this.v2_id = ThinVersionHelper.convertTz(this.v2);
		I_Position pos1 = tf.newPosition(path, v1_id);
		I_Position pos2 = tf.newPosition(path, v2_id);
		this.createRefsetConcept(pos1, pos2, null);
		{
			this.refset_map = new RefsetPropertyMap(REFSET_TYPES.CID_STR);
			I_GetConceptData active_status = tf
					.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());
			int status_id = active_status.getConceptId();
			refset_map.put(REFSET_PROPERTY.PATH, path.getConceptId());
			refset_map.put(REFSET_PROPERTY.STATUS, status_id);
			refset_map.put(REFSET_PROPERTY.VERSION, Integer.MAX_VALUE);
		}
		{
			this.refset_map_member = new RefsetPropertyMap(REFSET_TYPES.CID);
			I_GetConceptData active_status = tf
					.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());
			int status_id = active_status.getConceptId();
			refset_map_member.put(REFSET_PROPERTY.PATH, path.getConceptId());
			refset_map_member.put(REFSET_PROPERTY.STATUS, status_id);
			refset_map_member.put(REFSET_PROPERTY.VERSION, Integer.MAX_VALUE);
		}
		ArrayList<Integer> change_cons = new ArrayList<Integer>();
		if (this.added_concepts)
			change_cons.add(this.added_concept_change);
		if (this.deleted_concepts)
			change_cons.add(this.deleted_concept_change);
		if (this.changed_concept_status)
			change_cons.add(this.concept_status_change);
		if (this.changed_defined)
			change_cons.add(this.defined_change);
		if (this.added_descriptions)
			change_cons.add(this.added_description_change);
		if (this.deleted_descriptions)
			change_cons.add(this.deleted_description_change);
		if (this.changed_description_status)
			change_cons.add(this.description_status_change);
		if (this.changed_description_term)
			change_cons.add(this.description_term_change);
		if (this.changed_description_type)
			change_cons.add(this.description_type_change);
		if (this.changed_description_language)
			change_cons.add(this.description_language_change);
		if (this.changed_description_case)
			change_cons.add(this.description_case_change);
		if (this.added_relationships)
			change_cons.add(this.added_relationship_change);
		if (this.deleted_relationships)
			change_cons.add(this.deleted_relationship_change);
		if (this.changed_relationship_status)
			change_cons.add(this.relationship_status_change);
		if (this.changed_relationship_characteristic)
			change_cons.add(this.relationship_characteristic_change);
		if (this.changed_relationship_refinability)
			change_cons.add(this.relationship_refinability_change);
		if (this.changed_relationship_type)
			change_cons.add(this.relationship_type_change);
		if (this.changed_relationship_group)
			change_cons.add(this.relationship_group_change);
		for (int con : change_cons) {
			this.createRefsetConcept(pos1, pos2, con);
		}
		getLog().info("Refset: " + refset.getInitialText());
		getLog().info("Refset: " + refset.getUids().get(0));
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");
		// Date d = sdf.parse("2008.07.31 00:00:00 PDT");
		// long v1x = ThinVersionHelper.convert(d.getTime());
		// d = sdf.parse("2009.01.31 00:00:00 PST");
		// long v2x = ThinVersionHelper.convert(d.getTime());
		// logConfig("v1 parse = " + v1x + " " + (v1x - v1_id));
		// logConfig("v2 parse = " + v2x + " " + (v2x - v2_id));
		logConfig("v1: " + v1);
		logConfig("v2: " + v2);
		logConfig("v1_id = " + this.v1_id);
		logConfig("v2_id = " + this.v2_id);
		logConfig("v1 local tz: " + pos1.toString());
		logConfig("v2 local tz: " + pos2.toString());
		logConfig("added_concepts = " + this.added_concepts);
		this.diff_count.put(this.added_concept_change, 0);
		logConfig("deleted_concepts = " + this.deleted_concepts);
		this.diff_count.put(this.deleted_concept_change, 0);
		logConfig("changed_concept_status = " + this.changed_concept_status);
		this.diff_count.put(this.concept_status_change, 0);
		this.v1_concept_status_int = buildConceptEnum(this.v1_concept_status,
				"v1_concept_status");
		this.v2_concept_status_int = buildConceptEnum(this.v2_concept_status,
				"v2_concept_status");
		logConfig("changed_defined = " + this.changed_defined);
		this.diff_count.put(this.defined_change, 0);
		this.v1_isa_filter_int = buildConceptEnum(this.v1_isa_filter,
				"v1_isa_filter");
		this.v2_isa_filter_int = buildConceptEnum(this.v2_isa_filter,
				"v2_isa_filter");
		this.v1_concept_status_filter_int = buildConceptEnum(
				this.v1_concept_status_filter, "v1_concept_status_filter");
		this.v2_concept_status_filter_int = buildConceptEnum(
				this.v2_concept_status_filter, "v2_concept_status_filter");
		logConfig("added_descriptions = " + this.added_descriptions);
		this.diff_count.put(this.added_description_change, 0);
		logConfig("deleted_descriptions = " + this.deleted_descriptions);
		this.diff_count.put(this.deleted_description_change, 0);
		logConfig("changed_description_status = "
				+ this.changed_description_status);
		this.diff_count.put(this.description_status_change, 0);
		this.v1_description_status_int = buildConceptEnum(
				this.v1_description_status, "v1_description_status");
		this.v2_description_status_int = buildConceptEnum(
				this.v2_description_status, "v2_description_status");
		logConfig("changed_description_term = " + this.changed_description_term);
		this.diff_count.put(this.description_term_change, 0);
		logConfig("changed_description_type = " + this.changed_description_type);
		this.diff_count.put(this.description_type_change, 0);
		logConfig("changed_description_language = "
				+ this.changed_description_language);
		this.diff_count.put(this.description_language_change, 0);
		logConfig("changed_description_case = " + this.changed_description_case);
		this.diff_count.put(this.description_case_change, 0);
		this.v1_description_status_filter_int = buildConceptEnum(
				this.v1_description_status_filter,
				"v1_description_status_filter");
		this.v2_description_status_filter_int = buildConceptEnum(
				this.v2_description_status_filter,
				"v2_description_status_filter");
		this.v1_description_type_filter_int = buildConceptEnum(
				this.v1_description_type_filter, "v1_description_type_filter");
		this.v2_description_type_filter_int = buildConceptEnum(
				this.v2_description_type_filter, "v2_description_type_filter");
		logConfig("v1_description_term_filter = "
				+ this.v1_description_term_filter);
		logConfig("v2_description_term_filter = "
				+ this.v2_description_term_filter);
		logConfig("v1_description_lang_filter = "
				+ this.v1_description_lang_filter);
		logConfig("v2_description_lang_filter = "
				+ this.v2_description_lang_filter);
		logConfig("v1_description_case_filter = "
				+ this.v1_description_case_filter);
		logConfig("v2_description_case_filter = "
				+ this.v2_description_case_filter);
		logConfig("added_relationships = " + this.added_relationships);
		this.diff_count.put(this.added_relationship_change, 0);
		logConfig("deleted_relationships = " + this.deleted_relationships);
		this.diff_count.put(this.deleted_relationship_change, 0);
		logConfig("changed_relationship_status = "
				+ this.changed_relationship_status);
		this.diff_count.put(this.relationship_status_change, 0);
		this.v1_relationship_status_int = buildConceptEnum(
				this.v1_relationship_status, "v1_relationship_status");
		this.v2_relationship_status_int = buildConceptEnum(
				this.v2_relationship_status, "v2_relationship_status");
		logConfig("changed_relationship_characteristic = "
				+ this.changed_relationship_characteristic);
		this.diff_count.put(this.relationship_characteristic_change, 0);
		logConfig("changed_relationship_refinability = "
				+ this.changed_relationship_refinability);
		this.diff_count.put(this.relationship_refinability_change, 0);
		logConfig("changed_relationship_type = "
				+ this.changed_relationship_type);
		this.diff_count.put(this.relationship_type_change, 0);
		logConfig("changed_relationship_group = "
				+ this.changed_relationship_group);
		this.diff_count.put(this.relationship_group_change, 0);
		this.v1_relationship_status_filter_int = buildConceptEnum(
				this.v1_relationship_status_filter,
				"v1_relationship_status_filter");
		this.v2_relationship_status_filter_int = buildConceptEnum(
				this.v2_relationship_status_filter,
				"v2_relationship_status_filter");
		this.v1_relationship_characteristic_filter_int = buildConceptEnum(
				this.v1_relationship_characteristic_filter,
				"v1_relationship_characteristic_filter");
		this.v2_relationship_characteristic_filter_int = buildConceptEnum(
				this.v2_relationship_characteristic_filter,
				"v2_relationship_characteristic_filter");
		this.v1_relationship_refinability_filter_int = buildConceptEnum(
				this.v1_relationship_refinability_filter,
				"v1_relationship_refinability_filter");
		this.v2_relationship_refinability_filter_int = buildConceptEnum(
				this.v2_relationship_refinability_filter,
				"v2_relationship_refinability_filter");
		this.v1_relationship_type_filter_int = buildConceptEnum(
				this.v1_relationship_type_filter, "v1_relationship_type_filter");
		this.v2_relationship_type_filter_int = buildConceptEnum(
				this.v2_relationship_type_filter, "v2_relationship_type_filter");
		// return path;
	}

	int concepts = 0;

	int concepts_filtered = 0;

	private void diff() throws Exception {
		I_TermFactory tf = Terms.get();
		// this.path = tf.getPath(Arrays.asList(UUID.fromString(path_uuid)));
		processConfig();

		listRoots(path);

		HashSet<Integer> v1_isa_desc = new HashSet<Integer>();
		for (int v1_i : v1_isa_filter_int) {
			v1_isa_desc.addAll(getDescendants(v1_i, path, v1_id));
		}
		HashSet<Integer> v2_isa_desc = new HashSet<Integer>();
		for (int v2_i : v2_isa_filter_int) {
			v2_isa_desc.addAll(getDescendants(v2_i, path, v2_id));
		}

		ArrayList<Integer> all_concepts = new ArrayList<Integer>();
		for (I_IterateIds i = tf.getConceptIdSet().iterator(); i.next();) {
			all_concepts.add(i.nid());
		}
		long start = System.currentTimeMillis();
		getLog().info(
				"concepts " + concepts + " of " + all_concepts.size()
						+ " Elapsed "
						+ ((System.currentTimeMillis() - start) / 1000)
						+ " secs.");
		for (int nid : all_concepts) {
			concepts++;
//			if (concepts == 20001) {
//				getLog().info("BREAK concepts " + concepts);
//				break;
//			}
			if (concepts % 10000 == 0) {
				long cur = System.currentTimeMillis();
				float elap = cur - start;
				float done = ((float) concepts) / ((float) all_concepts.size());
				float total = elap / done;
				getLog().info(
						"concepts " + concepts + " of " + all_concepts.size()
								+ ". Elapsed " + ((int) (elap / 1000))
								+ " secs. " + ((int) (done * 100))
								+ "% done. Remaining "
								+ ((int) ((total - elap) / 1000)) + " secs.");
			}
			I_GetConceptData c = tf.getConcept(nid);

			I_ConceptAttributePart a1 = null;
			I_ConceptAttributePart a2 = null;
			for (I_ConceptAttributePart a : c.getConceptAttributes()
					.getMutableParts()) {
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

			if (v1_concept_status_filter_int.size() > 0 && a1 != null
					&& !v1_concept_status_filter_int.contains(a1.getStatusId()))
				continue;
			if (v2_concept_status_filter_int.size() > 0 && a2 != null
					&& !v2_concept_status_filter_int.contains(a2.getStatusId()))
				continue;

			if (v1_isa_desc.size() > 0
					&& !v1_isa_desc.contains(c.getConceptId()))
				continue;
			if (v2_isa_desc.size() > 0
					&& !v2_isa_desc.contains(c.getConceptId()))
				continue;
			concepts_filtered++;

			compareAttributes(c, path);
			compareDescriptions(c, path);
			compareRelationships(c, path);
		}
		tf.addUncommittedNoChecks(this.refset);
		for (I_GetConceptData c : this.refsets.values()) {
			tf.addUncommittedNoChecks(c);
		}
		getLog().info(
				"concepts " + concepts + " of " + all_concepts.size()
						+ " Elapsed "
						+ ((System.currentTimeMillis() - start) / 1000)
						+ " secs.");

		logStats("concepts " + concepts);
		logStats("concepts_filtered " + concepts_filtered);
		logStats("descriptions " + descriptions);
		logStats("descriptions_filtered " + descriptions_filtered);
		logStats("relationships " + relationships);
		logStats("relationships_filtered " + relationships_filtered);
		for (Entry<Integer, Integer> e : this.diff_count.entrySet()) {
			logStats(tf.getConcept(e.getKey()).getInitialText() + " "
					+ e.getValue());
		}

		tf.commit();

	}

	private void listDiff() throws Exception {
		getLog().info("diff list");
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
				list_file)));
		I_TermFactory tf = Terms.get();
		List<Integer> description_changes = Arrays.asList(
				this.added_description_change, this.deleted_description_change,
				this.description_status_change, this.description_term_change,
				this.description_type_change, this.description_language_change,
				this.description_case_change);
		int diffs = 0;
		for (I_ExtendByRef mem : tf.getRefsetExtensionMembers(refset
				.getConceptId())) {
			diffs++;
			if (diffs % 1000 == 0)
				getLog().info("diffs " + diffs);
			I_GetConceptData mem_con = tf.getConcept(mem.getComponentId());
			I_ExtendByRefPart p = mem.getMutableParts().get(0);
			if (p instanceof I_ExtendByRefPartCidString) {
				I_ExtendByRefPartCidString pccs = (I_ExtendByRefPartCidString) p;
				if (description_changes.contains(pccs.getC1id())) {
					String[] comments = pccs.getStringValue().split("\t");
					int id = Integer.parseInt(comments[0]);
					// I_DescriptionVersioned descr = tf.getDescription(id, pccs
					// .getC1id());
					out.println(tf.getConcept(pccs.getC1id()).getInitialText()
							+ "\t" + "<" + id + ">" + comments[1] + "\t"
							+ mem_con.getInitialText());
					continue;
				}
				out.println(tf.getConcept(pccs.getC1id()).getInitialText()
						+ "\t" + pccs.getStringValue() + "\t"
						+ mem_con.getInitialText());
			} else {
				getLog().info("Wrong type: " + mem_con.getInitialText());
			}
		}
		getLog().info("diffs " + diffs);
		for (I_GetConceptData rs : refsets.values()) {
			diffs = 0;
			for (I_ExtendByRef mem : tf.getRefsetExtensionMembers(rs
					.getConceptId())) {
				diffs++;
				if (diffs % 1000 == 0)
					getLog().info(rs.getInitialText() + " diffs " + diffs);
				I_GetConceptData mem_con = tf.getConcept(mem.getComponentId());
				I_ExtendByRefPart p = mem.getMutableParts().get(0);
				if (p instanceof I_ExtendByRefPartCid) {
					I_ExtendByRefPartCid pccs = (I_ExtendByRefPartCid) p;
					out.println(rs.getInitialText() + "\t" + "MEMBER" + "\t"
							+ tf.getConcept(pccs.getC1id()).getInitialText());
				} else {
					getLog().info("Wrong type: " + mem_con.getInitialText());
				}
			}
		}
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
		I_TermFactory tf = Terms.get();
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

	private void listDescriptionType() throws Exception {
		I_TermFactory tf = Terms.get();
		for (Concept c : Arrays
				.asList(
						ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE,
						ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE,
						ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE)) {
			getLog().info(
					"Description type: "
							+ tf.getConcept(c.getUids()).getUids().get(0) + " "
							+ tf.getConcept(c.getUids()).getInitialText());
		}
	}

	private void listCharacteristic() throws Exception {
		I_TermFactory tf = Terms.get();
		for (Concept c : Arrays.asList(
				ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC,
				ArchitectonicAuxiliary.Concept.HISTORICAL_CHARACTERISTIC,
				ArchitectonicAuxiliary.Concept.QUALIFIER_CHARACTERISTIC,
				ArchitectonicAuxiliary.Concept.ADDITIONAL_CHARACTERISTIC)) {
			getLog().info(
					"Characteristic type: "
							+ tf.getConcept(c.getUids()).getUids().get(0) + " "
							+ tf.getConcept(c.getUids()).getInitialText());
		}
	}

	private void listRefinability() throws Exception {
		I_TermFactory tf = Terms.get();
		for (Concept c : Arrays.asList(
				ArchitectonicAuxiliary.Concept.NOT_REFINABLE,
				ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY,
				ArchitectonicAuxiliary.Concept.MANDATORY_REFINABILITY)) {
			getLog().info(
					"Refinability type: "
							+ tf.getConcept(c.getUids()).getUids().get(0) + " "
							+ tf.getConcept(c.getUids()).getInitialText());
		}
	}

	private void listRel() throws Exception {
		I_TermFactory tf = Terms.get();
		for (org.dwfa.cement.SNOMED.Concept c : Arrays
				.asList(SNOMED.Concept.IS_A)) {
			getLog()
					.info(
							"Rel type: "
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
		I_TermFactory tf = Terms.get();
		I_GetConceptData c = tf.getConcept(SNOMED.Concept.ROOT.getUids());
		getLog().info(c.getInitialText());
		I_ConceptAttributeVersioned cv = c.getConceptAttributes();
		for (I_ConceptAttributePart cvp : cv.getMutableParts()) {
			getLog().info("Attr: " + cvp.getVersion());
		}
		I_GetConceptData syn_type = tf
				.getConcept(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE
						.getUids());
		for (I_DescriptionVersioned cd : c.getDescriptions()) {
			for (I_DescriptionPart cvp : cd.getMutableParts()) {
				if (cvp.getTypeId() == syn_type.getConceptId()
						&& cvp.getText().contains("time")) {
					getLog().info("Version: " + cvp.getText());
					getLog().info("         " + cvp.getVersion());
					getLog().info(
							"         "
									+ ThinVersionHelper
											.format(cvp.getVersion()));
					getLog().info(
							"         "
									+ ThinVersionHelper.formatTz(cvp
											.getVersion()));
					for (String tz : Arrays.asList("GMT", "EST", "PST",
							"GMT-04:00")) {
						getLog().info(
								"         "
										+ ThinVersionHelper.formatTz(cvp
												.getVersion(), tz));
					}
				}
			}
		}
	}

	private HashSet<Integer> getDescendants(int concept_id, I_Path path,
			int version) throws Exception {
		HashSet<Integer> ret = new HashSet<Integer>();
		getDescendants1(concept_id, path, version, ret);
		return ret;
	}

	private void getDescendants1(int concept_id, I_Path path, int version,
			HashSet<Integer> ret) throws Exception {
		if (ret.contains(concept_id))
			return;
		ret.add(concept_id);
		for (int ch : getChildren(concept_id, path, version)) {
			getDescendants1(ch, path, version, ret);
		}
	}

	private ArrayList<Integer> getChildren(int concept_id, I_Path path,
			int version) throws Exception {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		I_TermFactory tf = Terms.get();
		I_GetConceptData c = tf.getConcept(concept_id);
		for (I_RelVersioned d : c.getDestRels()) {
			I_RelPart dm = null;
			for (I_RelPart dd : d.getMutableParts()) {
				if (dd.getPathId() != path.getConceptId())
					continue;
				if (dd.getTypeId() != tf.getConcept(
						SNOMED.Concept.IS_A.getUids()).getConceptId())
					continue;
				// Find the greatest version <= the one of interest
				if (dd.getVersion() <= version
						&& (dm == null || dm.getVersion() < dd.getVersion()))
					dm = dd;
			}
			if (dm != null
					&& dm.getStatusId() == tf.getConcept(
							ArchitectonicAuxiliary.Concept.CURRENT.getUids())
							.getConceptId())
				ret.add(d.getC1Id());
		}
		return ret;
	}

	private void listRoots(I_Path path) throws Exception {
		I_TermFactory tf = Terms.get();
		I_GetConceptData c = tf.getConcept(SNOMED.Concept.ROOT.getUids());
		for (I_RelVersioned d : c.getDestRels()) {
			I_RelPart d1 = null;
			I_RelPart d2 = null;
			for (I_RelPart dd : d.getMutableParts()) {
				if (dd.getPathId() != path.getConceptId())
					continue;
				if (dd.getTypeId() != tf.getConcept(
						SNOMED.Concept.IS_A.getUids()).getConceptId())
					continue;
				// Find the greatest version <= the one of interest
				if (dd.getVersion() <= v1_id
						&& (d1 == null || d1.getVersion() < dd.getVersion()))
					d1 = dd;
				if (dd.getVersion() <= v2_id
						&& (d2 == null || d2.getVersion() < dd.getVersion()))
					d2 = dd;

			}
			getLog().info(
					"Root- "
							+ " 1: "
							+ (d1 != null)
							+ " "
							+ (d1 != null ? tf.getConcept(d1.getStatusId())
									.getInitialText() : d1)
							+ " 2: "
							+ (d2 != null)
							+ " "
							+ (d2 != null ? tf.getConcept(d2.getStatusId())
									.getInitialText() : d2) + " "
							+ tf.getConcept(d.getC1Id()).getUids().get(0) + " "
							+ tf.getConcept(d.getC1Id()).getInitialText());
		}
		for (int ch : getChildren(c.getConceptId(), path, v1_id)) {
			getLog().info(
					"Root 1: " + tf.getConcept(ch).getUids().get(0) + " "
							+ tf.getConcept(ch).getInitialText());
		}
		for (int ch : getChildren(c.getConceptId(), path, v2_id)) {
			getLog().info(
					"Root 2: " + tf.getConcept(ch).getUids().get(0) + " "
							+ tf.getConcept(ch).getInitialText());
		}
	}

	private void listPaths() throws Exception {
		I_TermFactory tf = Terms.get();
		for (I_Path path : tf.getPaths()) {
			I_GetConceptData path_con = tf.getConcept(path.getConceptId());
			getLog().info("Path: " + path_con.getInitialText());
			getLog().info("      " + path_con.getUids().get(0));
			for (I_Position position : path.getOrigins()) {
				getLog().info("Position: " + position);
				getLog().info("Version: " + position.getVersion());
				I_GetConceptData position_con = tf.getConcept(position
						.getPath().getConceptNid());
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
	 * 
	 * 
	 * 
	 * <br>&lt;path_uuid&gt;8c230474-9f11-30ce-9cad-185a96fd03a2
	 * 
	 * &lt;/path_uuid&gt;
	 * 
	 * <br>&lt;v1&gt;-612920153&lt;/v1&gt;
	 * 
	 * <br>&lt;v2&gt;-597018953&lt;/v2&gt;
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
			this.listDescriptionType();
			this.listCharacteristic();
			this.listRefinability();
			this.listRel();

			this.setupConcepts();

			this.diff();
			if (this.list_file != null)
				this.listDiff();
		} catch (Exception e) {
			throw new MojoFailureException(e.getLocalizedMessage(), e);
		}
	}

	// "Configuration"
	// "Statistics"
	// "Added Concept"
	// "Deleted Concept"
	// "Changed Concept Status"
	// "Changed Defined"
	// "Added Description"
	// "Deleted Description"
	// "Changed Description Status"
	// "Changed Description Term"
	// "Changed Description Type"
	// "Changed Description Language"
	// "Changed Description Case"
	// "Added Relationship"
	// "Deleted Relationship"
	// "Changed Relationship Status"
	// "Changed Relationship Characteristic"
	// "Changed Relationship Refinability"
	// "Changed Relationship Type"
	// "Changed Relationship Group"

}
