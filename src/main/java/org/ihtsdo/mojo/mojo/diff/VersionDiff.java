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
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_IterateIds;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
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
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

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
public class VersionDiff extends DiffBase {

	/**
	 * The uuid for the path to create the refsets
	 * 
	 * @parameter
	 */
	protected String refset_path_uuid;

	/**
	 * The name to identify this comparison
	 * 
	 * @parameter
	 */
	private String name;

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

	private I_ConfigAceFrame config_ace_frame;

	private RefsetPropertyMap refset_map;

	private RefsetPropertyMap refset_map_member;

	/*
	 * Creates the refset concept, if member_refset != null then create a member
	 * refset
	 */
	private void createRefsetConcept(PathBI path, PositionBI pos1,
			PositionBI pos2, Integer member_refset) throws Exception {
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
		// PathBI path = tf
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
		tf.newRelationship(
				UUID.randomUUID(),
				new_refset,
				tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()),
				(member_refset != null ? refset : tf
						.getConcept(RefsetAuxiliary.Concept.REFSET_IDENTITY
								.getUids())),
				tf.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC
						.getUids()),
				tf.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE
						.getUids()), tf
						.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE
								.getUids()), 0, config_ace_frame);
		tf.newRelationship(
				UUID.randomUUID(),
				new_refset,
				tf.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE_REL.getUids()),
				(member_refset != null ? tf
						.getConcept(RefsetAuxiliary.Concept.CONCEPT_EXTENSION
								.getUids())
						: tf.getConcept(RefsetAuxiliary.Concept.CONCEPT_STRING_EXTENSION
								.getUids())),
				tf.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC
						.getUids()),
				tf.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE
						.getUids()), tf
						.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE
								.getUids()), 0, config_ace_frame);
		tf.commit();
		if (member_refset != null) {
			refsets.put(member_refset, new_refset);
		} else {
			refset = new_refset;
		}
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
			refsetHelper.getOrCreateRefsetExtension(refset.getConceptNid(),
					concept_id, REFSET_TYPES.CID_STR, refset_map,
					UUID.randomUUID());
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
			refsetHelper.getOrCreateRefsetExtension(
					member_refset.getConceptNid(), concept_id,
					REFSET_TYPES.CID, refset_map_member, UUID.randomUUID());
		}
	}

	@Override
	protected void addedConcept(I_GetConceptData c) throws Exception {
		super.addedConcept(c);
		addToRefset(c.getConceptNid(), this.added_concept_change, c.toString());
	}

	@Override
	protected void deletedConcept(I_GetConceptData c) throws Exception {
		super.deletedConcept(c);
		addToRefset(c.getConceptNid(), this.deleted_concept_change,
				c.toString());
	}

	@Override
	protected void changedConceptStatus(I_GetConceptData c, int v1, int v2)
			throws Exception {
		super.changedConceptStatus(c, v1, v2);
		addToRefset(c.getConceptNid(), this.concept_status_change,
				getConceptName(v1) + " -> " + getConceptName(v2));
	}

	@Override
	protected void changedDefined(I_GetConceptData c, boolean v1, boolean v2)
			throws Exception {
		super.changedDefined(c, v1, v2);
		addToRefset(c.getConceptNid(), this.defined_change, v1 + " -> " + v2);
	}

	private void compareAttributesOrig(I_GetConceptData c, PathBI path)
			throws Exception {
		I_TermFactory tf = Terms.get();
		I_ConceptAttributePart a1 = null;
		I_ConceptAttributePart a2 = null;
		for (I_ConceptAttributePart a : c.getConceptAttributes()
				.getMutableParts()) {
			// Find the greatest version <= the one of interest
			if (a.getPathNid() != pos1.getPath().getConceptNid()
					&& a.getTime() <= pos1.getTime()
					&& (a1 == null || a1.getTime() < a.getTime()))
				a1 = a;
			if (a.getPathNid() != pos2.getPath().getConceptNid()
					&& a.getTime() <= pos2.getTime()
					&& (a2 == null || a2.getTime() < a.getTime()))
				a2 = a;
		}
		if (this.added_concepts && a1 == null && a2 != null) {
			addToRefset(c.getConceptNid(), this.added_concept_change,
					c.toString());
			incr(this.added_concept_change);
		}
		if (this.deleted_concepts && a1 != null && a2 == null) {
			addToRefset(c.getConceptNid(), this.deleted_concept_change,
					c.toString());
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
				addToRefset(c.getConceptNid(), this.concept_status_change, tf
						.getConcept(a1.getStatusId()).getInitialText()
						+ " -> "
						+ tf.getConcept(a2.getStatusId()).getInitialText());
				incr(this.concept_status_change);
			}
			if (this.changed_defined && a1.isDefined() != a2.isDefined()) {
				addToRefset(c.getConceptNid(), this.defined_change,
						a1.isDefined() + " -> " + a2.isDefined());
				incr(this.defined_change);
			}
		}
	}

	@Override
	protected void addedDescription(I_GetConceptData c, I_DescriptionTuple d)
			throws Exception {
		super.addedDescription(c, d);
		addToRefset(c.getConceptNid(), this.added_description_change,
				String.valueOf(d.getDescId()) + "\t" + d);
	}

	// @Override
	protected void deletedDescription(I_GetConceptData c, I_DescriptionTuple d)
			throws Exception {
		// super.deletedDescription(c, d);
		addToRefset(c.getConceptNid(), this.deleted_description_change,
				String.valueOf(d.getDescId()) + "\t" + d);
		incr(this.deleted_description_change);
	}

	@Override
	protected void changedDescriptionStatus(I_GetConceptData c,
			I_DescriptionTuple d1, I_DescriptionTuple d2) throws Exception {
		super.changedDescriptionStatus(c, d1, d2);
		addToRefset(c.getConceptNid(), this.description_status_change,
				String.valueOf(d1.getDescId()) + "\t" + d2.getText() + ": "
						+ getConceptName(d1.getStatusNid()) + " -> "
						+ getConceptName(d2.getStatusNid()));
	}

	@Override
	protected void changedDescriptionTerm(I_GetConceptData c,
			I_DescriptionTuple d1, I_DescriptionTuple d2) throws Exception {
		super.changedDescriptionTerm(c, d1, d2);
		addToRefset(c.getConceptNid(), this.description_term_change,
				String.valueOf(d1.getDescId()) + "\t" + d1.getText() + " -> "
						+ d2.getText());
	}

	@Override
	protected void changedDescriptionLang(I_GetConceptData c,
			I_DescriptionTuple d1, I_DescriptionTuple d2) throws Exception {
		super.changedDescriptionLang(c, d1, d2);
		addToRefset(c.getConceptNid(), this.description_language_change,
				String.valueOf(d1.getDescId()) + "\t" + d2.getText() + ": "
						+ d1.getLang() + " -> " + d2.getLang());
	}

	@Override
	protected void changedDescriptionCase(I_GetConceptData c,
			I_DescriptionTuple d1, I_DescriptionTuple d2) throws Exception {
		super.changedDescriptionCase(c, d1, d2);
		addToRefset(
				c.getConceptNid(),
				this.description_case_change,
				String.valueOf(d1.getDescId()) + "\t" + d2.getText() + ": "
						+ d1.isInitialCaseSignificant() + " -> "
						+ d2.isInitialCaseSignificant());
	}

	@Override
	protected void changedDescriptionType(I_GetConceptData c,
			I_DescriptionTuple d1, I_DescriptionTuple d2) throws Exception {
		super.changedDescriptionType(c, d1, d2);
		addToRefset(c.getConceptNid(), this.description_type_change,
				String.valueOf(d1.getDescId()) + "\t" + d2.getText() + ": "
						+ getConceptName(d1.getTypeNid()) + " -> "
						+ getConceptName(d2.getTypeNid()));
	}

	private void compareDescriptionsOrig(I_GetConceptData c) throws Exception {
		I_TermFactory tf = Terms.get();
		for (I_DescriptionVersioned d : c.getDescriptions()) {
			descriptions++;
			I_DescriptionPart d1 = null;
			I_DescriptionPart d2 = null;
			for (I_DescriptionPart dd : d.getMutableParts()) {
				// Find the greatest version <= the one of interest
				if (dd.getPathId() != pos1.getPath().getConceptNid()
						&& dd.getVersion() <= pos1.getVersion()
						&& (d1 == null || d1.getVersion() < dd.getVersion()))
					d1 = dd;
				if (dd.getPathId() != pos2.getPath().getConceptNid()
						&& dd.getVersion() <= pos2.getVersion()
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
				addToRefset(c.getConceptNid(), this.added_description_change,
						String.valueOf(d.getDescId()) + "\t" + d2);
				incr(this.added_description_change);
			}
			if (this.deleted_descriptions && d1 != null && d2 == null) {
				addToRefset(c.getConceptNid(), this.deleted_description_change,
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
					addToRefset(c.getConceptNid(),
							this.description_status_change,
							String.valueOf(d.getDescId())
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
					addToRefset(c.getConceptNid(),
							this.description_term_change,
							String.valueOf(d.getDescId()) + "\t" + d1.getText()
									+ " -> " + d2.getText());
					incr(this.description_term_change);
				}
				// type
				if (this.changed_description_type
						&& d1.getTypeId() != d2.getTypeId()) {
					addToRefset(c.getConceptNid(),
							this.description_type_change,
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
					addToRefset(
							c.getConceptNid(),
							this.description_language_change,
							String.valueOf(d.getDescId()) + "\t" + d2.getText()
									+ ": " + d1.getLang() + " -> "
									+ d2.getLang());
					incr(this.description_language_change);
				}
				// case
				if (this.changed_description_case
						&& d1.isInitialCaseSignificant() != d2
								.isInitialCaseSignificant()) {
					addToRefset(c.getConceptNid(),
							this.description_case_change,
							String.valueOf(d.getDescId()) + "\t" + d2.getText()
									+ ": " + d1.isInitialCaseSignificant()
									+ " -> " + d2.isInitialCaseSignificant());
					incr(this.description_case_change);
				}
			}
		}
	}

	@Override
	protected void addedRelationship(I_GetConceptData c, I_RelTuple d)
			throws Exception {
		super.addedRelationship(c, d);
		addToRefset(c.getConceptNid(), this.added_relationship_change,
				String.valueOf(d.getRelId()) + "\t" + d);
	}

	protected void deletedRelationship(I_GetConceptData c, I_RelTuple d)
			throws Exception {
		// super.deletedRelationship(c, d);
		addToRefset(c.getConceptNid(), this.deleted_relationship_change,
				String.valueOf(d.getRelId()) + "\t" + d);
		incr(this.deleted_relationship_change);
	}

	@Override
	protected void changedRelationshipStatus(I_GetConceptData c, I_RelTuple d1,
			I_RelTuple d2) throws Exception {
		super.changedRelationshipStatus(c, d1, d2);
		addToRefset(c.getConceptNid(), this.relationship_status_change,
				String.valueOf(d1.getRelId()) + "\t" + d2 + ": "
						+ getConceptName(d1.getStatusNid()) + " -> "
						+ getConceptName(d2.getStatusNid()));
	}

	@Override
	protected void changedRelationshipType(I_GetConceptData c, I_RelTuple d1,
			I_RelTuple d2) throws Exception {
		super.changedRelationshipType(c, d1, d2);
		addToRefset(c.getConceptNid(), this.relationship_type_change,
				String.valueOf(d1.getRelId()) + "\t" + d2 + ": "
						+ getConceptName(d1.getTypeNid()) + " -> "
						+ getConceptName(d2.getTypeNid()));
	}

	@Override
	protected void changedRelationshipCharacteristic(I_GetConceptData c,
			I_RelTuple d1, I_RelTuple d2) throws Exception {
		super.changedRelationshipCharacteristic(c, d1, d2);
		addToRefset(c.getConceptNid(), this.relationship_characteristic_change,
				String.valueOf(d1.getRelId()) + "\t" + d2 + ": "
						+ getConceptName(d1.getCharacteristicId()) + " -> "
						+ getConceptName(d2.getCharacteristicId()));
	}

	@Override
	protected void changedRelationshipRefinability(I_GetConceptData c,
			I_RelTuple d1, I_RelTuple d2) throws Exception {
		super.changedRelationshipRefinability(c, d1, d2);
		addToRefset(c.getConceptNid(), this.relationship_refinability_change,
				String.valueOf(d1.getRelId()) + "\t" + d2 + ": "
						+ getConceptName(d1.getCharacteristicId()) + " -> "
						+ getConceptName(d2.getCharacteristicId()));
	}

	@Override
	protected void changedRelationshipGroup(I_GetConceptData c, I_RelTuple d1,
			I_RelTuple d2) throws Exception {
		super.changedRelationshipGroup(c, d1, d2);
		addToRefset(
				c.getConceptNid(),
				this.relationship_group_change,
				String.valueOf(d1.getRelId()) + "\t" + d2 + ": "
						+ d1.getGroup() + " -> " + d2.getGroup());
	}

	private void compareRelationshipsOrig(I_GetConceptData c, PathBI path)
			throws Exception {
		I_TermFactory tf = Terms.get();
		for (I_RelVersioned d : c.getSourceRels()) {
			relationships++;
			I_RelPart r1 = null;
			I_RelPart r2 = null;
			for (I_RelPart dd : d.getMutableParts()) {
				if (dd.getPathId() != path.getConceptNid())
					continue;
				// Find the greatest version <= the one of interest
				if (dd.getVersion() <= pos1.getTime()
						&& (r1 == null || r1.getVersion() < dd.getVersion()))
					r1 = dd;
				if (dd.getVersion() <= pos2.getTime()
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
				addToRefset(c.getConceptNid(), this.added_relationship_change,
						String.valueOf(d.getRelId()) + "\t" + r2);
				incr(this.added_relationship_change);
			}
			if (this.deleted_relationships && r1 != null && r2 == null) {
				addToRefset(c.getConceptNid(),
						this.deleted_relationship_change,
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
					addToRefset(c.getConceptNid(),
							this.relationship_status_change,
							String.valueOf(d.getRelId())
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
					addToRefset(c.getConceptNid(),
							this.relationship_characteristic_change,
							String.valueOf(d.getRelId())
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
					addToRefset(c.getConceptNid(),
							this.relationship_refinability_change,
							String.valueOf(d.getRelId())
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
					addToRefset(c.getConceptNid(),
							this.relationship_type_change,
							String.valueOf(d.getRelId())
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
					addToRefset(c.getConceptNid(),
							this.relationship_group_change,
							String.valueOf(d.getRelId()) + "\t" + r2 + ": "
									+ r1.getGroup() + " -> " + r2.getGroup());
					incr(this.relationship_group_change);
				}
			}
		}
	}

	protected void logConfig(String str) throws Exception {
		super.logConfig(str);
		addToRefset(refset.getConceptNid(), this.config, str);
	}

	protected void logStats(String str) throws Exception {
		super.logConfig(str);
		addToRefset(refset.getConceptNid(), this.stats, str);
	}

	protected void processConfigFilters() throws Exception {
		super.processConfigFilters();
		I_TermFactory tf = Terms.get();
		this.config_ace_frame = tf.newAceFrameConfig();
		PathBI path = tf.getPath(Arrays.asList(UUID
				.fromString(refset_path_uuid)));
		this.createRefsetConcept(path, pos1, pos2, null);
		{
			this.refset_map = new RefsetPropertyMap(REFSET_TYPES.CID_STR);
			I_GetConceptData active_status = tf
					.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());
			int status_id = active_status.getConceptNid();
			refset_map.put(REFSET_PROPERTY.PATH, path.getConceptNid());
			refset_map.put(REFSET_PROPERTY.STATUS, status_id);
			refset_map.put(REFSET_PROPERTY.VERSION, Integer.MAX_VALUE);
		}
		{
			this.refset_map_member = new RefsetPropertyMap(REFSET_TYPES.CID);
			I_GetConceptData active_status = tf
					.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());
			int status_id = active_status.getConceptNid();
			refset_map_member.put(REFSET_PROPERTY.PATH, path.getConceptNid());
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
			this.createRefsetConcept(path, pos1, pos2, con);
		}
		getLog().info("Refset: " + refset.getInitialText());
		getLog().info("Refset: " + refset.getUids().get(0));
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
				.getConceptNid())) {
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
					.getConceptNid())) {
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

	private HashSet<Integer> getDescendants(int concept_id, PathBI path,
			int version) throws Exception {
		HashSet<Integer> ret = new HashSet<Integer>();
		getDescendants1(concept_id, path, version, ret);
		return ret;
	}

	private void getDescendants1(int concept_id, PathBI path, int version,
			HashSet<Integer> ret) throws Exception {
		if (ret.contains(concept_id))
			return;
		ret.add(concept_id);
		for (int ch : getChildren(concept_id, path, version)) {
			getDescendants1(ch, path, version, ret);
		}
	}

	private ArrayList<Integer> getChildren(int concept_id, PathBI path,
			int version) throws Exception {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		I_TermFactory tf = Terms.get();
		I_GetConceptData c = tf.getConcept(concept_id);
		for (I_RelVersioned d : c.getDestRels()) {
			I_RelPart dm = null;
			for (I_RelPart dd : d.getMutableParts()) {
				if (dd.getPathId() != path.getConceptNid())
					continue;
				if (dd.getTypeId() != tf.getConcept(
						SNOMED.Concept.IS_A.getUids()).getConceptNid())
					continue;
				// Find the greatest version <= the one of interest
				if (dd.getVersion() <= version
						&& (dm == null || dm.getVersion() < dd.getVersion()))
					dm = dd;
			}
			if (dm != null
					&& dm.getStatusId() == tf.getConcept(
							ArchitectonicAuxiliary.Concept.CURRENT.getUids())
							.getConceptNid())
				ret.add(d.getC1Id());
		}
		return ret;
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
			super.execute();

			I_TermFactory tf = Terms.get();
			ArrayList<Integer> all_concepts = getAllConcepts();
			long start = System.currentTimeMillis();
			getLog().info(
					"concepts " + concepts + " of " + all_concepts.size()
							+ " Elapsed "
							+ ((System.currentTimeMillis() - start) / 1000)
							+ " secs.");
			for (int nid : all_concepts) {
				concepts++;
				// if (concepts == 20001) {
				// getLog().info("BREAK concepts " + concepts);
				// break;
				// }
				if (concepts % 10000 == 0) {
					long cur = System.currentTimeMillis();
					float elap = cur - start;
					float done = ((float) concepts)
							/ ((float) all_concepts.size());
					float total = elap / done;
					getLog().info(
							"concepts " + concepts + " of "
									+ all_concepts.size() + ". Elapsed "
									+ ((int) (elap / 1000)) + " secs. "
									+ ((int) (done * 100))
									+ "% done. Remaining "
									+ ((int) ((total - elap) / 1000))
									+ " secs.");
				}
				I_GetConceptData c = tf.getConcept(nid);

				compareAttributes(c);
				compareDescriptions(c);
				compareRelationships(c);
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
			if (this.list_file != null)
				this.listDiff();
		} catch (Exception e) {
			throw new MojoFailureException(e.getLocalizedMessage(), e);
		}
	}

}
