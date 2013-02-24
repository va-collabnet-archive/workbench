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
import org.ihtsdo.tk.dto.concept.component.refex.type_long.TkRefexLongMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_long.TkRefexLongRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_int.TkRefexIntMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_int.TkRefexIntRevision;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;

/**
 * The Transformer IntegerToLongTransformer. <br>
 * Transform a integer extension to a long extension.<br>
 * Some times it's also necessary to change the values. A scalar figure can be provided for value transformation (the scalar will be multiplied by the value, a scalar of 1 will result in the same value).
 */
public class IntegerToLongTransformer extends AbstractTransformer {
	
	/** The refset uuid. */
	private UUID refsetUuid;
	
	/** The id. */
	private final String id = "int-to-long";
	
	/** The concept count. */
	private transient int conceptCount = 0;
	
	/** The scalar. */
	private Long scalar;

	/**
	 * Instantiates a new boolean to enumerated transformer.
	 */
	public IntegerToLongTransformer() {
	}

	/**
	 * Instantiates a new boolean to enumerated transformer.
	 *
	 * @param refsetUuid the refset uuid
	 * @param scalar the scalar
	 */
	public IntegerToLongTransformer(UUID refsetUuid, Long scalar) {
		this.refsetUuid = refsetUuid;
		this.scalar = scalar;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#setupFromXml(java.lang.String)
	 */
	@Override
	public void setupFromXml(String xmlFile) throws Exception {
		TransformersConfigApi api = new TransformersConfigApi(xmlFile);

		ConceptDescriptor refset = api.getConceptDescriptor(api.getIntId(id), "parameters.refset");
		setRefsetUuid(refset.getVerifiedConcept().getPrimUuid());
		
		setScalar(Long.parseLong(api.getValueAt(api.getIntId(id), "parameters.scalar")));

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
			UUID intTypeId = RefsetAuxiliary.Concept.INT_EXTENSION.getPrimoridalUid();
			UUID longTypeId = RefsetAuxiliary.Concept.LONG_EXTENSION.getPrimoridalUid();
			if (relationship.getTypeUuid().equals(refsetTypeRelId) && 
					relationship.getRelationshipTargetUuid().equals(intTypeId) &&
					relationship.getRelationshipSourceUuid().equals(refsetUuid)) {
				relationship.setC2Uuid(longTypeId);
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
			TkRefexIntMember intMember = (TkRefexIntMember) member;
			TkRefexLongMember longMember = transformExtension(intMember);

			concept.getRefsetMembers().remove(intMember);
			concept.getRefsetMembers().add(longMember);
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
			TkRefexIntMember intMember = (TkRefexIntMember) annotation;
			TkRefexLongMember longMember = transformExtension(intMember);

			component.getAnnotations().remove(longMember);
			component.getAnnotations().add(intMember);
			conceptCount++;
			if (conceptCount % 1000 == 0 || conceptCount == 1) {
				System.out.println("**** Converted " + conceptCount + " members");
			}
		}
		
	}
	
	/**
	 * Transform extension.
	 *
	 * @param extension the extension
	 * @return the tk refset cid Long member
	 */
	public TkRefexLongMember transformExtension(TkRefexIntMember extension) {
		TkRefexLongMember longMember = new TkRefexLongMember();

		longMember.setAdditionalIdComponents(extension.getAdditionalIdComponents());
		longMember.setAnnotations(extension.getAnnotations());
		longMember.setAuthorUuid(extension.getAuthorUuid());
                longMember.setModuleUuid(extension.getModuleUuid());
		longMember.setComponentUuid(extension.getComponentUuid());
		longMember.setPathUuid(extension.getPathUuid());
		longMember.setPrimordialComponentUuid(extension.getPrimordialComponentUuid());
		longMember.setRefsetUuid(extension.getRefexUuid());
		longMember.setStatusUuid(extension.getStatusUuid());
		longMember.setTime(extension.getTime());
		
		if (scalar != null) {
			longMember.setLong1(new Long(extension.getInt1() * scalar));
		} else {
			longMember.setLong1(extension.getInt1());
		}

		longMember.setRevisions(new ArrayList<TkRefexLongRevision>());
		if (extension.getRevisions() != null) {
			for (TkRefexIntRevision intRevision : extension.getRevisions()) {
				TkRefexLongRevision longRevision = new TkRefexLongRevision();
				longRevision.setAuthorUuid(intRevision.getAuthorUuid());
                                longRevision.setModuleUuid(intRevision.getModuleUuid());
				longRevision.setPathUuid(intRevision.getPathUuid());
				longRevision.setStatusUuid(intRevision.getStatusUuid());
				longRevision.setTime(intRevision.getTime());
				
				if (scalar != null) {
					longRevision.setLong1(new Long(intRevision.getInt1() * scalar));
				} else {
					longRevision.setLong1(intRevision.getInt1());
				}
				
				longMember.getRevisions().add(longRevision);
			}
		}
		
		return longMember;
		
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
		System.out.println("**** Running " + id + " conversion");
		
	}

	/**
	 * Gets the scalar.
	 *
	 * @return the scalar
	 */
	public Long getScalar() {
		return scalar;
	}

	/**
	 * Sets the scalar.
	 *
	 * @param scalar the new scalar
	 */
	public void setScalar(Long scalar) {
		this.scalar = scalar;
	}

}
