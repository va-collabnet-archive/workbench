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
                MEASUREMENT_EXTENSION(new String[] { "measurement extension by reference", "measurement extensions"}, REFSET_TYPE),
                LANGUAGE_EXTENSION(new String[] { "language extension by reference", "language extensions"}, REFSET_TYPE),
                SCOPED_LANGUAGE_EXTENSION(new String[] { "scoped language extension by reference", "scoped language extensions"}, REFSET_TYPE),
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
			REFSET_PURPOSE(new String[] { "refset purpose", "refset purpose"}, REFSET_AUXILIARY),
                RELATIONSHIP_ORDER(new String[] { "relationship order", "relationship order"}, REFSET_PURPOSE),
                ANNOTATION_ORDER(new String[] { "annotation", "annotation"}, REFSET_PURPOSE),
                MEASUREMENT_ASSOCIATION(new String[] { "measurement association", "measurement association"}, REFSET_PURPOSE),
                INCLUSION_SPECIFICATION(new String[] { "inclusion specification", "inclusion specification"}, REFSET_PURPOSE),
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
					});
		
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
