package org.dwfa.cement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

public class SNOMEDExtension implements I_AddToMemoryTermServer {
	
	private static Set<UUID> snomedIdsUsed;
	
	public static Set<UUID> getSnomedIdsUsed() {
		if (snomedIdsUsed == null) {
			snomedIdsUsed = new HashSet<UUID>();
			snomedIdsUsed.addAll(SNOMED.Concept.UNIT_OF_TIME.getUids());
			snomedIdsUsed.addAll(SNOMED.Concept.IS_A.getUids());
		}
		return snomedIdsUsed;
	}
	
	public enum Concept implements I_ConceptualizeUniversally {
				GREGORIAN_DATE(new String[] { "Gregorian Date"}, 
						new I_ConceptualizeUniversally [] { SNOMED.Concept.UNIT_OF_TIME }, 
						new I_ConceptualizeUniversally [] { SNOMED.Concept.IS_A });
		
		private Collection<UUID> conceptUids = new ArrayList<UUID>();
		
		private Boolean primitive = true;
		
		private UniversalFixedRel[] rels;
		
		private UniversalFixedDescription[] descriptions;
		
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
		ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE};
	/* (non-Javadoc)
	 * @see org.dwfa.cement.I_AddToMemoryTermServer#addToMemoryTermServer(org.dwfa.cement.MemoryTermServer)
	 */
	public void addToMemoryTermServer(MemoryTermServer server) throws Exception {
		//server.addRoot(Concept.REFSET_AUXILIARY);
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
