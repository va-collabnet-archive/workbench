/*
 * 
 */
package org.ihtsdo.mojo.schema.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.ihtsdo.mojo.db.ConceptDescriptor;
import org.ihtsdo.mojo.schema.AbstractTransformer;
import org.ihtsdo.mojo.schema.config.TransformersConfigApi;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidRevision;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;

/**
 * The Transformer EnumerationToEnumerationTransformer.<br>
 * The transformer replaces the value of a concept enumeration following the specification in a source-target replace map.
 */
public class EnumerationToEnumerationTransformer extends AbstractTransformer {
	
	/** The refset uuid. */
	private UUID refsetUuid;
	
	/** The replace map. */
	private Map<UUID,UUID> replaceMap;
	
	/** The id. */
	private final String id = "enumerated-to-enumerated";
	
	/** The concept count. */
	private transient int conceptCount = 0;

	/**
	 * Instantiates a new enumeration to enumeration transformer.
	 */
	public EnumerationToEnumerationTransformer() {
	}

	/**
	 * Instantiates a new enumeration to enumeration transformer.
	 *
	 * @param refsetUuid the refset uuid
	 * @param replaceMap the replace map
	 */
	public EnumerationToEnumerationTransformer(UUID refsetUuid, Map<UUID,UUID> replaceMap) {
		this.refsetUuid = refsetUuid;
		this.replaceMap = replaceMap;
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
		replaceMap = new HashMap<>();
		for (String loopValue : api.getCollectionAt(api.getIntId(id), "parameters.replaceMap.pair")) {
			ConceptDescriptor sourceDescriptor = api.getConceptDescriptor(api.getIntId(id), 
					"parameters.replaceMap.pair(" + i + ").source");
			ConceptDescriptor targetDescriptor = api.getConceptDescriptor(api.getIntId(id), 
					"parameters.replaceMap.pair(" + i + ").target");
			replaceMap.put(sourceDescriptor.getVerifiedConcept().getPrimUuid(), 
					targetDescriptor.getVerifiedConcept().getPrimUuid());
			i++;
		}
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
		// nothing
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformMember(org.ihtsdo.tk.dto.concept.component.refset.TkRefexAbstractMember, org.ihtsdo.tk.dto.concept.TkConcept)
	 */
	@Override
	public void transformMember(TkRefexAbstractMember<?> member, TkConcept concept) {
		if (member.getRefexUuid().equals(refsetUuid)) {
			TkRefexUuidMember cidMember = (TkRefexUuidMember) member;
			transformExtension(cidMember);
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
			TkRefexUuidMember cidMember = (TkRefexUuidMember) annotation;
			transformExtension(cidMember);
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

	/**
	 * Transform extension.
	 *
	 * @param cidMember the cid member
	 * @return the tk refset cid member
	 */
	private TkRefexUuidMember transformExtension(TkRefexUuidMember cidMember) {

		if (replaceMap.get(cidMember.getUuid1()) != null) {
			cidMember.setUuid1(replaceMap.get(cidMember.getUuid1()));
		}

		if (cidMember.getRevisions() != null) {
			for (TkRefexUuidRevision cidRevision : cidMember.getRevisions()) {
				if (replaceMap.get(cidRevision.getUuid1()) != null) {
					cidRevision.setUuid1(replaceMap.get(cidRevision.getUuid1()));
				}
			}
		}

		return cidMember;
	}

	/**
	 * Gets the replace map.
	 *
	 * @return the replace map
	 */
	public Map<UUID, UUID> getReplaceMap() {
		return replaceMap;
	}

	/**
	 * Sets the replace map.
	 *
	 * @param replaceMap the replace map
	 */
	public void setReplaceMap(Map<UUID, UUID> replaceMap) {
		this.replaceMap = replaceMap;
	}

}
