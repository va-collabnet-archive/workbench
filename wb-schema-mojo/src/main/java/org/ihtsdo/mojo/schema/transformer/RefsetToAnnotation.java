/*
 * 
 */
package org.ihtsdo.mojo.schema.transformer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.mojo.db.ConceptDescriptor;
import org.ihtsdo.mojo.schema.AbstractTransformer;
import org.ihtsdo.mojo.schema.config.TransformersConfigApi;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;

/**
 * The Transformer RefsetToAnnotation.<br>
 * Transforms a refset into an annotation. The refset is retrieved in the pre-iteration phase, annotations are added and the refset is stripped of members during the iteration.
 */
public class RefsetToAnnotation extends AbstractTransformer {
	
	/** The id. */
	private final String id = "refset-to-annotation";
	
	/** The concept count. */
	private transient int conceptCount = 0;
	
	/** The refset uuid. */
	private UUID refsetUuid;
	
	/** The members map. */
	private Map<UUID,TkRefexAbstractMember<?>> membersMap;
	
	/**
	 * Instantiates a new refset to annotation.
	 */
	public RefsetToAnnotation() {
	}
	
	/**
	 * Instantiates a new refset to annotation.
	 *
	 * @param refsetUuid the refset uuid
	 */
	public RefsetToAnnotation(UUID refsetUuid) {
		super();
		this.refsetUuid = refsetUuid;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#setupFromXml(java.lang.String)
	 */
	@Override
	public void setupFromXml(String xmlFile) throws Exception {
		TransformersConfigApi api = new TransformersConfigApi(xmlFile);

		ConceptDescriptor refset = api.getConceptDescriptor(api.getIntId(id), "parameters.refset");
		setRefsetUuid(refset.getVerifiedConcept().getPrimUuid());

	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformAttributes(org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes)
	 */
	@Override
	public void transformAttributes(TkConceptAttributes attributes, TkConcept concept) {
		if (membersMap.get(attributes.getPrimordialComponentUuid()) != null) {
			if (attributes.getAnnotations() == null)
				attributes.setAnnotations(new ArrayList<TkRefexAbstractMember<?>>());
			attributes.getAnnotations().add(membersMap.get(attributes.getPrimordialComponentUuid()));
			count();
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformDescription(org.ihtsdo.tk.dto.concept.component.description.TkDescription)
	 */
	@Override
	public void transformDescription(TkDescription description, TkConcept concept) {
		if (membersMap.get(description.getPrimordialComponentUuid()) != null) {
			if (description.getAnnotations() == null)
				description.setAnnotations(new ArrayList<TkRefexAbstractMember<?>>());
			description.getAnnotations().add(membersMap.get(description.getPrimordialComponentUuid()));
			count();
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformRelationship(org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship)
	 */
	@Override
	public void transformRelationship(TkRelationship relationship, TkConcept concept) {
		if (membersMap.get(relationship.getPrimordialComponentUuid()) != null) {
			if (relationship.getAnnotations() == null)
				relationship.setAnnotations(new ArrayList<TkRefexAbstractMember<?>>());
			relationship.getAnnotations().add(membersMap.get(relationship.getPrimordialComponentUuid()));
			count();
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformAnnotation(org.ihtsdo.tk.dto.concept.component.refset.TkRefexAbstractMember, org.ihtsdo.tk.dto.concept.component.TkComponent)
	 */
	@Override
	public void transformAnnotation(TkRefexAbstractMember<?> annotation,
			TkComponent<?> component) {
		// Not supported

	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformMember(org.ihtsdo.tk.dto.concept.component.refset.TkRefexAbstractMember, org.ihtsdo.tk.dto.concept.TkConcept)
	 */
	@Override
	public void transformMember(TkRefexAbstractMember<?> member,
			TkConcept concept) {
		if (member.getRefexUuid().equals(refsetUuid) && concept.getConceptAttributes().getPrimordialComponentUuid().equals(refsetUuid)) {
			concept.getRefsetMembers().remove(member);
		}
		if (membersMap.get(member.getPrimordialComponentUuid()) != null) {
			if (member.getAnnotations() == null)
				member.setAnnotations(new ArrayList<TkRefexAbstractMember<?>>());
			member.getAnnotations().add(membersMap.get(member.getPrimordialComponentUuid()));
			count();
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

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#preProcessIteration()
	 */
	@Override
	public void preProcessIteration() {
		System.out.println("**** Running refset-to-annotation conversion");
		try {
			I_GetConceptData refset = Terms.get().getConcept(refsetUuid);
			TkConcept refsetTkConcept = new TkConcept(refset);
			membersMap = new HashMap<UUID,TkRefexAbstractMember<?>>();
			for ( TkRefexAbstractMember<?> loopMember : refsetTkConcept.getRefsetMembers()) {
				membersMap.put(loopMember.getComponentUuid(), loopMember);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}		
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
