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
package org.dwfa.cement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.I_DescribeConceptUniversally;
import org.dwfa.tapi.I_ManifestLocally;
import org.dwfa.tapi.I_ManifestUniversally;
import org.dwfa.tapi.I_RelateConceptsUniversally;
import org.dwfa.tapi.I_StoreUniversalFixedTerminology;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedConcept;
import org.dwfa.tapi.impl.MemoryTermServer;
import org.dwfa.tapi.impl.UniversalFixedDescription;
import org.dwfa.tapi.impl.UniversalFixedRel;
import org.dwfa.util.id.Type3UuidFactory;

public class RefsetAuxiliary implements I_AddToMemoryTermServer {

    public enum Concept implements I_ConceptEnumeration, I_ConceptualizeUniversally {
        REFSET_AUXILIARY(new String[] { "Refset Auxiliary Concept", "Refset Auxiliary Concept" }), REFSET_SPEC(new String[] {
            "refset specification concept", "refset spec" }, REFSET_AUXILIARY), SPEC_GROUPING(new String[] {
            "Refset specification grouping", "Refset specification grouping" }, REFSET_SPEC), REFSET_AND_GROUPING(new String[] {
            "AND", "AND" }, SPEC_GROUPING), REFSET_OR_GROUPING(new String[] { "OR", "OR" }, SPEC_GROUPING), CONCEPT_CONTAINS_REL_GROUPING(new String[] {
            "CONCEPT-CONTAINS-REL", "CONCEPT-CONTAINS-REL" }, SPEC_GROUPING), CONCEPT_CONTAINS_DESC_GROUPING(new String[] {
            "CONCEPT-CONTAINS-DESC", "CONCEPT-CONTAINS-DESC" }, SPEC_GROUPING), SPEC_QUERY_TOKEN(new String[] {
            "Refset specification token", "Refset specification token" }, REFSET_SPEC), CONCEPT_IS_MEMBER_OF(new String[] {
            "CONCEPT IS MEMBER OF", "concept is member of" }, SPEC_QUERY_TOKEN), CONCEPT_STATUS_IS(new String[] {
            "CONCEPT STATUS IS", "concept status is" }, SPEC_QUERY_TOKEN), CONCEPT_STATUS_IS_KIND_OF(new String[] {
            "CONCEPT STATUS IS KIND OF", "concept status is kind of" }, SPEC_QUERY_TOKEN), CONCEPT_STATUS_IS_CHILD_OF(new String[] {
            "CONCEPT STATUS IS CHILD OF", "concept status is child of" }, SPEC_QUERY_TOKEN), CONCEPT_STATUS_IS_DESCENDENT_OF(new String[] {
            "CONCEPT STATUS IS DESCENDENT OF", "concept status is descendent of" }, SPEC_QUERY_TOKEN), CONCEPT_IS(new String[] {
            "CONCEPT IS", "concept is" }, SPEC_QUERY_TOKEN), CONCEPT_IS_CHILD_OF(new String[] { "CONCEPT IS CHILD OF",
            "concept is child of" }, SPEC_QUERY_TOKEN), CONCEPT_IS_DESCENDENT_OF(new String[] {
            "CONCEPT IS DESCENDENT OF", "concept is descendent of" }, SPEC_QUERY_TOKEN), CONCEPT_IS_KIND_OF(new String[] {
            "CONCEPT IS KIND OF", "concept is kind of" }, SPEC_QUERY_TOKEN), DESC_IS(new String[] { "DESC IS",
            "desc is" }, SPEC_QUERY_TOKEN), DESC_IS_MEMBER_OF(new String[] { "DESC IS MEMBER OF", "desc is member of" }, SPEC_QUERY_TOKEN), DESC_STATUS_IS(new String[] {
            "DESC STATUS IS", "desc status is" }, SPEC_QUERY_TOKEN), DESC_STATUS_IS_KIND_OF(new String[] {
            "DESC STATUS IS KIND OF", "desc status is kind of" }, SPEC_QUERY_TOKEN), DESC_STATUS_IS_CHILD_OF(new String[] {
            "DESC STATUS IS CHILD OF", "desc status is child of" }, SPEC_QUERY_TOKEN), DESC_STATUS_IS_DESCENDENT_OF(new String[] {
            "DESC STATUS IS DESCENDENT OF", "desc status is descendent of" }, SPEC_QUERY_TOKEN), DESC_TYPE_IS(new String[] {
            "DESC TYPE IS", "desc type is" }, SPEC_QUERY_TOKEN), DESC_TYPE_IS_KIND_OF(new String[] {
            "DESC TYPE IS KIND OF", "desc type is kind of" }, SPEC_QUERY_TOKEN), DESC_TYPE_IS_CHILD_OF(new String[] {
            "DESC TYPE IS CHILD OF", "desc type is child of" }, SPEC_QUERY_TOKEN), DESC_TYPE_IS_DESCENDENT_OF(new String[] {
            "DESC TYPE IS DESCENDENT OF", "desc type is descendent of" }, SPEC_QUERY_TOKEN), DESC_REGEX_MATCH(new String[] {
            "DESC REGEX MATCH", "desc regex match" }, SPEC_QUERY_TOKEN), DESC_LUCENE_MATCH(new String[] {
            "DESC LUCENE MATCH", "desc lucene match" }, SPEC_QUERY_TOKEN), REL_IS(new String[] { "REL IS", "rel is" }, SPEC_QUERY_TOKEN), REL_RESTRICTION_IS(new String[] {
            "REL RESTRICTION IS", "rel restriction is" }, SPEC_QUERY_TOKEN), REL_IS_MEMBER_OF(new String[] {
            "REL IS MEMBER OF", "rel is member of" }, SPEC_QUERY_TOKEN), REL_STATUS_IS(new String[] { "REL STATUS IS",
            "rel status is" }, SPEC_QUERY_TOKEN), REL_STATUS_IS_KIND_OF(new String[] { "REL STATUS IS KIND OF",
            "rel status is kind of" }, SPEC_QUERY_TOKEN), REL_STATUS_IS_CHILD_OF(new String[] {
            "REL STATUS IS CHILD OF", "rel status is child of" }, SPEC_QUERY_TOKEN), REL_STATUS_IS_DESCENDENT_OF(new String[] {
            "REL STATUS IS DESCENDENT OF", "rel status is descendent of" }, SPEC_QUERY_TOKEN), REL_TYPE_IS(new String[] {
            "REL TYPE IS", "rel type is" }, SPEC_QUERY_TOKEN), REL_TYPE_IS_KIND_OF(new String[] {
            "REL TYPE IS KIND OF", "rel type is kind of" }, SPEC_QUERY_TOKEN), REL_TYPE_IS_CHILD_OF(new String[] {
            "REL TYPE IS CHILD OF", "rel type is child of" }, SPEC_QUERY_TOKEN), REL_TYPE_IS_DESCENDENT_OF(new String[] {
            "REL TYPE IS DESCENDENT OF", "rel type is descendent of" }, SPEC_QUERY_TOKEN), REL_LOGICAL_QUANTIFIER_IS(new String[] {
            "REL LOGICAL QUANTIFIER IS", "rel logical quantifier is" }, SPEC_QUERY_TOKEN), REL_LOGICAL_QUANTIFIER_IS_KIND_OF(new String[] {
            "REL LOGICAL QUANTIFIER IS KIND OF", "rel logical quantifier is kind of" }, SPEC_QUERY_TOKEN), REL_LOGICAL_QUANTIFIER_IS_CHILD_OF(new String[] {
            "REL LOGICAL QUANTIFIER IS CHILD OF", "rel logical quantifier is child of" }, SPEC_QUERY_TOKEN), REL_LOGICAL_QUANTIFIER_IS_DESCENDENT_OF(new String[] {
            "REL LOGICAL QUANTIFIER IS DESCENDENT OF", "rel logical quantifier is descendent of" }, SPEC_QUERY_TOKEN), REL_CHARACTERISTIC_IS(new String[] {
            "REL CHARACTERISTIC IS", "rel characteristic is" }, SPEC_QUERY_TOKEN), REL_CHARACTERISTIC_IS_KIND_OF(new String[] {
            "REL CHARACTERISTIC IS KIND OF", "rel characteristic is kind of" }, SPEC_QUERY_TOKEN), REL_CHARACTERISTIC_IS_CHILD_OF(new String[] {
            "REL CHARACTERISTIC IS CHILD OF", "rel characteristic is child of" }, SPEC_QUERY_TOKEN), REL_CHARACTERISTIC_IS_DESCENDENT_OF(new String[] {
            "REL CHARACTERISTIC IS DESCENDENT OF", "rel characteristic is descendent of" }, SPEC_QUERY_TOKEN), REL_REFINABILITY_IS(new String[] {
            "REL REFINABILITY IS", "rel refinability is" }, SPEC_QUERY_TOKEN), REL_REFINABILITY_IS_CHILD_OF(new String[] {
            "REL REFINABILITY IS CHILD OF", "rel refinability is child of" }, SPEC_QUERY_TOKEN), REL_REFINABILITY_IS_DESCENDENT_OF(new String[] {
            "REL REFINABILITY IS DESCENDENT OF", "rel refinability is descendent of" }, SPEC_QUERY_TOKEN), REL_REFINABILITY_IS_KIND_OF(new String[] {
            "REL REFINABILITY IS KIND OF", "rel refinability is kind of" }, SPEC_QUERY_TOKEN),
            REL_DESTINATION_IS(new String[] { "REL DESTINATION IS", "rel destination is" }, SPEC_QUERY_TOKEN),
            REL_DESTINATION_IS_CHILD_OF(new String[] { "REL DESTINATION IS CHILD OF", "rel destination is child of" }, SPEC_QUERY_TOKEN), 
            REL_DESTINATION_IS_DESCENDENT_OF(new String[] { "REL DESTINATION IS DESCENDENT OF", "rel destination is descendent of" }, SPEC_QUERY_TOKEN), 
            REL_DESTINATION_IS_KIND_OF(new String[] { "REL DESTINATION IS KIND OF", "rel destination is kind of" }, SPEC_QUERY_TOKEN),

		DIFFERENCE_V1_IS(new String[] { "V1 IS", "V1 IS" }, SPEC_QUERY_TOKEN), DIFFERENCE_V2_IS(
				new String[] { "V2 IS", "V2 IS" }, SPEC_QUERY_TOKEN), DIFFERENCE_V1_GROUPING(
				new String[] { "V1", "V1" }, SPEC_GROUPING), DIFFERENCE_V2_GROUPING(
				new String[] { "V2", "V2" }, SPEC_GROUPING),

            DIFFERENCE_QUERY(new String[] {
            "Difference Query", "difference query" }, SPEC_QUERY_TOKEN), DIFFERENCE_CONFIGURATION(new String[] {
            "DIFFERENCE CONFIGURATION", "difference configuration" }, DIFFERENCE_QUERY), DIFFERENCE_STATISTICS(new String[] {
            "DIFFERENCE STATISTICS", "difference statistics" }, DIFFERENCE_QUERY), ADDED_CONCEPT(new String[] {
            "ADDED CONCEPT", "added concept" }, DIFFERENCE_QUERY), DELETED_CONCEPT(new String[] { "DELETED CONCEPT",
            "deleted concept" }, DIFFERENCE_QUERY), CHANGED_CONCEPT_STATUS(new String[] { "CHANGED CONCEPT STATUS",
            "changed concept status" }, DIFFERENCE_QUERY), CHANGED_DEFINED(new String[] { "CHANGED DEFINED",
            "changed defined" }, DIFFERENCE_QUERY), ADDED_DESCRIPTION(new String[] { "ADDED DESCRIPTION",
            "added description" }, DIFFERENCE_QUERY), DELETED_DESCRIPTION(new String[] { "DELETED DESCRIPTION",
            "deleted description" }, DIFFERENCE_QUERY), CHANGED_DESCRIPTION_STATUS(new String[] {
            "CHANGED DESCRIPTION STATUS", "changed description status" }, DIFFERENCE_QUERY), CHANGED_DESCRIPTION_TERM(new String[] {
            "CHANGED DESCRIPTION TERM", "changed description term" }, DIFFERENCE_QUERY), CHANGED_DESCRIPTION_TYPE(new String[] {
            "CHANGED DESCRIPTION TYPE", "changed description type" }, DIFFERENCE_QUERY), CHANGED_DESCRIPTION_LANGUAGE(new String[] {
            "CHANGED DESCRIPTION LANGUAGE", "changed description language" }, DIFFERENCE_QUERY), CHANGED_DESCRIPTION_CASE(new String[] {
            "CHANGED DESCRIPTION CASE", "changed description case" }, DIFFERENCE_QUERY), ADDED_RELATIONSHIP(new String[] {
            "ADDED RELATIONSHIP", "added relationship" }, DIFFERENCE_QUERY), DELETED_RELATIONSHIP(new String[] {
            "DELETED RELATIONSHIP", "deleted relationship" }, DIFFERENCE_QUERY), CHANGED_RELATIONSHIP_STATUS(new String[] {
            "CHANGED RELATIONSHIP STATUS", "changed relationship status" }, DIFFERENCE_QUERY), CHANGED_RELATIONSHIP_CHARACTERISTIC(new String[] {
            "CHANGED RELATIONSHIP CHARACTERISTIC", "changed relationship characteristic" }, DIFFERENCE_QUERY), CHANGED_RELATIONSHIP_TYPE(new String[] {
            "CHANGED RELATIONSHIP TYPE", "changed relationship type" }, DIFFERENCE_QUERY), CHANGED_RELATIONSHIP_REFINABILITY(new String[] {
            "CHANGED RELATIONSHIP REFINABILITY", "changed relationship refinability" }, DIFFERENCE_QUERY), CHANGED_RELATIONSHIP_GROUP(new String[] {
            "CHANGED RELATIONSHIP GROUP", "changed relationship group" }, DIFFERENCE_QUERY), 
            
            REFSET_COMPUTE_TYPE(new String[] {"refset compute type", "refset compute type" }, REFSET_AUXILIARY), 
            CONCEPT_COMPUTE_TYPE(new String[] {"concept compute type", "concept compute type" }, REFSET_COMPUTE_TYPE), 
            DESCRIPTION_COMPUTE_TYPE(new String[] {"description compute type", "description compute type" }, REFSET_COMPUTE_TYPE), 
            RELATIONSHIP_COMPUTE_TYPE(new String[] {"relationship compute type", "relationship compute type" }, REFSET_COMPUTE_TYPE), 
            
            REFSET_TYPE(new String[] {"refset type", "refset type" }, REFSET_AUXILIARY), 
        		MEMBERSHIP_EXTENSION(new String[] {"membership extension by reference", "membership extension" }, REFSET_TYPE), 
        		BOOLEAN_EXTENSION(new String[] {"boolean extension by reference", "boolean extension" }, REFSET_TYPE), 
            	STRING_EXTENSION(new String[] {"string extension by reference", "string extension" }, REFSET_TYPE), 
            	INT_EXTENSION(new String[] {"int extension by reference", "int extension" }, REFSET_TYPE), 
            	LONG_EXTENSION(new String[] {"long extension by reference", "long extension" }, REFSET_TYPE), 
            	CONCEPT_EXTENSION(new String[] {"component id extension by reference", "cid extension" }, REFSET_TYPE), 
            	CONCEPT_CONCEPT_EXTENSION(new String[] {"component id--component id extension by reference", "cid-cid extension" }, REFSET_TYPE), 
            	CONCEPT_STRING_EXTENSION(new String[] {"component id--string extension by reference", "cid-string extension" }, REFSET_TYPE), 
            	CONCEPT_CONCEPT_CONCEPT_EXTENSION(new String[] {"component id--component id--component id extension by reference", "cid-cid-cid extension" }, REFSET_TYPE), 
            	CONCEPT_CONCEPT_STRING_EXTENSION(new String[] {"component id--component id--string extension by reference", "cid--cid-string extension" }, REFSET_TYPE), 
            	CONCEPT_INT_EXTENSION(new String[] {"component id--int extension by reference", "cid--int extension" }, REFSET_TYPE), 
            	CID_LONG_EXTENSION(new String[] {"component id--long extension by reference", "cid--long extension" }, REFSET_TYPE), 
            	MEASUREMENT_EXTENSION(new String[] {"measurement extension by reference", "measurement extension" }, REFSET_TYPE), 
            	LANGUAGE_EXTENSION(new String[] {"rf1b language extension by reference", "rf1b language extension" }, REFSET_TYPE), 
            	SCOPED_LANGUAGE_EXTENSION(new String[] {"rf1b scoped language extension by reference", "rf1b scoped language extension" }, REFSET_TYPE), 
            	CROSS_MAP_REL_EXTENSION(new String[] {"cross map relationship extenstion", "cross map for rel" }, REFSET_TYPE), 
            	CROSS_MAP_EXTENSION(new String[] { "cross map extension", "cross map" }, REFSET_TYPE), 
            	TEMPLATE_REL_EXTENSION(new String[] { "template relationship extension", "template for rel" }, REFSET_TYPE), 
            	TEMPLATE_EXTENSION(new String[] {"template extension", "template" }, REFSET_TYPE),
            	LANGUAGE_ENUMERATION_EXTENSION(new String[] {"language enumeration refset", "language enumeratrion refset" }, REFSET_TYPE),
            	LANGUAGE_SPEC_EXTENSION(new String[] {"language spec refset", "language spec refset" }, REFSET_TYPE),

        BOOLEAN_CIRCLE_ICONS(new String[] { "boolean with circle icon", "boolean with circle" }, REFSET_AUXILIARY), BOOLEAN_CIRCLE_ICONS_TRUE(new String[] {
            "true with circle check icon", "true" }, BOOLEAN_CIRCLE_ICONS), BOOLEAN_CIRCLE_ICONS_FALSE(new String[] {
            "false with forbidden icon", "false" }, BOOLEAN_CIRCLE_ICONS), BOOLEAN_CHECK_CROSS_ICONS(new String[] {
            "boolean with check or cross icon", "boolean with check or cross" }, REFSET_AUXILIARY), BOOLEAN_CHECK_CROSS_ICONS_TRUE(new String[] {
            "true with check icon", "true" }, BOOLEAN_CHECK_CROSS_ICONS), BOOLEAN_CHECK_CROSS_ICONS_FALSE(new String[] {
            "false with cross icon", "false" }, BOOLEAN_CHECK_CROSS_ICONS), INCLUSION_SPECIFICATION_TYPE(new String[] {
            "inclusion specification type", "inclusion type" }, REFSET_AUXILIARY), INCLUDE_INDIVIDUAL(new String[] {
            "include concept no children", "individual include" }, INCLUSION_SPECIFICATION_TYPE), INCLUDE_LINEAGE(new String[] {
            "include concept with children", "lineage include" }, INCLUSION_SPECIFICATION_TYPE), EXCLUDE_INDIVIDUAL(new String[] {
            "exclude concept", "exclude individual" }, INCLUSION_SPECIFICATION_TYPE), EXCLUDE_LINEAGE(new String[] {
            "exclude concept and children", "exclude lineage" }, INCLUSION_SPECIFICATION_TYPE), LINGUISTIC_ROLE_TYPE(new String[] {
            "linguistic role type", "linguistic role type" }, REFSET_AUXILIARY), ATTRIBUTE_LINGUISTIC_ROLE(new String[] {
            "attribute linguistic role", "attribute" }, LINGUISTIC_ROLE_TYPE), NON_ATTRIBUTE_LINGUISTIC_ROLE(new String[] {
            "non-attribute linguistic role", "non-attribute" }, LINGUISTIC_ROLE_TYPE), MEMBER_TYPE(new String[] {
            "member type", "member type" }, new I_ConceptualizeUniversally[] { REFSET_AUXILIARY }, UUID.fromString("fa28e447-d635-49b1-9b55-86254a7a2f97")), MARKED_PARENT(new String[] {
            "marked parent member", "marked parent member" }, new I_ConceptualizeUniversally[] { MEMBER_TYPE }, UUID.fromString("125f3d04-de17-490e-afec-1431c2a39e29")), NORMAL_MEMBER(new String[] {
            "normal member", "normal member" }, new I_ConceptualizeUniversally[] { MEMBER_TYPE }, UUID.fromString("cc624429-b17d-4ac5-a69e-0b32448aaf3c")), REFSET_PURPOSE(new String[] {
            "refset purpose", "refset purpose" }, REFSET_AUXILIARY), ANNOTATION_PURPOSE(new String[] { "annotation",
            "annotation" }, REFSET_PURPOSE), ENUMERATED_ANNOTATION_PURPOSE(new String[] { "enumerated annotation",
            "enumerated annotation" }, ANNOTATION_PURPOSE), STRING_ANNOTATION_PURPOSE(new String[] {
            "string annotation", "string annotation" }, ANNOTATION_PURPOSE), ATTRIBUTE_VALUE(new String[] {
            "attribute value", "attribute value" }, REFSET_PURPOSE), ENUMERATED_ATTRIBUTE_VALUE_PURPOSE(new String[] {
            "enumerated attribute value", "enumerated attribute value" }, ATTRIBUTE_VALUE), STRING_ATTRIBUTE_VALUE_PURPOSE(new String[] {
            "string attribute value", "string attribute value" }, ATTRIBUTE_VALUE), NAVIGATION(new String[] {
            "navigation", "navigation" }, REFSET_PURPOSE), RELATIONSHIP_ORDER(new String[] { "relationship order",
            "relationship order" }, NAVIGATION), TYPED_NAVIGATION(new String[] { "typed navigation", "typed navigation" }, NAVIGATION), SPECIFICATION(new String[] {
            "specification", "specification" }, REFSET_PURPOSE), REFSET_SPECIFICATION(new String[] {
            "refset specification", "refset specification" }, SPECIFICATION), INCLUSION_SPECIFICATION(new String[] {
            "inclusion specification", "inclusion specification" }, SPECIFICATION), LINGUISTIC_ROLE(new String[] {
            "linguistic role", "lingiustic role" }, REFSET_PURPOSE), SUBJECT_TYPE(new String[] { "subject type",
            "subject type" }, REFSET_PURPOSE), INDEX_KEYS(new String[] { "index key", "index key" }, REFSET_PURPOSE), ANCILLARY_DATA(new String[] {
            "ancillary data", "ancillary data" }, REFSET_PURPOSE), DIALECT(new String[] { "dialect", "dialect" }, REFSET_PURPOSE), QUERY_SPECIFICATION(new String[] {
            "query specification", "query specification" }, REFSET_PURPOSE), MAPPING_PURPOSE(new String[] {
            "mapping purpose", "mapping purpose" }, REFSET_PURPOSE), SIMPLE_MAP_PURPOSE(new String[] {
            "simple mapping purpose", "simple mapping purpose" }, REFSET_PURPOSE), ALTERNATE_MAP_PURPOSE(new String[] {
            "alternate mapping purpose", "alternate mapping purpose" }, REFSET_PURPOSE), TYPED_MAP_PURPOSE(new String[] {
            "typed mapping purpose", "typed mapping purpose" }, REFSET_PURPOSE), SIMPLE_COMPONENT(new String[] {
            "simple component", "simple component" }, REFSET_PURPOSE), LANGUAGE_PURPOSE(new String[] {
            "language purpose", "language purpose" }, REFSET_PURPOSE), DESCRIPTION_TYPE_PURPOSE(new String[] {
            "description type purpose", "desc type purpose" }, REFSET_PURPOSE), MEASUREMENT_ASSOCIATION(new String[] {
            "measurement association", "measurement association" }, SIMPLE_COMPONENT), INT_MEASUREMENT_ASSOCIATION(new String[] {
            "integer measurement association", "integer measurement association" }, MEASUREMENT_ASSOCIATION), FLOAT_MEASUREMENT_ASSOCIATION(new String[] {
            "float measurement association", "float measurement association" }, MEASUREMENT_ASSOCIATION), REFSET_MEMBER_PURPOSE(new String[] {
            "refset membership", "refset membership" }, new I_ConceptualizeUniversally[] { REFSET_PURPOSE }, UUID.fromString("090a41ac-3299-54b4-a287-4f279b85d059")), REFSET_PARENT_MEMBER_PURPOSE(new String[] {
            "marked parent membership", "marked parent membership" }, new I_ConceptualizeUniversally[] { REFSET_PURPOSE }, UUID.fromString("7dd8fa86-7a20-56ab-8606-f82d28f6fd67")), MODULE_DEPENDENCY(new String[] {
            "module dependency", "module dependency" }, REFSET_PURPOSE), PATH_ORIGIN(new String[] { "path origin",
            "path origin" }, REFSET_PURPOSE), REFSET_PURPOSE_PATH(new String[] { "path", "path" }, REFSET_PURPOSE), REFSET_PURPOSE_POSITION(new String[] {
            "position", "position" }, REFSET_PURPOSE), TEMPLATE_VALUE_TYPE(new String[] { "template value type",
            "value type" }, REFSET_AUXILIARY), TEMPLATE_CODE_VALUE_TYPE(new String[] { "template code value type",
            "code" }, TEMPLATE_VALUE_TYPE), TEMPLATE_NUMBER_VALUE_TYPE(new String[] { "template number value type",
            "number" }, TEMPLATE_VALUE_TYPE), TEMPLATE_DATE_VALUE_TYPE(new String[] { "template date value type",
            "date" }, TEMPLATE_VALUE_TYPE), TEMPLATE_SEMANTIC_STATUS(new String[] { "template semantic status",
            "template semantic status" }, REFSET_AUXILIARY), TEMPLATE_FINAL_SEMANTIC_STATUS(new String[] {
            "template final semantic status", "final" }, TEMPLATE_SEMANTIC_STATUS), TEMPLATE_REFINABLE_SEMANTIC_STATUS(new String[] {
            "template refinable semantic status", "refinable" }, TEMPLATE_SEMANTIC_STATUS), TEMPLATE_NUMERIC_QUALIFIER_REFINE_SEMANTIC_STATUS(new String[] {
            "template numeric qualifier refine semantic status", "numeric qualifier" }, TEMPLATE_SEMANTIC_STATUS), TEMPLATE_MANDATORY_TO_REFINE_SEMANTIC_STATUS(new String[] {
            "template mandatory refinement semantic status", "mandatory refinement" }, TEMPLATE_SEMANTIC_STATUS), TEMPLATE_CHILD_REFINE_SEMANTIC_STATUS(new String[] {
            "template child refine semantic status", "child refinement" }, TEMPLATE_SEMANTIC_STATUS), TEMPLATE_QUALIFIER_REFINE_SEMANTIC_STATUS(new String[] {
            "template qualifier refine semantic status", "qualifier refine" }, TEMPLATE_SEMANTIC_STATUS), TEMPLATE_UNSPECIFIED_SEMANTIC_STATUS(new String[] {
            "template unspecified semantic status", "unspecified" }, TEMPLATE_SEMANTIC_STATUS), TEMPLATE_ATTRIBUTE_DISPLAY_STATUS(new String[] {
            "template attribute display status", "attribute display status" }, REFSET_AUXILIARY), TEMPLATE_ATTRIBUTE_DISPLAYED(new String[] {
            "template attribute displayed", "attribute displayed" }, TEMPLATE_ATTRIBUTE_DISPLAY_STATUS), TEMPLATE_ATTRIBUTE_HIDDEN(new String[] {
            "template attribute hidden", "attribute hidden" }, TEMPLATE_ATTRIBUTE_DISPLAY_STATUS), TEMPLATE_ATTRIBUTE_UNSPECIFIED(new String[] {
            "template attribute unspecified", "attribute unspecified" }, TEMPLATE_ATTRIBUTE_DISPLAY_STATUS), TEMPLATE_CHARACTERSITIC_STATUS(new String[] {
            "template characteristic status", "template characteristic status" }, REFSET_AUXILIARY), TEMPLATE_CHARACTERSITIC_QUALIFIER(new String[] {
            "template characteristic qualifier", "qualifier" }, TEMPLATE_CHARACTERSITIC_STATUS), TEMPLATE_CHARACTERSITIC_ATOM(new String[] {
            "template characteristic atom", "atom" }, TEMPLATE_CHARACTERSITIC_STATUS), TEMPLATE_CHARACTERSITIC_FACT(new String[] {
            "template characteristic fact", "fact" }, TEMPLATE_CHARACTERSITIC_STATUS), REFINABILITY_FLAG(new String[] {
            "refinability flag", "refinability flag" }, REFSET_AUXILIARY), COMPLETE_REFINABILITY_FLAG(new String[] {
            "refinability complete", "refinability complete" }, REFINABILITY_FLAG), MANDATORY_REFINABILITY_FLAG(new String[] {
            "refinability mandatory", "refinability mandatory" }, REFINABILITY_FLAG), POSSIBLE_REFINABILITY_FLAG(new String[] {
            "refinability possible", "refinability possible" }, REFINABILITY_FLAG), ADDITIONAL_CODE_FLAG(new String[] {
            "additional code flag", "additional code flag" }, REFSET_AUXILIARY), COMPLETE_ADDITIONAL_CODE_FLAG(new String[] {
            "additional code complete", "additional code complete" }, ADDITIONAL_CODE_FLAG), MANDATORY_ADDITIONAL_CODE_FLAG(new String[] {
            "additional code mandatory", "additional code mandatory" }, ADDITIONAL_CODE_FLAG), POSSIBLE_ADDITIONAL_CODE_FLAG(new String[] {
            "additional code possible", "additional code possible" }, ADDITIONAL_CODE_FLAG), MAP_STATUS(new String[] {
            "map status", "map status" }, REFSET_AUXILIARY), EXACT_MAP_STATUS(new String[] { "exact map status",
            "exact map status" }, MAP_STATUS), GENERAL_MAP_STATUS(new String[] { "general map status",
            "general map status" }, MAP_STATUS), DEFAULT_MAP_STATUS(new String[] { "default map status",
            "default map status" }, MAP_STATUS), REQUIRES_CHECKING_MAP_STATUS(new String[] {
            "requires checking map status", "requires checking exact map status" }, MAP_STATUS), ALTERNATIVE_MAP_STATUS(new String[] {
            "alternative map status", "alternative map status" }, MAP_STATUS), UNMAPPABLE_MAP_STATUS(new String[] {
            "unmappable map status", "unmappable map status" }, MAP_STATUS), REFSET_RELATIONSHIP(new String[] {
            "refset relationship", "refset relationship" }, new I_ConceptualizeUniversally[] {
            ArchitectonicAuxiliary.Concept.RELATIONSHIP, REFSET_AUXILIARY }), REFSET_TYPE_REL(new String[] {
            "refset type rel", "refset type rel" }, REFSET_RELATIONSHIP), REFSET_PURPOSE_REL(new String[] {
            "refset purpose rel", "refset purpose rel" }, REFSET_RELATIONSHIP), REFSET_OWNER(new String[] {
            "refset owner", "refset owner" }, REFSET_RELATIONSHIP), REFSET_EDITOR(new String[] { "refset editor",
            "refset editor" }, REFSET_RELATIONSHIP), REFSET_REVIEWER(new String[] { "refset reviewer",
            "refset reviewer" }, REFSET_RELATIONSHIP), SPECIFIES_REFSET(new String[] { "specifies refset",
            "specifies refset" }, REFSET_RELATIONSHIP), MARKED_PARENT_REFSET(new String[] { "marked parent refset",
            "marked parent refset" }, new I_ConceptualizeUniversally[] { REFSET_RELATIONSHIP }, UUID.fromString("8d0bcde8-6610-5573-86bd-8ab050dfc6a3")), MARKED_PARENT_IS_A_TYPE(new String[] {
            "marked parent is-a type", "marked parent is-a type" }, REFSET_RELATIONSHIP), PROMOTION_REL(new String[] {
            "promotion rel", "promotion rel" }, REFSET_RELATIONSHIP), COMMENTS_REL(new String[] { "comments rel",
            "comments rel" }, REFSET_RELATIONSHIP),
            EDIT_TIME_REL(new String[] { "edit time rel",
            "edit time rel" }, REFSET_RELATIONSHIP),
            COMPUTE_TIME_REL(new String[] { "compute time rel",
            "compute time rel" }, REFSET_RELATIONSHIP),
            REFSET_COMPUTE_TYPE_REL(new String[] { "refset compute type rel", "refset compute type rel" }, REFSET_RELATIONSHIP),
            LANGUAGE_ENUMERATION_ORIGIN_REL(new String[] { "language enumeration origin rel", "language enumeration origin rel" }, REFSET_RELATIONSHIP),
            LANGUAGE_SPEC_PREF_ORDER_REL(new String[] { "language spec pref order rel", "language spec pref order rel" }, REFSET_RELATIONSHIP),
            ATTRIBUTE_VALUE_REFSET_REL(new String[] { "attribute value refset rel", "attribute value refset rel" }, REFSET_RELATIONSHIP),
            WORKFLOW(new String[] {"Workflow Refsets", "Workflow Refsets" }, REFSET_RELATIONSHIP), 
            WORKFLOW_HISTORY(new String[] {"history workflow refset", "history workflow refset" }, WORKFLOW), 
            EDITOR_CATEGORY(new String[] {"editor category workflow refset", "editor category workflow refset" }, WORKFLOW), 
            SEMANTIC_AREA_HIERARCHY(new String[] {"semantic area hierarchy workflow refset", "semantic area hierarchy workflow refset" }, WORKFLOW), 
            STATE_TRANSITION(new String[] {"state transition workflow refset", "state transition workflow refset" }, WORKFLOW), 
            

        SUPPORTING_REFSETS(new String[] { "supporting refsets", "supporting refsets" }, REFSET_AUXILIARY),

        REFSET_IDENTITY(new String[] { "refset identity", "refset" }, REFSET_AUXILIARY),

        GB(new String[] { "GB", "GB" }, new I_ConceptualizeUniversally[] {
            REFSET_IDENTITY, ANCILLARY_DATA }, new I_ConceptualizeUniversally[] {
            ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),
            
            GB_NON_UKTC(new String[] { "Non UKTC", "Non UKTC" }, 
            		new I_ConceptualizeUniversally[] { GB, ANCILLARY_DATA },
            		new I_ConceptualizeUniversally[] {ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),
            GB_LPFIT(new String[] { "LPFIT", "LPFIT" }, 
            		new I_ConceptualizeUniversally[] { GB_NON_UKTC, ANCILLARY_DATA },
            		new I_ConceptualizeUniversally[] {ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),
            
            GB_UKTC(new String[] { "UKTC", "UKTC" }, 
            		new I_ConceptualizeUniversally[] { GB, ANCILLARY_DATA }, 
            		new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }), 
            
            GB_UKTC_CAB(new String[] { "CAB", "CAB" }, 
            		new I_ConceptualizeUniversally[] { GB_UKTC, ANCILLARY_DATA }, 
            		new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),

            GB_UKTC_CAB_OTHER(new String[] { "CAB Other", "CAB Other" }, 
            		new I_ConceptualizeUniversally[] { GB_UKTC_CAB, ANCILLARY_DATA }, 
            		new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),
        		   
            GB_UKTC_CAB_SNOMED_FULL(new String[] { "SNOMED Full", "SNOMED Full" }, 
                	new I_ConceptualizeUniversally[] { GB_UKTC_CAB, ANCILLARY_DATA }, 
                    new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),
                		   
            GB_UKTC_CAB_SNOMED_LITE(new String[] { "SNOMED Lite", "SNOMED Lite" }, 
                    new I_ConceptualizeUniversally[] { GB_UKTC_CAB, ANCILLARY_DATA }, 
                    new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),    		   
            
            GB_UKTC_CLINICAL_EXT(new String[] { "Clinical Extension", "Clinical Extension" }, 
            		new I_ConceptualizeUniversally[] { GB_UKTC, ANCILLARY_DATA }, 
            		new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }), 
            
            GB_UKTC_CLINICAL_EXT_ADMIN(new String[] { "Administrative", "Administrative"}, 
            		new I_ConceptualizeUniversally[] { GB_UKTC_CLINICAL_EXT, ANCILLARY_DATA }, 
            		new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),
            		
            GB_UKTC_CLINICAL_EXT_CRE(new String[] { "Care Record Element", "Care Record Element"}, 
                    new I_ConceptualizeUniversally[] { GB_UKTC_CLINICAL_EXT, ANCILLARY_DATA }, 
                    new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),
        
            GB_UKTC_CLINICAL_EXT_CMSG(new String[] { "Clinical Messaging", "Clinical Messaging"}, 
                    new I_ConceptualizeUniversally[] { GB_UKTC_CLINICAL_EXT, ANCILLARY_DATA }, 
                    new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),        		
                    		
            GB_UKTC_CLINICAL_EXT_DIAG_IP(new String[] { "Diagnostic Imaging Procedure", "Diagnostic Imaging Procedure"}, 
                    new I_ConceptualizeUniversally[] { GB_UKTC_CLINICAL_EXT, ANCILLARY_DATA }, 
                    new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),      		
                    		
           GB_UKTC_CLINICAL_EXT_ENDOSCOPY(new String[] { "Endoscopy", "Endoscopy"}, 
                    new I_ConceptualizeUniversally[] { GB_UKTC_CLINICAL_EXT, ANCILLARY_DATA }, 
                    new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),           		
            
           GB_UKTC_CLINICAL_EXT_LANG(new String[] { "Language", "Language"}, 
                    new I_ConceptualizeUniversally[] { GB_UKTC_CLINICAL_EXT, ANCILLARY_DATA }, 
                    new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),   
           
           GB_UKTC_CLINICAL_EXT_LNK_ASERT(new String[] { "Link Assertion", "Link Assertion"}, 
                    new I_ConceptualizeUniversally[] { GB_UKTC_CLINICAL_EXT, ANCILLARY_DATA }, 
                    new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),  
           
           GB_UKTC_CLINICAL_EXT_NON_HUMAN(new String[] { "Non Human", "Non Human"},
           	        new I_ConceptualizeUniversally[] { GB_UKTC_CLINICAL_EXT, ANCILLARY_DATA }, 
                    new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),                   
          
           GB_UKTC_CLINICAL_EXT_OC_THERAPY(new String[] { "Occupational Therapy", "Occupational Therapy"}, 
                    new I_ConceptualizeUniversally[] { GB_UKTC_CLINICAL_EXT, ANCILLARY_DATA }, 
                    new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),                      
                    
           GB_UKTC_CLINICAL_EXT_PATH(new String[] { "Pathology Bounded Code List", "Pathology Bounded Code List"}, 
                    new I_ConceptualizeUniversally[] { GB_UKTC_CLINICAL_EXT, ANCILLARY_DATA }, 
                    new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),   
                                            
           GB_UKTC_CLINICAL_EXT_PATH_CAT(new String[] { "Pathology Catalog", "Pathology Catalog"}, 
                    new I_ConceptualizeUniversally[] { GB_UKTC_CLINICAL_EXT, ANCILLARY_DATA }, 
                    new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),                                
                                            
           GB_UKTC_CLINICAL_EXT_PUBHL(new String[] { "Public Health Language", "Public Health Language"}, 
                    new I_ConceptualizeUniversally[] { GB_UKTC_CLINICAL_EXT, ANCILLARY_DATA }, 
                    new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),                                 
                                            
           GB_UKTC_CLINICAL_EXT_SSERP(new String[] { "SSERP", "SSERP"}, 
                    new I_ConceptualizeUniversally[] { GB_UKTC_CLINICAL_EXT, ANCILLARY_DATA }, 
                    new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),                                  
                                            
           GB_UKTC_CLINICAL_EXT_SCG(new String[] { "Standards Consulting Group", "Standards Consulting Group"}, 
                    new I_ConceptualizeUniversally[] { GB_UKTC_CLINICAL_EXT, ANCILLARY_DATA }, 
                    new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),                                 
          
           GB_UKTC_CLINICAL_EXT_RELIGION(new String[] { "Religions", "Religions"}, 
                    new I_ConceptualizeUniversally[] { GB_UKTC_CLINICAL_EXT_SCG, ANCILLARY_DATA }, 
                    new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }), 
                                                                       
           GB_UKTC_DRUG(new String[] { "UK Drug Extension", "UK Drug Extension" }, 
                    new I_ConceptualizeUniversally[] { GB_UKTC, ANCILLARY_DATA }, 
                    new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),                                 
            
           GB_UKTC_DRUG_CMSG(new String[] { "Drug Clinical Messaging", "Drug Clinical Messaging" }, 
                    new I_ConceptualizeUniversally[] { GB_UKTC_DRUG, ANCILLARY_DATA }, 
                    new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }), 
                                            
           GB_UKTC_DRUG_DMD(new String[] { "DMD", "DMD" }, 
                     new I_ConceptualizeUniversally[] { GB_UKTC_DRUG, ANCILLARY_DATA }, 
                     new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),                                  
            
           GB_UKTC_DRUG_EPRES(new String[] { "EPrescribing", "EPrescribing" }, 
                     new I_ConceptualizeUniversally[] { GB_UKTC_DRUG, ANCILLARY_DATA }, 
                     new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }), 
            
           GB_UKTC_DRUG_LANG(new String[] { "Drug Language", "Drug Language" }, 
                     new I_ConceptualizeUniversally[] { GB_UKTC_DRUG, ANCILLARY_DATA }, 
                     new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }), 

        DOCUMENT_SECTION_ORDER(new String[] { "document section order", "document section order", "ORG_DWFA_DOC_SECTION_ORDER" }, 
        		     new I_ConceptualizeUniversally[] { REFSET_IDENTITY, RELATIONSHIP_ORDER, INT_EXTENSION }, 
        		     new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL, REFSET_TYPE_REL }), 

           GB_READ_CODES(new String[] { "Read", "Read" }, 
        	            		new I_ConceptualizeUniversally[] { GB, ANCILLARY_DATA },
        	            		new I_ConceptualizeUniversally[] {ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL }),
        	            		
           SNCT_RELEASED_BOOL(new String[] { "SNOMED Released ", "SNOMED released flag", "ORG_DWFA_SNCT_RELEASED_BOOL" },
				new I_ConceptualizeUniversally[] { GB_READ_CODES, SUBJECT_TYPE, BOOLEAN_EXTENSION },
				new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL,	REFSET_PURPOSE_REL, REFSET_TYPE_REL }),

		   CTV3_WORD_KEYS(new String[] { "Clinical Terms Version 3 Word Key", "CTV3 word key", "ORG_DWFA_CTV3_WORD_KEYS" },
				new I_ConceptualizeUniversally[] { GB_READ_CODES, INDEX_KEYS, STRING_EXTENSION }, 
				new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL, REFSET_TYPE_REL }),

		   CTV3_RELEASED_BOOL(new String[] { "Clinical Terms Version 2 and 3 Released ", "CTV2-3 released flag", "ORG_DWFA_CTV3_RELEASED_BOOL" },
				new I_ConceptualizeUniversally[] { GB_READ_CODES, SUBJECT_TYPE, BOOLEAN_EXTENSION },
				new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL,	REFSET_PURPOSE_REL, REFSET_TYPE_REL }),

		   CTV3_TEMPLATE_BROWSE_VALUE(new String[] { "Clinical Terms Version 3 Template Browse Value Order", "template browse value order",
				"ORG_DWFA_CTV3_TEMPLATE_BROWSE_VALUE" },
				new I_ConceptualizeUniversally[] { GB_READ_CODES,	ANCILLARY_DATA, INT_EXTENSION },
				new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL, REFSET_TYPE_REL }),

		   CTV3_TEMPLATE_CARDINALITY(new String[] { "Clinical Terms Version 3 Template Cardinality", "template cardinality", 
				"ORG_DWFA_CTV3_TEMPLATE_CARDINALITY" },
				new I_ConceptualizeUniversally[] { GB_READ_CODES, ANCILLARY_DATA, INT_EXTENSION },
				new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL,	REFSET_PURPOSE_REL, REFSET_TYPE_REL }),

		   CTV3_TEMPLATE_BROWSE_ATTRIBUTE(new String[] { "Clinical Terms Version 3 Template Browse Attribute Order",
				"template browse attribute order", "ORG_DWFA_CTV3_TEMPLATE_BROWSE_ATTRIBUTE" },
				new I_ConceptualizeUniversally[] { GB_READ_CODES, ANCILLARY_DATA, INT_EXTENSION },
				new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL,	REFSET_PURPOSE_REL, REFSET_TYPE_REL }),

		   CTV3_TEMPLATE_NOTES_SCREEN(new String[] { "Clinical Terms Version 3 Template Notes Screen Order", "template notes screen order",
				"ORG_DWFA_CTV3_TEMPLATE_NOTES_SCREEN" },
				new I_ConceptualizeUniversally[] { GB_READ_CODES, ANCILLARY_DATA, INT_EXTENSION },
				new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL,	REFSET_PURPOSE_REL, REFSET_TYPE_REL }),

		   CTV3_TEMPLATE_VALUE_TYPE(new String[] { "Clinical Terms Version 3 Template Value Type", "template value type", 
				   "ORG_DWFA_CTV3_TEMPLATE_VALUE_TYPE" },
				new I_ConceptualizeUniversally[] { GB_READ_CODES, ANCILLARY_DATA, CONCEPT_EXTENSION },
				new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL, REFSET_TYPE_REL }),

		   CTV3_TEMPLATE_SEMANTIC_STATUS(new String[] {	"Clinical Terms Version 3 Template Semantic Status", "template semantic status",
				"ORG_DWFA_CTV3_TEMPLATE_SEMANTIC_STATUS" },
				new I_ConceptualizeUniversally[] { GB_READ_CODES, ANCILLARY_DATA, CONCEPT_EXTENSION },
				new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL,	REFSET_PURPOSE_REL, REFSET_TYPE_REL }),

		   CTV3_TEMPLATE_DISPLAY_STATUS(new String[] { "Clinical Terms Version 3 Template Attribute Display Status", "template attribute display status",
				"ORG_DWFA_CTV3_TEMPLATE_DISPLAY_STATUS" },
				new I_ConceptualizeUniversally[] { GB_READ_CODES, ANCILLARY_DATA, CONCEPT_EXTENSION },
				new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL, REFSET_TYPE_REL }),

		   CTV3_TEMPLATE_CHARACTERISTIC_STATUS(new String[] { "Clinical Terms Version 3 Template Characteristic Display Status", "template characteristic status",
						"ORG_DWFA_CTV3_TEMPLATE_CHARACTERISTIC_STATUS" },
				new I_ConceptualizeUniversally[] { GB_READ_CODES, ANCILLARY_DATA, CONCEPT_EXTENSION },
				new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL,	REFSET_PURPOSE_REL, REFSET_TYPE_REL }),

		   CTV3_LINGUISTIC_ROLE(new String[] { "Clinical Terms Version 3 linguistic role", "CTV3 linguistic role", "ORG_DWFA_CTV3_LINGUISTIC_ROLE" },
				new I_ConceptualizeUniversally[] { GB_READ_CODES, LINGUISTIC_ROLE, CONCEPT_EXTENSION },
				new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL,	REFSET_PURPOSE_REL, REFSET_TYPE_REL }),

		   CTV3_REL_ORDER(new String[] { "Clinical Terms Version 3 relationship order", "CTV3 rel order", "ORG_DWFA_CTV3_REL_ORDER" },
				new I_ConceptualizeUniversally[] { GB_READ_CODES, RELATIONSHIP_ORDER, INT_EXTENSION },
				new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL, REFSET_TYPE_REL }),

		   CTV3_SUBJECT_TYPE(new String[] { "Clinical Terms Version 3 subject type", "CTV3 subject type", "ORG_DWFA_CTV3_SUBJECT_TYPE" },
				new I_ConceptualizeUniversally[] { GB_READ_CODES, SUBJECT_TYPE, CONCEPT_EXTENSION },
				new I_ConceptualizeUniversally[] { ArchitectonicAuxiliary.Concept.IS_A_REL,	REFSET_PURPOSE_REL, REFSET_TYPE_REL }),
				                                                        
        REFSET_PATHS(new String[] { "Path reference set", "Path reference set" }, new I_ConceptualizeUniversally[] {
            REFSET_IDENTITY, REFSET_PURPOSE_PATH, CONCEPT_EXTENSION }, new I_ConceptualizeUniversally[] {
            ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL, REFSET_TYPE_REL }), REFSET_PATH_ORIGINS(new String[] {
            "Path origin reference set", "Path origin reference set" }, new I_ConceptualizeUniversally[] {
            REFSET_IDENTITY, REFSET_PURPOSE_POSITION, CONCEPT_INT_EXTENSION }, new I_ConceptualizeUniversally[] {
            ArchitectonicAuxiliary.Concept.IS_A_REL, REFSET_PURPOSE_REL, REFSET_TYPE_REL }), 
            
	    MRCM_DOMAINS(new String[] { "MRCM Domains refsets", "MRCM Domains refsets" }, 
	    		new I_ConceptualizeUniversally[] { REFSET_IDENTITY }), 
		MRCM_RANGES(new String[] { "MRCM Domains refsets", "MRCM Domains refsets" }, 
				new I_ConceptualizeUniversally[] { REFSET_IDENTITY }), 
            
        // Issue Repository Auxiliary root;
        ISSUE_MANAGER_ROOT(new String[] { "Issue Manager Auxiliary Concept", "Issue Manager Auxiliary Concept" }, 
        		new I_ConceptualizeUniversally[] {}),
		ISSUE_REPOSITORY(new String[] { "Issue repository", "Issue repository" }, 
	    		new I_ConceptualizeUniversally[] {ISSUE_MANAGER_ROOT}),
		ISSUE_REPOSITORY_METADATA_REFSET(new String[] { "Issue repository metadata refset", "Issue repository metadata refset" }, 
        		new I_ConceptualizeUniversally[] {ISSUE_MANAGER_ROOT}),
        		
        // Rules repository metadata
		RULES_AUXILIARY(new String[] {"rules auxiliary concept", "rules auxiliary concept" }, REFSET_AUXILIARY),
		RULES_DEPLOYMENT_PKG(new String[] {"rules deployment package", "rules deployment package" }, RULES_AUXILIARY),
		RULES_DEPLOYMENT_PKG_METADATA_REFSET(new String[] { "rules deployment package metadata refset", 
				"rules deployment package metadata refset" }, RULES_AUXILIARY),
        RULES_CONTEXT(new String[] { "rules context", "rules context" },  RULES_AUXILIARY),
        RULES_CONTEXT_METADATA_REFSET(new String[] { "rules context metadata refset", 
		"rules context metadata refset" }, RULES_AUXILIARY),
        REALTIME_QA_CONTEXT(new String[] { "realtime qa context", "realtime qa context" },  RULES_CONTEXT),		
        BATCH_QA_CONTEXT(new String[] { "batch qa context", "batch qa context" },  RULES_CONTEXT),
        TEMPLATE_CONTEXT(new String[] { "template context", "template context" },  RULES_CONTEXT),;		

        private ArrayList<UUID> conceptUids = new ArrayList<UUID>();

        private Boolean primitive = true;

        private UniversalFixedRel[] rels;

        private UniversalFixedDescription[] descriptions;

        public String[] parents_S;
        public String[] descriptions_S;

        public String[] getParents_S() {
            return parents_S;
        }

        public String[] getDescriptions_S() {
            return descriptions_S;
        }

		@Override
		public UUID getPrimoridalUid() throws IOException, TerminologyException {
			return conceptUids.get(0);
		}

        private Concept(String[] descriptions) {
            this(descriptions, new I_ConceptualizeUniversally[] {});
        }

        private Concept(String[] descriptions, I_ConceptualizeUniversally parent) {
            this(descriptions, new I_ConceptualizeUniversally[] { parent });
        }

        private Concept(String[] descriptionStrings, I_ConceptualizeUniversally[] parents) {
            this.conceptUids.add(Type3UuidFactory.fromEnum(this));
            if (parents.length > 0) {
                parents_S = new String[parents.length];
                for (int i = 0; i < parents.length; i++) {
                    parents_S[i] = parents[i].toString();
                }
            }
            if (descriptionStrings.length > 0) {
                descriptions_S = descriptionStrings;
            }

            try {
                this.rels = DocumentAuxiliary.makeRels(this, parents);
                this.descriptions = DocumentAuxiliary.makeDescriptions(this, descriptionStrings, descTypeOrder);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private Concept(String[] descriptionStrings, I_ConceptualizeUniversally[] parents, UUID primaryUuid) {
            if (primaryUuid != null) {
                this.conceptUids.add(primaryUuid);
            } else {
                this.conceptUids.add(Type3UuidFactory.fromEnum(this));
            }
            try {
                this.rels = DocumentAuxiliary.makeRels(this, parents);
                this.descriptions = DocumentAuxiliary.makeDescriptions(this, descriptionStrings, descTypeOrder);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private Concept(String[] descriptionStrings, I_ConceptualizeUniversally[] relDestinations,
                I_ConceptualizeUniversally[] relTypes) {
            this.conceptUids.add(Type3UuidFactory.fromEnum(this));
            try {
                this.rels = DocumentAuxiliary.makeRels(this, relDestinations, relTypes);
                this.descriptions = DocumentAuxiliary.makeDescriptions(this, descriptionStrings, descTypeOrder);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public boolean isPrimitive(I_StoreUniversalFixedTerminology server) {
            return true;
        }

        public Collection<UUID> getUids() {
            return conceptUids;
        }

        public boolean isUniversal() {
            return true;
        }

        public I_ManifestLocally localize(I_StoreUniversalFixedTerminology server) {
            throw new UnsupportedOperationException();
        }

        public I_DescribeConceptUniversally getDescription(List<I_ConceptualizeUniversally> typePriorityList,
                I_StoreUniversalFixedTerminology termStore) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_DescribeConceptUniversally> getDescriptions(I_StoreUniversalFixedTerminology server) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_ConceptualizeUniversally> getDestRelConcepts(I_StoreUniversalFixedTerminology server) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_ConceptualizeUniversally> getDestRelConcepts(Collection<I_ConceptualizeUniversally> types,
                I_StoreUniversalFixedTerminology termStore) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_RelateConceptsUniversally> getDestRels(I_StoreUniversalFixedTerminology server) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_RelateConceptsUniversally> getSourceRels(I_StoreUniversalFixedTerminology server) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_ConceptualizeUniversally> getSrcRelConcepts(I_StoreUniversalFixedTerminology server) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_ConceptualizeUniversally> getSrcRelConcepts(Collection<I_ConceptualizeUniversally> types,
                I_StoreUniversalFixedTerminology termStore) {
            throw new UnsupportedOperationException();
        }

        public I_ManifestUniversally getExtension(I_ConceptualizeUniversally extensionType,
                I_StoreUniversalFixedTerminology extensionServer) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_RelateConceptsUniversally> getDestRels(Collection<I_ConceptualizeUniversally> types,
                I_StoreUniversalFixedTerminology termStore) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_RelateConceptsUniversally> getSourceRels(Collection<I_ConceptualizeUniversally> types,
                I_StoreUniversalFixedTerminology termStore) {
            throw new UnsupportedOperationException();
        }

        public I_ConceptualizeLocally localize() throws IOException, TerminologyException {
            return LocalFixedConcept.get(getUids(), primitive);
        }
    }

    private static I_ConceptualizeUniversally[] descTypeOrder = {
        ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE,
        ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE, ArchitectonicAuxiliary.Concept.EXTENSION_TABLE };

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.cement.I_AddToMemoryTermServer#addToMemoryTermServer(org.dwfa
     * .cement.MemoryTermServer)
     */
    public void addToMemoryTermServer(MemoryTermServer server) throws Exception {
        server.addRoot(Concept.REFSET_AUXILIARY);
        for (Concept s : Concept.values()) {
            server.add(s);
            for (I_DescribeConceptUniversally d : s.descriptions) {
                server.add(d);
            }
            for (I_RelateConceptsUniversally r : s.rels) {
                server.add(r);
            }
        }
    }
}
