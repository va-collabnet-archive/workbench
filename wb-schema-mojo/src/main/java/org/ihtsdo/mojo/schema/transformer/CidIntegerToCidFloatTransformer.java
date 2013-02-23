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
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_float.TkRefexUuidFloatMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_float.TkRefexUuidFloatRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_int.TkRefexUuidIntMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_int.TkRefexUuidIntRevision;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;

/**
 * The Transformer CidIntegerToCidFloatTransformer.
 * Transform a concept-integer extension to a concept-float extension.<br>
 * It is also possible to change the associated concept, for those cases where the concept represents a unit of measure. The source and target concepts in the xml preferences should be updated accordingly.<br>
 * When the unit of measure changes, some times it's also necessary to change the values accordingly. A scalar figure can be provided for value transformation (the scalar will be multiplied by the value, a scalar of 1 will result in the same value).
 */
public class CidIntegerToCidFloatTransformer extends AbstractTransformer {
	
	/** The refset uuid. */
	private UUID refsetUuid;
	
	/** The id. */
	private final String id = "cid-int-to-cid-float";
	
	/** The concept count. */
	private transient int conceptCount = 0;
	
	/** The scalar map. */
	private Map<UUID, Long> scalarMap;
	
	/** The target measure unit map. */
	private Map<UUID, UUID> targetMeasureUnitMap;

	/**
	 * Instantiates a new boolean to enumerated transformer.
	 */
	public CidIntegerToCidFloatTransformer() {
	}

	/**
	 * Instantiates a new boolean to enumerated transformer.
	 *
	 * @param refsetUuid the refset uuid
	 * @param scalarMap the scalar map
	 * @param targetMeasureUnitMap the target measure unit map
	 */
	public CidIntegerToCidFloatTransformer(UUID refsetUuid, Map<UUID, Long> scalarMap, Map<UUID, UUID> targetMeasureUnitMap) {
		this.refsetUuid = refsetUuid;
		this.scalarMap = scalarMap;
		this.targetMeasureUnitMap = targetMeasureUnitMap;
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
		scalarMap = new HashMap<>();
		targetMeasureUnitMap = new HashMap<>();
		
		for (String loopValue : api.getCollectionAt(api.getIntId(id), "parameters.replaceMap.set")) {
			ConceptDescriptor sourceDescriptor = api.getConceptDescriptor(api.getIntId(id), 
					"parameters.replaceMap.pair(" + i + ").source");
			ConceptDescriptor targetDescriptor = api.getConceptDescriptor(api.getIntId(id), 
					"parameters.replaceMap.pair(" + i + ").target");
			Long scalar = Long.parseLong(api.getValueAt(api.getIntId(id),  
					"parameters.replaceMap.pair(" + i + ").scalar"));
			scalarMap.put(sourceDescriptor.getVerifiedConcept().getPrimUuid(), scalar); 
			targetMeasureUnitMap.put(sourceDescriptor.getVerifiedConcept().getPrimUuid(), 
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
		try {
			UUID refsetTypeRelId = RefsetAuxiliary.Concept.REFSET_TYPE_REL.getPrimoridalUid();
			UUID cidIntTypeId = RefsetAuxiliary.Concept.CONCEPT_INT_EXTENSION.getPrimoridalUid();
			UUID cidFloatTypeId = RefsetAuxiliary.Concept.CONCEPT_FLOAT_EXTENSION.getPrimoridalUid();
			if (relationship.getTypeUuid().equals(refsetTypeRelId) && 
					relationship.getRelationshipTargetUuid().equals(cidIntTypeId) &&
					relationship.getRelationshipSourceUuid().equals(refsetUuid)) {
				relationship.setC2Uuid(cidFloatTypeId);
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
			TkRefexUuidIntMember cidIntMember = (TkRefexUuidIntMember) member;
			TkRefexUuidFloatMember cidFloatMember = transformExtension(cidIntMember);

			concept.getRefsetMembers().remove(cidIntMember);
			concept.getRefsetMembers().add(cidFloatMember);
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
			TkRefexUuidIntMember cidIntMember = (TkRefexUuidIntMember) annotation;
			TkRefexUuidFloatMember cidFloatMember = transformExtension(cidIntMember);

			component.getAnnotations().remove(cidFloatMember);
			component.getAnnotations().add(cidIntMember);
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
	 * @return the tk refset cid float member
	 */
	public TkRefexUuidFloatMember transformExtension(TkRefexUuidIntMember extension) {
		TkRefexUuidFloatMember cidFloatMember = new TkRefexUuidFloatMember();

		cidFloatMember.setAdditionalIdComponents(extension.getAdditionalIdComponents());
		cidFloatMember.setAnnotations(extension.getAnnotations());
		cidFloatMember.setAuthorUuid(extension.getAuthorUuid());
                cidFloatMember.setModuleUuid(extension.getModuleUuid());
		cidFloatMember.setComponentUuid(extension.getComponentUuid());
		cidFloatMember.setPathUuid(extension.getPathUuid());
		cidFloatMember.setPrimordialComponentUuid(extension.getPrimordialComponentUuid());
		cidFloatMember.setRefsetUuid(extension.getRefexUuid());
		cidFloatMember.setStatusUuid(extension.getStatusUuid());
		cidFloatMember.setTime(extension.getTime());
		
		UUID targetUnitId = null;
		Long scalar = null;
		
		if (targetMeasureUnitMap.get(extension.getUuid1()) != null) {
			targetUnitId = targetMeasureUnitMap.get(extension.getUuid1());
		} 
		
		if (targetUnitId != null) {
			cidFloatMember.setUuid1(targetUnitId);
		} else {
			cidFloatMember.setUuid1(extension.getUuid1());
		}
		
		if (scalarMap.get(extension.getUuid1()) != null) {
			scalar = scalarMap.get(extension.getUuid1());
		} 
		
		if (scalar != null) {
			cidFloatMember.setFloat1(new Float(extension.getInt1() * scalar));
		} else {
			cidFloatMember.setFloat1(extension.getInt1());
		}

		cidFloatMember.setRevisions(new ArrayList<TkRefexUuidFloatRevision>());
		if (extension.getRevisions() != null) {
			for (TkRefexUuidIntRevision cidIntRevision : extension.getRevisions()) {
				TkRefexUuidFloatRevision cidFloatRevision = new TkRefexUuidFloatRevision();
				cidFloatRevision.setAuthorUuid(cidIntRevision.getAuthorUuid());
                                cidFloatRevision.setModuleUuid(cidIntRevision.getModuleUuid());
				cidFloatRevision.setPathUuid(cidIntRevision.getPathUuid());
				cidFloatRevision.setStatusUuid(cidIntRevision.getStatusUuid());
				cidFloatRevision.setTime(cidIntRevision.getTime());
				
				if (targetUnitId != null) {
					cidFloatRevision.setUuid1(targetUnitId);
				} else {
					cidFloatRevision.setUuid1(cidIntRevision.getUuid1());
				}
				
				if (scalar != null) {
					cidFloatRevision.setFloat1(new Float(cidIntRevision.getInt1() * scalar));
				} else {
					cidFloatRevision.setFloat1(cidIntRevision.getInt1());
				}
				
				cidFloatMember.getRevisions().add(cidFloatRevision);
			}
		}
		
		return cidFloatMember;
		
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
	 * Gets the scalar map.
	 *
	 * @return the scalar map
	 */
	public Map<UUID, Long> getScalarMap() {
		return scalarMap;
	}

	/**
	 * Sets the scalar map.
	 *
	 * @param scalarMap the scalar map
	 */
	public void setScalarMap(Map<UUID, Long> scalarMap) {
		this.scalarMap = scalarMap;
	}

	/**
	 * Gets the target measure unit map.
	 *
	 * @return the target measure unit map
	 */
	public Map<UUID, UUID> getTargetMeasureUnitMap() {
		return targetMeasureUnitMap;
	}

	/**
	 * Sets the target measure unit map.
	 *
	 * @param targetMeasureUnitMap the target measure unit map
	 */
	public void setTargetMeasureUnitMap(Map<UUID, UUID> targetMeasureUnitMap) {
		this.targetMeasureUnitMap = targetMeasureUnitMap;
	}

}
