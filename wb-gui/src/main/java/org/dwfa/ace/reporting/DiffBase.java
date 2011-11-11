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
package org.dwfa.ace.reporting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.TaskFailedException;
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
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_str.RefexCnidStrVersionBI;
import org.ihtsdo.tk.api.refex.type_str.RefexStrVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

/**
 *
 */
public class DiffBase {

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
     * Set to true to include concepts added to refex.
     * 
     * @parameter default-value=true
     */
    protected boolean added_concepts_refex;
    /**
     * Set to true to include concepts deleted from refex.
     * 
     * @parameter default-value=true
     */
    protected boolean changed_concepts_refex;
    /**
     * Set to true to include concepts with changed status.
     * 
     * @parameter default-value=true
     */
    protected boolean changed_concept_status;
    /**
     * Set to true to include concepts by author.
     * 
     * @parameter default-value=false
     */
    protected boolean changed_concept_author;
    protected boolean changed_description_author;
    protected boolean changed_rel_author;
    protected boolean changed_refex_author;
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
     * Optional list of concept status to filter v1 concept status changes
     * 
     * @parameter
     */
    protected List<String> v1_concept_author = new ArrayList<String>();
    protected List<Integer> v1_concept_author_int;
    /**
     * Optional list of concept status to filter v2 concept status changes
     * 
     * @parameter
     */
    protected List<String> v2_concept_author = new ArrayList<String>();
    protected List<Integer> v2_concept_author_int;
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
    protected int added_concept_change_refex;
    protected int deleted_concept_change_refex;
    protected int concept_status_change;
    protected ChangedValueCounter concept_status_change_stat = new ChangedValueCounter(
            "concept status");
    protected int defined_change;
    protected int[] defined_change_stat = new int[]{0, 0};
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
    protected boolean debug_p = false;
    protected I_ConfigAceFrame config;
    protected long v1_id;
    protected long v2_id;
    protected boolean noDescendantsV1 = false;
    protected boolean noDescendantsV2 = false;

    public DiffBase(String v1, String v2, String path1_uuid, String path2_uuid,
            List<Integer> v1_relationship_characteristic_filter_int, List<Integer> v2_relationship_characteristic_filter_int,
            List<Integer> v1_concept_status_filter_int, List<Integer> v2_concept_status_filter_int,
            List<Integer> v1_description_status_filter_int, List<Integer> v2_description_status_filter_int,
            List<Integer> v1_relationship_status_filter_int, List<Integer> v2_relationship_status_filter_int,
            boolean added_concepts, boolean deleted_concepts, boolean added_concepts_refex, boolean changed_concepts_refex,
            boolean changed_concept_status, boolean changed_concept_author, boolean changed_description_author,
            boolean changed_rel_author, boolean changed_refex_author,
            List<Integer> author1, List<Integer> author2, boolean changed_defined,
            boolean added_descriptions, boolean deleted_descriptions, boolean changed_description_status,
            boolean changed_description_term, boolean changed_description_type, boolean changed_description_language,
            boolean changed_description_case, boolean added_relationships, boolean deleted_relationships,
            boolean changed_relationship_status, boolean changed_relationship_characteristic,
            boolean changed_relationship_refinability,
            boolean changed_relationship_type, boolean changed_relationship_group,
            I_ConfigAceFrame config, boolean noDescendantsV1, boolean noDescendantsV2) {
        this.config = config;
        this.v1 = v1;
        this.v2 = v2;
        this.path1_uuid = path1_uuid;
        this.path2_uuid = path2_uuid;
        this.added_concepts = added_concepts;
        this.deleted_concepts = deleted_concepts;
        this.added_concepts_refex = added_concepts_refex;
        this.changed_concepts_refex = changed_concepts_refex;
        this.changed_concept_status = changed_concept_status;
        this.v1_concept_status_filter_int = v1_concept_status_filter_int;
        this.v2_concept_status_filter_int = v2_concept_status_filter_int;
        this.changed_concept_author = changed_concept_author;
        this.changed_description_author = changed_description_author;
        this.changed_rel_author = changed_rel_author;
        this.changed_refex_author = changed_refex_author;
        v1_concept_author_int = author1;
        v2_concept_author_int = author2;
        this.changed_defined = changed_defined;
        this.added_descriptions = added_descriptions;
        this.deleted_descriptions = deleted_descriptions;
        this.changed_description_status = changed_description_status;
        this.v1_description_status_filter_int = v1_description_status_filter_int;
        this.v2_description_status_filter_int = v2_description_status_filter_int;
        this.changed_description_term = changed_description_term;
        this.changed_description_type = changed_description_type;
        this.changed_description_language = changed_description_language;
        this.changed_description_case = changed_description_case;
        this.added_relationships = added_relationships;
        this.deleted_relationships = deleted_relationships;
        this.changed_relationship_status = changed_relationship_status;
        this.v1_relationship_status_filter_int = v1_relationship_status_filter_int;
        this.v2_relationship_status_filter_int = v2_relationship_status_filter_int;
        this.v1_relationship_characteristic_filter_int = v1_relationship_characteristic_filter_int;
        this.v2_relationship_characteristic_filter_int = v2_relationship_characteristic_filter_int;
        this.changed_relationship_characteristic = changed_relationship_characteristic;
        this.changed_relationship_refinability = changed_relationship_refinability;
        this.changed_relationship_type = changed_relationship_type;
        this.changed_relationship_group = changed_relationship_group;
        this.noDescendantsV1 = noDescendantsV1;
        this.noDescendantsV2 = noDescendantsV2;
    }

    protected void logConfig(String... str) throws Exception {
        String ss = "";
        for (String s : str) {
            ss += s + " ";
        }
        AceLog.getAppLog().info(ss);
    }

    protected void logStats(String str) throws Exception {
        AceLog.getAppLog().info(str);
    }

    protected void processConfig() throws Exception {
        I_TermFactory tf = Terms.get();
        PathBI path1 = tf.getPath(Arrays.asList(UUID.fromString(path1_uuid)));
        PathBI path2 = tf.getPath(Arrays.asList(UUID.fromString(path2_uuid)));
        v1_id = ThinVersionHelper.convert(this.v1);
        v2_id = ThinVersionHelper.convert(this.v2);
        pos1 = tf.newPosition(path1, v1_id);
        pos2 = tf.newPosition(path2, v2_id);
        allowed_position1 = new PositionSetReadOnly(pos1);
        allowed_position2 = new PositionSetReadOnly(pos2);

        AceLog.getAppLog().info("path1: " + tf.getConcept(path1.getConceptNid()).getInitialText());
        for (PositionBI p : path1.getInheritedOrigins()) {
            AceLog.getAppLog().info("\t" + p);
        }
        AceLog.getAppLog().info("path2: " + tf.getConcept(path2.getConceptNid()).getInitialText());
        for (PositionBI p : path2.getInheritedOrigins()) {
            AceLog.getAppLog().info("\t" + p);
        }
        logConfig("path1", path1_uuid);
        logConfig("path2", path2_uuid);
        logConfig("path1 - exclude descendants", "" + this.noDescendantsV1);
        logConfig("path2 - exclude descendants", "" + this.noDescendantsV1);
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
            I_GetConceptData con = tf.getConcept(Arrays.asList(UUID.fromString(str)));
            if (con == null) {
                logConfig(tag + " ERROR - Can't find " + str);
                continue;
            }
            ret.add(con.getConceptNid());
            logConfig(tag, con.getInitialText(), str);
        }
        return ret;
    }

    protected void logConfigList(List<Integer> conceptNids, String tag)
            throws Exception {
        I_TermFactory tf = Terms.get();
        if (conceptNids.isEmpty()) {
            logConfig(tag, "null");
        }
        for (Integer conceptNid : conceptNids) {
            I_GetConceptData con = tf.getConcept(conceptNid);
            if (con == null) {
                logConfig(tag, "null");
                continue;
            }
            logConfig(tag, con.getInitialText());
        }
    }

    protected void processConfigFilters() throws Exception {
        logConfig("added_concepts", "" + this.added_concepts);
        this.diff_count.put(this.added_concept_change, 0);
        logConfig("deleted_concepts", "" + this.deleted_concepts);
        logConfig("added_concepts_refex", "" + this.added_concepts_refex);
        this.diff_count.put(this.added_concept_change_refex, 0);
        logConfig("changed_concepts_refex", "" + this.changed_concepts_refex);
        this.diff_count.put(this.deleted_concept_change_refex, 0);
        logConfig("changed_concept_status", "" + this.changed_concept_status);
        this.diff_count.put(this.concept_status_change, 0);
//        this.v1_concept_status_int = buildConceptEnum(this.v1_concept_status,
//                "v1_concept_status");
//        this.v2_concept_status_int = buildConceptEnum(this.v2_concept_status,
//                "v2_concept_status");
        logConfig("changed_concept_author", "" + this.changed_concept_author);
        logConfig("changed_description_author", "" + this.changed_description_author);
        logConfig("changed_rel_author", "" + this.changed_rel_author);
        logConfig("changed_refex_author", "" + this.changed_refex_author);
        logConfigList(v1_concept_author_int, "v1_concept_author");
        logConfigList(v2_concept_author_int, "v2_concept_author");
//        if (v1_concept_author != null && v2_concept_author != null) {
//            this.v1_concept_author_int = buildConceptEnum(this.v1_concept_author,
//                    "v1_concept_author");
//            this.v2_concept_author_int = buildConceptEnum(this.v2_concept_author,
//                    "v2_concept_author");
//        }
        logConfig("changed_defined", "" + this.changed_defined);
        this.diff_count.put(this.defined_change, 0);
        this.v1_isa_filter_int = buildConceptEnum(this.v1_isa_filter,
                "v1_isa_filter");
        this.v2_isa_filter_int = buildConceptEnum(this.v2_isa_filter,
                "v2_isa_filter");
        logConfigList(v1_concept_status_filter_int, "v1_concept_status_filter");
        logConfigList(v2_concept_status_filter_int, "v2_concept_status_filter");
//        this.v1_concept_status_filter_int = buildConceptEnum(
//                this.v1_concept_status_filter, "v1_concept_status_filter");
//        this.v2_concept_status_filter_int = buildConceptEnum(
//                this.v2_concept_status_filter, "v2_concept_status_filter");
        logConfig("added_descriptions", "" + this.added_descriptions);
        this.diff_count.put(this.added_description_change, 0);
        logConfig("deleted_descriptions", "" + this.deleted_descriptions);
        this.diff_count.put(this.deleted_description_change, 0);
        logConfig("changed_description_status", ""
                + this.changed_description_status);
        this.diff_count.put(this.description_status_change, 0);
//        this.v1_description_status_int = buildConceptEnum(
//                this.v1_description_status, "v1_description_status");
//        this.v2_description_status_int = buildConceptEnum(
//                this.v2_description_status, "v2_description_status");
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
        logConfigList(v1_description_status_filter_int, "v1_description_status_filter");
        logConfigList(v2_description_status_filter_int, "v2_description_status_filter");
//        this.v1_description_status_filter_int = buildConceptEnum(
//                this.v1_description_status_filter,
//                "v1_description_status_filter");
//        this.v2_description_status_filter_int = buildConceptEnum(
//                this.v2_description_status_filter,
//                "v2_description_status_filter");
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
//        this.v1_relationship_status_int = buildConceptEnum(
//                this.v1_relationship_status, "v1_relationship_status");
//        this.v2_relationship_status_int = buildConceptEnum(
//                this.v2_relationship_status, "v2_relationship_status");
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
        logConfigList(v1_relationship_status_filter_int, "v1_relationship_status_filter");
        logConfigList(v2_relationship_status_filter_int, "v2_relationship_status_filter");
//        this.v1_relationship_status_filter_int = buildConceptEnum(
//                this.v1_relationship_status_filter,
//                "v1_relationship_status_filter");
//        this.v2_relationship_status_filter_int = buildConceptEnum(
//                this.v2_relationship_status_filter,
//                "v2_relationship_status_filter");
        logConfigList(v1_relationship_characteristic_filter_int, "v1_relationship_characteristic_filter");
        logConfigList(v2_relationship_characteristic_filter_int, "v2_relationship_characteristic_filter");
//        this.v1_relationship_characteristic_filter_int = buildConceptEnum(
//                this.v1_relationship_characteristic_filter,
//                "v1_relationship_characteristic_filter");
//        this.v2_relationship_characteristic_filter_int = buildConceptEnum(
//                this.v2_relationship_characteristic_filter,
//                "v2_relationship_characteristic_filter");
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
            this.v1_assertion_type_filter_enum = RelAssertionType.valueOf(this.v1_assertion_type_filter);
        }
        logConfig("v1_assertion_type_filter_enum", ""
                + this.v1_assertion_type_filter_enum);
        if (this.v2_assertion_type_filter != null) {
            this.v2_assertion_type_filter_enum = RelAssertionType.valueOf(this.v2_assertion_type_filter);
        }
        logConfig("v2_assertion_type_filter_enum", ""
                + this.v2_assertion_type_filter_enum);
    }

    protected void listConcepts(String tag,
            I_ConceptualizeUniversally... concepts) throws Exception {
        I_TermFactory tf = Terms.get();
        for (I_ConceptualizeUniversally c : concepts) {
            AceLog.getAppLog().info(
                    tag + ": " + tf.getConcept(c.getUids()).getUids().get(0)
                    + " " + tf.getConcept(c.getUids()).getInitialText());
            AceLog.getAppLog().info(
                    "\t"
                    + getConceptPreferredDescription(tf.getConcept(c.getUids())));
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
            AceLog.getAppLog().info(
                    "Root 1: " + tf.getConcept(ch).getUids().get(0) + " "
                    + tf.getConcept(ch).getInitialText());
        }
        for (int ch : getChildren(c.getConceptNid(), this.allowed_position2)) {
            AceLog.getAppLog().info(
                    "Root 2: " + tf.getConcept(ch).getUids().get(0) + " "
                    + tf.getConcept(ch).getInitialText());
        }
    }

    protected void listVersions() throws Exception {
        I_TermFactory tf = Terms.get();
        I_GetConceptData c = tf.getConcept(SNOMED.Concept.ROOT.getUids());
        AceLog.getAppLog().info(c.getInitialText());
        I_ConceptAttributeVersioned<?> cv = c.getConceptAttributes();
        for (I_ConceptAttributePart cvp : cv.getMutableParts()) {
            AceLog.getAppLog().info("Attr: " + cvp.getTime());
        }
        I_GetConceptData syn_type = tf.getConcept(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids());
        for (I_DescriptionVersioned<?> cd : c.getDescriptions()) {
            for (I_DescriptionPart cvp : cd.getMutableParts()) {
                if (cvp.getTypeNid() == syn_type.getConceptNid() // && cvp.getText().contains("time")
                        ) {
                    AceLog.getAppLog().info("Version: " + cvp.getText());
                    AceLog.getAppLog().info("         " + tf.getPath(cvp.getPathNid()));
                    AceLog.getAppLog().info("         " + cvp.getTime());
                    AceLog.getAppLog().info(
                            "         "
                            + ThinVersionHelper.format(ThinVersionHelper.convert(cvp.getTime())));
                    AceLog.getAppLog().info(
                            "         "
                            + ThinVersionHelper.formatTz(ThinVersionHelper.convert(cvp.getTime())));
                    for (String tz : Arrays.asList("GMT", "EST", "PST",
                            "GMT-04:00")) {
                        AceLog.getAppLog().info(
                                "         "
                                + ThinVersionHelper.formatTz(
                                ThinVersionHelper.convert(cvp.getTime()), tz));
                    }
                }
            }
        }
    }

    protected void initStatusAndType() throws Exception {
        I_TermFactory tf = Terms.get();
        if (current_status == null) {
            current_status = new NidSet();
            current_status.add(SnomedMetadataRfx.getSTATUS_CURRENT_NID());
        }
        if (isa_type == null) {
            isa_type = new NidSet();
            isa_type.add(tf.getConcept(SNOMED.Concept.IS_A.getUids()).getNid());
            isa_type.add(tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getNid());
        }
        if (pref_type == null) {
            pref_type = new NidSet();
            pref_type.add(SnomedMetadataRfx.getDESC_PREFERRED_NID());
        }
        if (fsn_type == null) {
            fsn_type = new NidSet();
            fsn_type.add(SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID());
        }
        classifier_type = tf.getConcept(
                ArchitectonicAuxiliary.Concept.SNOROCKET.getUids()).getNid();
    }
    private HashMap<Integer, String> concept_cache = new HashMap<Integer, String>();

    protected String getConceptName(int id) throws Exception {
        String text = concept_cache.get(id);
        if (text != null) {
            return text;
        }
        text = getConceptPreferredDescription(id);
        concept_cache.put(id, text);
        return text;
    }
    private HashMap<Integer, UUID> concept_uuid_cache = new HashMap<Integer, UUID>();

    protected String getConceptUUID(int id) throws Exception {
        UUID text = concept_uuid_cache.get(id);
        if (text != null) {
            return text.toString();
        }
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
                if (d.getTypeNid() == id && d.getLang().equals("en")) {
                    return d.getText();
                }
            }
        }
        if (ds.size() > 0) {
            return ds.get(0).getText();
        }
        return con.getInitialText();
    }
    private long beg;

    protected ArrayList<Integer> getDescendants(int concept_id,
            PositionSetBI pos) throws Exception {
        ArrayList<Integer> ret = new ArrayList<Integer>();
        HashSet<Integer> visited = new HashSet<Integer>();
        if (concept_id == Terms.get().getConcept(SNOMED.Concept.ROOT.getUids()).getNid()) {
            ConcurrentHashMap<Integer, ArrayList<Integer>> children = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
            Ts.get().iterateConceptDataInParallel(new Processor(children, pos));
            this.beg = System.currentTimeMillis();
            getDescendants1(concept_id, children, pos, ret, visited);
        } else {
            this.beg = System.currentTimeMillis();
            getDescendants2(concept_id, pos, ret, visited);
        }
        return ret;
    }

    private void getDescendants1(int concept_id,
            ConcurrentHashMap<Integer, ArrayList<Integer>> children,
            PositionSetBI pos, ArrayList<Integer> ret, HashSet<Integer> visited)
            throws Exception {
        if (visited.contains(concept_id)) {
            return;
        }
        ret.add(concept_id);
        visited.add(concept_id);
        if (ret.size() % 10000 == 0) {
            System.out.println(ret.size() + " "
                    + ((System.currentTimeMillis() - this.beg) / 1000));
        }
        for (int ch : getChildren1(concept_id, children, pos)) {
            getDescendants1(ch, children, pos, ret, visited);
        }
    }

    private void getDescendants2(int concept_id, PositionSetBI pos,
            ArrayList<Integer> ret, HashSet<Integer> visited) throws Exception {
        if (visited.contains(concept_id)) {
            return;
        }
        ret.add(concept_id);
        visited.add(concept_id);
        if (ret.size() % 10000 == 0) {
            System.out.println(ret.size() + " "
                    + ((System.currentTimeMillis() - this.beg) / 1000));
        }
        for (int ch : getChildren1(concept_id, pos)) {
            getDescendants2(ch, pos, ret, visited);
        }
    }

    private ArrayList<Integer> getChildren1(int concept_id,
            ConcurrentHashMap<Integer, ArrayList<Integer>> children,
            PositionSetBI pos) throws Exception {
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
        ConcurrentHashMap<Integer, ArrayList<Integer>> children;
        long beg = System.currentTimeMillis();

        public Processor(
                ConcurrentHashMap<Integer, ArrayList<Integer>> children,
                PositionSetBI pos) throws IOException {
            this.pos = pos;
            allConcepts = Ts.get().getAllConceptNids();
            this.children = children;
        }

        @Override
        public void processUnfetchedConceptData(int cNid,
                ConceptFetcherBI fetcher) throws Exception {
            I_GetConceptData c = (I_GetConceptData) fetcher.fetch();
            children.put(cNid, getChildren1(c, pos));
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
            if (path_con.getUids().size() != 0) {
                AceLog.getAppLog().info("Path: " + path_con.getInitialText());
                AceLog.getAppLog().info("      " + path_con.getUids().get(0));
                for (PositionBI position : path.getOrigins()) {
                    AceLog.getAppLog().info("Origin: " + position);
                    AceLog.getAppLog().info("Version: " + position.getVersion());
                }
            } else {
                AceLog.getAppLog().info("Path concept does not exist: " + path);
            }
        }
    }

    protected void setupConcepts() throws Exception {
        this.added_concept_change = RefsetAuxiliary.Concept.ADDED_CONCEPT.localize().getNid();
        this.deleted_concept_change = RefsetAuxiliary.Concept.DELETED_CONCEPT.localize().getNid();
        this.added_concept_change_refex = RefsetAuxiliary.Concept.ADDED_CONCEPT_REFEX.localize().getNid();
        this.deleted_concept_change_refex = RefsetAuxiliary.Concept.DELETED_CONCEPT_REFEX.localize().getNid();
        this.concept_status_change = RefsetAuxiliary.Concept.CHANGED_CONCEPT_STATUS.localize().getNid();
        this.defined_change = RefsetAuxiliary.Concept.CHANGED_DEFINED.localize().getNid();

        this.added_description_change = RefsetAuxiliary.Concept.ADDED_DESCRIPTION.localize().getNid();
        this.deleted_description_change = RefsetAuxiliary.Concept.DELETED_DESCRIPTION.localize().getNid();
        this.description_status_change = RefsetAuxiliary.Concept.CHANGED_DESCRIPTION_STATUS.localize().getNid();
        this.description_term_change = RefsetAuxiliary.Concept.CHANGED_DESCRIPTION_TERM.localize().getNid();
        this.description_type_change = RefsetAuxiliary.Concept.CHANGED_DESCRIPTION_TYPE.localize().getNid();
        this.description_language_change = RefsetAuxiliary.Concept.CHANGED_DESCRIPTION_LANGUAGE.localize().getNid();
        this.description_case_change = RefsetAuxiliary.Concept.CHANGED_DESCRIPTION_CASE.localize().getNid();

        this.added_relationship_change = RefsetAuxiliary.Concept.ADDED_RELATIONSHIP.localize().getNid();
        this.deleted_relationship_change = RefsetAuxiliary.Concept.DELETED_RELATIONSHIP.localize().getNid();
        this.relationship_status_change = RefsetAuxiliary.Concept.CHANGED_RELATIONSHIP_STATUS.localize().getNid();
        this.relationship_characteristic_change = RefsetAuxiliary.Concept.CHANGED_RELATIONSHIP_CHARACTERISTIC.localize().getNid();

        this.relationship_refinability_change = RefsetAuxiliary.Concept.CHANGED_RELATIONSHIP_REFINABILITY.localize().getNid();
        this.relationship_type_change = RefsetAuxiliary.Concept.CHANGED_RELATIONSHIP_TYPE.localize().getNid();
        this.relationship_group_change = RefsetAuxiliary.Concept.CHANGED_RELATIONSHIP_GROUP.localize().getNid();

        this.diff_count.put(this.added_concept_change, 0);
        this.diff_count.put(this.deleted_concept_change, 0);
        this.diff_count.put(this.added_concept_change_refex, 0);
        this.diff_count.put(this.deleted_concept_change_refex, 0);
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
        List<? extends I_ConceptAttributeTuple> a1s = c.getConceptAttributeTuples(null, allowed_position1, precedence,
                contradiction_mgr, v1_id);
        List<? extends I_ConceptAttributeTuple> a2s = c.getConceptAttributeTuples(null, allowed_position2, precedence,
                contradiction_mgr, v2_id);
        I_ConceptAttributeTuple<?> a1 = (a1s != null && a1s.size() > 0 ? a1s.get(0) : null);
        I_ConceptAttributeTuple<?> a2 = (a2s != null && a2s.size() > 0 ? a2s.get(0) : null);
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
        if (noDescendantsV1 && a1 != null
                && a1.getPathNid() != Ts.get().getNidForUuids(UUID.fromString(path1_uuid))) {
            a1 = null;
        }
        if (noDescendantsV2 && a2 != null
                && a2.getPathNid() != Ts.get().getNidForUuids(UUID.fromString(path2_uuid))) {
            a2 = null;
        }
        if (v1_concept_status_filter_int.size() > 0 && a1 != null
                && !v1_concept_status_filter_int.contains(a1.getStatusNid())) {
            return;
        }
        if (v2_concept_status_filter_int.size() > 0 && a2 != null
                && !v2_concept_status_filter_int.contains(a2.getStatusNid())) {
            return;
        }
        if (v1_isa_desc.size() > 0 && !v1_isa_desc.contains(c.getConceptNid())) {
            return;
        }
        if (v2_isa_desc.size() > 0 && !v2_isa_desc.contains(c.getConceptNid())) {
            return;
        }
        if (changed_concept_author) {
            if (v1_concept_author_int.size() > 0 && a1 != null
                    && !v1_concept_author_int.contains(a1.getAuthorNid())) {
                return;
            }
            if (v2_concept_author_int.size() > 0 && a2 != null
                    && !v2_concept_author_int.contains(a2.getAuthorNid())) {
                return;
            }
        }
        concepts_filtered++;

        if (a1 == null && a2 != null && this.added_concepts) {
            addedConcept(c);
        }
        if (a1 != null && a2 == null && this.deleted_concepts) {
            deletedConcept(c);
        }
        if (a1 != null
                && a2 != null
                && !(a1.getPathNid() == a2.getPathNid() && a1.getTime() == a2.getTime())) {
            // Something changed
            if (a1.getStatusNid() != a2.getStatusNid()
                    && this.changed_concept_status
                    && (this.v1_concept_status.isEmpty() || this.v1_concept_status_int.contains(a1.getStatusNid()))
                    && (this.v2_concept_status.isEmpty() || this.v2_concept_status_int.contains(a2.getStatusNid()))) {
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
            String term_filter, String lang_filter,
            Boolean case_filter, List<Integer> author_filter) {
        if (status_filter.size() > 0
                && !status_filter.contains(d.getStatusNid())) {
            return false;
        }
        if (type_filter.size() > 0 && !type_filter.contains(d.getTypeNid())) {
            return false;
        }
        if (term_filter != null && !term_filter.equals("")
                && !d.getText().matches(term_filter)) {
            return false;
        }
        if (lang_filter != null && !lang_filter.equals("")
                && !d.getLang().matches(lang_filter)) {
            return false;
        }
        if (case_filter != null && !d.isInitialCaseSignificant() == case_filter) {
            return false;
        }
        if (author_filter.size() > 0 && !author_filter.contains(d.getAuthorNid())) {
            return false;
        }
        return true;
    }

    protected void compareDescriptions(I_GetConceptData c) throws Exception {
        List<? extends I_DescriptionTuple> d1s = c.getDescriptionTuples(null,
                null, allowed_position1, precedence, contradiction_mgr, v1_id);
        List<? extends I_DescriptionTuple> d2s = c.getDescriptionTuples(null,
                null, allowed_position2, precedence, contradiction_mgr, v2_id);
        for (I_DescriptionTuple d2 : d2s) {
            if (noDescendantsV2
                    && d2.getPathNid() != Ts.get().getNidForUuids(UUID.fromString(path2_uuid))) {
                continue;
            }
            descriptions++;
            if (!testDescriptionFilter(d2,
                    v2_description_status_filter_int,
                    v2_description_type_filter_int,
                    v2_description_term_filter,
                    v2_description_lang_filter,
                    v2_description_case_filter,
                    v2_concept_author_int)) {
                continue;
            }
            boolean found = false;
            for (I_DescriptionTuple d1 : d1s) {
                if (d1.getDescId() == d2.getDescId()) {
                    if (noDescendantsV1
                            && d1.getPathNid() != Ts.get().getNidForUuids(UUID.fromString(path1_uuid))) {
                        continue;
                    }
                    if (!testDescriptionFilter(d1,
                            v1_description_status_filter_int,
                            v1_description_type_filter_int,
                            v1_description_term_filter,
                            v1_description_lang_filter,
                            v1_description_case_filter,
                            v1_concept_author_int)) {
                        continue;
                    }
                    found = true;
//                    if (changed_description_author) {
//                        if (v1_concept_author_int.size() > 0 && d1 != null
//                                && !v1_concept_author_int.contains(d1.getAuthorNid())) {
//                            return;
//                        }
//                        if (v2_concept_author_int.size() > 0 && d2 != null
//                                && !v2_concept_author_int.contains(d2.getAuthorNid())) {
//                            return;
//                        }
//                    }
                    descriptions_filtered++;
                    if (d1.getStatusNid() != d2.getStatusNid()
                            && this.changed_description_status
                            && (this.v1_description_status.isEmpty() || this.v1_description_status_int.contains(d1.getStatusNid()))
                            && (this.v2_description_status.isEmpty() || this.v2_description_status_int.contains(d2.getStatusNid()))) {
                        changedDescriptionStatus(c, d1, d2);
                    }
                    if (d1.getTypeNid() != d2.getTypeNid()
                            && this.changed_description_type) {
                        changedDescriptionType(c, d1, d2);
                    }
                    if (!d1.getText().equals(d2.getText())
                            && this.changed_description_term) {
                        changedDescriptionTerm(c, d1, d2);
                    }
                    if (!d1.getLang().equals(d2.getLang())
                            && this.changed_description_language) {
                        changedDescriptionLang(c, d1, d2);
                    }
                    if (d1.isInitialCaseSignificant() != d2.isInitialCaseSignificant()
                            && this.changed_description_case) {
                        changedDescriptionCase(c, d1, d2);
                    }
                    break;
                }
            }
            if (!found && this.added_descriptions) {
                addedDescription(c, d2);
            }
        }
    }
    protected int relationships = 0;
    protected int relationships_filtered = 0;

    protected boolean testRelationshipFilter(I_RelTuple r,
            List<Integer> status_filter, List<Integer> type_filter,
            List<Integer> characteristic_filter,
            List<Integer> refinability_filter,
            List<Integer> author_filter) {
        if (status_filter.size() > 0
                && !status_filter.contains(r.getStatusNid())) {
            return false;
        }
        if (type_filter.size() > 0 && !type_filter.contains(r.getTypeNid())) {
            return false;
        }
        if (characteristic_filter.size() > 0
                && !characteristic_filter.contains(r.getCharacteristicNid())) {
            return false;
        }
        if (refinability_filter.size() > 0
                && !refinability_filter.contains(r.getRefinabilityNid())) {
            return false;
        }
        if (author_filter.size() > 0
                && !author_filter.contains(r.getAuthorNid())) {
            return false;
        }
        return true;
    }

    protected void compareRelationships(I_GetConceptData c) throws Exception {
        List<? extends I_RelTuple> d1s = (this.v1_assertion_type_filter_enum == null ? c.getSourceRelTuples(null, null, allowed_position1, precedence,
                contradiction_mgr, v1_id) : c.getSourceRelTuples(null, null,
                allowed_position1, precedence, contradiction_mgr,
                classifier_type, v1_assertion_type_filter_enum, v1_id));
        List<? extends I_RelTuple> d2s = (this.v2_assertion_type_filter_enum == null ? c.getSourceRelTuples(null, null, allowed_position2, precedence,
                contradiction_mgr, v2_id) : c.getSourceRelTuples(null, null,
                allowed_position2, precedence, contradiction_mgr,
                classifier_type, v2_assertion_type_filter_enum, v2_id));
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
            if (noDescendantsV2
                    && d2.getPathNid() != Ts.get().getNidForUuids(UUID.fromString(path2_uuid))) {
                continue;
            }
            if (!testRelationshipFilter(d2, v2_relationship_status_filter_int,
                    v2_relationship_type_filter_int,
                    v2_relationship_characteristic_filter_int,
                    v2_relationship_refinability_filter_int,
                    v2_concept_author_int)) {
                continue;
            }
            boolean found = false;
            for (I_RelTuple d1 : d1s) {
                if (d1.getRelId() == d2.getRelId()) {
                    if (noDescendantsV1
                            && d1.getPathNid() != Ts.get().getNidForUuids(UUID.fromString(path1_uuid))) {
                        continue;
                    }
                    if (!testRelationshipFilter(d1,
                            v1_relationship_status_filter_int,
                            v1_relationship_type_filter_int,
                            v1_relationship_characteristic_filter_int,
                            v1_relationship_refinability_filter_int,
                            v1_concept_author_int)) {
                        continue;
                    }
                    found = true;
//                    if (changed_rel_author) {
//                        if (v1_concept_author_int.size() > 0 && d1 != null
//                                && !v1_concept_author_int.contains(d1.getAuthorNid())) {
//                            return;
//                        }
//                        if (v2_concept_author_int.size() > 0 && d2 != null
//                                && !v2_concept_author_int.contains(d2.getAuthorNid())) {
//                            return;
//                        }
//                    }
                    relationships_filtered++;
                    if (d1.getStatusNid() != d2.getStatusNid()
                            && this.changed_relationship_status
                            && (this.v1_relationship_status.isEmpty() || this.v1_relationship_status_int.contains(d1.getStatusNid()))
                            && (this.v2_relationship_status.isEmpty() || this.v2_relationship_status_int.contains(d2.getStatusNid()))) {
                        changedRelationshipStatus(c, d1, d2);
                    }
                    if (d1.getTypeNid() != d2.getTypeNid()
                            && this.changed_relationship_type) {
                        changedRelationshipType(c, d1, d2);
                    }
                    if (d1.getCharacteristicNid() != d2.getCharacteristicNid()
                            && this.changed_relationship_characteristic) {
                        changedRelationshipCharacteristic(c, d1, d2);
                    }
                    if (d1.getRefinabilityNid() != d2.getRefinabilityNid()
                            && this.changed_relationship_refinability) {
                        changedRelationshipRefinability(c, d1, d2);
                    }
                    if (d1.getGroup() != d2.getGroup()
                            && this.changed_relationship_group) {
                        changedRelationshipGroup(c, d1, d2);
                    }
                    break;
                }
            }
            if (!found && this.added_relationships) {
                addedRelationship(c, d2);
            }
        }
        //
        for (I_RelTuple d1 : d1s) {
            // relationships++;
            if (!testRelationshipFilter(d1, v1_relationship_status_filter_int,
                    v1_relationship_type_filter_int,
                    v1_relationship_characteristic_filter_int,
                    v1_relationship_refinability_filter_int,
                    v1_concept_author_int)) {
                continue;
            }
            boolean found = false;
            for (I_RelTuple d2 : d2s) {
                if (d2.getRelId() == d1.getRelId()) {
                    found = true;
                    if (!testRelationshipFilter(d2,
                            v2_relationship_status_filter_int,
                            v2_relationship_type_filter_int,
                            v2_relationship_characteristic_filter_int,
                            v2_relationship_refinability_filter_int,
                            v2_concept_author_int)) {
                        continue;
                    }
                    // relationships_filtered++;
                    break;
                }
            }
            if (!found && this.deleted_relationships) {
                deletedRelationship(c, d1);
            }
        }
    }
    protected int refexes = 0;

    protected void compareRefexes(I_GetConceptData c) throws Exception {
        ViewCoordinate vc = config.getViewCoordinate();
        ViewCoordinate vc1 = new ViewCoordinate(vc);
        vc1.setPositionSet(allowed_position1);
        ViewCoordinate vc2 = new ViewCoordinate(vc);
        vc2.setPositionSet(allowed_position2);
        ConceptChronicleBI concept = Ts.get().getConcept(c.getConceptNid());
        Collection<? extends RefexVersionBI<?>> members1 = concept.getCurrentRefsetMembers(vc1, v1_id);
        Collection<? extends RefexVersionBI<?>> members2 = concept.getCurrentRefsetMembers(vc2, v2_id);
        for (RefexVersionBI member : members1) {
            if (member.getTime() > v1_id) {
                members1.remove(member);
            }
            if (noDescendantsV1
                    && member.getPathNid() != Ts.get().getNidForUuids(UUID.fromString(path1_uuid))) {
                members1.remove(member);
            }
        }
        for (RefexVersionBI member : members2) {
            if (member.getTime() > v2_id) {
                members2.remove(member);
            }
            if (noDescendantsV2
                    && member.getPathNid() != Ts.get().getNidForUuids(UUID.fromString(path2_uuid))) {
                members2.remove(member);
            }
        }
        if (this.changed_concepts_refex) {
            for (RefexVersionBI member1 : members1) {
                if (changed_concept_author) {
                    if (v1_concept_author_int.size() > 0 && member1 != null
                            && !v1_concept_author_int.contains(member1.getAuthorNid())) {
                        return;
                    }
                }
                if (!members2.contains(member1)) {
                    String m1 = member1.toUserString();
                    String m2 = "";
                    deletedConceptFromRefex(c, m1, m2);
                }
                for (RefexVersionBI member2 : members2) {
                    if (member1.getNid() == member2.getNid()) {
                        if (RefexStrVersionBI.class.isAssignableFrom(member1.getClass())) {
                            RefexStrVersionBI rsv1 = (RefexStrVersionBI) member1;
                            RefexStrVersionBI rsv2 = (RefexStrVersionBI) member2;
                            if (!rsv1.getStr1().equals(rsv2)) {
                                String m1 = member1.toUserString();
                                String m2 = member2.toUserString();
                                deletedConceptFromRefex(c, m1, m2);
                            }
                        } else if (RefexCnidStrVersionBI.class.isAssignableFrom(member1.getClass())) {
                            RefexCnidStrVersionBI rcsv1 = (RefexCnidStrVersionBI) member1;
                            RefexCnidStrVersionBI rcsv2 = (RefexCnidStrVersionBI) member2;
                            if (!rcsv1.getStr1().equals(rcsv2)) {
                                String m1 = member1.toUserString();
                                String m2 = member2.toUserString();
                                deletedConceptFromRefex(c, m1, m2);
                            } else if (rcsv1.getCnid1() != rcsv2.getCnid1()) {
                                String m1 = member1.toUserString();
                                String m2 = member2.toUserString();
                                deletedConceptFromRefex(c, m1, m2);
                            }
                        }
                    }
                }
            }
        }
        if (this.added_concepts_refex) {

            for (RefexVersionBI member : members2) {
                RefexCnidStrVersionBI rv = (RefexCnidStrVersionBI) member;
                if (changed_concept_author) {
                    if (v1_concept_author_int.size() > 0 && member != null
                            && !v1_concept_author_int.contains(member.getAuthorNid())) {
                        return;
                    }
                }
                if (!members1.contains(member)) {
                    String m = member.toUserString();
                    addedConceptToRefex(c, m);
                }
            }
        }
    }

    protected void addedConceptToRefex(I_GetConceptData c, String m) throws Exception {
        incr(this.added_concept_change_refex);
    }

    protected void deletedConceptFromRefex(I_GetConceptData c, String m1,
            String m2) throws Exception {
        incr(this.deleted_concept_change_refex);
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
        for (NidBitSetItrBI i = Terms.get().getConceptNidSet().iterator(); i.next();) {
            all_concepts.add(i.nid());
        }
        return all_concepts;
    }

    //@Override
    public void execute() throws TaskFailedException {
        try {
            initStatusAndType();
            processConfig();
            this.listVersions();
            this.listPaths();
            this.listStatus();
            this.listDescriptionType();
            this.listCharacteristic();
            this.listRefinability();
            this.listRel();
            this.listRoots();
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }
}
