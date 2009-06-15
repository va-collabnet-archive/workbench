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

	public enum Concept implements I_ConceptualizeUniversally {
		REFSET_AUXILIARY(new String[] { "Refset Auxiliary Concept", "Refset Auxiliary Concept"}),
			REFSET_SPEC(new String[] { "refset specification concept", "refset spec"}, REFSET_AUXILIARY),
            	SPEC_GROUPING(new String[] { "Refset specification grouping", "Refset specification grouping"}, REFSET_SPEC),
            		REFSET_AND_GROUPING(new String[] { "AND", "AND"}, SPEC_GROUPING),
            		REFSET_OR_GROUPING(new String[] { "OR", "OR"}, SPEC_GROUPING),
            		CONCEPT_CONTAINS_REL_GROUPING(new String[] { "CONCEPT-CONTAINS-REL", "CONCEPT-CONTAINS-REL"}, SPEC_GROUPING),
            		CONCEPT_CONTAINS_DESC_GROUPING(new String[] { "CONCEPT-CONTAINS-DESC", "CONCEPT-CONTAINS-DESC"}, SPEC_GROUPING),
            	SPEC_QUERY_TOKEN(new String[] { "Refset specification token", "Refset specification token"}, REFSET_SPEC),
            		CONCEPT_IS_MEMBER_OF(new String[] { "CONCEPT IS MEMBER OF", "concept is member of"}, SPEC_QUERY_TOKEN),
            		CONCEPT_STATUS_IS(new String[] { "CONCEPT STATUS IS", "concept status is"}, SPEC_QUERY_TOKEN),
            		CONCEPT_STATUS_IS_KIND_OF(new String[] { "CONCEPT STATUS IS KIND OF", "concept status is kind of"}, SPEC_QUERY_TOKEN),
            		CONCEPT_IS(new String[] { "CONCEPT IS", "concept is"}, SPEC_QUERY_TOKEN),
            		CONCEPT_IS_CHILD_OF(new String[] { "CONCEPT IS CHILD OF", "concept is child of"}, SPEC_QUERY_TOKEN),
            		CONCEPT_IS_DESCENDENT_OF(new String[] { "CONCEPT IS DESCENDENT OF", "concept is descendent of"}, SPEC_QUERY_TOKEN),
            		CONCEPT_IS_KIND_OF(new String[] { "CONCEPT IS KIND OF", "concept is kind of"}, SPEC_QUERY_TOKEN),
            		DESC_IS_MEMBER_OF(new String[] { "DESC IS MEMBER OF", "desc is member of"}, SPEC_QUERY_TOKEN),
            		DESC_STATUS_IS(new String[] { "DESC STATUS IS", "desc status is"}, SPEC_QUERY_TOKEN),
            		DESC_STATUS_IS_KIND_OF(new String[] { "DESC STATUS IS KIND OF", "desc status is kind of"}, SPEC_QUERY_TOKEN),
            		DESC_TYPE_IS(new String[] { "DESC TYPE IS", "desc type is"}, SPEC_QUERY_TOKEN),
            		DESC_TYPE_IS_KIND_OF(new String[] { "DESC TYPE IS KIND OF", "desc type is kind of"}, SPEC_QUERY_TOKEN),
            		DESC_REGEX_MATCH(new String[] { "DESC REGEX MATCH", "desc regex match"}, SPEC_QUERY_TOKEN),
            		DESC_LUCENE_MATCH(new String[] { "DESC LUCENE MATCH", "desc lucene match"}, SPEC_QUERY_TOKEN),
            		REL_IS_MEMBER_OF(new String[] { "REL IS MEMBER OF", "rel is member of"}, SPEC_QUERY_TOKEN),
            		REL_STATUS_IS(new String[] { "REL STATUS IS", "rel status is"}, SPEC_QUERY_TOKEN),
            		REL_STATUS_IS_KIND_OF(new String[] { "REL STATUS IS KIND OF", "rel status is kind of"}, SPEC_QUERY_TOKEN),
            		REL_TYPE_IS(new String[] { "REL TYPE IS", "rel type is"}, SPEC_QUERY_TOKEN),
            		REL_TYPE_IS_KIND_OF(new String[] { "REL TYPE IS KIND OF", "rel type is kind of"}, SPEC_QUERY_TOKEN),
            		REL_LOGICAL_QUANTIFIER_IS(new String[] { "REL LOGICAL QUANTIFIER IS", "rel logical quantifier is"}, SPEC_QUERY_TOKEN),
            		REL_LOGICAL_QUANTIFIER_IS_KIND_OF(new String[] { "REL LOGICAL QUANTIFIER IS KIND OF", "rel logical quantifier is kind of"}, SPEC_QUERY_TOKEN),
            		REL_CHARACTERISTIC_IS(new String[] { "REL CHARACTERISTIC IS", "rel characteristic is"}, SPEC_QUERY_TOKEN),
            		REL_CHARACTERISTIC_IS_KIND_OF(new String[] { "REL CHARACTERISTIC IS KIND OF", "rel characteristic is kind of"}, SPEC_QUERY_TOKEN),
            		REL_REFINABILITY_IS(new String[] { "REL REFINABILITY IS", "rel refinability is"}, SPEC_QUERY_TOKEN),
            		REL_REFINABILITY_IS_KIND_OF(new String[] { "REL REFINABILITY IS KIND OF", "rel refinability is kind of"}, SPEC_QUERY_TOKEN),
			REFSET_TYPE(new String[] { "refset type", "refset type"}, REFSET_AUXILIARY),
                BOOLEAN_EXTENSION(new String[] { "boolean extension by reference", "boolean extension"}, REFSET_TYPE),
                STRING_EXTENSION(new String[] { "string extension by reference", "string extension"}, REFSET_TYPE),
                INT_EXTENSION(new String[] { "int extension by reference", "int extension"}, REFSET_TYPE),
                CONCEPT_EXTENSION(new String[] { "concept extension by reference", "concept extension"}, REFSET_TYPE),
                CONCEPT_CONCEPT_EXTENSION(new String[] { "concept-concept extension by reference", "concept-concept extension"}, REFSET_TYPE),
                CONCEPT_STRING_EXTENSION(new String[] { "concept-string extension by reference", "concept-string extension"}, REFSET_TYPE),
                CONCEPT_CONCEPT_CONCEPT_EXTENSION(new String[] { "concept-concept-concept extension by reference", "concept-concept-concept extension"}, REFSET_TYPE),
                CONCEPT_CONCEPT_STRING_EXTENSION(new String[] { "concept-concept-string extension by reference", "concept-concept-string extension"}, REFSET_TYPE),
                CONCEPT_INT_EXTENSION(new String[] { "concept int extension by reference", "concept int extension"}, REFSET_TYPE),
                MEASUREMENT_EXTENSION(new String[] { "measurement extension by reference", "measurement extension"}, REFSET_TYPE),
                LANGUAGE_EXTENSION(new String[] { "language extension by reference", "language extension"}, REFSET_TYPE),
                SCOPED_LANGUAGE_EXTENSION(new String[] { "scoped language extension by reference", "scoped language extension"}, REFSET_TYPE),
                CROSS_MAP_REL_EXTENSION(new String[] { "cross map relationship extenstion", "cross map for rel"}, REFSET_TYPE),
                CROSS_MAP_EXTENSION(new String[] { "cross map extension", "cross map"}, REFSET_TYPE),
                TEMPLATE_REL_EXTENSION(new String[] { "template relationship extension", "template for rel"}, REFSET_TYPE),
                TEMPLATE_EXTENSION(new String[] { "template extension", "template"}, REFSET_TYPE),
                
            BOOLEAN_CIRCLE_ICONS(new String[] { "boolean with circle icon", "boolean with circle"}, REFSET_AUXILIARY),
                BOOLEAN_CIRCLE_ICONS_TRUE(new String[] { "true with circle check icon", "true"}, BOOLEAN_CIRCLE_ICONS),
                BOOLEAN_CIRCLE_ICONS_FALSE(new String[] { "false with forbidden icon", "false"}, BOOLEAN_CIRCLE_ICONS),
            BOOLEAN_CHECK_CROSS_ICONS(new String[] { "boolean with check or cross icon", "boolean with check or cross"}, REFSET_AUXILIARY),
                BOOLEAN_CHECK_CROSS_ICONS_TRUE(new String[] { "true with check icon", "true"}, BOOLEAN_CHECK_CROSS_ICONS),
                BOOLEAN_CHECK_CROSS_ICONS_FALSE(new String[] { "false with cross icon", "false"}, BOOLEAN_CHECK_CROSS_ICONS),
            INCLUSION_SPECIFICATION_TYPE(new String[] { "inclusion specification type", "inclusion type"}, REFSET_AUXILIARY),
                INCLUDE_INDIVIDUAL(new String[] { "include concept no children", "individual include"}, INCLUSION_SPECIFICATION_TYPE),
                INCLUDE_LINEAGE(new String[] { "include concept with children", "lineage include"}, INCLUSION_SPECIFICATION_TYPE),
                EXCLUDE_INDIVIDUAL(new String[] { "exclude concept", "exclude individual"}, INCLUSION_SPECIFICATION_TYPE),
                EXCLUDE_LINEAGE(new String[] { "exclude concept and children", "exclude lineage"}, INCLUSION_SPECIFICATION_TYPE),
            LINGUISTIC_ROLE_TYPE(new String[] { "linguistic role type", "linguistic role type"}, REFSET_AUXILIARY),
                ATTRIBUTE_LINGUISTIC_ROLE(new String[] { "attribute linguistic role", "attribute"}, LINGUISTIC_ROLE_TYPE),
                NON_ATTRIBUTE_LINGUISTIC_ROLE(new String[] { "non-attribute linguistic role", "non-attribute"}, LINGUISTIC_ROLE_TYPE),
			REFSET_PURPOSE(new String[] { "refset purpose", "refset purpose"}, REFSET_AUXILIARY),
                RELATIONSHIP_ORDER(new String[] { "relationship order", "relationship order"}, REFSET_PURPOSE),
                ANNOTATION_ORDER(new String[] { "annotation", "annotation"}, REFSET_PURPOSE),
                MEASUREMENT_ASSOCIATION(new String[] { "measurement association", "measurement association"}, REFSET_PURPOSE),
                INCLUSION_SPECIFICATION(new String[] { "inclusion specification", "inclusion specification"}, REFSET_PURPOSE),
                LINGUISTIC_ROLE(new String[] { "linguistic role", "lingiustic role"}, REFSET_PURPOSE),
                SUBJECT_TYPE(new String[] { "subject type", "subject type"}, REFSET_PURPOSE),
                INDEX_KEYS(new String[] { "index key", "index key"}, REFSET_PURPOSE),
                ANCILLARY_DATA(new String[] { "ancillary data", "ancillary data"}, REFSET_PURPOSE),
            TEMPLATE_VALUE_TYPE(new String[] { "template value type", "value type"}, REFSET_AUXILIARY),
                TEMPLATE_CODE_VALUE_TYPE(new String[] { "template code value type", "code"}, TEMPLATE_VALUE_TYPE),
                TEMPLATE_NUMBER_VALUE_TYPE(new String[] { "template number value type", "number"}, TEMPLATE_VALUE_TYPE),
                TEMPLATE_DATE_VALUE_TYPE(new String[] { "template date value type", "date"}, TEMPLATE_VALUE_TYPE),
            TEMPLATE_SEMANTIC_STATUS(new String[] { "template semantic status", "template semantic status"}, REFSET_AUXILIARY),
                TEMPLATE_FINAL_SEMANTIC_STATUS(new String[] { "template final semantic status", "final"}, TEMPLATE_SEMANTIC_STATUS),
                TEMPLATE_REFINABLE_SEMANTIC_STATUS(new String[] { "template refinable semantic status", "refinable"}, TEMPLATE_SEMANTIC_STATUS),
                TEMPLATE_NUMERIC_QUALIFIER_REFINE_SEMANTIC_STATUS(new String[] { "template numeric qualifier refine semantic status", "numeric qualifier"}, TEMPLATE_SEMANTIC_STATUS),
                TEMPLATE_MANDATORY_TO_REFINE_SEMANTIC_STATUS(new String[] { "template mandatory refinement semantic status", "mandatory refinement"}, TEMPLATE_SEMANTIC_STATUS),
                TEMPLATE_CHILD_REFINE_SEMANTIC_STATUS(new String[] { "template child refine semantic status", "child refinement"}, TEMPLATE_SEMANTIC_STATUS),
                TEMPLATE_QUALIFIER_REFINE_SEMANTIC_STATUS(new String[] { "template qualifier refine semantic status", "qualifier refine"}, TEMPLATE_SEMANTIC_STATUS),
                TEMPLATE_UNSPECIFIED_SEMANTIC_STATUS(new String[] { "template unspecified semantic status", "unspecified"}, TEMPLATE_SEMANTIC_STATUS),
            TEMPLATE_ATTRIBUTE_DISPLAY_STATUS(new String[] { "template attribute display status", "attribute display status"}, REFSET_AUXILIARY),
                TEMPLATE_ATTRIBUTE_DISPLAYED(new String[] { "template attribute displayed", "attribute displayed"}, TEMPLATE_ATTRIBUTE_DISPLAY_STATUS),
                TEMPLATE_ATTRIBUTE_HIDDEN(new String[] { "template attribute hidden", "attribute hidden"}, TEMPLATE_ATTRIBUTE_DISPLAY_STATUS),
                TEMPLATE_ATTRIBUTE_UNSPECIFIED(new String[] { "template attribute unspecified", "attribute unspecified"}, TEMPLATE_ATTRIBUTE_DISPLAY_STATUS),
            TEMPLATE_CHARACTERSITIC_STATUS(new String[] { "template characteristic status", "template characteristic status"}, REFSET_AUXILIARY),
                TEMPLATE_CHARACTERSITIC_QUALIFIER(new String[] { "template characteristic qualifier", "qualifier"}, TEMPLATE_CHARACTERSITIC_STATUS),
                TEMPLATE_CHARACTERSITIC_ATOM(new String[] { "template characteristic atom", "atom"}, TEMPLATE_CHARACTERSITIC_STATUS),
                TEMPLATE_CHARACTERSITIC_FACT(new String[] { "template characteristic fact", "fact"}, TEMPLATE_CHARACTERSITIC_STATUS),
            REFINABILITY_FLAG(new String[] { "refinability flag", "refinability flag"}, REFSET_AUXILIARY),
                COMPLETE_REFINABILITY_FLAG(new String[] { "refinability complete", "refinability complete"}, REFINABILITY_FLAG),
                MANDATORY_REFINABILITY_FLAG(new String[] { "refinability mandatory", "refinability mandatory"}, REFINABILITY_FLAG),
                POSSIBLE_REFINABILITY_FLAG(new String[] { "refinability possible", "refinability possible"}, REFINABILITY_FLAG),
            ADDITIONAL_CODE_FLAG(new String[] { "additional code flag", "additional code flag"}, REFSET_AUXILIARY),
                COMPLETE_ADDITIONAL_CODE_FLAG(new String[] { "additional code complete", "additional code complete"}, ADDITIONAL_CODE_FLAG),
                MANDATORY_ADDITIONAL_CODE_FLAG(new String[] { "additional code mandatory", "additional code mandatory"}, ADDITIONAL_CODE_FLAG),
                POSSIBLE_ADDITIONAL_CODE_FLAG(new String[] { "additional code possible", "additional code possible"}, ADDITIONAL_CODE_FLAG),
            MAP_STATUS(new String[] { "map status", "map status"}, REFSET_AUXILIARY),
                EXACT_MAP_STATUS(new String[] { "exact map status", "exact map status"}, MAP_STATUS),
                GENERAL_MAP_STATUS(new String[] { "general map status", "general map status"}, MAP_STATUS),
                DEFAULT_MAP_STATUS(new String[] { "default map status", "default map status"}, MAP_STATUS),
                REQUIRES_CHECKING_MAP_STATUS(new String[] { "requires checking map status", "requires checking exact map status"}, MAP_STATUS),
                ALTERNATIVE_MAP_STATUS(new String[] { "alternative map status", "alternative map status"}, MAP_STATUS),
                UNMAPPABLE_MAP_STATUS(new String[] { "unmappable map status", "unmappable map status"}, MAP_STATUS),
			REFSET_RELATIONSHIP(new String[] { "refset relationship", "refset relationship" }, 
					new I_ConceptualizeUniversally [] { 
						ArchitectonicAuxiliary.Concept.RELATIONSHIP, 
						REFSET_AUXILIARY}),
				REFSET_TYPE_REL(new String[] { "refset type rel", "refset type rel"}, REFSET_RELATIONSHIP),
				REFSET_PURPOSE_REL(new String[] { "refest purpose rel", "refest purpose rel"}, REFSET_RELATIONSHIP),
				SPECIFIES_REFSET(new String[] { "specifies refset", "specifies refset"}, REFSET_RELATIONSHIP),
			REFSET_IDENTITY(new String[] { "refset identity", "refset"}, REFSET_AUXILIARY),
            
                PATHOLOGY_INCLUSION_SPEC(new String[] { "pathology inclusion specification","pathology inclusion specification",
                    "ORG_DWFA_PATHOLOGY_INCLUSION_SPEC"}, 
                    new I_ConceptualizeUniversally [] { REFSET_IDENTITY,
                        INCLUSION_SPECIFICATION, CONCEPT_EXTENSION}, 
                        new I_ConceptualizeUniversally [] { 
                        ArchitectonicAuxiliary.Concept.IS_A_REL,
                        REFSET_PURPOSE_REL, REFSET_TYPE_REL
                    }),
                    
                DISCHARGE_INCLUSION_SPEC(new String[] { "discharge inclusion specification","discharge inclusion specification",
                    "ORG_DWFA_DISCHARGE_INCLUSION_SPEC"}, 
                    new I_ConceptualizeUniversally [] { REFSET_IDENTITY,
                        INCLUSION_SPECIFICATION, CONCEPT_EXTENSION}, 
                        new I_ConceptualizeUniversally [] { 
                        ArchitectonicAuxiliary.Concept.IS_A_REL,
                        REFSET_PURPOSE_REL, REFSET_TYPE_REL
                    }),
                    
                ALLERGY_RXN_INCLUSION_SPEC(new String[] { "allergy & adverse reaction inclusion specification",
                        "allergy & adverse reaction inclusion specification",
                    "ORG_DWFA_ALLERGY_RXN_INCLUSION_SPEC"}, 
                    new I_ConceptualizeUniversally [] { REFSET_IDENTITY,
                        INCLUSION_SPECIFICATION, CONCEPT_EXTENSION}, 
                        new I_ConceptualizeUniversally [] { 
                        ArchitectonicAuxiliary.Concept.IS_A_REL,
                        REFSET_PURPOSE_REL, REFSET_TYPE_REL
                    }),

                DOCUMENT_SECTION_ORDER(new String[] { "document section order","document section order",
													  "ORG_DWFA_DOC_SECTION_ORDER"}, 
						new I_ConceptualizeUniversally [] { REFSET_IDENTITY,
						RELATIONSHIP_ORDER, INT_EXTENSION}, 
						new I_ConceptualizeUniversally [] { 
						ArchitectonicAuxiliary.Concept.IS_A_REL,
						REFSET_PURPOSE_REL, REFSET_TYPE_REL
					}),
		
		        CTV3_REL_ORDER(new String[] { "Clinical Terms Version 3 relationship order","CTV3 rel order",
                                              "ORG_DWFA_CTV3_REL_ORDER"}, 
                       new I_ConceptualizeUniversally [] { REFSET_IDENTITY,
		                                                   RELATIONSHIP_ORDER, INT_EXTENSION}, 
		               new I_ConceptualizeUniversally [] { ArchitectonicAuxiliary.Concept.IS_A_REL,
		                                                   REFSET_PURPOSE_REL, REFSET_TYPE_REL}),

		        CTV3_LINGUISTIC_ROLE(new String[] { "Clinical Terms Version 3 linguistic role","CTV3 linguistic role",
		                                                   "ORG_DWFA_CTV3_LINGUISTIC_ROLE"}, 
		                            new I_ConceptualizeUniversally [] { REFSET_IDENTITY,
		                                                                LINGUISTIC_ROLE, CONCEPT_EXTENSION}, 
		                            new I_ConceptualizeUniversally [] { ArchitectonicAuxiliary.Concept.IS_A_REL,
		                                                                REFSET_PURPOSE_REL, REFSET_TYPE_REL}),
		                 
		        CTV3_SUBJECT_TYPE(new String[] { "Clinical Terms Version 3 subject type","CTV3 subject type",
		                                                                "ORG_DWFA_CTV3_SUBJECT_TYPE"}, 
		                             new I_ConceptualizeUniversally [] { REFSET_IDENTITY,
		                                                                 SUBJECT_TYPE, CONCEPT_EXTENSION}, 
		                             new I_ConceptualizeUniversally [] { ArchitectonicAuxiliary.Concept.IS_A_REL,
		                                                                 REFSET_PURPOSE_REL, REFSET_TYPE_REL}),
		                                                   
		       CTV3_WORD_KEYS(new String[] { "Clinical Terms Version 3 Word Key","CTV3 word key",
	                                                                        "ORG_DWFA_CTV3_WORD_KEYS"}, 
	                                     new I_ConceptualizeUniversally [] { REFSET_IDENTITY,
	                                                                         INDEX_KEYS, STRING_EXTENSION}, 
	                                     new I_ConceptualizeUniversally [] { ArchitectonicAuxiliary.Concept.IS_A_REL,
	                                                                         REFSET_PURPOSE_REL, REFSET_TYPE_REL}),

	           CTV3_PARTIAL_WORD_KEYS(new String[] { "Clinical Terms Version 3 Partial Word Key","CTV3 parital word key",
	                                                                            "ORG_DWFA_CTV3_PARTIAL_WORD_KEYS"}, 
	                                         new I_ConceptualizeUniversally [] { REFSET_IDENTITY,
	                                                                             INDEX_KEYS, STRING_EXTENSION}, 
	                                         new I_ConceptualizeUniversally [] { ArchitectonicAuxiliary.Concept.IS_A_REL,
	                                                                             REFSET_PURPOSE_REL, REFSET_TYPE_REL}),
	                                                                         
	            CTV3_ACRONYM_KEYS(new String[] { "Clinical Terms Version 3 Acronym Key","CTV3 acronym key",
	                                                                             "ORG_DWFA_CTV3_ACRONYM_KEYS"}, 
	                                          new I_ConceptualizeUniversally [] { REFSET_IDENTITY,
	                                                                              INDEX_KEYS, STRING_EXTENSION}, 
	                                          new I_ConceptualizeUniversally [] { ArchitectonicAuxiliary.Concept.IS_A_REL,
	                                                                              REFSET_PURPOSE_REL, REFSET_TYPE_REL}),

	            CTV3_TEMPLATE_VALUE_TYPE(new String[] { "Clinical Terms Version 3 Template Value Type","template value type",
	                                                                                 "ORG_DWFA_CTV3_TEMPLATE_VALUE_TYPE"}, 
	                                              new I_ConceptualizeUniversally [] { REFSET_IDENTITY,
	                                                                                  ANCILLARY_DATA, CONCEPT_EXTENSION}, 
	                                              new I_ConceptualizeUniversally [] { ArchitectonicAuxiliary.Concept.IS_A_REL,
	                                                                                  REFSET_PURPOSE_REL, REFSET_TYPE_REL}),
	                                                                              
	            CTV3_TEMPLATE_CARDINALITY(new String[] { "Clinical Terms Version 3 Template Cardinality","template cardinality",
	                                                                                     "ORG_DWFA_CTV3_TEMPLATE_CARDINALITY"}, 
	                                                  new I_ConceptualizeUniversally [] { REFSET_IDENTITY,
	                                                                                      ANCILLARY_DATA, INT_EXTENSION}, 
	                                                  new I_ConceptualizeUniversally [] { ArchitectonicAuxiliary.Concept.IS_A_REL,
	                                                                                      REFSET_PURPOSE_REL, REFSET_TYPE_REL}),
	                                                                                  
	                                                                                      
                  CTV3_TEMPLATE_SEMANTIC_STATUS(new String[] { "Clinical Terms Version 3 Template Semantic Status","template semantic status",
                                                                                           "ORG_DWFA_CTV3_TEMPLATE_SEMANTIC_STATUS"}, 
                                                        new I_ConceptualizeUniversally [] { REFSET_IDENTITY,
                                                                                            ANCILLARY_DATA, CONCEPT_EXTENSION}, 
                                                        new I_ConceptualizeUniversally [] { ArchitectonicAuxiliary.Concept.IS_A_REL,
                                                                                            REFSET_PURPOSE_REL, REFSET_TYPE_REL}),
                  CTV3_TEMPLATE_BROWSE_ATTRIBUTE(new String[] { "Clinical Terms Version 3 Template Browse Attribute Order","template browse attribute order",
                                                                                            "ORG_DWFA_CTV3_TEMPLATE_BROWSE_ATTRIBUTE"}, 
                                                         new I_ConceptualizeUniversally [] { REFSET_IDENTITY,
                                                                                             ANCILLARY_DATA, INT_EXTENSION}, 
                                                         new I_ConceptualizeUniversally [] { ArchitectonicAuxiliary.Concept.IS_A_REL,
                                                                                             REFSET_PURPOSE_REL, REFSET_TYPE_REL}),
                  CTV3_TEMPLATE_BROWSE_VALUE(new String[] { "Clinical Terms Version 3 Template Browse Value Order","template browse value order",
                                                                                             "ORG_DWFA_CTV3_TEMPLATE_BROWSE_VALUE"}, 
                                                          new I_ConceptualizeUniversally [] { REFSET_IDENTITY,
                                                                                              ANCILLARY_DATA, INT_EXTENSION}, 
                                                          new I_ConceptualizeUniversally [] { ArchitectonicAuxiliary.Concept.IS_A_REL,
                                                                                              REFSET_PURPOSE_REL, REFSET_TYPE_REL}),
                 CTV3_TEMPLATE_NOTES_SCREEN(new String[] { "Clinical Terms Version 3 Template Notes Screen Order","template notes screen order",
                                                                                              "ORG_DWFA_CTV3_TEMPLATE_NOTES_SCREEN"}, 
                                                           new I_ConceptualizeUniversally [] { REFSET_IDENTITY,
                                                                                               ANCILLARY_DATA, INT_EXTENSION}, 
                                                           new I_ConceptualizeUniversally [] { ArchitectonicAuxiliary.Concept.IS_A_REL,
                                                                                               REFSET_PURPOSE_REL, REFSET_TYPE_REL}),
                 CTV3_TEMPLATE_DISPLAY_STATUS(new String[] { "Clinical Terms Version 3 Template Attribute Display Status","template attribute display status",
                                                                                               "ORG_DWFA_CTV3_TEMPLATE_DISPLAY_STATUS"}, 
                                                            new I_ConceptualizeUniversally [] { REFSET_IDENTITY,
                                                                                                ANCILLARY_DATA, CONCEPT_EXTENSION}, 
                                                            new I_ConceptualizeUniversally [] { ArchitectonicAuxiliary.Concept.IS_A_REL,
                                                                                                REFSET_PURPOSE_REL, REFSET_TYPE_REL}),
                 CTV3_TEMPLATE_CHARACTERISTIC_STATUS(new String[] { "Clinical Terms Version 3 Template Characteristic Display Status","template characteristic status",
                                                                                                "ORG_DWFA_CTV3_TEMPLATE_CHARACTERISTIC_STATUS"}, 
                                                             new I_ConceptualizeUniversally [] { REFSET_IDENTITY,
                                                                                                 ANCILLARY_DATA, CONCEPT_EXTENSION}, 
                                                             new I_ConceptualizeUniversally [] { ArchitectonicAuxiliary.Concept.IS_A_REL,
                                                                                                 REFSET_PURPOSE_REL, REFSET_TYPE_REL}),
//	                                                                                                                                                            
                CTV3_TEMPLATE_FOR_REL(new String[] { "Clinical Terms Version 3 Template for rel","CTV3 template for rel",
                                                                                                 "ORG_DWFA_CTV3_TEMPLATE_FOR_REL"}, 
                                                              new I_ConceptualizeUniversally [] { REFSET_IDENTITY,
                                                                                                  ANCILLARY_DATA, TEMPLATE_REL_EXTENSION}, 
                                                              new I_ConceptualizeUniversally [] { ArchitectonicAuxiliary.Concept.IS_A_REL,
                                                                                                  REFSET_PURPOSE_REL, REFSET_TYPE_REL}),
	                                                                              
                CTV3_TEMPLATE(new String[] { "Clinical Terms Version 3 Template","CTV3 template",
                                                                                                  "ORG_DWFA_CTV3_TEMPLATE"}, 
                                                               new I_ConceptualizeUniversally [] { REFSET_IDENTITY,
                                                                                                   ANCILLARY_DATA, TEMPLATE_EXTENSION}, 
                                                               new I_ConceptualizeUniversally [] { ArchitectonicAuxiliary.Concept.IS_A_REL,
                                                                                                   REFSET_PURPOSE_REL, REFSET_TYPE_REL}),
                                                                                   
                CTV3_CROSS_MAP_FOR_REL(new String[] { "Clinical Terms Version 3 Cross Map for rel","CTV3 cross map for rel",
                                                                                                   "ORG_DWFA_CTV3_CROSS_MAP_FOR_REL"}, 
                                                                new I_ConceptualizeUniversally [] { REFSET_IDENTITY,
                                                                                                    ANCILLARY_DATA, CROSS_MAP_REL_EXTENSION}, 
                                                                new I_ConceptualizeUniversally [] { ArchitectonicAuxiliary.Concept.IS_A_REL,
                                                                                                    REFSET_PURPOSE_REL, REFSET_TYPE_REL}),
                                                                                    
               CTV3_CROSS_MAP(new String[] { "Clinical Terms Version 3 Cross Map","CTV3 cross map",
                                                                                                    "ORG_DWFA_CTV3_CROSS_MAP"}, 
                                                                 new I_ConceptualizeUniversally [] { REFSET_IDENTITY,
                                                                                                     ANCILLARY_DATA, CROSS_MAP_EXTENSION}, 
                                                                 new I_ConceptualizeUniversally [] { ArchitectonicAuxiliary.Concept.IS_A_REL,
                                                                                                     REFSET_PURPOSE_REL, REFSET_TYPE_REL}),
                                                                                     
	                                                                              ;
		
		private Collection<UUID> conceptUids = new ArrayList<UUID>();
		
		private Boolean primitive = true;
		
		private UniversalFixedRel[] rels;
		
		private UniversalFixedDescription[] descriptions;
		
      private Concept(String[] descriptions) {
         this(descriptions, new I_ConceptualizeUniversally[] { });
      }
      private Concept(String[] descriptions, I_ConceptualizeUniversally parent) {
         this(descriptions, new I_ConceptualizeUniversally[] {parent});
      }
		private Concept(String[] descriptionStrings, I_ConceptualizeUniversally[] parents) {
			this.conceptUids.add(Type3UuidFactory.fromEnum(this)); 
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


		public I_DescribeConceptUniversally getDescription(List<I_ConceptualizeUniversally> typePriorityList, I_StoreUniversalFixedTerminology termStore) {
			throw new UnsupportedOperationException();
		}

		public Collection<I_DescribeConceptUniversally> getDescriptions(I_StoreUniversalFixedTerminology server) {
			throw new UnsupportedOperationException();
		}



		public Collection<I_ConceptualizeUniversally> getDestRelConcepts(I_StoreUniversalFixedTerminology server) {
			throw new UnsupportedOperationException();
		}



		public Collection<I_ConceptualizeUniversally> getDestRelConcepts(
				Collection<I_ConceptualizeUniversally> types, I_StoreUniversalFixedTerminology termStore) {
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



		public Collection<I_ConceptualizeUniversally> getSrcRelConcepts(
				Collection<I_ConceptualizeUniversally> types, I_StoreUniversalFixedTerminology termStore) {
			throw new UnsupportedOperationException();
		}



		public I_ManifestUniversally getExtension(I_ConceptualizeUniversally extensionType, I_StoreUniversalFixedTerminology extensionServer) {
			throw new UnsupportedOperationException();
		}

		public Collection<I_RelateConceptsUniversally> getDestRels(Collection<I_ConceptualizeUniversally> types, I_StoreUniversalFixedTerminology termStore) {
			throw new UnsupportedOperationException();
		}
		public Collection<I_RelateConceptsUniversally> getSourceRels(Collection<I_ConceptualizeUniversally> types, I_StoreUniversalFixedTerminology termStore) {
			throw new UnsupportedOperationException();
		}


		public I_ConceptualizeLocally localize() throws IOException, TerminologyException {
			return LocalFixedConcept.get(getUids(), primitive);
		}
	}	
	private static I_ConceptualizeUniversally[] descTypeOrder = { 
      ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE,
      ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE,
		ArchitectonicAuxiliary.Concept.EXTENSION_TABLE};
	/* (non-Javadoc)
	 * @see org.dwfa.cement.I_AddToMemoryTermServer#addToMemoryTermServer(org.dwfa.cement.MemoryTermServer)
	 */
	public void addToMemoryTermServer(MemoryTermServer server) throws Exception {
		server.addRoot(Concept.REFSET_AUXILIARY);
		for (Concept s: Concept.values()) {
			server.add(s);
			for (I_DescribeConceptUniversally d: s.descriptions) {
				server.add(d);
			}
			for (I_RelateConceptsUniversally r: s.rels) {
				server.add(r);
			}
		}
	}
}
