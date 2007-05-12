package org.dwfa.cement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.I_DescribeConceptUniversally;
import org.dwfa.tapi.I_ManifestUniversally;
import org.dwfa.tapi.I_RelateConceptsUniversally;
import org.dwfa.tapi.I_StoreLocalFixedTerminology;
import org.dwfa.tapi.I_StoreUniversalFixedTerminology;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedConcept;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.tapi.impl.MemoryTermServer;
import org.dwfa.tapi.impl.UniversalFixedDescription;
import org.dwfa.tapi.impl.UniversalFixedRel;
import org.dwfa.util.id.Type3UuidFactory;

public class ArchitectonicAuxiliary implements I_AddToMemoryTermServer {
	public static final UUID NAME_SPACE = UUID.fromString("d0cb73c0-aaf7-11db-8294-0002a5d5c51b");

	private static String getArchitectonicText() {
		StringBuffer b = new StringBuffer();

		b.append("<html>The subject matter of <font color=blue>architectonic</font> is the structure of all human knowledge. ");
		b.append("The purpose of providing an architectonic scheme is to classify different types of knowledge and explain ");
		b.append("the relationships that exist between these classifications. Peirce's own architectonic system divides ");
		b.append("knowledge according to it status as a \"science\" and then explains the interrelation of these different "); 
		b.append("scientific disciplines. His belief was that philosophy must be placed within this systematic account of ");
		b.append("knowledge as science. Peirce adopts his architectonic ambitions of structuring all knowledge, and ");
		b.append("organizing philosophy within it, from his great philosophical hero, Kant. This systematizing approach "); 
		b.append("became crucial for Peirce in his later work. However, his belief in a structured philosophy related ");
		b.append("systematically to all other scientific disciplines was important to him throughout his philosophical ");
		b.append("life.");
		b.append("<center><img src='ace:1c4214ec-147a-11db-ac5d-0800200c9a66'></center><br>");
		b.append("Source:  <a href='http://www.iep.utm.edu/p/PeirceAr.htm'>The Internet Encyclopedia of Philosophy</a>");
		b.append("</html>");
		return b.toString();
	}
		

	public enum Concept implements I_ConceptualizeUniversally {
		
		ARCHITECTONIC_ROOT_CONCEPT(new String[] {"Architectonic Concept", getArchitectonicText()},
			new I_ConceptualizeUniversally[] { }),
		IMAGE_TYPE("image type",
				new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
			AUXILLARY_IMAGE("auxiliary image",
					new I_ConceptualizeUniversally[] { IMAGE_TYPE }),
			VIEWER_IMAGE("viewer image",
					new I_ConceptualizeUniversally[] { IMAGE_TYPE }),
		DESCRIPTION_TYPE("description type",
				new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
			FULLY_SPECIFIED_DESCRIPTION_TYPE(PrimordialId.FULLY_SPECIFIED_DESCRIPTION_TYPE_ID, 
					new String[] {"fully specified"},
					new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
			SYNONYM_DESCRIPTION_TYPE("synonym",
					new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
			UNSPECIFIED_DESCRIPTION_TYPE("unspecified",
					new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
			PREFERRED_DESCRIPTION_TYPE("preferred",
					new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
			ENTRY_DESCRIPTION_TYPE("entry term",
					new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
			XHTML_PREFERRED_DESC_TYPE("xhtml preferred",
					new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
			XHTML_SYNONYM_DESC_TYPE("xhtml synonym",
					new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
			XHTML_FULLY_SPECIFIED_DESC_TYPE("xhtml fully specified",
					new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
			XHTML_DEF(PrimordialId.XHTML_DEF_ID, new String[] {"xhtml def"},
					new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
			EXTENSION_TABLE(PrimordialId.XHTML_DEF_ID, new String[] {"extension table"},
					new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
			CHANGE_COMMENT("change comment",
					new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
		RELATIONSHIP("relationship",
				new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
			IS_A_REL(PrimordialId.IS_A_REL_ID, new String[] {"is-a rel (terminology constant)"},
					new I_ConceptualizeUniversally[] { RELATIONSHIP }),
			ALLOWED_QUALIFIER_REL("allowed qualifier",
					new I_ConceptualizeUniversally[] { RELATIONSHIP }),
			MAPPING_REL("mapping relationship",
					new I_ConceptualizeUniversally[] { RELATIONSHIP }),
				CLASSIFIED_SPECIFIC_REIMB_REL("classified, specific for reimbursement",
						new I_ConceptualizeUniversally[] { MAPPING_REL }),
				CLASSIFIED_NOT_SPECIFIC_REIMB_REL("classified, NOT specific for reimbursement",
						new I_ConceptualizeUniversally[] { MAPPING_REL }),
				NOT_VALID_AS_PRIMARY_REL("not valid as primary",
						new I_ConceptualizeUniversally[] { MAPPING_REL }),
				NOT_VALID_WITHOUT_ADDITIONAL_CODES_REL("not valid without additional codes",
						new I_ConceptualizeUniversally[] { MAPPING_REL }),
				PC_NEEDED_SPECIFIC_REIMB_REL("patient characteristics needed to classify, specific for reimbursement",
						new I_ConceptualizeUniversally[] { MAPPING_REL }),
				PC_NEEDED_SPECIFIC_NOT_REIMB_REL("patient characteristics needed to classify, NOT specific for reimbursement",
						new I_ConceptualizeUniversally[] { MAPPING_REL }),
				NO_MAPPING_REL("no mapping possible",
						new I_ConceptualizeUniversally[] { MAPPING_REL }),
		CHARACTERISTIC_TYPE("charactersitic type",
				new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
			DEFINING_CHARACTERISTIC(PrimordialId.DEFINING_CHARACTERISTIC_ID, 
					new String[] {"defining"},
					new I_ConceptualizeUniversally[] { CHARACTERISTIC_TYPE }),
				STATED_RELATIONSHIP("stated",
						new I_ConceptualizeUniversally[] { DEFINING_CHARACTERISTIC }),
				INFERRED_RELATIONSHIP("inferred",
						new I_ConceptualizeUniversally[] { DEFINING_CHARACTERISTIC }),
			QUALIFIER_CHARACTERISTIC("qualifier",
					new I_ConceptualizeUniversally[] { CHARACTERISTIC_TYPE }),
			HISTORICAL_CHARACTERISTIC("historical",
					new I_ConceptualizeUniversally[] { CHARACTERISTIC_TYPE }),
			ADDITIONAL_CHARACTERISTIC("additional",
					new I_ConceptualizeUniversally[] { CHARACTERISTIC_TYPE }),
		RELATIONSHIP_REFINABILITY("relationship refinability",
				new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
			NOT_REFINABLE(PrimordialId.NOT_REFINABLE_ID, new String[] {"not refinable"},
					new I_ConceptualizeUniversally[] { RELATIONSHIP_REFINABILITY }),
			OPTIONAL_REFINABILITY("optional",
					new I_ConceptualizeUniversally[] { RELATIONSHIP_REFINABILITY }),
			MANDATORY_REFINABILITY("mandatory",
					new I_ConceptualizeUniversally[] { RELATIONSHIP_REFINABILITY }),
		STATUS("status",
				new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
			ACTIVE("active",
					new I_ConceptualizeUniversally[] { STATUS }),
					PENDING_MOVE("pending move",
							new I_ConceptualizeUniversally[] { ACTIVE }),
					CONCEPT_RETIRED("concept retired",
							new I_ConceptualizeUniversally[] { ACTIVE }),
					LIMITED("limited",
							new I_ConceptualizeUniversally[] { ACTIVE }),
					CURRENT(PrimordialId.CURRENT_ID, new String[] { "current" },
									new I_ConceptualizeUniversally[] { ACTIVE }),
					FLAGGED_FOR_REVIEW("flagged",
							new I_ConceptualizeUniversally[] { ACTIVE }),
			INACTIVE("inactive",
					new I_ConceptualizeUniversally[] { STATUS }),
					CONFLICTING("conflicting",
							new I_ConceptualizeUniversally[] { INACTIVE }),
					NOT_YET_CREATED("not yet created",
							new I_ConceptualizeUniversally[] { INACTIVE }),
					RETIRED("retired",
							new I_ConceptualizeUniversally[] { INACTIVE }),
					RETIRED_MISSPELLED("retired-misspelled",
							new I_ConceptualizeUniversally[] { INACTIVE }),
					DUPLICATE("duplicate",
							new I_ConceptualizeUniversally[] { INACTIVE }),
					OUTDATED("outdated",
							new I_ConceptualizeUniversally[] { INACTIVE }),
					AMBIGUOUS("ambiguous",
							new I_ConceptualizeUniversally[] { INACTIVE }),
					ERRONEOUS("erroneous",
							new I_ConceptualizeUniversally[] { INACTIVE }),
					INAPPROPRIATE("inappropriate",
							new I_ConceptualizeUniversally[] { INACTIVE }),
					IMPLIED_RELATIONSHIP("implied",
							new I_ConceptualizeUniversally[] { INACTIVE }),
					MOVED_ELSEWHERE("moved elsewhere",
							new I_ConceptualizeUniversally[] { INACTIVE }),
			CONSTANT("constant",
					new I_ConceptualizeUniversally[] { STATUS }),
		PATH("path",
				new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
			RELEASE("release",
					new I_ConceptualizeUniversally[] { PATH }),
				SNOMED_CORE("SNOMED Core",
						new I_ConceptualizeUniversally[] { RELEASE }),
						SNOMED_20060731("SNOMED 2006-07-31",
								new I_ConceptualizeUniversally[] { SNOMED_CORE }),
						SNOMED_20060131("SNOMED 2006-01-31",
								new I_ConceptualizeUniversally[] { SNOMED_CORE }),
						SNOMED_20050731("SNOMED 2005-07-31",
								new I_ConceptualizeUniversally[] { SNOMED_CORE }),
						SNOMED_20050131("SNOMED 2005-01-31",
								new I_ConceptualizeUniversally[] { SNOMED_CORE }),
				ICD_10_AM("ICD-10-AM",
						new I_ConceptualizeUniversally[] { PATH }),
				ICD_10_AM_SNOMED_CORE_MAP("ICD-10-AM to SNOMED Core map",
						new I_ConceptualizeUniversally[] { PATH }),
				ARCHITECTONIC_BRANCH(PrimordialId.ACE_AUXILIARY_ID, new String[] {"ACE Auxiliary"},
						new I_ConceptualizeUniversally[] { PATH }),
		TEST("test",
				new I_ConceptualizeUniversally[] { PATH }),
			SNOMED_CORE_TEST("SNOMED Core test",
					new I_ConceptualizeUniversally[] { TEST }),
			ICD_10_AM_TEST("ICD-10-AM test",
					new I_ConceptualizeUniversally[] { TEST }),
			ICD_10_AM_SNOMED_CORE_MAP_TEST("ICD-10-AM to SNOMED Core map test",
					new I_ConceptualizeUniversally[] { TEST }),
			ARCHITECTONIC_AUXILIARY_TEST("ACE Auxiliary test",
					new I_ConceptualizeUniversally[] { TEST }),
			TGA_TEST_DATA("TGA Test Data", new I_ConceptualizeUniversally[] { TEST }),
		DEVELOPMENT("development",
				new I_ConceptualizeUniversally[] { PATH }),
			SNOMED_CORE_DEV("SNOMED Core dev",
					new I_ConceptualizeUniversally[] { DEVELOPMENT }),
			ICD_10_AM_DEV("ICD-10-AM dev",
					new I_ConceptualizeUniversally[] { DEVELOPMENT }),
			ICD_10_AM_SNOMED_CORE_MAP_DEV("ICD-10-AM to SNOMED Core map dev",
					new I_ConceptualizeUniversally[] { DEVELOPMENT }),
			ARCHITECTONIC_AUXILIARY_DEV("ACE Auxiliary dev",
							new I_ConceptualizeUniversally[] { DEVELOPMENT }),
			TGA_DATA("TGA Data", new I_ConceptualizeUniversally[] { DEVELOPMENT }),
			AMT_SOURCE_DATA("AMT Source Data", new I_ConceptualizeUniversally[] { DEVELOPMENT }),
	ID_SOURCE("identifier source",
			new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
		SNOMED_INT_ID("SNOMED integer id",
				new I_ConceptualizeUniversally[] { ID_SOURCE }),
		SNOMED_T3_UUID("SNOMED Type 3 UUID",
				new I_ConceptualizeUniversally[] { ID_SOURCE }),
		UNSPECIFIED_UUID(PrimordialId.ACE_AUX_ENCODING_ID, new String[] {"generated UUID"},
				new I_ConceptualizeUniversally[] { ID_SOURCE });
		;
		private Collection<UUID> conceptUids = new ArrayList<UUID>();
		
		private Boolean primitive = true;
		
		private UniversalFixedRel[] rels;
		
		private UniversalFixedDescription[] descriptions;
		
		private static PrimordialId[] descTypeOrder;
		
		private Concept(String descriptionString, I_ConceptualizeUniversally[] parents) {
			this(new String[] {descriptionString}, parents);
		}
		// PrimordialId
		private Concept(String[] descriptionStrings, I_ConceptualizeUniversally[] parents) {
			this.conceptUids.add(Type3UuidFactory.fromEnum(this)); 
			init(descriptionStrings, parents);
		}
		private Concept(PrimordialId id, String[] descriptionStrings, I_ConceptualizeUniversally[] parents) {
			this.conceptUids = id.getUids();
			init(descriptionStrings, parents);
		}
		private void init(String[] descriptionStrings, I_ConceptualizeUniversally[] parents) {
			try {
				this.rels = makeRels(this, parents);
				this.descriptions = makeDescriptions(this, descriptionStrings);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		public UniversalFixedRel[] makeRels(I_ConceptualizeUniversally source, I_ConceptualizeUniversally[] parents) throws Exception {
			UniversalFixedRel[] rels = new UniversalFixedRel[parents.length];
			int i = 0;
			for (I_ConceptualizeUniversally p: parents) {
				int relGrp = 0;
				int parentIndex = i++;
				rels[parentIndex] = new UniversalFixedRel(Type3UuidFactory.forRel(source.getUids(), 
						PrimordialId.IS_A_REL_ID.getUids(), p.getUids()), 
						source.getUids(),
						PrimordialId.IS_A_REL_ID.getUids(), p.getUids(),
						PrimordialId.DEFINING_CHARACTERISTIC_ID.getUids(),
						PrimordialId.NOT_REFINABLE_ID.getUids(), relGrp);
			}
			return rels;
		}

		public UniversalFixedDescription[] makeDescriptions(I_ConceptualizeUniversally source, String[] descriptionStrings) throws Exception {
			if (descTypeOrder == null) {
				descTypeOrder = new PrimordialId[] { 
						PrimordialId.FULLY_SPECIFIED_DESCRIPTION_TYPE_ID, 
						PrimordialId.XHTML_DEF_ID };
			}
			UniversalFixedDescription[] descriptions = new UniversalFixedDescription[descriptionStrings.length];
			int i = 0;
			boolean initialCapSig = true;
			String langCode = "en";
			for (String descText: descriptionStrings) {
				if (descText != null) {
					descriptions[i] = new UniversalFixedDescription(Type3UuidFactory.forDesc(source.getUids(), descTypeOrder[i].getUids(), descText),
							PrimordialId.CURRENT_ID.getUids(), 
							source.getUids(),
							initialCapSig, descTypeOrder[i].getUids(), descText,
							langCode);
				}
				i++;
			}
			return descriptions;
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


		public I_DescribeConceptUniversally getDescription(List<I_ConceptualizeUniversally> typePriorityList, I_StoreUniversalFixedTerminology termStore)  {
			throw new UnsupportedOperationException();
		}

		public Collection<I_DescribeConceptUniversally> getDescriptions(I_StoreUniversalFixedTerminology server)  {
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



		public I_ConceptualizeLocally localize() throws IOException, TerminologyException {
			return LocalFixedConcept.get(getUids(), primitive);
		}
		public Collection<I_RelateConceptsUniversally> getDestRels(Collection<I_ConceptualizeUniversally> types, I_StoreUniversalFixedTerminology termStore) {
			throw new UnsupportedOperationException();
		}
		public Collection<I_RelateConceptsUniversally> getSourceRels(Collection<I_ConceptualizeUniversally> types, I_StoreUniversalFixedTerminology termStore) {
			throw new UnsupportedOperationException();
		}
	}	
	
	public void addToMemoryTermServer(MemoryTermServer server) throws Exception {
		server.addRoot(Concept.ARCHITECTONIC_ROOT_CONCEPT);
		for (Concept c: Concept.values()) {
			server.add(c);
			for (I_DescribeConceptUniversally d: c.descriptions) {
				server.add(d);
			}
			for (I_RelateConceptsUniversally r: c.rels) {
				server.add(r);
			}
		}
	}
	
	/**
	 * Values
	 * <li>0 Unspecified This may be assigned as either a Preferred Term or
	 * Synonym by a I_Describe Subset for a language, dialect or realm.
	 * <li>1 Preferred This is the Preferred Term for the associated I_Concept.
	 * <li>2 Synonym This is a Synonym for the associated I_Concept.
	 * <li>3 FullySpecifiedName This is the FullySpecifiedName for the
	 * associated I_Concept.
	 * 
	 * @param type
	 * @return
	 * @throws IdentifierIsNotNativeException
	 * @throws QueryException
	 * @throws RemoteException
	 */
	public static I_ConceptualizeUniversally getSnomedDescriptionType(int type) {
		switch (type) {
		case 0:
			return ArchitectonicAuxiliary.Concept.UNSPECIFIED_DESCRIPTION_TYPE;
		case 1:
			return ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE;
		case 2:
			return ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE;
		case 3:
			return ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE;
		}
		return ArchitectonicAuxiliary.Concept.DESCRIPTION_TYPE;

	}
	
	/**
	 * Values:
	 * <li>0 Defining This relationship represents a defining characteristic of
	 * the sourceId concept. Hierarchical relationships (e.g. “ISA” and
	 * “PART-OF”) are also regarded as defining relationships Example: “‘Site’ =
	 * ‘Liver’” is a defining characteristic of ‘Liver biopsy’.
	 * <li>1 Qualifier This relationship represents an optional qualifying
	 * characteristic. Example: “‘Revision status’ = ‘Conversion from other type
	 * of arthroplasty’” is a possible qualification of ‘Hip replacement’
	 * <li>2 Historical This is used to relate an inactive concept to another
	 * concept. Example: The “Same As” relationship connects an inactive concept
	 * with the concept it duplicated. Only used in the Historical Relationships
	 * File.
	 * <li>3 Additional This relationship represents a context specific
	 * characteristic. This is used to convey characteristics of a concept that
	 * apply at a particular time within a particular organization but which are
	 * not intrinsic to the concept. Example: ‘Prescription Only Medicine’ is a
	 * context specific characteristic of the I_Concept ‘Amoxycillin 250mg
	 * capsule’. It is true currently in the UK but is not true in some other
	 * countries.
	 * 
	 * @param type
	 * @return
	 * @throws IdentifierIsNotNativeException
	 * @throws QueryException
	 * @throws RemoteException
	 */
	public static I_ConceptualizeUniversally getSnomedCharacteristicType(int type)  {
		switch (type) {
		case 0:
			return ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC;
		case 1:
			return ArchitectonicAuxiliary.Concept.QUALIFIER_CHARACTERISTIC;
		case 2:
			return ArchitectonicAuxiliary.Concept.HISTORICAL_CHARACTERISTIC;
		case 3:
			return ArchitectonicAuxiliary.Concept.ADDITIONAL_CHARACTERISTIC;
		}
		return ArchitectonicAuxiliary.Concept.CHARACTERISTIC_TYPE;
	}
	

	/**
	 * An indication of whether it is possible to refine the target concept when
	 * this I_Relate is used as a template for clinical data entry.
	 * <p>
	 * <p>
	 * Values
	 * <li>0 Not refinable Not refinable.
	 * <li>1 Optional May be refined by selecting subtypes.
	 * <li>2 Mandatory Must be refined by selecting a subtype.
	 * 
	 * @param type
	 * @return
	 * @throws IdentifierIsNotNativeException
	 * @throws QueryException
	 * @throws RemoteException
	 */
	public static I_ConceptualizeUniversally getSnomedRefinabilityType(int type)  {
		switch (type) {
		case 0:
			return ArchitectonicAuxiliary.Concept.NOT_REFINABLE;
		case 1:
			return ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY;
		case 2:
			return ArchitectonicAuxiliary.Concept.MANDATORY_REFINABILITY;
		}
		return ArchitectonicAuxiliary.Concept.RELATIONSHIP_REFINABILITY;
	}
	
	   /**
		 * Values
		 * <li>-2 Conflicting
		 * <li>-1 Not-yet created.
		 * <li>0 Current The Description and its associated Concept are in
		 * current use.
		 * <li>1 Non-Current The Description has been withdrawn without a
		 * specified reason.
		 * <li>2 Duplicate The Description has been withdrawn from current use
		 * because it duplicates another description containing the same term
		 * (or a very similar term) associated with the same Concept.
		 * <li>3 Outdated The Description has been withdrawn from current use
		 * because this Term is no longer in general clinical use as a label for
		 * the associated Concept.
		 * <li>4 Ambiguous The Concept has been withdrawn from current use
		 * because it is inherently ambiguous. These concepts are considered
		 * inactive.
		 * <li>5 Erroneous The Description has been withdrawn as the Term
		 * contains errors.
		 * <li>6 Limited The Description is a valid Description of a Concept
		 * which has “limited” status (i.e. the Concept has ConceptStatus = 6).
		 * <li>7 Inappropriate The Description has been withdrawn as the Term
		 * should not refer to this concept.
		 * <li>8 Concept noncurrent The Description is a valid Description of a
		 * Concept which has been made non-current (i.e. the Concept has
		 * ConceptStatus 1, 2, 3, 4, 5, or 10).
		 * <li>9 Implied Relationship withdrawn but is implied by other active
		 * Relationships.
		 * <li>10 Moved elsewhere The Description has been moved to an
		 * extension, to a different extension, or to the core. A reference will
		 * indicate the namespace to which the description has been moved.
		 * <li>11 Pending move The Description will be moved to an extension,
		 * to a different extension, or to the core. A reference will indicate
		 * the namespace to which the description has been moved when the
		 * recipient organization confirms the move (Future Use).
		 * 
		 * @param statusCode
		 * @return
		 */
    public static I_ConceptualizeUniversally getStatusFromId(int statusCode) {
        switch (statusCode) {
        case -2:
            return ArchitectonicAuxiliary.Concept.CONFLICTING;
        case -1:
            return ArchitectonicAuxiliary.Concept.NOT_YET_CREATED;
        case 0:
            return ArchitectonicAuxiliary.Concept.CURRENT;
        case 1:
            return ArchitectonicAuxiliary.Concept.RETIRED;
        case 2:
            return ArchitectonicAuxiliary.Concept.DUPLICATE;
        case 3:
            return ArchitectonicAuxiliary.Concept.OUTDATED;
        case 4:
            return ArchitectonicAuxiliary.Concept.AMBIGUOUS;
        case 5:
            return ArchitectonicAuxiliary.Concept.ERRONEOUS;
        case 6:
            return ArchitectonicAuxiliary.Concept.LIMITED;
        case 7:
            return ArchitectonicAuxiliary.Concept.INAPPROPRIATE;
        case 8:
            return ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED;
        case 9:
            return ArchitectonicAuxiliary.Concept.IMPLIED_RELATIONSHIP;
        case 10:
            return ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE;
        case 11:
            return ArchitectonicAuxiliary.Concept.PENDING_MOVE;

        }
        throw new IllegalArgumentException("Unknown status code: " + statusCode);
    }

    public static void main(String[] args) throws Exception {
    	try {
    		File directory = new File(args[0]);
    		directory.mkdirs();
    		File conceptFile = new File(directory, "concepts.txt");
    		File descFile = new File(directory, "descriptions.txt");
    		File relFile = new File(directory, "relationships.txt");
       		File rootsFile = new File(directory, "roots.txt");    	
       		File extTypeFile = new File(directory, "extensions.txt");    	
       		File altIdFile = new File(directory, "alt_ids.txt");    	

    		MemoryTermServer mts = new MemoryTermServer();
    		LocalFixedTerminology.setStore(mts);
    		mts.setGenerateIds(true);
    		ArchitectonicAuxiliary aa = new ArchitectonicAuxiliary();
    		aa.addToMemoryTermServer(mts);
    		DocumentAuxiliary da = new DocumentAuxiliary();
    		da.addToMemoryTermServer(mts);
    		RefsetAuxiliary rsa = new RefsetAuxiliary();
    		rsa.addToMemoryTermServer(mts);
    		
    		SNOMEDExtension sme = new SNOMEDExtension();
    		sme.addToMemoryTermServer(mts);
    		
    		HL7 hl7 = new HL7();
    		hl7.addToMemoryTermServer(mts);
    		
    		QueueType queueType = new QueueType();
    		queueType.addToMemoryTermServer(mts);
    		
    		mts.setGenerateIds(false);

       		Writer altIdWriter = new FileWriter(altIdFile);

       		Writer conceptWriter = new FileWriter(conceptFile);
    		mts.writeConcepts(conceptWriter, altIdWriter);
    		conceptWriter.close();

    		Writer descWriter = new FileWriter(descFile);
    		mts.writeDescriptions(descWriter, altIdWriter);
    		descWriter.close();

    		Writer relWriter = new FileWriter(relFile);
    		mts.writeRelationships(relWriter, altIdWriter);
    		relWriter.close();

    		Writer rootsWriter = new FileWriter(rootsFile);
    		mts.writeRoots(rootsWriter);
    		rootsWriter.close();

    		Writer extensionTypeWriter = new FileWriter(extTypeFile);
    		mts.writeExtensionTypes(extensionTypeWriter, altIdWriter);
    		extensionTypeWriter.close();
    		
    		I_ConceptualizeLocally[] descTypeOrder = new I_ConceptualizeLocally[] { 
    				mts.getConcept(mts.getNid(ArchitectonicAuxiliary.Concept.EXTENSION_TABLE.getUids())) };
    		List<I_ConceptualizeLocally> descTypePriorityList = Arrays.asList(descTypeOrder);

    		for (I_ConceptualizeLocally extensionType: mts.getExtensionTypes()) {
    			I_DescribeConceptLocally typeDesc = extensionType.getDescription(descTypePriorityList);
    			File extensionFile = new File(directory, typeDesc.getText() + ".txt");
    			Writer extensionWriter = new FileWriter(extensionFile);
    			mts.writeExtension(extensionType, extensionWriter, altIdWriter);
    			extensionWriter.close();
    		}


    	} catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }

    private static List<I_ConceptualizeUniversally> preferredDescPrefList;
    public static List<I_ConceptualizeUniversally> getUniversalPreferredDescPrefList() {
    	if (preferredDescPrefList == null) {
       		preferredDescPrefList = new ArrayList<I_ConceptualizeUniversally>();

       		preferredDescPrefList.add(Concept.XHTML_PREFERRED_DESC_TYPE);
    		preferredDescPrefList.add(Concept.PREFERRED_DESCRIPTION_TYPE);
    		preferredDescPrefList.add(Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE);
     		preferredDescPrefList.add(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE);
     		preferredDescPrefList.add(Concept.SYNONYM_DESCRIPTION_TYPE);
     		preferredDescPrefList.add(Concept.ENTRY_DESCRIPTION_TYPE);
    	}
    	return preferredDescPrefList;
    }
    public static List<I_ConceptualizeLocally> getLocalPreferredDescPrefList() throws IOException, TerminologyException {
		List<I_ConceptualizeLocally> localList = new ArrayList<I_ConceptualizeLocally>();
		for (I_ConceptualizeUniversally uc: getUniversalPreferredDescPrefList()) {
			localList.add(uc.localize());
		}
		return localList;
    }
    
    private static List<I_ConceptualizeUniversally> toStringDescPrefList;
    public static List<I_ConceptualizeUniversally> getToStringDescPrefList() {
    	if (toStringDescPrefList == null) {
    		toStringDescPrefList = new ArrayList<I_ConceptualizeUniversally>();
    		toStringDescPrefList.add(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE);
    		toStringDescPrefList.add(Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE);
    		toStringDescPrefList.add(Concept.PREFERRED_DESCRIPTION_TYPE);
    		toStringDescPrefList.add(Concept.XHTML_PREFERRED_DESC_TYPE);
    	}
    	return toStringDescPrefList;
    }
    
    private static List<I_ConceptualizeUniversally> fullySpecifiedDescPrefList;
    public static List<I_ConceptualizeUniversally> getFullySpecifiedDescPrefList() {
    	if (fullySpecifiedDescPrefList == null) {
    		fullySpecifiedDescPrefList = new ArrayList<I_ConceptualizeUniversally>();
    		fullySpecifiedDescPrefList.add(Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE);
    		fullySpecifiedDescPrefList.add(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE);
    		fullySpecifiedDescPrefList.add(Concept.XHTML_PREFERRED_DESC_TYPE);
    		fullySpecifiedDescPrefList.add(Concept.PREFERRED_DESCRIPTION_TYPE);
    	}
    	return fullySpecifiedDescPrefList;
    }
    
    private static Map<I_StoreLocalFixedTerminology, List<I_ConceptualizeLocally>> localToStringDescPrefListMap = new HashMap<I_StoreLocalFixedTerminology, List<I_ConceptualizeLocally>>();
    public static List<I_ConceptualizeLocally> getLocalToStringDescPrefList(I_StoreLocalFixedTerminology termServer) throws Exception {
    	if (localToStringDescPrefListMap.get(termServer) == null) {
    		List<I_ConceptualizeLocally> localList = new ArrayList<I_ConceptualizeLocally>();
    		for (I_ConceptualizeUniversally uc: getToStringDescPrefList()) {
    			localList.add(uc.localize());
    		}
    		localToStringDescPrefListMap.put(termServer, localList);
    	}
    	return localToStringDescPrefListMap.get(termServer);
    }
    
    private static List<I_ConceptualizeLocally> localFullySpecifiedDescPrefList;
    ;
    public static List<I_ConceptualizeLocally> getLocalFullySpecifiedDescPrefList() throws Exception {
    	if (localFullySpecifiedDescPrefList == null) {
    		localFullySpecifiedDescPrefList = new ArrayList<I_ConceptualizeLocally>();
    		for (I_ConceptualizeUniversally uc: getFullySpecifiedDescPrefList()) {
    			localFullySpecifiedDescPrefList.add(uc.localize());
    		}
    	}
    	return localFullySpecifiedDescPrefList;
    }
    
}
