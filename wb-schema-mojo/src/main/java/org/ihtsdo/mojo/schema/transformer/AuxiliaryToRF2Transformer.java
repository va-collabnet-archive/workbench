/*
 * 
 */
package org.ihtsdo.mojo.schema.transformer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.mojo.schema.AbstractTransformer;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.tk.spec.ValidationException;

/**
 * The Transformer AnnotationToRefset.<br>
 * Transforms an annotation style extension to a refset style extension. All annotations are removed from components and saved as members of the same refset.
 */
public class AuxiliaryToRF2Transformer extends AbstractTransformer {

	/** The id. */
	private final String id = "auxiliary-to-rf2";

	/** The concept count. */
	private transient int conceptCount = 0;
	private ConceptSpec isaSpec;

	/**
	 * Instantiates a new transformer.
	 */
	public AuxiliaryToRF2Transformer() {
		isaSpec = new ConceptSpec("Is a (attribute)", UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"));
	}


	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#setupFromXml(java.lang.String)
	 */
	@Override
	public void setupFromXml(String xmlFile) throws Exception {
		//TransformersConfigApi api = new TransformersConfigApi(xmlFile);
		//ConceptDescriptor refset = api.getConceptDescriptor(api.getIntId(id), "parameters.refset");
		//setRefsetUuid(refset.getVerifiedConcept().getPrimUuid());

	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformAttributes(org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes)
	 */
	@Override
	public void transformAttributes(TkConceptAttributes attributes, TkConcept concept) {
		try {
			if (attributes.getStatusUuid().equals(SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid())) {
				attributes.setStatusUuid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid());
			} else if (attributes.getStatusUuid().equals(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
				attributes.setStatusUuid(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid());
			} else if (attributes.getStatusUuid().equals(SnomedMetadataRf1.AMBIGUOUS_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
				attributes.setStatusUuid(SnomedMetadataRf2.AMBIGUOUS_COMPONENT_RF2.getLenient().getPrimUuid());
			} else if (attributes.getStatusUuid().equals(SnomedMetadataRf1.DUPLICATE_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
				attributes.setStatusUuid(SnomedMetadataRf2.DUPLICATE_COMPONENT_RF2.getLenient().getPrimUuid());
			} else if (attributes.getStatusUuid().equals(SnomedMetadataRf1.ERRONEOUS_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
				attributes.setStatusUuid(SnomedMetadataRf2.ERRONEOUS_COMPONENT_RF2.getLenient().getPrimUuid());
			} else if (attributes.getStatusUuid().equals(SnomedMetadataRf1.INAPPROPRIATE_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
				attributes.setStatusUuid(SnomedMetadataRf2.INAPPROPRIATE_COMPONENT_RF2.getLenient().getPrimUuid());
			} else if (attributes.getStatusUuid().equals(SnomedMetadataRf1.LIMITED_ACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
				attributes.setStatusUuid(SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getLenient().getPrimUuid());
			}
		} catch (ValidationException e) {
			AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
		} catch (IOException e) {
			AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformDescription(org.ihtsdo.tk.dto.concept.component.description.TkDescription)
	 */
	@Override
	public void transformDescription(TkDescription description, TkConcept concept) {
		try {
			if (description.getStatusUuid().equals(SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid())) {
				description.setStatusUuid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid());
			} else if (description.getStatusUuid().equals(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
				description.setStatusUuid(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid());
			} else if (description.getStatusUuid().equals(SnomedMetadataRf1.AMBIGUOUS_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
				description.setStatusUuid(SnomedMetadataRf2.AMBIGUOUS_COMPONENT_RF2.getLenient().getPrimUuid());
			} else if (description.getStatusUuid().equals(SnomedMetadataRf1.DUPLICATE_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
				description.setStatusUuid(SnomedMetadataRf2.DUPLICATE_COMPONENT_RF2.getLenient().getPrimUuid());
			} else if (description.getStatusUuid().equals(SnomedMetadataRf1.ERRONEOUS_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
				description.setStatusUuid(SnomedMetadataRf2.ERRONEOUS_COMPONENT_RF2.getLenient().getPrimUuid());
			} else if (description.getStatusUuid().equals(SnomedMetadataRf1.INAPPROPRIATE_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
				description.setStatusUuid(SnomedMetadataRf2.INAPPROPRIATE_COMPONENT_RF2.getLenient().getPrimUuid());
			} else if (description.getStatusUuid().equals(SnomedMetadataRf1.LIMITED_ACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
				description.setStatusUuid(SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getLenient().getPrimUuid());
			}

			if (description.getTypeUuid().equals(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient().getPrimUuid())) {
				description.setTypeUuid(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getPrimUuid());
				TkRefexUuidMember usMember = new TkRefexUuidMember();
				usMember.setPrimordialComponentUuid(UUID.randomUUID());
				usMember.setAuthorUuid(description.getAuthorUuid());
                                usMember.setModuleUuid(description.getModuleUuid());
				usMember.setUuid1(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
				usMember.setComponentUuid(description.getPrimordialComponentUuid());
				usMember.setPathUuid(description.getPathUuid());
				usMember.setRefsetUuid(SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getLenient().getPrimUuid());
				usMember.setStatusUuid(description.getStatusUuid());
				usMember.setTime(description.getTime());
				description.getAnnotations().add(usMember);
				
				TkRefexUuidMember gbMember = new TkRefexUuidMember();
				gbMember.setPrimordialComponentUuid(UUID.randomUUID());
				gbMember.setAuthorUuid(description.getAuthorUuid());
                                gbMember.setModuleUuid(description.getModuleUuid());
				gbMember.setUuid1(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
				gbMember.setComponentUuid(description.getPrimordialComponentUuid());
				gbMember.setPathUuid(description.getPathUuid());
				gbMember.setRefsetUuid(SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2.getLenient().getPrimUuid());
				gbMember.setStatusUuid(description.getStatusUuid());
				gbMember.setTime(description.getTime());
				description.getAnnotations().add(gbMember);
			} else if (description.getTypeUuid().equals(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid())) {
				description.setTypeUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid());
				TkRefexUuidMember usMember = new TkRefexUuidMember();
				usMember.setPrimordialComponentUuid(UUID.randomUUID());
				usMember.setAuthorUuid(description.getAuthorUuid());
                                usMember.setModuleUuid(description.getModuleUuid());
				usMember.setUuid1(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
				usMember.setComponentUuid(description.getPrimordialComponentUuid());
				usMember.setPathUuid(description.getPathUuid());
				usMember.setRefsetUuid(SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getLenient().getPrimUuid());
				usMember.setStatusUuid(description.getStatusUuid());
				usMember.setTime(description.getTime());
				description.getAnnotations().add(usMember);
				
				TkRefexUuidMember gbMember = new TkRefexUuidMember();
				gbMember.setPrimordialComponentUuid(UUID.randomUUID());
				gbMember.setAuthorUuid(description.getAuthorUuid());
                                gbMember.setModuleUuid(description.getModuleUuid());
				gbMember.setUuid1(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid());
				gbMember.setComponentUuid(description.getPrimordialComponentUuid());
				gbMember.setPathUuid(description.getPathUuid());
				gbMember.setRefsetUuid(SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2.getLenient().getPrimUuid());
				gbMember.setStatusUuid(description.getStatusUuid());
				gbMember.setTime(description.getTime());
				description.getAnnotations().add(gbMember);

			} else if (description.getTypeUuid().equals(SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getLenient().getPrimUuid())) {
				description.setTypeUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid());
				TkRefexUuidMember usMember = new TkRefexUuidMember();
				usMember.setPrimordialComponentUuid(UUID.randomUUID());
				usMember.setAuthorUuid(description.getAuthorUuid());
                                usMember.setModuleUuid(description.getModuleUuid());
				usMember.setUuid1(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getPrimUuid());
				usMember.setComponentUuid(description.getPrimordialComponentUuid());
				usMember.setPathUuid(description.getPathUuid());
				usMember.setRefsetUuid(SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getLenient().getPrimUuid());
				usMember.setStatusUuid(description.getStatusUuid());
				usMember.setTime(description.getTime());
				description.getAnnotations().add(usMember);
				
				TkRefexUuidMember gbMember = new TkRefexUuidMember();
				gbMember.setPrimordialComponentUuid(UUID.randomUUID());
				gbMember.setAuthorUuid(description.getAuthorUuid());
                                gbMember.setModuleUuid(description.getModuleUuid());
				gbMember.setUuid1(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getPrimUuid());
				gbMember.setComponentUuid(description.getPrimordialComponentUuid());
				gbMember.setPathUuid(description.getPathUuid());
				gbMember.setRefsetUuid(SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2.getLenient().getPrimUuid());
				gbMember.setStatusUuid(description.getStatusUuid());
				gbMember.setTime(description.getTime());
				description.getAnnotations().add(gbMember);

			}
		} catch (ValidationException e) {
			AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
		} catch (IOException e) {
			AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
		}

	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformRelationship(org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship)
	 */
	@Override
	public void transformRelationship(TkRelationship relationship, TkConcept concept) {
		try {
			if (relationship.getStatusUuid().equals(SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid())) {
				relationship.setStatusUuid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid());
			} else if (relationship.getStatusUuid().equals(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
				relationship.setStatusUuid(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid());
			}

			if (relationship.getCharacteristicUuid().equals(SnomedMetadataRf1.DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getPrimUuid())) {
				relationship.setCharacteristicUuid(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getPrimUuid());
			} else if (relationship.getCharacteristicUuid().equals(SnomedMetadataRf1.STATED_DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getPrimUuid())) {
				relationship.setCharacteristicUuid(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getPrimUuid());
			}
			
			if (relationship.getTypeUuid().equals(ArchitectonicAuxiliary.Concept.IS_A_REL.getPrimoridalUid())) {
				relationship.setTypeUuid(isaSpec.getLenient().getPrimUuid());
			}
		} catch (IOException e) {
			AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformAnnotation(org.ihtsdo.tk.dto.concept.component.refset.TkRefexAbstractMember, org.ihtsdo.tk.dto.concept.component.TkComponent)
	 */
	@Override
	public void transformAnnotation(TkRefexAbstractMember<?> annotation,
			TkComponent<?> component) {
		try {
			if (annotation.getStatusUuid().equals(SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid())) {
				annotation.setStatusUuid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid());
			} else if (annotation.getStatusUuid().equals(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
				annotation.setStatusUuid(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid());
			}
		} catch (ValidationException e) {
			AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
		} catch (IOException e) {
			AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformMember(org.ihtsdo.tk.dto.concept.component.refset.TkRefexAbstractMember, org.ihtsdo.tk.dto.concept.TkConcept)
	 */
	@Override
	public void transformMember(TkRefexAbstractMember<?> member,
			TkConcept concept) {
		try {
			if (member.getStatusUuid().equals(SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid())) {
				member.setStatusUuid(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid());
			} else if (member.getStatusUuid().equals(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
				member.setStatusUuid(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid());
			}
		} catch (ValidationException e) {
			AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
		} catch (IOException e) {
			AceLog.getAppLog().log(Level.SEVERE, e.getMessage(), e);
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#postProcessConcept()
	 */
	@Override
	public boolean postProcessConcept(TkConcept eConcept) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#postProcessIteration()
	 */
	@Override
	public List<TkConcept> postProcessIteration() {
		System.out.println("**** Final, total converted " + conceptCount + " members");
		List<TkConcept> postProcessList = new ArrayList<>();
		return postProcessList;
	}


	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#preProcessIteration()
	 */
	@Override
	public void preProcessIteration() {
	}

	/**
	 * Count.
	 */
	public void count() {
		conceptCount++;
		if (conceptCount % 1000 == 0 || conceptCount == 1) {
			System.out.println("**** Converted " + conceptCount + " members");
		}
	}

	@Override
	public String getId() {
		return id;
	}

}
