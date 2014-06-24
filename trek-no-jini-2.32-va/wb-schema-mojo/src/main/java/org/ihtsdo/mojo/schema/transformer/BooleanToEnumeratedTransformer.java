/*
 * 
 */
package org.ihtsdo.mojo.schema.transformer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.mojo.db.ConceptDescriptor;
import org.ihtsdo.mojo.schema.AbstractTransformer;
import org.ihtsdo.mojo.schema.config.TransformersConfigApi;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_boolean.TkRefexBooleanMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_boolean.TkRefexBooleanRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidRevision;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;

/**
 * The Transformer BooleanToEnumeratedTransformer. <br>
 * Transform the boolean extension to a concept enumeration based on a the parameters:<br>
 * <ul>
 * <li>Cid value for True</li>
 * <li>Cid value for False</li>
 * </ul>
 */
public class BooleanToEnumeratedTransformer extends AbstractTransformer {
	
	/** The refset uuid. */
	private UUID refsetUuid;
	
	/** The value for true. */
	private UUID valueForTrue;
	
	/** The value for false. */
	private UUID valueForFalse;
	
	/** The id. */
	private final String id = "boolean-to-enumerated";
	
	/** The concept count. */
	private transient int conceptCount = 0;

	/**
	 * Instantiates a new boolean to enumerated transformer.
	 */
	public BooleanToEnumeratedTransformer() {
	}

	/**
	 * Instantiates a new boolean to enumerated transformer.
	 *
	 * @param refsetUuid the refset uuid
	 * @param valueForTrue the value for true
	 * @param valueForFalse the value for false
	 */
	public BooleanToEnumeratedTransformer(UUID refsetUuid, UUID valueForTrue, UUID valueForFalse) {
		this.refsetUuid = refsetUuid;
		this.valueForTrue = valueForTrue;
		this.valueForFalse = valueForFalse;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#setupFromXml(java.lang.String)
	 */
	@Override
	public void setupFromXml(String xmlFile) throws Exception {
		TransformersConfigApi api = new TransformersConfigApi(xmlFile);

		ConceptDescriptor refset = api.getConceptDescriptor(api.getIntId(id), "parameters.refset");
		setRefsetUuid(refset.getVerifiedConcept().getPrimUuid());

		ConceptDescriptor conceptTrue = api.getConceptDescriptor(api.getIntId(id), "parameters.valueForTrue");
		setValueForTrue(conceptTrue.getVerifiedConcept().getPrimUuid());

		ConceptDescriptor conceptFalse = api.getConceptDescriptor(api.getIntId(id), "parameters.valueForFalse");
		setValueForFalse(conceptFalse.getVerifiedConcept().getPrimUuid());
	}

	/**
	 * Gets the refset uuid.
	 *
	 * @return the refset uuid
	 */
	public UUID getRefsetUuid() {
		return refsetUuid;
	}

	/**
	 * Sets the refset uuid.
	 *
	 * @param refsetUuid the new refset uuid
	 */
	public void setRefsetUuid(UUID refsetUuid) {
		this.refsetUuid = refsetUuid;
	}

	/**
	 * Gets the value for true.
	 *
	 * @return the value for true
	 */
	public UUID getValueForTrue() {
		return valueForTrue;
	}

	/**
	 * Sets the value for true.
	 *
	 * @param valueForTrue the new value for true
	 */
	public void setValueForTrue(UUID valueForTrue) {
		this.valueForTrue = valueForTrue;
	}

	/**
	 * Gets the value for false.
	 *
	 * @return the value for false
	 */
	public UUID getValueForFalse() {
		return valueForFalse;
	}

	/**
	 * Sets the value for false.
	 *
	 * @param valueForFalse the new value for false
	 */
	public void setValueForFalse(UUID valueForFalse) {
		this.valueForFalse = valueForFalse;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
    @Override
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformAttributes(org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes)
	 */
	@Override
	public void transformAttributes(TkConceptAttributes attributes, TkConcept concept) {
		// nothing
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformDescription(org.ihtsdo.tk.dto.concept.component.description.TkDescription)
	 */
	@Override
	public void transformDescription(TkDescription description, TkConcept concept) {
		// nothing
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformRelationship(org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship)
	 */
	@Override
	public void transformRelationship(TkRelationship relationship, TkConcept concept) {
		try {
			UUID refsetTypeRelId = RefsetAuxiliary.Concept.REFSET_TYPE_REL.getPrimoridalUid();
			UUID cidTypeId = RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getPrimoridalUid();
			UUID booleanTypeId = RefsetAuxiliary.Concept.BOOLEAN_EXTENSION.getPrimoridalUid();
			if (relationship.getTypeUuid().equals(refsetTypeRelId) && 
					relationship.getRelationshipTargetUuid().equals(booleanTypeId) &&
					relationship.getRelationshipSourceUuid().equals(refsetUuid)) {
				relationship.setC2Uuid(cidTypeId);
			}
		} catch (IOException | TerminologyException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformMember(org.ihtsdo.tk.dto.concept.component.refset.TkRefexAbstractMember, org.ihtsdo.tk.dto.concept.TkConcept)
	 */
	@Override
	public void transformMember(TkRefexAbstractMember<?> member, TkConcept concept) {
		if (member.getRefexUuid().equals(refsetUuid)) {
			TkRefexBooleanMember booleanMember = (TkRefexBooleanMember) member;
			TkRefexUuidMember cidMember = new TkRefexUuidMember();

			cidMember.setAdditionalIdComponents(booleanMember.getAdditionalIdComponents());
			cidMember.setAnnotations(booleanMember.getAnnotations());
			cidMember.setAuthorUuid(booleanMember.getAuthorUuid());
                        cidMember.setModuleUuid(booleanMember.getModuleUuid());
			cidMember.setComponentUuid(booleanMember.getComponentUuid());
			cidMember.setPathUuid(booleanMember.getPathUuid());
			cidMember.setPrimordialComponentUuid(booleanMember.getPrimordialComponentUuid());
			cidMember.setRefsetUuid(booleanMember.getRefexUuid());
			cidMember.setStatusUuid(booleanMember.getStatusUuid());
			cidMember.setTime(booleanMember.getTime());

			if (booleanMember.getBoolean1()) {
				cidMember.setUuid1(valueForTrue);
			} else {
				cidMember.setUuid1(valueForFalse);
			}

			cidMember.setRevisions(new ArrayList<TkRefexUuidRevision>());
			if (booleanMember.getRevisions() != null) {
				for (TkRefexBooleanRevision booleanRevision : booleanMember.getRevisions()) {
					TkRefexUuidRevision cidRevision = new TkRefexUuidRevision();
					cidRevision.setAuthorUuid(booleanRevision.getAuthorUuid());
                                        cidRevision.setModuleUuid(booleanRevision.getModuleUuid());
					if (booleanRevision.getBoolean1()) {
						cidRevision.setUuid1(valueForTrue);
					} else {
						cidRevision.setUuid1(valueForFalse);
					}
					cidRevision.setPathUuid(booleanRevision.getPathUuid());
					cidRevision.setStatusUuid(booleanRevision.getStatusUuid());
					cidRevision.setTime(booleanRevision.getTime());
					cidMember.getRevisions().add(cidRevision);
				}
			}

			concept.getRefsetMembers().remove(booleanMember);
			concept.getRefsetMembers().add(cidMember);
			conceptCount++;
			if (conceptCount % 1000 == 0 || conceptCount == 1) {
				System.out.println("**** Converted " + conceptCount + " members");
			}
		}


	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformAnnotation(org.ihtsdo.tk.dto.concept.component.refset.TkRefexAbstractMember, org.ihtsdo.tk.dto.concept.component.TkComponent)
	 */
	@Override
	public void transformAnnotation(TkRefexAbstractMember<?> annotation,
			TkComponent<?> component) {
		if (annotation.getRefexUuid().equals(refsetUuid)) {
			TkRefexBooleanMember booleanMember = (TkRefexBooleanMember) annotation;
			TkRefexUuidMember cidMember = new TkRefexUuidMember();

			cidMember.setAdditionalIdComponents(booleanMember.getAdditionalIdComponents());
			cidMember.setAnnotations(booleanMember.getAnnotations());
			cidMember.setAuthorUuid(booleanMember.getAuthorUuid());
                        cidMember.setModuleUuid(booleanMember.getModuleUuid());
			cidMember.setComponentUuid(booleanMember.getComponentUuid());
			cidMember.setPathUuid(booleanMember.getPathUuid());
			cidMember.setPrimordialComponentUuid(booleanMember.getPrimordialComponentUuid());
			cidMember.setRefsetUuid(booleanMember.getRefexUuid());
			cidMember.setStatusUuid(booleanMember.getStatusUuid());
			cidMember.setTime(booleanMember.getTime());

			if (booleanMember.getBoolean1()) {
				cidMember.setUuid1(valueForTrue);
			} else {
				cidMember.setUuid1(valueForFalse);
			}

			cidMember.setRevisions(new ArrayList<TkRefexUuidRevision>());
			if (booleanMember.getRevisions() != null) {
				for (TkRefexBooleanRevision booleanRevision : booleanMember.getRevisions()) {
					TkRefexUuidRevision cidRevision = new TkRefexUuidRevision();
					cidRevision.setAuthorUuid(booleanRevision.getAuthorUuid());
                                        cidRevision.setModuleUuid(booleanRevision.getModuleUuid());
					if (booleanRevision.getBoolean1()) {
						cidRevision.setUuid1(valueForTrue);
					} else {
						cidRevision.setUuid1(valueForFalse);
					}
					cidRevision.setPathUuid(booleanRevision.getPathUuid());
					cidRevision.setStatusUuid(booleanRevision.getStatusUuid());
					cidRevision.setTime(booleanRevision.getTime());
					cidMember.getRevisions().add(cidRevision);
				}
			}

			component.getAnnotations().remove(booleanMember);
			component.getAnnotations().add(cidMember);
			conceptCount++;
			if (conceptCount % 1000 == 0 || conceptCount == 1) {
				System.out.println("**** Converted " + conceptCount + " members");
			}
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
		System.out.println("**** Running boolean-to-enumerated conversion");
		
	}

}
