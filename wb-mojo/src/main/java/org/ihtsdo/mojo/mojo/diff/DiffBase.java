/**
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
package org.ihtsdo.mojo.mojo.diff;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.conflict.IdentifyAllConflictStrategy;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.NidBitSetItrBI;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.RelAssertionType;

/**
 *
 */
public class DiffBase extends AbstractMojo {

	/**
	 * The uuid for path1.
	 * 
	 * @parameter
	 */
	protected String path1_uuid;

	/**
	 * The uuid for path2.
	 * 
	 * @parameter
	 */
	protected String path2_uuid;

	/**
	 * The time for v1 in yyyy.mm.dd hh:mm:ss zzz format
	 * 
	 * @parameter
	 */
	protected String v1;

	/**
	 * The time for v2 in yyyy.mm.dd hh:mm:ss zzz format
	 * 
	 * @parameter
	 */
	protected String v2;

	protected PositionBI pos1;

	protected PositionSetBI allowed_position1;

	protected PositionBI pos2;

	protected PositionSetBI allowed_position2;

	/**
	 * Set to true to include added concepts.
	 * 
	 * @parameter default-value=true
	 */
	protected boolean added_concepts;

	/**
	 * Set to true to include deleted concepts.
	 * 
	 * @parameter default-value=true
	 */
	protected boolean deleted_concepts;

	/**
	 * Set to true to include concepts with changed status.
	 * 
	 * @parameter default-value=true
	 */
	protected boolean changed_concept_status;

	/**
	 * Set to true to include concepts with changed defined
	 * 
	 * @parameter default-value=true
	 */
	protected boolean changed_defined;

	/**
	 * Optional list of concept status to filter v1 concept status changes
	 * 
	 * @parameter
	 */
	protected List<String> v1_concept_status = new ArrayList<String>();

	protected List<Integer> v1_concept_status_int;

	/**
	 * Optional list of concept status to filter v2 concept status changes
	 * 
	 * @parameter
	 */
	protected List<String> v2_concept_status = new ArrayList<String>();

	protected List<Integer> v2_concept_status_int;

	/**
	 * Optional list of isa to filter v1
	 * 
	 * @parameter
	 */
	protected List<String> v1_isa_filter = new ArrayList<String>();

	protected List<Integer> v1_isa_filter_int;

	HashSet<Integer> v1_isa_desc = new HashSet<Integer>();
	HashSet<Integer> v2_isa_desc = new HashSet<Integer>();

	/**
	 * Optional list of isa to filter v2
	 * 
	 * @parameter
	 */
	protected List<String> v2_isa_filter = new ArrayList<String>();

	protected List<Integer> v2_isa_filter_int;

	/**
	 * Optional list of concept status to filter v1 concept status
	 * 
	 * @parameter
	 */
	protected List<String> v1_concept_status_filter = new ArrayList<String>();

	protected List<Integer> v1_concept_status_filter_int;

	/**
	 * Optional list of concept status to filter v2 concept status
	 * 
	 * @parameter
	 */
	protected List<String> v2_concept_status_filter = new ArrayList<String>();

	protected List<Integer> v2_concept_status_filter_int;

	/**
	 * Set to true to include added descriptions.
	 * 
	 * @parameter default-value=true
	 */
	protected boolean added_descriptions;

	/**
	 * Set to true to include deleted descriptions.
	 * 
	 * @parameter default-value=true
	 */
	protected boolean deleted_descriptions;

	/**
	 * Set to true to include descriptions with changed status.
	 * 
	 * @parameter default-value=true
	 */
	protected boolean changed_description_status;

	/**
	 * Optional list of description status to filter v1 changed status
	 * 
	 * @parameter
	 */
	protected List<String> v1_description_status = new ArrayList<String>();

	protected List<Integer> v1_description_status_int;

	/**
	 * Optional list of description status to filter v2 changed status
	 * 
	 * @parameter
	 */
	protected List<String> v2_description_status = new ArrayList<String>();

	protected List<Integer> v2_description_status_int;

	/**
	 * Set to true to include descriptions with changed term.
	 * 
	 * @parameter default-value=true
	 */
	protected boolean changed_description_term;

	/**
	 * Set to true to include descriptions with changed type.
	 * 
	 * @parameter default-value=true
	 */
	protected boolean changed_description_type;

	/**
	 * Set to true to include descriptions with changed language.
	 * 
	 * @parameter default-value=true
	 */
	protected boolean changed_description_language;

	/**
	 * Set to true to include descriptions with changed case.
	 * 
	 * @parameter default-value=true
	 */
	protected boolean changed_description_case;

	/**
	 * Optional list of description status to filter v1 status
	 * 
	 * @parameter
	 */
	protected List<String> v1_description_status_filter = new ArrayList<String>();

	protected List<Integer> v1_description_status_filter_int;

	/**
	 * Optional list of description status to filter v2 status
	 * 
	 * @parameter
	 */
	protected List<String> v2_description_status_filter = new ArrayList<String>();

	protected List<Integer> v2_description_status_filter_int;

	/**
	 * Optional list of description type to filter v1 type
	 * 
	 * @parameter
	 */
	protected List<String> v1_description_type_filter = new ArrayList<String>();

	protected List<Integer> v1_description_type_filter_int;

	/**
	 * Optional list of description type to filter v2 type
	 * 
	 * @parameter
	 */
	protected List<String> v2_description_type_filter = new ArrayList<String>();

	protected List<Integer> v2_description_type_filter_int;

	/**
	 * Optional regex to filter v1 term
	 * 
	 * @parameter
	 */
	protected String v1_description_term_filter = "";

	/**
	 * Optional regex to filter v2 term
	 * 
	 * @parameter
	 */
	protected String v2_description_term_filter = "";

	/**
	 * Optional regex to filter v1 lang
	 * 
	 * @parameter
	 */
	protected String v1_description_lang_filter = "";

	/**
	 * Optional regex to filter v2 lang
	 * 
	 * @parameter
	 */
	protected String v2_description_lang_filter = "";

	/**
	 * Optional filter on v1 case
	 * 
	 * @parameter
	 */
	protected Boolean v1_description_case_filter;

	/**
	 * Optional filter on v2 case
	 * 
	 * @parameter
	 */
	protected Boolean v2_description_case_filter;

	/**
	 * Set to true to include added relationships.
	 * 
	 * @parameter default-value=true
	 */
	protected boolean added_relationships;

	/**
	 * Set to true to include deleted relationships.
	 * 
	 * @parameter default-value=true
	 */
	protected boolean deleted_relationships;

	/**
	 * Set to true to include relationships with changed status.
	 * 
	 * @parameter default-value=true
	 */
	protected boolean changed_relationship_status;

	/**
	 * Optional list of relationship status to filter v1
	 * 
	 * @parameter
	 */
	protected List<String> v1_relationship_status = new ArrayList<String>();

	protected List<Integer> v1_relationship_status_int;

	/**
	 * Optional list of relationship status to filter v2
	 * 
	 * @parameter
	 */
	protected List<String> v2_relationship_status = new ArrayList<String>();

	protected List<Integer> v2_relationship_status_int;

	/**
	 * Set to true to include relationships with changed characteristic.
	 * 
	 * @parameter default-value=true
	 */
	protected boolean changed_relationship_characteristic;

	/**
	 * Set to true to include relationships with changed refinability.
	 * 
	 * @parameter default-value=true
	 */
	protected boolean changed_relationship_refinability;

	/**
	 * Set to true to include relationships with changed type.
	 * 
	 * @parameter default-value=true
	 */
	protected boolean changed_relationship_type;

	/**
	 * Set to true to include relationships with changed group.
	 * 
	 * @parameter default-value=true
	 */
	protected boolean changed_relationship_group;

	/**
	 * Optional list of relationship status to filter v1
	 * 
	 * @parameter
	 */
	protected List<String> v1_relationship_status_filter = new ArrayList<String>();

	protected List<Integer> v1_relationship_status_filter_int;

	/**
	 * Optional list of relationship status to filter v2
	 * 
	 * @parameter
	 */
	protected List<String> v2_relationship_status_filter = new ArrayList<String>();

	protected List<Integer> v2_relationship_status_filter_int;

	/**
	 * Optional list of relationship characteristic to filter v1 characteristic
	 * 
	 * @parameter
	 */
	protected List<String> v1_relationship_characteristic_filter = new ArrayList<String>();

	protected List<Integer> v1_relationship_characteristic_filter_int;

	/**
	 * Optional list of relationship characteristic to filter v2
	 * 
	 * @parameter
	 */
	protected List<String> v2_relationship_characteristic_filter = new ArrayList<String>();

	protected List<Integer> v2_relationship_characteristic_filter_int;

	/**
	 * Optional list of relationship refinability to filter v1
	 * 
	 * @parameter
	 */
	protected List<String> v1_relationship_refinability_filter = new ArrayList<String>();

	protected List<Integer> v1_relationship_refinability_filter_int;

	/**
	 * Optional list of relationship refinability to filter v2
	 * 
	 * @parameter
	 */
	protected List<String> v2_relationship_refinability_filter = new ArrayList<String>();

	protected List<Integer> v2_relationship_refinability_filter_int;

	/**
	 * Optional list of relationship type to filter v1
	 * 
	 * @parameter
	 */
	protected List<String> v1_relationship_type_filter = new ArrayList<String>();

	protected List<Integer> v1_relationship_type_filter_int;

	/**
	 * Optional list of relationship type to filter v2
	 * 
	 * @parameter
	 */
	protected List<String> v2_relationship_type_filter = new ArrayList<String>();

	protected List<Integer> v2_relationship_type_filter_int;

	/**
	 * Optional assertion type to filter v1
	 * 
	 * @parameter
	 */
	protected String v1_assertion_type_filter;

	protected RelAssertionType v1_assertion_type_filter_enum;

	/**
	 * Optional assertion type to filter v2
	 * 
	 * @parameter
	 */
	protected String v2_assertion_type_filter;

	protected RelAssertionType v2_assertion_type_filter_enum;

	protected NidSetBI current_status;

	protected NidSetBI isa_type;

	protected NidSetBI pref_type;

	protected NidSetBI fsn_type;

	protected int classifier_type;

	protected Precedence precedence = Precedence.PATH;

	protected ContradictionManagerBI contradiction_mgr = new IdentifyAllConflictStrategy();

	protected int added_concept_change;

	protected int deleted_concept_change;

	protected int concept_status_change;

	protected ChangedValueCounter concept_status_change_stat = new ChangedValueCounter(
			"concept status");

	protected int defined_change;

	protected int[] defined_change_stat = new int[] { 0, 0 };

	protected int added_description_change;

	protected int deleted_description_change;

	protected int description_status_change;

	protected ChangedValueCounter description_status_change_stat = new ChangedValueCounter(
			"description status");

	protected int description_term_change;

	protected int description_type_change;

	protected ChangedValueCounter description_type_change_stat = new ChangedValueCounter(
			"description type");

	protected int description_language_change;

	protected int description_case_change;

	protected int added_relationship_change;

	protected int deleted_relationship_change;

	protected int relationship_status_change;

	protected ChangedValueCounter relationship_status_change_stat = new ChangedValueCounter(
			"relationship status");

	protected int relationship_characteristic_change;

	protected ChangedValueCounter relationship_characteristic_change_stat = new ChangedValueCounter(
			"relationship characteristic type");

	protected int relationship_refinability_change;

	protected ChangedValueCounter relationship_refinability_change_stat = new ChangedValueCounter(
			"relationship refinability type");

	protected int relationship_type_change;

	protected ChangedValueCounter relationship_type_change_stat = new ChangedValueCounter(
			"relationship type");

	protected int relationship_group_change;

	protected boolean test_p = false;

	protected boolean test_descendants_p = false;

	protected boolean debug_p = false;

	protected void logConfig(String... str) throws Exception {
		String ss = "";
		for (String s : str) {
			ss += s + " ";
		}
		getLog().info(ss);
	}

	protected void logStats(String str) throws Exception {
		getLog().info(str);
	}

	protected void processConfig() throws Exception {
		I_TermFactory tf = Terms.get();
		PathBI path1 = tf.getPath(Arrays.asList(UUID.fromString(path1_uuid)));
		PathBI path2 = tf.getPath(Arrays.asList(UUID.fromString(path2_uuid)));
		Integer v1_id = ThinVersionHelper.convertTz(this.v1);
		Integer v2_id = ThinVersionHelper.convertTz(this.v2);
		pos1 = tf.newPosition(path1, v1_id);
		pos2 = tf.newPosition(path2, v2_id);
		allowed_position1 = new PositionSetReadOnly(pos1);
		allowed_position2 = new PositionSetReadOnly(pos2);

		getLog().info(
				"path1: "
						+ tf.getConcept(path1.getConceptNid()).getInitialText());
		for (PositionBI p : path1.getInheritedOrigins()) {
			getLog().info("\t" + p);
		}
		getLog().info(
				"path2: "
						+ tf.getConcept(path2.getConceptNid()).getInitialText());
		for (PositionBI p : path2.getInheritedOrigins()) {
			getLog().info("\t" + p);
		}
		logConfig("path1", path1_uuid);
		logConfig("path2", path2_uuid);
		logConfig("v1", v1);
		logConfig("v2", v2);
		// logConfig("v1", pos1.toString());
		// logConfig("v2", pos2.toString());

		setupConcepts();

		processConfigFilters();

		for (int v1_i : v1_isa_filter_int) {
			v1_isa_desc.addAll(getDescendants(v1_i, allowed_position1));
		}
		for (int v2_i : v2_isa_filter_int) {
			v2_isa_desc.addAll(getDescendants(v2_i, allowed_position2));
		}

	}

	protected List<Integer> buildConceptEnum(List<String> concepts, String tag)
			throws Exception {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		// logConfig(tag + " size = " + concepts.size());
		I_TermFactory tf = Terms.get();
		for (String str : concepts) {
			I_GetConceptData con = tf.getConcept(Arrays.asList(UUID
					.fromString(str)));
			if (con == null) {
				logConfig(tag + " ERROR - Can't find " + str);
				continue;
			}
			ret.add(con.getConceptNid());
			logConfig(tag, con.getInitialText(), str);
		}
		return ret;
	}

	protected void processConfigFilters() throws Exception {
		logConfig("added_concepts", "" + this.added_concepts);
		this.diff_count.put(this.added_concept_change, 0);
		logConfig("deleted_concepts", "" + this.deleted_concepts);
		this.diff_count.put(this.deleted_concept_change, 0);
		logConfig("changed_concept_status", "" + this.changed_concept_status);
		this.diff_count.put(this.concept_status_change, 0);
		this.v1_concept_status_int = buildConceptEnum(this.v1_concept_status,
				"v1_concept_status");
		this.v2_concept_status_int = buildConceptEnum(this.v2_concept_status,
				"v2_concept_status");
		logConfig("changed_defined", "" + this.changed_defined);
		this.diff_count.put(this.defined_change, 0);
		this.v1_isa_filter_int = buildConceptEnum(this.v1_isa_filter,
				"v1_isa_filter");
		this.v2_isa_filter_int = buildConceptEnum(this.v2_isa_filter,
				"v2_isa_filter");
		this.v1_concept_status_filter_int = buildConceptEnum(
				this.v1_concept_status_filter, "v1_concept_status_filter");
		this.v2_concept_status_filter_int = buildConceptEnum(
				this.v2_concept_status_filter, "v2_concept_status_filter");
		logConfig("added_descriptions", "" + this.added_descriptions);
		this.diff_count.put(this.added_description_change, 0);
		logConfig("deleted_descriptions", "" + this.deleted_descriptions);
		this.diff_count.put(this.deleted_description_change, 0);
		logConfig("changed_description_status", ""
				+ this.changed_description_status);
		this.diff_count.put(this.description_status_change, 0);
		this.v1_description_status_int = buildConceptEnum(
				this.v1_description_status, "v1_description_status");
		this.v2_description_status_int = buildConceptEnum(
				this.v2_description_status, "v2_description_status");
		logConfig("changed_description_term", ""
				+ this.changed_description_term);
		this.diff_count.put(this.description_term_change, 0);
		logConfig("changed_description_type", ""
				+ this.changed_description_type);
		this.diff_count.put(this.description_type_change, 0);
		logConfig("changed_description_language", ""
				+ this.changed_description_language);
		this.diff_count.put(this.description_language_change, 0);
		logConfig("changed_description_case", ""
				+ this.changed_description_case);
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
		logConfig("v1_description_term_filter", ""
				+ this.v1_description_term_filter);
		logConfig("v2_description_term_filter", ""
				+ this.v2_description_term_filter);
		logConfig("v1_description_lang_filter", ""
				+ this.v1_description_lang_filter);
		logConfig("v2_description_lang_filter", ""
				+ this.v2_description_lang_filter);
		logConfig("v1_description_case_filter", ""
				+ this.v1_description_case_filter);
		logConfig("v2_description_case_filter", ""
				+ this.v2_description_case_filter);
		logConfig("added_relationships", "" + this.added_relationships);
		this.diff_count.put(this.added_relationship_change, 0);
		logConfig("deleted_relationships", "" + this.deleted_relationships);
		this.diff_count.put(this.deleted_relationship_change, 0);
		logConfig("changed_relationship_status", ""
				+ this.changed_relationship_status);
		this.diff_count.put(this.relationship_status_change, 0);
		this.v1_relationship_status_int = buildConceptEnum(
				this.v1_relationship_status, "v1_relationship_status");
		this.v2_relationship_status_int = buildConceptEnum(
				this.v2_relationship_status, "v2_relationship_status");
		logConfig("changed_relationship_characteristic", ""
				+ this.changed_relationship_characteristic);
		this.diff_count.put(this.relationship_characteristic_change, 0);
		logConfig("changed_relationship_refinability", ""
				+ this.changed_relationship_refinability);
		this.diff_count.put(this.relationship_refinability_change, 0);
		logConfig("changed_relationship_type", ""
				+ this.changed_relationship_type);
		this.diff_count.put(this.relationship_type_change, 0);
		logConfig("changed_relationship_group", ""
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
		if (this.v1_assertion_type_filter != null) {
			this.v1_assertion_type_filter_enum = RelAssertionType
					.valueOf(this.v1_assertion_type_filter);
		}
		logConfig("v1_assertion_type_filter_enum", ""
				+ this.v1_assertion_type_filter_enum);
		if (this.v2_assertion_type_filter != null) {
			this.v2_assertion_type_filter_enum = RelAssertionType
					.valueOf(this.v2_assertion_type_filter);
		}
		logConfig("v2_assertion_type_filter_enum", ""
				+ this.v2_assertion_type_filter_enum);
	}

	protected void listConcepts(String tag,
			I_ConceptualizeUniversally... concepts) throws Exception {
		I_TermFactory tf = Terms.get();
		for (I_ConceptualizeUniversally c : concepts) {
			getLog().info(
					tag + ": " + tf.getConcept(c.getUids()).getUids().get(0)
							+ " " + tf.getConcept(c.getUids()).getInitialText());
			getLog().info(
					"\t"
							+ getConceptPreferredDescription(tf.getConcept(c
									.getUids())));
		}
	}

	protected void listStatus() throws Exception {
		listConcepts("Status", ArchitectonicAuxiliary.Concept.CURRENT,
				ArchitectonicAuxiliary.Concept.LIMITED,
				ArchitectonicAuxiliary.Concept.PENDING_MOVE,
				ArchitectonicAuxiliary.Concept.RETIRED,
				ArchitectonicAuxiliary.Concept.DUPLICATE,
				ArchitectonicAuxiliary.Concept.OUTDATED,
				ArchitectonicAuxiliary.Concept.AMBIGUOUS,
				ArchitectonicAuxiliary.Concept.ERRONEOUS,
				ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE);
	}

	protected void listDescriptionType() throws Exception {
		listConcepts(
				"Description type",
				ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE,
				ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE,
				ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE);
	}

	protected void listCharacteristic() throws Exception {
		listConcepts("Characteristic type",
				ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC,
				ArchitectonicAuxiliary.Concept.HISTORICAL_CHARACTERISTIC,
				ArchitectonicAuxiliary.Concept.QUALIFIER_CHARACTERISTIC,
				ArchitectonicAuxiliary.Concept.ADDITIONAL_CHARACTERISTIC);
	}

	protected void listRefinability() throws Exception {
		listConcepts("Refinability type",
				ArchitectonicAuxiliary.Concept.NOT_REFINABLE,
				ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY,
				ArchitectonicAuxiliary.Concept.MANDATORY_REFINABILITY);
	}

	protected void listRel() throws Exception {
		listConcepts("Rel type", SNOMED.Concept.IS_A);
	}

	protected void listRoots() throws Exception {
		I_TermFactory tf = Terms.get();
		I_GetConceptData c = tf.getConcept(SNOMED.Concept.ROOT.getUids());
		for (int ch : getChildren(c.getConceptNid(), this.allowed_position1)) {
			getLog().info(
					"Root 1: " + tf.getConcept(ch).getUids().get(0) + " "
							+ tf.getConcept(ch).getInitialText());
		}
		for (int ch : getChildren(c.getConceptNid(), this.allowed_position2)) {
			getLog().info(
					"Root 2: " + tf.getConcept(ch).getUids().get(0) + " "
							+ tf.getConcept(ch).getInitialText());
		}
	}

	protected void listVersions() throws Exception {
		I_TermFactory tf = Terms.get();
		I_GetConceptData c = tf.getConcept(SNOMED.Concept.ROOT.getUids());
		getLog().info(c.getInitialText());
		I_ConceptAttributeVersioned<?> cv = c.getConceptAttributes();
		for (I_ConceptAttributePart cvp : cv.getMutableParts()) {
			getLog().info("Attr: " + cvp.getTime());
		}
		I_GetConceptData syn_type = tf
				.getConcept(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE
						.getUids());
		for (I_DescriptionVersioned<?> cd : c.getDescriptions()) {
			for (I_DescriptionPart cvp : cd.getMutableParts()) {
				if (cvp.getTypeNid() == syn_type.getConceptNid()
				// && cvp.getText().contains("time")
				) {
					getLog().info("Version: " + cvp.getText());
					getLog().info("         " + tf.getPath(cvp.getPathNid()));
					getLog().info("         " + cvp.getTime());
					getLog().info(
							"         "
									+ ThinVersionHelper
											.format(ThinVersionHelper
													.convert(cvp.getTime())));
					getLog().info(
							"         "
									+ ThinVersionHelper
											.formatTz(ThinVersionHelper
													.convert(cvp.getTime())));
					for (String tz : Arrays.asList("GMT", "EST", "PST",
							"GMT-04:00")) {
						getLog().info(
								"         "
										+ ThinVersionHelper.formatTz(
												ThinVersionHelper.convert(cvp
														.getTime()), tz));
					}
				}
			}
		}
	}

	protected void initStatusAndType() throws Exception {
		I_TermFactory tf = Terms.get();
		if (current_status == null) {
			current_status = new NidSet();
			current_status.add(tf.getConcept(
					ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getNid());
		}
		if (isa_type == null) {
			isa_type = new NidSet();
			isa_type.add(tf.getConcept(SNOMED.Concept.IS_A.getUids()).getNid());
		}
		if (pref_type == null) {
			pref_type = new NidSet();
			pref_type.add(tf.getConcept(
					ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
							.getUids()).getNid());
		}
		if (fsn_type == null) {
			fsn_type = new NidSet();
			fsn_type.add(tf
					.getConcept(
							ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
									.getUids()).getNid());
		}
		classifier_type = tf.getConcept(
				ArchitectonicAuxiliary.Concept.SNOROCKET.getUids()).getNid();
	}

	private HashMap<Integer, String> concept_cache = new HashMap<Integer, String>();

	protected String getConceptName(int id) throws Exception {
		String text = concept_cache.get(id);
		if (text != null)
			return text;
		text = getConceptPreferredDescription(id);
		concept_cache.put(id, text);
		return text;
	}

	private HashMap<Integer, UUID> concept_uuid_cache = new HashMap<Integer, UUID>();

	protected String getConceptUUID(int id) throws Exception {
		UUID text = concept_uuid_cache.get(id);
		if (text != null)
			return text.toString();
		text = Terms.get().getUids(id).iterator().next();
		concept_uuid_cache.put(id, text);
		return text.toString();
	}

	protected String getConceptPreferredDescription(int id) throws Exception {
		return getConceptPreferredDescription(Terms.get().getConcept(id));
	}

	protected String getConceptPreferredDescription(I_GetConceptData con)
			throws Exception {
		List<? extends I_DescriptionTuple> ds = con.getDescriptionTuples(
				current_status, null, allowed_position2, precedence,
				contradiction_mgr);
		for (int id : Arrays.asList(pref_type.getSetValues()[0],
				fsn_type.getSetValues()[0])) {
			for (I_DescriptionTuple d : ds) {
				if (d.getTypeNid() == id && d.getLang().equals("en"))
					return d.getText();
			}
		}
		if (ds.size() > 0)
			return ds.get(0).getText();
		return con.getInitialText();
	}

	private long beg;

	protected ArrayList<Integer> getDescendants(int concept_id,
			PositionSetBI pos) throws Exception {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		HashSet<Integer> visited = new HashSet<Integer>();
		HashMap<Integer, ArrayList<Integer>> children = new HashMap<Integer, ArrayList<Integer>>();
		Ts.get().iterateConceptDataInParallel(new Processor(children, pos));
		this.beg = System.currentTimeMillis();
		getDescendants1(concept_id, children, pos, ret, visited);
		return ret;
	}

	private void getDescendants1(int concept_id,
			HashMap<Integer, ArrayList<Integer>> children, PositionSetBI pos,
			ArrayList<Integer> ret, HashSet<Integer> visited) throws Exception {
		if (visited.contains(concept_id))
			return;
		ret.add(concept_id);
		visited.add(concept_id);
		if (ret.size() % 10000 == 0) {
			System.out.println(ret.size() + " "
					+ ((System.currentTimeMillis() - this.beg) / 1000));
		}
		if (test_descendants_p && ret.size() >= 10000)
			return;
		for (int ch : getChildren1(concept_id, children, pos)) {
			getDescendants1(ch, children, pos, ret, visited);
		}
	}

	private ArrayList<Integer> getChildren1(int concept_id,
			HashMap<Integer, ArrayList<Integer>> children, PositionSetBI pos)
			throws Exception {
		return children.get(concept_id);
	}

	protected ArrayList<Integer> getChildren(int concept_id, PositionSetBI pos)
			throws Exception {
		return getChildren1(concept_id, pos);
	}

	private ArrayList<Integer> getChildren1(int concept_id, PositionSetBI pos)
			throws Exception {
		I_TermFactory tf = Terms.get();
		I_GetConceptData c = tf.getConcept(concept_id);
		return getChildren1(c, pos);
	}

	private ArrayList<Integer> getChildren1(I_GetConceptData c,
			PositionSetBI pos) throws Exception {
		// Some may be dups
		TreeSet<Integer> ret = new TreeSet<Integer>();
		for (I_RelTuple rel : c.getDestRelTuples(current_status, isa_type, pos,
				precedence, contradiction_mgr)) {
			ret.add(rel.getC1Id());
		}
		return new ArrayList<Integer>(ret);
	}

	private class Processor implements ProcessUnfetchedConceptDataBI {

		AtomicInteger i = new AtomicInteger();
		NidBitSetBI allConcepts;
		PositionSetBI pos;
		HashMap<Integer, ArrayList<Integer>> children;
		long beg = System.currentTimeMillis();

		public Processor(HashMap<Integer, ArrayList<Integer>> children,
				PositionSetBI pos) throws IOException {
			this.pos = pos;
			allConcepts = Ts.get().getAllConceptNids();
			this.children = children;
		}

		@Override
		public void processUnfetchedConceptData(int cNid,
				ConceptFetcherBI fetcher) throws Exception {
			I_GetConceptData c = (I_GetConceptData) fetcher.fetch();
			synchronized (children) {
				children.put(cNid, getChildren1(c, pos));
			}
			if (i.incrementAndGet() % 10000 == 0) {
				System.out.println("Processed getChildren: " + i + " "
						+ ((System.currentTimeMillis() - this.beg) / 1000));
			}
		}

		@Override
		public NidBitSetBI getNidSet() throws IOException {
			return allConcepts;
		}

		@Override
		public boolean continueWork() {
			return true;
		}
	}

	protected void listPaths() throws Exception {
		I_TermFactory tf = Terms.get();
		for (PathBI path : tf.getPaths()) {
			I_GetConceptData path_con = tf.getConcept(path.getConceptNid());
			getLog().info("Path: " + path_con.getInitialText());
			getLog().info("      " + path_con.getUids().get(0));
			for (PositionBI position : path.getOrigins()) {
				getLog().info("Origin: " + position);
				getLog().info("Version: " + position.getVersion());
			}
		}
	}

	protected void setupConcepts() throws Exception {
		this.added_concept_change = RefsetAuxiliary.Concept.ADDED_CONCEPT
				.localize().getNid();
		this.deleted_concept_change = RefsetAuxiliary.Concept.DELETED_CONCEPT
				.localize().getNid();
		this.concept_status_change = RefsetAuxiliary.Concept.CHANGED_CONCEPT_STATUS
				.localize().getNid();
		this.defined_change = RefsetAuxiliary.Concept.CHANGED_DEFINED
				.localize().getNid();

		this.added_description_change = RefsetAuxiliary.Concept.ADDED_DESCRIPTION
				.localize().getNid();
		this.deleted_description_change = RefsetAuxiliary.Concept.DELETED_DESCRIPTION
				.localize().getNid();
		this.description_status_change = RefsetAuxiliary.Concept.CHANGED_DESCRIPTION_STATUS
				.localize().getNid();
		this.description_term_change = RefsetAuxiliary.Concept.CHANGED_DESCRIPTION_TERM
				.localize().getNid();
		this.description_type_change = RefsetAuxiliary.Concept.CHANGED_DESCRIPTION_TYPE
				.localize().getNid();
		this.description_language_change = RefsetAuxiliary.Concept.CHANGED_DESCRIPTION_LANGUAGE
				.localize().getNid();
		this.description_case_change = RefsetAuxiliary.Concept.CHANGED_DESCRIPTION_CASE
				.localize().getNid();

		this.added_relationship_change = RefsetAuxiliary.Concept.ADDED_RELATIONSHIP
				.localize().getNid();
		this.deleted_relationship_change = RefsetAuxiliary.Concept.DELETED_RELATIONSHIP
				.localize().getNid();
		this.relationship_status_change = RefsetAuxiliary.Concept.CHANGED_RELATIONSHIP_STATUS
				.localize().getNid();
		this.relationship_characteristic_change = RefsetAuxiliary.Concept.CHANGED_RELATIONSHIP_CHARACTERISTIC
				.localize().getNid();

		this.relationship_refinability_change = RefsetAuxiliary.Concept.CHANGED_RELATIONSHIP_REFINABILITY
				.localize().getNid();
		this.relationship_type_change = RefsetAuxiliary.Concept.CHANGED_RELATIONSHIP_TYPE
				.localize().getNid();
		this.relationship_group_change = RefsetAuxiliary.Concept.CHANGED_RELATIONSHIP_GROUP
				.localize().getNid();

		this.diff_count.put(this.added_concept_change, 0);
		this.diff_count.put(this.deleted_concept_change, 0);
		this.diff_count.put(this.concept_status_change, 0);
		this.diff_count.put(this.defined_change, 0);
		this.diff_count.put(this.added_description_change, 0);
		this.diff_count.put(this.deleted_description_change, 0);
		this.diff_count.put(this.description_status_change, 0);
		this.diff_count.put(this.description_term_change, 0);
		this.diff_count.put(this.description_type_change, 0);
		this.diff_count.put(this.description_language_change, 0);
		this.diff_count.put(this.description_case_change, 0);
		this.diff_count.put(this.added_relationship_change, 0);
		this.diff_count.put(this.deleted_relationship_change, 0);
		this.diff_count.put(this.relationship_status_change, 0);
		this.diff_count.put(this.relationship_characteristic_change, 0);
		this.diff_count.put(this.relationship_refinability_change, 0);
		this.diff_count.put(this.relationship_type_change, 0);
		this.diff_count.put(this.relationship_group_change, 0);
	}

	protected HashMap<Integer, Integer> diff_count = new HashMap<Integer, Integer>();

	protected void incr(int id) {
		this.diff_count.put(id, this.diff_count.get(id) + 1);
	}

	protected int concepts = 0;

	protected int concepts_filtered = 0;

	protected void compareAttributes(I_GetConceptData c) throws Exception {
		List<? extends I_ConceptAttributeTuple> a1s = c
				.getConceptAttributeTuples(null, allowed_position1, precedence,
						contradiction_mgr);
		List<? extends I_ConceptAttributeTuple> a2s = c
				.getConceptAttributeTuples(null, allowed_position2, precedence,
						contradiction_mgr);
		I_ConceptAttributeTuple<?> a1 = (a1s != null && a1s.size() > 0 ? a1s
				.get(0) : null);
		I_ConceptAttributeTuple<?> a2 = (a2s != null && a2s.size() > 0 ? a2s
				.get(0) : null);
		if (debug_p) {
			System.out.println("P1: " + allowed_position1);
			System.out.println("P2: " + allowed_position2);
			System.out.println("A1: " + a1);
			System.out.println("A2: " + a2);
			I_ConceptAttributeVersioned<?> attr = c.getConceptAttributes();
			for (I_ConceptAttributePart a : attr.getMutableParts()) {
				System.out.println("A:  " + a);
			}
		}

		if (v1_concept_status_filter_int.size() > 0 && a1 != null
				&& !v1_concept_status_filter_int.contains(a1.getStatusNid()))
			return;
		if (v2_concept_status_filter_int.size() > 0 && a2 != null
				&& !v2_concept_status_filter_int.contains(a2.getStatusNid()))
			return;
		if (v1_isa_desc.size() > 0 && !v1_isa_desc.contains(c.getConceptNid()))
			return;
		if (v2_isa_desc.size() > 0 && !v2_isa_desc.contains(c.getConceptNid()))
			return;
		concepts_filtered++;

		if (a1 == null && a2 != null && this.added_concepts) {
			addedConcept(c);
		}
		if (a1 != null && a2 == null && this.deleted_concepts) {
			deletedConcept(c);
		}
		if (a1 != null
				&& a2 != null
				&& !(a1.getPathNid() == a2.getPathNid() && a1.getTime() == a2
						.getTime())) {
			// Something changed
			if (a1.getStatusNid() != a2.getStatusNid()
					&& this.changed_concept_status
					&& (this.v1_concept_status.isEmpty() || this.v1_concept_status_int
							.contains(a1.getStatusNid()))
					&& (this.v2_concept_status.isEmpty() || this.v2_concept_status_int
							.contains(a2.getStatusNid()))) {
				changedConceptStatus(c, a1.getStatusNid(), a2.getStatusNid());
			}
			if (a1.isDefined() != a2.isDefined() && this.changed_defined) {
				changedDefined(c, a1.isDefined(), a2.isDefined());
			}
		}
	}

	protected int descriptions = 0;

	protected int descriptions_filtered = 0;

	protected boolean testDescriptionFilter(I_DescriptionTuple d,
			List<Integer> status_filter, List<Integer> type_filter,
			String term_filter, String lang_filter, Boolean case_filter) {
		if (status_filter.size() > 0
				&& !status_filter.contains(d.getStatusNid()))
			return false;
		if (type_filter.size() > 0 && !type_filter.contains(d.getTypeNid()))
			return false;
		if (term_filter != null && !term_filter.equals("")
				&& !d.getText().matches(term_filter))
			return false;
		if (lang_filter != null && !lang_filter.equals("")
				&& !d.getLang().matches(lang_filter))
			return false;
		if (case_filter != null && !d.isInitialCaseSignificant() == case_filter)
			return false;
		return true;
	}

	protected void compareDescriptions(I_GetConceptData c) throws Exception {
		List<? extends I_DescriptionTuple> d1s = c.getDescriptionTuples(null,
				null, allowed_position1, precedence, contradiction_mgr);
		List<? extends I_DescriptionTuple> d2s = c.getDescriptionTuples(null,
				null, allowed_position2, precedence, contradiction_mgr);
		for (I_DescriptionTuple d2 : d2s) {
			descriptions++;
			if (!testDescriptionFilter(d2, v2_description_status_filter_int,
					v2_description_type_filter_int, v2_description_term_filter,
					v2_description_lang_filter, v2_description_case_filter))
				continue;
			boolean found = false;
			for (I_DescriptionTuple d1 : d1s) {
				if (d1.getDescId() == d2.getDescId()) {
					if (!testDescriptionFilter(d1,
							v1_description_status_filter_int,
							v1_description_type_filter_int,
							v1_description_term_filter,
							v1_description_lang_filter,
							v1_description_case_filter))
						continue;
					found = true;
					descriptions_filtered++;
					if (d1.getStatusNid() != d2.getStatusNid()
							&& this.changed_description_status
							&& (this.v1_description_status.isEmpty() || this.v1_description_status_int
									.contains(d1.getStatusNid()))
							&& (this.v2_description_status.isEmpty() || this.v2_description_status_int
									.contains(d2.getStatusNid())))
						changedDescriptionStatus(c, d1, d2);
					if (d1.getTypeNid() != d2.getTypeNid()
							&& this.changed_description_type)
						changedDescriptionType(c, d1, d2);
					if (!d1.getText().equals(d2.getText())
							&& this.changed_description_term)
						changedDescriptionTerm(c, d1, d2);
					if (!d1.getLang().equals(d2.getLang())
							&& this.changed_description_language)
						changedDescriptionLang(c, d1, d2);
					if (d1.isInitialCaseSignificant() != d2
							.isInitialCaseSignificant()
							&& this.changed_description_case)
						changedDescriptionCase(c, d1, d2);
					break;
				}
			}
			if (!found && this.added_descriptions)
				addedDescription(c, d2);
		}
	}

	protected int relationships = 0;

	protected int relationships_filtered = 0;

	protected boolean testRelationshipFilter(I_RelTuple r,
			List<Integer> status_filter, List<Integer> type_filter,
			List<Integer> characteristic_filter,
			List<Integer> refinability_filter) {
		if (status_filter.size() > 0
				&& !status_filter.contains(r.getStatusNid()))
			return false;
		if (type_filter.size() > 0 && !type_filter.contains(r.getTypeNid()))
			return false;
		if (characteristic_filter.size() > 0
				&& !characteristic_filter.contains(r.getCharacteristicNid()))
			return false;
		if (refinability_filter.size() > 0
				&& !refinability_filter.contains(r.getRefinabilityNid()))
			return false;
		return true;
	}

	protected void compareRelationships(I_GetConceptData c) throws Exception {
		List<? extends I_RelTuple> d1s = (this.v1_assertion_type_filter_enum == null ? c
				.getSourceRelTuples(null, null, allowed_position1, precedence,
						contradiction_mgr) : c.getSourceRelTuples(null, null,
				allowed_position1, precedence, contradiction_mgr,
				classifier_type, v1_assertion_type_filter_enum));
		List<? extends I_RelTuple> d2s = (this.v2_assertion_type_filter_enum == null ? c
				.getSourceRelTuples(null, null, allowed_position2, precedence,
						contradiction_mgr) : c.getSourceRelTuples(null, null,
				allowed_position2, precedence, contradiction_mgr,
				classifier_type, v2_assertion_type_filter_enum));
		if (debug_p) {
			for (I_RelTuple d1 : c.getSourceRelTuples(null, null, null,
					precedence, contradiction_mgr)) {
				System.out.println("D: " + d1);
			}
			System.out.println("P1: " + allowed_position1);
			System.out.println("P2: " + allowed_position2);
			for (I_RelTuple d1 : d1s) {
				System.out.println("D1: " + d1);
			}
			for (I_RelTuple d2 : d2s) {
				System.out.println("D2: " + d2);
			}
		}
		for (I_RelTuple d2 : d2s) {
			relationships++;
			if (!testRelationshipFilter(d2, v2_relationship_status_filter_int,
					v2_relationship_type_filter_int,
					v2_relationship_characteristic_filter_int,
					v2_relationship_refinability_filter_int))
				continue;
			boolean found = false;
			for (I_RelTuple d1 : d1s) {
				if (d1.getRelId() == d2.getRelId()) {
					found = true;
					if (!testRelationshipFilter(d1,
							v1_relationship_status_filter_int,
							v1_relationship_type_filter_int,
							v1_relationship_characteristic_filter_int,
							v1_relationship_refinability_filter_int))
						continue;
					relationships_filtered++;
					if (d1.getStatusNid() != d2.getStatusNid()
							&& this.changed_relationship_status
							&& (this.v1_relationship_status.isEmpty() || this.v1_relationship_status_int
									.contains(d1.getStatusNid()))
							&& (this.v2_relationship_status.isEmpty() || this.v2_relationship_status_int
									.contains(d2.getStatusNid())))
						changedRelationshipStatus(c, d1, d2);
					if (d1.getTypeNid() != d2.getTypeNid()
							&& this.changed_relationship_type)
						changedRelationshipType(c, d1, d2);
					if (d1.getCharacteristicNid() != d2.getCharacteristicNid()
							&& this.changed_relationship_characteristic)
						changedRelationshipCharacteristic(c, d1, d2);
					if (d1.getRefinabilityNid() != d2.getRefinabilityNid()
							&& this.changed_relationship_refinability)
						changedRelationshipRefinability(c, d1, d2);
					if (d1.getGroup() != d2.getGroup()
							&& this.changed_relationship_group)
						changedRelationshipGroup(c, d1, d2);
					break;
				}
			}
			if (!found && this.added_relationships)
				addedRelationship(c, d2);
		}
		//
		for (I_RelTuple d1 : d1s) {
			// relationships++;
			if (!testRelationshipFilter(d1, v1_relationship_status_filter_int,
					v1_relationship_type_filter_int,
					v1_relationship_characteristic_filter_int,
					v1_relationship_refinability_filter_int))
				continue;
			boolean found = false;
			for (I_RelTuple d2 : d2s) {
				if (d2.getRelId() == d1.getRelId()) {
					found = true;
					if (!testRelationshipFilter(d2,
							v2_relationship_status_filter_int,
							v2_relationship_type_filter_int,
							v2_relationship_characteristic_filter_int,
							v2_relationship_refinability_filter_int))
						continue;
					// relationships_filtered++;
					break;
				}
			}
			if (!found && this.deleted_relationships)
				deletedRelationship(c, d1);
		}
	}

	protected void addedConcept(I_GetConceptData c) throws Exception {
		incr(this.added_concept_change);
	}

	protected void deletedConcept(I_GetConceptData c) throws Exception {
		incr(this.deleted_concept_change);
	}

	protected void changedConceptStatus(I_GetConceptData c, int v1, int v2)
			throws Exception {
		incr(this.concept_status_change);
		this.concept_status_change_stat.changedValue(v1, v2);
	}

	protected void changedDefined(I_GetConceptData c, boolean v1, boolean v2)
			throws Exception {
		incr(this.defined_change);
		if (v1) {
			defined_change_stat[0]++;
		} else {
			defined_change_stat[1]++;
		}
	}

	protected void addedDescription(I_GetConceptData c, I_DescriptionTuple d)
			throws Exception {
		incr(this.added_description_change);
	}

	protected void changedDescriptionStatus(I_GetConceptData c,
			I_DescriptionTuple d1, I_DescriptionTuple d2) throws Exception {
		incr(this.description_status_change);
		this.description_status_change_stat.changedValue(d1.getStatusNid(),
				d2.getStatusNid());
	}

	protected void changedDescriptionTerm(I_GetConceptData c,
			I_DescriptionTuple d1, I_DescriptionTuple d2) throws Exception {
		incr(this.description_term_change);
	}

	protected void changedDescriptionLang(I_GetConceptData c,
			I_DescriptionTuple d1, I_DescriptionTuple d2) throws Exception {
		incr(this.description_language_change);
	}

	protected void changedDescriptionCase(I_GetConceptData c,
			I_DescriptionTuple d1, I_DescriptionTuple d2) throws Exception {
		incr(this.description_case_change);
	}

	protected void changedDescriptionType(I_GetConceptData c,
			I_DescriptionTuple d1, I_DescriptionTuple d2) throws Exception {
		incr(this.description_type_change);
		this.description_type_change_stat.changedValue(d1.getTypeNid(),
				d2.getTypeNid());
	}

	protected void addedRelationship(I_GetConceptData c, I_RelTuple d)
			throws Exception {
		incr(this.added_relationship_change);
	}

	protected void deletedRelationship(I_GetConceptData c, I_RelTuple d)
			throws Exception {
		incr(this.deleted_relationship_change);
	}

	protected void changedRelationshipStatus(I_GetConceptData c, I_RelTuple d1,
			I_RelTuple d2) throws Exception {
		incr(this.relationship_status_change);
		this.relationship_status_change_stat.changedValue(d1.getStatusNid(),
				d2.getStatusNid());
	}

	protected void changedRelationshipType(I_GetConceptData c, I_RelTuple d1,
			I_RelTuple d2) throws Exception {
		incr(this.relationship_type_change);
		this.relationship_type_change_stat.changedValue(d1.getTypeNid(),
				d2.getTypeNid());
	}

	protected void changedRelationshipCharacteristic(I_GetConceptData c,
			I_RelTuple d1, I_RelTuple d2) throws Exception {
		incr(this.relationship_characteristic_change);
		this.relationship_characteristic_change_stat.changedValue(
				d1.getCharacteristicNid(), d2.getCharacteristicNid());
	}

	protected void changedRelationshipRefinability(I_GetConceptData c,
			I_RelTuple d1, I_RelTuple d2) throws Exception {
		incr(this.relationship_refinability_change);
		this.relationship_refinability_change_stat.changedValue(
				d1.getRefinabilityNid(), d2.getRefinabilityNid());
	}

	protected void changedRelationshipGroup(I_GetConceptData c, I_RelTuple d1,
			I_RelTuple d2) throws Exception {
		incr(this.relationship_group_change);
	}

	protected ArrayList<Integer> getAllConcepts() throws Exception {
		ArrayList<Integer> all_concepts = new ArrayList<Integer>();
		for (NidBitSetItrBI i = Terms.get().getConceptNidSet().iterator(); i
				.next();) {
			all_concepts.add(i.nid());
		}
		return all_concepts;
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			processConfig();
			initStatusAndType();
			this.listVersions();
			this.listPaths();
			this.listStatus();
			this.listDescriptionType();
			this.listCharacteristic();
			this.listRefinability();
			this.listRel();
			this.listRoots();
		} catch (Exception e) {
			throw new MojoFailureException(e.getLocalizedMessage(), e);
		}
	}

}
