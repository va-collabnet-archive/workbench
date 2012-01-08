/*
 * 
 */
package org.ihtsdo.mojo.schema.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.mojo.db.ConceptDescriptor;
import org.ihtsdo.mojo.schema.AbstractTransformer;
import org.ihtsdo.mojo.schema.config.TransformersConfigApi;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.cid.TkRefsetCidMember;
import org.ihtsdo.tk.dto.concept.component.refset.cid.TkRefsetCidRevision;
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
		replaceMap = new HashMap<UUID,UUID>();
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
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformMember(org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember, org.ihtsdo.tk.dto.concept.TkConcept)
	 */
	@Override
	public void transformMember(TkRefsetAbstractMember<?> member, TkConcept concept) {
		if (member.getRefsetUuid().equals(refsetUuid)) {
			TkRefsetCidMember cidMember = (TkRefsetCidMember) member;
			transformExtension(cidMember);
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
			TkRefsetCidMember cidMember = (TkRefsetCidMember) annotation;
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
		System.out.println("**** Running boolean-to-enumerated conversion");

	}

	/**
	 * Transform extension.
	 *
	 * @param cidMember the cid member
	 * @return the tk refset cid member
	 */
	private TkRefsetCidMember transformExtension(TkRefsetCidMember cidMember) {

		if (replaceMap.get(cidMember.getC1Uuid()) != null) {
			cidMember.setC1Uuid(replaceMap.get(cidMember.getC1Uuid()));
		}

		if (cidMember.getRevisions() != null) {
			for (TkRefsetCidRevision cidRevision : cidMember.getRevisions()) {
				if (replaceMap.get(cidRevision.getC1Uuid()) != null) {
					cidRevision.setC1Uuid(replaceMap.get(cidRevision.getC1Uuid()));
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
