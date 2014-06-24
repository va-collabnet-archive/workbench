/*
 * 
 */
package org.ihtsdo.mojo.schema.transformer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.dwfa.util.id.Type3UuidFactory;

import org.ihtsdo.mojo.schema.AbstractTransformer;
import org.ihtsdo.mojo.schema.config.TransformersConfigApi;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributesRevision;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.description.TkDescriptionRevision;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrRevision;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipRevision;

/**
 * The Transformer AnnotationToRefset.<br>
 * Transforms an annotation style extension to a refset style extension. All annotations are removed from components and saved as members of the same refset.
 */
public class UUIDChangeBasedOnMapTransformer extends AbstractTransformer {

	/** The id. */
	private final String id = "uuid-based-on-map";

	/** The concept count. */
	private transient int conceptCount = 0;

	private File mappingFile;

	private HashMap<UUID,UUID> mapping;

	/**
	 * Instantiates a new annotation to refset.
	 */
	public UUIDChangeBasedOnMapTransformer() {
	}

	/**
	 * Instantiates a new annotation to refset.
	 *
	 * @param refsetUuid the refset uuid
	 */
	public UUIDChangeBasedOnMapTransformer(File mappingFile) {
		super();
		this.mappingFile = mappingFile;
		mapping = new HashMap<UUID,UUID>();

		try{
			FileInputStream fstream = new FileInputStream(mappingFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			br.readLine();
			while ((strLine = br.readLine()) != null)   {
				try {
					String columns[] = strLine.split("\t");
					String sctid = columns[3];
					String uuid = columns[4];
					UUID authoritativeUuid = UUID.fromString(uuid);
					UUID algorithmicUuid = Type3UuidFactory.fromSNOMED(sctid.trim());
					mapping.put(algorithmicUuid, authoritativeUuid);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			in.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#setupFromXml(java.lang.String)
	 */
	@Override
	public void setupFromXml(String xmlFile) throws Exception {
		TransformersConfigApi api = new TransformersConfigApi(xmlFile);
		mappingFile = new File(api.getValueAt(api.getIntId(id), "parameters.mappingFile"));
		if (!mappingFile.exists()) {
			throw new FileNotFoundException("Not found: " + mappingFile.getPath());
		}
		mapping = new HashMap<UUID,UUID>();
		try{
			FileInputStream fstream = new FileInputStream(mappingFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			br.readLine();
			while ((strLine = br.readLine()) != null)   {
				try {
					String columns[] = strLine.split("\t");
					String sctid = columns[3];
					String uuid = columns[4];
					UUID authoritativeUuid = UUID.fromString(uuid);
					UUID algorithmicUuid = Type3UuidFactory.fromSNOMED(sctid.trim());
					mapping.put(algorithmicUuid, authoritativeUuid);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			in.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformAttributes(org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes)
	 */
	@Override
	public void transformAttributes(TkConceptAttributes attributes, TkConcept concept) {
                assert attributes.pathUuid != null : attributes;
                assert attributes.authorUuid != null : attributes;
                assert attributes.statusUuid != null : attributes;
                assert attributes.primordialUuid != null : attributes;
                
		attributes.setAuthorUuid(transformWithMap(attributes.getAuthorUuid()));
                attributes.setModuleUuid(transformWithMap(attributes.getModuleUuid()));
		attributes.setPathUuid(transformWithMap(attributes.getPathUuid()));
		attributes.setPrimordialComponentUuid(transformWithMap(attributes.getPrimordialComponentUuid()));
		attributes.setStatusUuid(transformWithMap(attributes.getStatusUuid()));
		if (attributes.getRevisions() != null) {
			for (TkConceptAttributesRevision revision : attributes.getRevisions()) {
				revision.setAuthorUuid(transformWithMap(revision.getAuthorUuid()));
                                revision.setModuleUuid(transformWithMap(revision.getModuleUuid()));
				revision.setPathUuid(transformWithMap(revision.getPathUuid()));
				revision.setStatusUuid(transformWithMap(revision.getStatusUuid()));
			}
		}
	}

    /* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformDescription(org.ihtsdo.tk.dto.concept.component.description.TkDescription)
	 */
	@Override
	public void transformDescription(TkDescription description, TkConcept concept) {
                assert description.pathUuid != null : description;
                assert description.authorUuid != null : description;
                assert description.statusUuid != null : description;
                assert description.primordialUuid != null : description;
		description.setAuthorUuid(transformWithMap(description.getAuthorUuid()));
                description.setModuleUuid(transformWithMap(description.getModuleUuid()));
		description.setPathUuid(transformWithMap(description.getPathUuid()));
		description.setPrimordialComponentUuid(transformWithMap(description.getPrimordialComponentUuid()));
		description.setStatusUuid(transformWithMap(description.getStatusUuid()));
		description.setConceptUuid(transformWithMap(description.getConceptUuid()));
		if (description.getRevisions() != null) {
			description.setTypeUuid(transformWithMap(description.getTypeUuid()));
			for (TkDescriptionRevision revision : description.getRevisions()) {
				revision.setAuthorUuid(transformWithMap(revision.getAuthorUuid()));
                                revision.setModuleUuid(transformWithMap(revision.getModuleUuid()));
				revision.setPathUuid(transformWithMap(revision.getPathUuid()));
				revision.setStatusUuid(transformWithMap(revision.getStatusUuid()));
				revision.setTypeUuid(transformWithMap(revision.getTypeUuid()));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformRelationship(org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship)
	 */
	@Override
	public void transformRelationship(TkRelationship relationship, TkConcept concept) {
                assert relationship.pathUuid != null : relationship;
                assert relationship.authorUuid != null : relationship;
                assert relationship.statusUuid != null : relationship;
                assert relationship.primordialUuid != null : relationship;
		relationship.setAuthorUuid(transformWithMap(relationship.getAuthorUuid()));
                relationship.setModuleUuid(transformWithMap(relationship.getModuleUuid()));
		relationship.setPathUuid(transformWithMap(relationship.getPathUuid()));
		relationship.setPrimordialComponentUuid(transformWithMap(relationship.getPrimordialComponentUuid()));
		relationship.setStatusUuid(transformWithMap(relationship.getStatusUuid()));
		relationship.setC1Uuid(transformWithMap(relationship.getRelationshipSourceUuid()));
		relationship.setC2Uuid(transformWithMap(relationship.getRelationshipTargetUuid()));
		relationship.setCharacteristicUuid(transformWithMap(relationship.getCharacteristicUuid()));
		relationship.setRefinabilityUuid(transformWithMap(relationship.getRefinabilityUuid()));
		relationship.setTypeUuid(transformWithMap(relationship.getTypeUuid()));
		if (relationship.getRevisions() != null) {
			for (TkRelationshipRevision revision : relationship.getRevisions()) {
				revision.setAuthorUuid(transformWithMap(revision.getAuthorUuid()));
                                revision.setModuleUuid(transformWithMap(revision.getModuleUuid()));
				revision.setPathUuid(transformWithMap(revision.getPathUuid()));
				revision.setStatusUuid(transformWithMap(revision.getStatusUuid()));
				revision.setCharacteristicUuid(transformWithMap(revision.getCharacteristicUuid()));
				revision.setRefinabilityUuid(transformWithMap(revision.getRefinabilityUuid()));
				revision.setTypeUuid(transformWithMap(revision.getTypeUuid()));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#transformAnnotation(org.ihtsdo.tk.dto.concept.component.refset.TkRefexAbstractMember, org.ihtsdo.tk.dto.concept.component.TkComponent)
	 */
	@Override
	public void transformAnnotation(TkRefexAbstractMember<?> annotation,
			TkComponent<?> component) {
                assert annotation.pathUuid != null : annotation;
                assert annotation.authorUuid != null : annotation;
                assert annotation.statusUuid != null : annotation;
                assert annotation.primordialUuid != null : annotation;
		if (annotation instanceof TkRefexUuidMember) {
			TkRefexUuidMember annotationCid = (TkRefexUuidMember) annotation;
			annotationCid.setAuthorUuid(transformWithMap(annotationCid.getAuthorUuid()));
                        annotationCid.setModuleUuid(transformWithMap(annotationCid.getModuleUuid()));
			annotationCid.setPathUuid(transformWithMap(annotationCid.getPathUuid()));
			annotationCid.setPrimordialComponentUuid(transformWithMap(annotationCid.getPrimordialComponentUuid()));
			annotationCid.setStatusUuid(transformWithMap(annotationCid.getStatusUuid()));
			annotationCid.setRefsetUuid(transformWithMap(annotationCid.getRefexUuid()));
			annotationCid.setComponentUuid(transformWithMap(annotationCid.getComponentUuid()));
			annotationCid.setUuid1(transformWithMap(annotationCid.getUuid1()));
			if (annotationCid.getRevisions() != null) {
				for (TkRefexUuidRevision revision : annotationCid.getRevisions()) {
					revision.setAuthorUuid(transformWithMap(revision.getAuthorUuid()));
                                        revision.setModuleUuid(transformWithMap(revision.getModuleUuid()));
					revision.setPathUuid(transformWithMap(revision.getPathUuid()));
					revision.setStatusUuid(transformWithMap(revision.getStatusUuid()));
					revision.setUuid1(transformWithMap(revision.getUuid1()));
				}
			}
		} else if (annotation instanceof TkRefsetStrMember) {
			TkRefsetStrMember annotationStr = (TkRefsetStrMember) annotation;
			annotationStr.setAuthorUuid(transformWithMap(annotationStr.getAuthorUuid()));
                        annotationStr.setModuleUuid(transformWithMap(annotationStr.getModuleUuid()));
			annotationStr.setPathUuid(transformWithMap(annotationStr.getPathUuid()));
			annotationStr.setPrimordialComponentUuid(transformWithMap(annotationStr.getPrimordialComponentUuid()));
			annotationStr.setStatusUuid(transformWithMap(annotationStr.getStatusUuid()));
			annotationStr.setRefsetUuid(transformWithMap(annotationStr.getRefexUuid()));
			annotationStr.setComponentUuid(transformWithMap(annotationStr.getComponentUuid()));
			if (annotationStr.getRevisions() != null) {
				for (TkRefsetStrRevision revision : annotationStr.getRevisions()) {
					revision.setAuthorUuid(transformWithMap(revision.getAuthorUuid()));
                                        revision.setModuleUuid(transformWithMap(revision.getModuleUuid()));
					revision.setPathUuid(transformWithMap(revision.getPathUuid()));
					revision.setStatusUuid(transformWithMap(revision.getStatusUuid()));
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
                assert member.pathUuid != null : member;
                assert member.authorUuid != null : member;
                assert member.statusUuid != null : member;
                assert member.primordialUuid != null : member;
		if (member instanceof TkRefexUuidMember) {
			TkRefexUuidMember memberCid = (TkRefexUuidMember) member;
			memberCid.setAuthorUuid(transformWithMap(memberCid.getAuthorUuid()));
                        memberCid.setModuleUuid(transformWithMap(memberCid.getModuleUuid()));
			memberCid.setPathUuid(transformWithMap(memberCid.getPathUuid()));
			memberCid.setPrimordialComponentUuid(transformWithMap(memberCid.getPrimordialComponentUuid()));
			memberCid.setStatusUuid(transformWithMap(memberCid.getStatusUuid()));
			memberCid.setRefsetUuid(transformWithMap(memberCid.getRefexUuid()));
			memberCid.setComponentUuid(transformWithMap(memberCid.getComponentUuid()));
			memberCid.setUuid1(transformWithMap(memberCid.getUuid1()));
			if (memberCid.getRevisions() != null) {
				for (TkRefexUuidRevision revision : memberCid.getRevisions()) {
					revision.setAuthorUuid(transformWithMap(revision.getAuthorUuid()));
                                        revision.setModuleUuid(transformWithMap(revision.getModuleUuid()));
					revision.setPathUuid(transformWithMap(revision.getPathUuid()));
					revision.setStatusUuid(transformWithMap(revision.getStatusUuid()));
					revision.setUuid1(transformWithMap(revision.getUuid1()));
				}
			}
		} else if (member instanceof TkRefsetStrMember) {
			TkRefsetStrMember memberStr = (TkRefsetStrMember) member;
			memberStr.setAuthorUuid(transformWithMap(memberStr.getAuthorUuid()));
                        memberStr.setModuleUuid(transformWithMap(memberStr.getModuleUuid()));
			memberStr.setPathUuid(transformWithMap(memberStr.getPathUuid()));
			memberStr.setPrimordialComponentUuid(transformWithMap(memberStr.getPrimordialComponentUuid()));
			memberStr.setStatusUuid(transformWithMap(memberStr.getStatusUuid()));
			memberStr.setRefsetUuid(transformWithMap(memberStr.getRefexUuid()));
			memberStr.setComponentUuid(transformWithMap(memberStr.getComponentUuid()));
			if (memberStr.getRevisions() != null) {
				for (TkRefsetStrRevision revision : memberStr.getRevisions()) {
					revision.setAuthorUuid(transformWithMap(revision.getAuthorUuid()));
                                        revision.setModuleUuid(transformWithMap(revision.getModuleUuid()));
					revision.setPathUuid(transformWithMap(revision.getPathUuid()));
					revision.setStatusUuid(transformWithMap(revision.getStatusUuid()));
				}
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
		List<TkConcept> postProcessList = new ArrayList<>();
		return postProcessList;
	}


	/* (non-Javadoc)
	 * @see org.ihtsdo.mojo.schema.AbstractTransformer#preProcessIteration()
	 */
	@Override
	public void preProcessIteration() {
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

	public UUID transformWithMap(UUID uuid) {
            assert uuid != null;
		if (mapping.containsKey(uuid)) {
			return mapping.get(uuid);
		} else {
			return uuid;
		}
	}

}
