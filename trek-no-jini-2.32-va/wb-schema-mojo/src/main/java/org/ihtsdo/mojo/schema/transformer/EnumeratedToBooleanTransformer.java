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
 * The Transformer EnumeratedToBooleanTransformer.<br>
 * This class transforms a concept enumeration to a boolean extension, using a map that will specify which values will be transformed to false, and which ones to true.<br>
 * There is also a default boolean value that will be used when the concept enumeration contains a concept not specified in the conversion map.
 */
public class EnumeratedToBooleanTransformer extends AbstractTransformer {
	
	/** The refset uuid. */
	private UUID refsetUuid;
	
	/** The values for true. */
	private List<UUID> valuesForTrue;
	
	/** The values for false. */
	private List<UUID> valuesForFalse;
	
	/** The default value. */
	private boolean defaultValue;
	
	/** The id. */
	private final String id = "enumerated-to-boolean";
	
	/** The concept count. */
	private transient int conceptCount = 0;

	/**
	 * Instantiates a new enumerated to boolean transformer.
	 */
	public EnumeratedToBooleanTransformer() {
	}

	/**
	 * Instantiates a new enumerated to boolean transformer.
	 *
	 * @param refsetUuid the refset uuid
	 * @param valuesForTrue the values for true
	 * @param valueForFalse the value for false
	 * @param defaultValue the default value
	 */
	public EnumeratedToBooleanTransformer(UUID refsetUuid, List<UUID> valuesForTrue, 
			List<UUID> valueForFalse, boolean defaultValue) {
		this.refsetUuid = refsetUuid;
		this.valuesForTrue = valuesForTrue;
		this.valuesForFalse = valueForFalse;
		this.defaultValue = defaultValue;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#setupFromXml(java.lang.String)
	 */
	@Override
	public void setupFromXml(String xmlFile) throws Exception {
		TransformersConfigApi api = new TransformersConfigApi(xmlFile);
		ConceptDescriptor refset = api.getConceptDescriptor(api.getIntId(id), "parameters.refset");
		setRefsetUuid(refset.getVerifiedConcept().getPrimUuid());
		int i = 0;
		valuesForTrue = new ArrayList<>();
		for (String loopValue : api.getCollectionAt(api.getIntId(id), "parameters.valuesForTrue.concept")) {
			ConceptDescriptor loopDescriptor = api.getConceptDescriptor(api.getIntId(id), 
					"parameters.valuesForTrue.concept(" + i + ")");
			valuesForTrue.add(loopDescriptor.getVerifiedConcept().getPrimUuid());
			i++;
		}
		i = 0;
		valuesForFalse = new ArrayList<>();
		for (String loopValue : api.getCollectionAt(api.getIntId(id), "parameters.valuesForFalse.concept")) {
			ConceptDescriptor loopDescriptor = api.getConceptDescriptor(api.getIntId(id), 
					"parameters.valuesForFalse.concept(" + i + ")");
			valuesForFalse.add(loopDescriptor.getVerifiedConcept().getPrimUuid());
			i++;
		}
		setDefaultValue(Boolean.parseBoolean(api.getValueAt(api.getIntId(id), "parameters.defaultValue")));
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
	 * Gets the values for true.
	 *
	 * @return the values for true
	 */
	public List<UUID> getValuesForTrue() {
		return valuesForTrue;
	}

	/**
	 * Sets the values for true.
	 *
	 * @param valuesForTrue the new values for true
	 */
	public void setValuesForTrue(List<UUID> valuesForTrue) {
		this.valuesForTrue = valuesForTrue;
	}

	/**
	 * Gets the values for false.
	 *
	 * @return the values for false
	 */
	public List<UUID> getValuesForFalse() {
		return valuesForFalse;
	}

	/**
	 * Sets the values for false.
	 *
	 * @param valuesForFalse the new values for false
	 */
	public void setValuesForFalse(List<UUID> valuesForFalse) {
		this.valuesForFalse = valuesForFalse;
	}

	/**
	 * Checks if is default value.
	 *
	 * @return true, if is default value
	 */
	public boolean isDefaultValue() {
		return defaultValue;
	}

	/**
	 * Sets the default value.
	 *
	 * @param defaultValue the new default value
	 */
	public void setDefaultValue(boolean defaultValue) {
		this.defaultValue = defaultValue;
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
					relationship.getRelationshipTargetUuid().equals(cidTypeId) &&
					relationship.getRelationshipSourceUuid().equals(refsetUuid)) {
				relationship.setC2Uuid(booleanTypeId);
			}
		} catch (IOException | TerminologyException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformAnnotation(org.ihtsdo.tk.dto.concept.component.refset.TkRefexAbstractMember, org.ihtsdo.tk.dto.concept.component.TkComponent)
	 */
	@Override
	public void transformAnnotation(TkRefexAbstractMember<?> annotation,
			TkComponent<?> component) {
		if (annotation.getRefexUuid().equals(refsetUuid)) {
			if (annotation.getRefexUuid().equals(refsetUuid)) {
				TkRefexUuidMember cid1Member = (TkRefexUuidMember) annotation;
				TkRefexBooleanMember booleanMember = new TkRefexBooleanMember();

				booleanMember.setAdditionalIdComponents(cid1Member.getAdditionalIdComponents());
				booleanMember.setAnnotations(cid1Member.getAnnotations());
				booleanMember.setAuthorUuid(cid1Member.getAuthorUuid());
                                booleanMember.setModuleUuid(cid1Member.getModuleUuid());
				booleanMember.setComponentUuid(cid1Member.getComponentUuid());
				booleanMember.setPathUuid(cid1Member.getPathUuid());
				booleanMember.setPrimordialComponentUuid(cid1Member.getPrimordialComponentUuid());
				booleanMember.setRefsetUuid(cid1Member.getRefexUuid());
				booleanMember.setStatusUuid(cid1Member.getStatusUuid());
				booleanMember.setTime(cid1Member.getTime());

				if (valuesForTrue.contains(cid1Member.getUuid1())) {
					booleanMember.setBoolean1(true);
				} else if (valuesForFalse.contains(cid1Member.getUuid1())) {
					booleanMember.setBoolean1(false);
				} else {
					booleanMember.setBoolean1(defaultValue);
				}

				booleanMember.setRevisions(new ArrayList<TkRefexBooleanRevision>());
				if (cid1Member.getRevisions() != null) {
					for (TkRefexUuidRevision cidRevision : cid1Member.getRevisions()) {
						TkRefexBooleanRevision booleanRevision = new TkRefexBooleanRevision();
						booleanRevision.setAuthorUuid(cidRevision.getAuthorUuid());
                                                booleanRevision.setModuleUuid(cidRevision.getModuleUuid());
						if (valuesForTrue.contains(cidRevision.getUuid1())) {
							booleanRevision.setBoolean1(true);
						} else if (valuesForFalse.contains(cidRevision.getUuid1())) {
							booleanRevision.setBoolean1(false);
						} else {
							booleanRevision.setBoolean1(defaultValue);
						}
						booleanRevision.setPathUuid(cidRevision.getPathUuid());
						booleanRevision.setStatusUuid(cidRevision.getStatusUuid());
						booleanRevision.setTime(cidRevision.getTime());
						booleanMember.getRevisions().add(booleanRevision);
					}
				}

				component.getAnnotations().remove(cid1Member);
				component.getAnnotations().add(booleanMember);

				conceptCount++;
				if (conceptCount % 1000 == 0 || conceptCount == 1) {
					System.out.println("**** Converted " + conceptCount + " members");
				}
			}

		}

	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformMember(org.ihtsdo.tk.dto.concept.component.refset.TkRefexAbstractMember, org.ihtsdo.tk.dto.concept.TkConcept)
	 */
	@Override
	public void transformMember(TkRefexAbstractMember<?> member,
			TkConcept concept) {
		if (member.getRefexUuid().equals(refsetUuid)) {
			TkRefexUuidMember cid1Member = (TkRefexUuidMember) member;
			TkRefexBooleanMember booleanMember = new TkRefexBooleanMember();

			booleanMember.setAdditionalIdComponents(cid1Member.getAdditionalIdComponents());
			booleanMember.setAnnotations(cid1Member.getAnnotations());
			booleanMember.setAuthorUuid(cid1Member.getAuthorUuid());
                        booleanMember.setModuleUuid(cid1Member.getModuleUuid());
			booleanMember.setComponentUuid(cid1Member.getComponentUuid());
			booleanMember.setPathUuid(cid1Member.getPathUuid());
			booleanMember.setPrimordialComponentUuid(cid1Member.getPrimordialComponentUuid());
			booleanMember.setRefsetUuid(cid1Member.getRefexUuid());
			booleanMember.setStatusUuid(cid1Member.getStatusUuid());
			booleanMember.setTime(cid1Member.getTime());

			if (valuesForTrue.contains(cid1Member.getUuid1())) {
				booleanMember.setBoolean1(true);
			} else if (valuesForFalse.contains(cid1Member.getUuid1())) {
				booleanMember.setBoolean1(false);
			} else {
				booleanMember.setBoolean1(defaultValue);
			}

			booleanMember.setRevisions(new ArrayList<TkRefexBooleanRevision>());
			if (cid1Member.getRevisions() != null) {
				for (TkRefexUuidRevision cidRevision : cid1Member.getRevisions()) {
					TkRefexBooleanRevision booleanRevision = new TkRefexBooleanRevision();
					booleanRevision.setAuthorUuid(cidRevision.getAuthorUuid());
                                        booleanMember.setModuleUuid(cidRevision.getModuleUuid());
					if (valuesForTrue.contains(cidRevision.getUuid1())) {
						booleanRevision.setBoolean1(true);
					} else if (valuesForFalse.contains(cidRevision.getUuid1())) {
						booleanRevision.setBoolean1(false);
					} else {
						booleanRevision.setBoolean1(defaultValue);
					}
					booleanRevision.setPathUuid(cidRevision.getPathUuid());
					booleanRevision.setStatusUuid(cidRevision.getStatusUuid());
					booleanRevision.setTime(cidRevision.getTime());
					booleanMember.getRevisions().add(booleanRevision);
				}
			}

			concept.getRefsetMembers().remove(cid1Member);
			concept.getRefsetMembers().add(booleanMember);

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
		System.out.println("**** Running enumerated-to-boolean conversion");
	}

	@Override
	public String getId() {
		return id;
	}

}
