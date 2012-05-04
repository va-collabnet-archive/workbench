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
 * The Transformer LongToIntegerTransformer.<br>
 * Transform a long extension to an integer extension.<br>
 * Some times it's also necessary to change the values. A scalar figure can be provided for value transformation (the scalar will be multiplied by the value, a scalar of 1 will result in the same value).
 */
public class LongToIntegerTransformer extends AbstractTransformer {
	
	/** The refset uuid. */
	private UUID refsetUuid;
	
	/** The id. */
	private final String id = "long-to-int";
	
	/** The concept count. */
	private transient int conceptCount = 0;
	
	/** The scalar. */
	private Long scalar;
	
	/**
	 * Instantiates a new boolean to enumerated transformer.
	 */
	public LongToIntegerTransformer() {
	}

	/**
	 * Instantiates a new boolean to enumerated transformer.
	 *
	 * @param refsetUuid the refset uuid
	 * @param scalar the scalar
	 */
	public LongToIntegerTransformer(UUID refsetUuid, Long scalar) {
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
			UUID IntTypeId = RefsetAuxiliary.Concept.INT_EXTENSION.getPrimoridalUid();
			UUID LongTypeId = RefsetAuxiliary.Concept.LONG_EXTENSION.getPrimoridalUid();
			if (relationship.getTypeUuid().equals(refsetTypeRelId) && 
					relationship.getC2Uuid().equals(LongTypeId) &&
					relationship.getC1Uuid().equals(refsetUuid)) {
				relationship.setC2Uuid(IntTypeId);
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
			TkRefsetLongMember longMember = (TkRefsetLongMember) member;
			TkRefsetIntMember intMember = transformExtension(longMember);

			concept.getRefsetMembers().remove(longMember);
			concept.getRefsetMembers().add(intMember);
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
			TkRefsetLongMember longMember = (TkRefsetLongMember) annotation;
			TkRefsetIntMember intMember = transformExtension(longMember);

			component.getAnnotations().remove(intMember);
			component.getAnnotations().add(longMember);
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
	public  TkRefsetIntMember transformExtension(TkRefsetLongMember extension) {
		TkRefsetIntMember intMember = new TkRefsetIntMember();

		intMember.setAdditionalIdComponents(extension.getAdditionalIdComponents());
		intMember.setAnnotations(extension.getAnnotations());
		intMember.setAuthorUuid(extension.getAuthorUuid());
                intMember.setModuleUuid(extension.getModuleUuid());
		intMember.setComponentUuid(extension.getComponentUuid());
		intMember.setPathUuid(extension.getPathUuid());
		intMember.setPrimordialComponentUuid(extension.getPrimordialComponentUuid());
		intMember.setRefsetUuid(extension.getRefsetUuid());
		intMember.setStatusUuid(extension.getStatusUuid());
		intMember.setTime(extension.getTime());
		
		if (scalar != null) {
			intMember.setIntValue(Math.round(scalar * extension.getLongValue()));
		} else {
			intMember.setIntValue(Math.round(extension.getLongValue()));
		}

		intMember.setRevisions(new ArrayList<TkRefsetIntRevision>());
		if (extension.getRevisions() != null) {
			for (TkRefsetLongRevision LongRevision : extension.getRevisions()) {
				TkRefsetIntRevision IntRevision = new TkRefsetIntRevision();
				IntRevision.setAuthorUuid(LongRevision.getAuthorUuid());
                                IntRevision.setModuleUuid(LongRevision.getModuleUuid());
				IntRevision.setPathUuid(LongRevision.getPathUuid());
				IntRevision.setStatusUuid(LongRevision.getStatusUuid());
				IntRevision.setTime(LongRevision.getTime());
				
				if (scalar != null) {
					IntRevision.setIntValue(Math.round(scalar * LongRevision.getLongValue()));
				} else {
					IntRevision.setIntValue(Math.round(LongRevision.getLongValue()));
				}
				
				intMember.getRevisions().add(IntRevision);
			}
		}
		
		return intMember;
		
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
