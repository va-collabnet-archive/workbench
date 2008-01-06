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
			REFSET_TYPE(new String[] { "refset type", "refset type"}, REFSET_AUXILIARY),
                BOOLEAN_EXTENSION(new String[] { "boolean extension by reference", "boolean extensions"}, REFSET_TYPE),
                STRING_EXTENSION(new String[] { "string extension by reference", "string extensions"}, REFSET_TYPE),
                INT_EXTENSION(new String[] { "int extension by reference", "int extensions"}, REFSET_TYPE),
                CONCEPT_EXTENSION(new String[] { "concept extension by reference", "concept extensions"}, REFSET_TYPE),
                CONCEPT_INT_EXTENSION(new String[] { "concept int extension by reference", "concept int extensions"}, REFSET_TYPE),
                MEASUREMENT_EXTENSION(new String[] { "measurement extension by reference", "measurement extensions"}, REFSET_TYPE),
                LANGUAGE_EXTENSION(new String[] { "language extension by reference", "language extensions"}, REFSET_TYPE),
                SCOPED_LANGUAGE_EXTENSION(new String[] { "scoped language extension by reference", "scoped language extensions"}, REFSET_TYPE),
                CROSS_MAP_REL_EXTENSION(new String[] { "cross map relationship extenstion", "cross map for rel"}, REFSET_TYPE),
                CROSS_MAP_EXTENSION(new String[] { "cross map extension", "cross map"}, REFSET_TYPE),
                TEMPLATE_REL_EXTENSION(new String[] { "template relationship extension", "template for rel"}, REFSET_TYPE),
                TEMPLATE_EXTENSION(new String[] { "template extension", "template"}, REFSET_TYPE),
            BOOLEAN_CIRCLE_ICONS(new String[] { "boolean with circle icon", "boolean with circle"}, REFSET_AUXILIARY),
                BOOLEAN_CIRCLE_ICONS_TRUE(new String[] { "true with circle check icon", "true with circle check"}, BOOLEAN_CIRCLE_ICONS),
                BOOLEAN_CIRCLE_ICONS_FALSE(new String[] { "false with forbidden icon", "false with forbidden"}, BOOLEAN_CIRCLE_ICONS),
            BOOLEAN_CHECK_CROSS_ICONS(new String[] { "boolean with check or cross icon", "boolean with check or cross"}, REFSET_AUXILIARY),
                BOOLEAN_CHECK_CROSS_ICONS_TRUE(new String[] { "true with check icon", "true with check"}, BOOLEAN_CHECK_CROSS_ICONS),
                BOOLEAN_CHECK_CROSS_ICONS_FALSE(new String[] { "false with cross icon", "false with cross"}, BOOLEAN_CHECK_CROSS_ICONS),
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
			REFSET_RELATIONSHIP(new String[] { "refset relationship", "refset relationship" }, 
					new I_ConceptualizeUniversally [] { 
						ArchitectonicAuxiliary.Concept.RELATIONSHIP, 
						REFSET_AUXILIARY}),
				REFSET_TYPE_REL(new String[] { "refset type rel", "refset type rel"}, REFSET_RELATIONSHIP),
				REFSET_PURPOSE_REL(new String[] { "refest purpose rel", "refest purpose rel"}, REFSET_RELATIONSHIP),
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
