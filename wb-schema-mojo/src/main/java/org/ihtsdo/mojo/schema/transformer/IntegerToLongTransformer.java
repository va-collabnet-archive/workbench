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
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.mojo.db.ConceptDescriptor;
import org.ihtsdo.mojo.schema.AbstractTransformer;
import org.ihtsdo.mojo.schema.config.TransformersConfigApi;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.Long.TkRefsetLongMember;
import org.ihtsdo.tk.dto.concept.component.refset.Long.TkRefsetLongRevision;
import org.ihtsdo.tk.dto.concept.component.refset.integer.TkRefsetIntMember;
import org.ihtsdo.tk.dto.concept.component.refset.integer.TkRefsetIntRevision;
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
					relationship.getC2Uuid().equals(intTypeId) &&
					relationship.getC1Uuid().equals(refsetUuid)) {
				relationship.setC2Uuid(longTypeId);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformMember(org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember, org.ihtsdo.tk.dto.concept.TkConcept)
	 */
	@Override
	public void transformMember(TkRefsetAbstractMember<?> member, TkConcept concept) {
		if (member.getRefsetUuid().equals(refsetUuid)) {
			TkRefsetIntMember intMember = (TkRefsetIntMember) member;
			TkRefsetLongMember longMember = transformExtension(intMember);

			concept.getRefsetMembers().remove(intMember);
			concept.getRefsetMembers().add(longMember);
			conceptCount++;
			if (conceptCount % 1000 == 0 || conceptCount == 1) {
				System.out.println("**** Converted " + conceptCount + " members");
			}
		}


	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformAnnotation(org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember, org.ihtsdo.tk.dto.concept.component.TkComponent)
	 */
	@Override
	public void transformAnnotation(TkRefsetAbstractMember<?> annotation,
			TkComponent<?> component) {
		if (annotation.getRefsetUuid().equals(refsetUuid)) {
			TkRefsetIntMember intMember = (TkRefsetIntMember) annotation;
			TkRefsetLongMember longMember = transformExtension(intMember);

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
	public TkRefsetLongMember transformExtension(TkRefsetIntMember extension) {
		TkRefsetLongMember longMember = new TkRefsetLongMember();

		longMember.setAdditionalIdComponents(extension.getAdditionalIdComponents());
		longMember.setAnnotations(extension.getAnnotations());
		longMember.setAuthorUuid(extension.getAuthorUuid());
                longMember.setModuleUuid(extension.getModuleUuid());
		longMember.setComponentUuid(extension.getComponentUuid());
		longMember.setPathUuid(extension.getPathUuid());
		longMember.setPrimordialComponentUuid(extension.getPrimordialComponentUuid());
		longMember.setRefsetUuid(extension.getRefsetUuid());
		longMember.setStatusUuid(extension.getStatusUuid());
		longMember.setTime(extension.getTime());
		
		if (scalar != null) {
			longMember.setLongValue(new Long(extension.getIntValue() * scalar));
		} else {
			longMember.setLongValue(extension.getIntValue());
		}

		longMember.setRevisions(new ArrayList<TkRefsetLongRevision>());
		if (extension.getRevisions() != null) {
			for (TkRefsetIntRevision intRevision : extension.getRevisions()) {
				TkRefsetLongRevision longRevision = new TkRefsetLongRevision();
				longRevision.setAuthorUuid(intRevision.getAuthorUuid());
                                longRevision.setModuleUuid(intRevision.getModuleUuid());
				longRevision.setPathUuid(intRevision.getPathUuid());
				longRevision.setStatusUuid(intRevision.getStatusUuid());
				longRevision.setTime(intRevision.getTime());
				
				if (scalar != null) {
					longRevision.setLongValue(new Long(intRevision.getIntValue() * scalar));
				} else {
					longRevision.setLongValue(intRevision.getIntValue());
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
	public boolean postProcessConcept() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#postProcessIteration()
	 */
	@Override
	public List<EConcept> postProcessIteration() {
		System.out.println("**** Final, total converted " + conceptCount + " members");
		List<EConcept> postProcessList = new ArrayList<EConcept>();
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
