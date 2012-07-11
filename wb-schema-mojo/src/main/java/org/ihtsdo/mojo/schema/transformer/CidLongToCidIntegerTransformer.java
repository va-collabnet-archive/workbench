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
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.mojo.db.ConceptDescriptor;
import org.ihtsdo.mojo.schema.AbstractTransformer;
import org.ihtsdo.mojo.schema.config.TransformersConfigApi;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_int.TkRefexUuidIntMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_int.TkRefexUuidIntRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_long.TkRefexUuidLongMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_long.TkRefexUuidLongRevision;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;

/**
 * The Transformer CidLongToCidIntegerTransformer.
 * Transform a concept-long extension to a concept-integer extension.<br>
 * It is also possible to change the associated concept, for those cases where the concept represents a unit of measure. The source and target concepts in the xml preferences should be updated accordingly.<br>
 * When the unit of measure changes, some times it's also necessary to change the values accordingly. A scalar figure can be provided for value transformation (the scalar will be multiplied by the value, a scalar of 1 will result in the same value).
 */
public class CidLongToCidIntegerTransformer extends AbstractTransformer {
	
	/** The refset uuid. */
	private UUID refsetUuid;
	
	/** The id. */
	private final String id = "cid-long-to-cid-int";
	
	/** The concept count. */
	private transient int conceptCount = 0;
	
	/** The scalar map. */
	private Map<UUID, Long> scalarMap;
	
	/** The target measure unit map. */
	private Map<UUID, UUID> targetMeasureUnitMap;

	/**
	 * Instantiates a new boolean to enumerated transformer.
	 */
	public CidLongToCidIntegerTransformer() {
	}

	/**
	 * Instantiates a new boolean to enumerated transformer.
	 *
	 * @param refsetUuid the refset uuid
	 * @param scalarMap the scalar map
	 * @param targetMeasureUnitMap the target measure unit map
	 */
	public CidLongToCidIntegerTransformer(UUID refsetUuid, Map<UUID, Long> scalarMap, Map<UUID, UUID> targetMeasureUnitMap) {
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
		scalarMap = new HashMap<UUID,Long>();
		targetMeasureUnitMap = new HashMap<UUID,UUID>();
		
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
			UUID cidLongTypeId = RefsetAuxiliary.Concept.CID_LONG_EXTENSION.getPrimoridalUid();
			if (relationship.getTypeUuid().equals(refsetTypeRelId) && 
					relationship.getRelationshipTargetUuid().equals(cidLongTypeId) &&
					relationship.getRelationshipSourceUuid().equals(refsetUuid)) {
				relationship.setC2Uuid(cidIntTypeId);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformMember(org.ihtsdo.tk.dto.concept.component.refset.TkRefexAbstractMember, org.ihtsdo.tk.dto.concept.TkConcept)
	 */
	@Override
	public void transformMember(TkRefexAbstractMember<?> member, TkConcept concept) {
		if (member.getRefexUuid().equals(refsetUuid)) {
			TkRefexUuidLongMember cidLongMember = (TkRefexUuidLongMember) member;
			TkRefexUuidIntMember cidIntMember = transformExtension(cidLongMember);

			concept.getRefsetMembers().remove(cidLongMember);
			concept.getRefsetMembers().add(cidIntMember);
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
			TkRefexUuidLongMember cidLongMember = (TkRefexUuidLongMember) annotation;
			TkRefexUuidIntMember cidIntMember = transformExtension(cidLongMember);

			component.getAnnotations().remove(cidIntMember);
			component.getAnnotations().add(cidLongMember);
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
	public  TkRefexUuidIntMember transformExtension(TkRefexUuidLongMember extension) {
		TkRefexUuidIntMember cidIntMember = new TkRefexUuidIntMember();

		cidIntMember.setAdditionalIdComponents(extension.getAdditionalIdComponents());
		cidIntMember.setAnnotations(extension.getAnnotations());
		cidIntMember.setAuthorUuid(extension.getAuthorUuid());
                cidIntMember.setModuleUuid(extension.getModuleUuid());
		cidIntMember.setComponentUuid(extension.getComponentUuid());
		cidIntMember.setPathUuid(extension.getPathUuid());
		cidIntMember.setPrimordialComponentUuid(extension.getPrimordialComponentUuid());
		cidIntMember.setRefsetUuid(extension.getRefexUuid());
		cidIntMember.setStatusUuid(extension.getStatusUuid());
		cidIntMember.setTime(extension.getTime());
		
		UUID targetUnitId = null;
		Long scalar = null;
		
		if (targetMeasureUnitMap.get(extension.getUuid1()) != null) {
			targetUnitId = targetMeasureUnitMap.get(extension.getUuid1());
		} 
		
		if (targetUnitId != null) {
			cidIntMember.setUuid1(targetUnitId);
		} else {
			cidIntMember.setUuid1(extension.getUuid1());
		}
		
		if (scalarMap.get(extension.getUuid1()) != null) {
			scalar = scalarMap.get(extension.getUuid1());
		} 
		
		if (scalar != null) {
			cidIntMember.setInt1(Math.round(scalar * extension.getLong1()));
		} else {
			cidIntMember.setInt1(Math.round(extension.getLong1()));
		}

		cidIntMember.setRevisions(new ArrayList<TkRefexUuidIntRevision>());
		if (extension.getRevisions() != null) {
			for (TkRefexUuidLongRevision cidLongRevision : extension.getRevisions()) {
				TkRefexUuidIntRevision cidIntRevision = new TkRefexUuidIntRevision();
				cidIntRevision.setAuthorUuid(cidLongRevision.getAuthorUuid());
                                cidIntMember.setModuleUuid(cidLongRevision.getModuleUuid());
				cidIntRevision.setPathUuid(cidLongRevision.getPathUuid());
				cidIntRevision.setStatusUuid(cidLongRevision.getStatusUuid());
				cidIntRevision.setTime(cidLongRevision.getTime());
				
				if (targetUnitId != null) {
					cidIntRevision.setUuid1(targetUnitId);
				} else {
					cidIntRevision.setUuid1(cidLongRevision.getUuid1());
				}
				
				if (scalar != null) {
					cidIntRevision.setInt1(Math.round(scalar * cidLongRevision.getLong1()));
				} else {
					cidIntRevision.setInt1(Math.round(cidLongRevision.getLong1()));
				}
				
				cidIntMember.getRevisions().add(cidIntRevision);
			}
		}
		
		return cidIntMember;
		
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
