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
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.mojo.db.ConceptDescriptor;
import org.ihtsdo.mojo.schema.AbstractTransformer;
import org.ihtsdo.mojo.schema.config.TransformersConfigApi;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;

/**
 * The Transformer RefsetToAnnotation.<br> Transforms a refset into an
 * annotation. The refset is retrieved in the pre-iteration phase, annotations
 * are added and the refset is stripped of members during the iteration.
 */
public class RefsetToAnnotationMulti extends AbstractTransformer {

    /**
     * The id.
     */
    private final String id = "refset-to-annotation-multi";
    /**
     * The concept count.
     */
    private transient int conceptCount = 0;
    /**
     * The refset uuid.
     */
    private List<UUID> refsetUuids;
    /**
     * The members map.
     */
    private Map<UUID, Map<UUID, TkRefsetAbstractMember<?>>> membersMaps;

    /**
     * Instantiates a new refset to annotation.
     */
    public RefsetToAnnotationMulti() {
    }


    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#setupFromXml(java.lang.String)
     */
    @Override
    public void setupFromXml(String xmlFile) throws Exception {
        TransformersConfigApi api = new TransformersConfigApi(xmlFile);
        refsetUuids = new ArrayList<UUID>();

        List<String> refsets = api.getCollectionAt(api.getIntId(id), "parameters.refsets");
        for (String entry : refsets) {
            refsetUuids.add(UUID.fromString(entry));
        }

    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#transformAttributes(org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes)
     */
    @Override
    public void transformAttributes(TkConceptAttributes attributes, TkConcept concept) {
        for (UUID key : membersMaps.keySet()) {
            Map<UUID, TkRefsetAbstractMember<?>> membersMap = membersMaps.get(key);
            if (membersMap.get(attributes.getPrimordialComponentUuid()) != null) {
                if (attributes.getAnnotations() == null) {
                    attributes.setAnnotations(new ArrayList<TkRefsetAbstractMember<?>>());
                }
                attributes.getAnnotations().add(membersMap.get(attributes.getPrimordialComponentUuid()));
                count();
            }
        }

    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#transformDescription(org.ihtsdo.tk.dto.concept.component.description.TkDescription)
     */
    @Override
    public void transformDescription(TkDescription description, TkConcept concept) {
        for (UUID key : membersMaps.keySet()) {
            Map<UUID, TkRefsetAbstractMember<?>> membersMap = membersMaps.get(key);
            if (membersMap.get(description.getPrimordialComponentUuid()) != null) {
                if (description.getAnnotations() == null) {
                    description.setAnnotations(new ArrayList<TkRefsetAbstractMember<?>>());
                }
                description.getAnnotations().add(membersMap.get(description.getPrimordialComponentUuid()));
                count();
            }
        }

    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#transformRelationship(org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship)
     */
    @Override
    public void transformRelationship(TkRelationship relationship, TkConcept concept) {
        for (UUID key : membersMaps.keySet()) {
            Map<UUID, TkRefsetAbstractMember<?>> membersMap = membersMaps.get(key);
            if (membersMap.get(relationship.getPrimordialComponentUuid()) != null) {
                if (relationship.getAnnotations() == null) {
                    relationship.setAnnotations(new ArrayList<TkRefsetAbstractMember<?>>());
                }
                relationship.getAnnotations().add(membersMap.get(relationship.getPrimordialComponentUuid()));
                count();
            }
        }
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#transformAnnotation(org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember,
     * org.ihtsdo.tk.dto.concept.component.TkComponent)
     */
    @Override
    public void transformAnnotation(TkRefsetAbstractMember<?> annotation,
            TkComponent<?> component) {
        // Not supported
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#transformMember(org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember,
     * org.ihtsdo.tk.dto.concept.TkConcept)
     */
    @Override
    public void transformMember(TkRefsetAbstractMember<?> member,
            TkConcept concept) {

        if (refsetUuids.contains(member.getRefsetUuid()) && concept.getConceptAttributes().getPrimordialComponentUuid().equals(member.getRefsetUuid())) {
            concept.getRefsetMembers().remove(member);
        }

        for (UUID key : membersMaps.keySet()) {
            Map<UUID, TkRefsetAbstractMember<?>> membersMap = membersMaps.get(key);
            if (membersMap.get(member.getPrimordialComponentUuid()) != null) {
                if (member.getAnnotations() == null) {
                    member.setAnnotations(new ArrayList<TkRefsetAbstractMember<?>>());
                }
                member.getAnnotations().add(membersMap.get(member.getPrimordialComponentUuid()));
                count();
            }
        }
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#postProcessConcept()
     */
    @Override
    public boolean postProcessConcept() {
        return true;
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#postProcessIteration()
     */
    @Override
    public List<EConcept> postProcessIteration() {
        System.out.println("**** Final, total converted " + conceptCount + " members");
        List<EConcept> postProcessList = new ArrayList<EConcept>();
        return postProcessList;
    }

    /**
     * Gets the refset uuid.
     *
     * @return the refset uuid
     */
    public List<UUID> getRefsetUuid() {
        return refsetUuids;
    }

    /**
     * Sets the refset uuid.
     *
     * @param refsetUuid the new refset uuid
     */
    public void setRefsetUuid(List<UUID> refsetUuids) {
        this.refsetUuids = refsetUuids;
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#preProcessIteration()
     */
    @Override
    public void preProcessIteration() {

        try {            
            for (UUID key : membersMaps.keySet()) {
                Map<UUID, TkRefsetAbstractMember<?>> membersMap = membersMaps.get(key);
                I_GetConceptData refset = Terms.get().getConcept(key);
                EConcept refsetEConcept = new EConcept(refset);
                System.out.println("**** Running refset-to-annotation conversion for: " + refset.toUserString());
                membersMap = new HashMap<UUID, TkRefsetAbstractMember<?>>();
                if (refsetEConcept.getRefsetMembers() != null) {
                    for (TkRefsetAbstractMember<?> loopMember : refsetEConcept.getRefsetMembers()) {
                        membersMap.put(loopMember.getComponentUuid(), loopMember);
                    }
                } else {
                    System.out.println("**** RefsetMembers collection is NULL");
                }
                System.out.println("**** Number of members to convert for this reset : " + membersMap.size());
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
