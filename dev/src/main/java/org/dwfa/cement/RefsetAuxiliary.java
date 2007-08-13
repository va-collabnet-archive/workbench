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
		REFSET_AUXILIARY("Refset Auxiliary Concept"),
			REFSET_TYPE("refset type", REFSET_AUXILIARY),
            BOOLEAN_EXTENSION("boolean extensions", REFSET_TYPE),
            INT_EXTENSION("int extensions", REFSET_TYPE),
            CONCEPT_EXTENSION("concept extensions", REFSET_TYPE),
            LANGUAGE_EXTENSION("language extensions", REFSET_TYPE),
            SCOPED_LANGUAGE_EXTENSION("scoped language extensions", REFSET_TYPE),
			REFSET_PURPOSE("refset purpose", REFSET_AUXILIARY),
				RELATIONSHIP_ORDER("relationship order", REFSET_PURPOSE),
			REFSET_RELATIONSHIP(new String[] { "refset relationship" }, 
					new I_ConceptualizeUniversally [] { 
						ArchitectonicAuxiliary.Concept.RELATIONSHIP, 
						REFSET_AUXILIARY}),
				REFSET_TYPE_REL("refset type rel", REFSET_RELATIONSHIP),
				REFSET_PURPOSE_REL("refest purpose rel", REFSET_RELATIONSHIP),
			REFSET_IDENTITY("refset identity", REFSET_AUXILIARY),
				DOCUMENT_SECTION_ORDER(new String[] { "document section order",
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
		
		private Concept(String descriptionString) {
			this(new String[] { descriptionString }, new I_ConceptualizeUniversally[] { });
		}
		private Concept(String descriptionString, I_ConceptualizeUniversally parent) {
			this(new String[] { descriptionString }, new I_ConceptualizeUniversally[] {parent});
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
