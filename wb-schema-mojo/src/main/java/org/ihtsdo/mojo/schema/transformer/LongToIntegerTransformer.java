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
			UUID IntTypeId = RefsetAuxiliary.Concept.INT_EXTENSION.getPrimoridalUid();
			UUID LongTypeId = RefsetAuxiliary.Concept.LONG_EXTENSION.getPrimoridalUid();
			if (relationship.getTypeUuid().equals(refsetTypeRelId) && 
					relationship.getRelationshipTargetUuid().equals(LongTypeId) &&
					relationship.getRelationshipSourceUuid().equals(refsetUuid)) {
				relationship.setC2Uuid(IntTypeId);
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
			TkRefexLongMember longMember = (TkRefexLongMember) member;
			TkRefexIntMember intMember = transformExtension(longMember);

			concept.getRefsetMembers().remove(longMember);
			concept.getRefsetMembers().add(intMember);
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
			TkRefexLongMember longMember = (TkRefexLongMember) annotation;
			TkRefexIntMember intMember = transformExtension(longMember);

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
	public  TkRefexIntMember transformExtension(TkRefexLongMember extension) {
		TkRefexIntMember intMember = new TkRefexIntMember();

		intMember.setAdditionalIdComponents(extension.getAdditionalIdComponents());
		intMember.setAnnotations(extension.getAnnotations());
		intMember.setAuthorUuid(extension.getAuthorUuid());
                intMember.setModuleUuid(extension.getModuleUuid());
		intMember.setComponentUuid(extension.getComponentUuid());
		intMember.setPathUuid(extension.getPathUuid());
		intMember.setPrimordialComponentUuid(extension.getPrimordialComponentUuid());
		intMember.setRefsetUuid(extension.getRefexUuid());
		intMember.setStatusUuid(extension.getStatusUuid());
		intMember.setTime(extension.getTime());
		
		if (scalar != null) {
			intMember.setInt1(Math.round(scalar * extension.getLong1()));
		} else {
			intMember.setInt1(Math.round(extension.getLong1()));
		}

		intMember.setRevisions(new ArrayList<TkRefexIntRevision>());
		if (extension.getRevisions() != null) {
			for (TkRefexLongRevision LongRevision : extension.getRevisions()) {
				TkRefexIntRevision IntRevision = new TkRefexIntRevision();
				IntRevision.setAuthorUuid(LongRevision.getAuthorUuid());
                                IntRevision.setModuleUuid(LongRevision.getModuleUuid());
				IntRevision.setPathUuid(LongRevision.getPathUuid());
				IntRevision.setStatusUuid(LongRevision.getStatusUuid());
				IntRevision.setTime(LongRevision.getTime());
				
				if (scalar != null) {
					IntRevision.setInt1(Math.round(scalar * LongRevision.getLong1()));
				} else {
					IntRevision.setInt1(Math.round(LongRevision.getLong1()));
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
