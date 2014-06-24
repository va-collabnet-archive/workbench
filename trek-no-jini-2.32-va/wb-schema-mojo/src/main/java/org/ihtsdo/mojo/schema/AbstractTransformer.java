/*
 * 
 * 
 */
package org.ihtsdo.mojo.schema;

import java.util.ArrayList;
import java.util.List;

import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;

/**
 * The AbstractTransformer class is the template used to create transformers. <br>
 * There are 4 phases in the transformation <br>
 * <ul>
 *    <li>Pre process iteration: used to pre-cache any required information that will be used during the concept iteration.</li>
 *    <ul><li>Example: collect refset members that later will be added as annotations in concepts.</li></ul>
 *    <li>Concept transformation: the methods to transform each of the of the concept components need to be implemented according to the use case.</li>
 *    <li>Post process concept: used to apply changes after going through the concept parts. This stage will return true for normally writing the concept to the database or false to skipt the write concept and save it for the post-process iteration.</li>
 *    <ul><li>Example: this method returns false when finds the refset concept, and saves the refset concept at the end of the iteration with new members attached.</li></ul>
 *    <li>Post process iteration: final stage to write changes after the complete transformation. Returns a list of EConcepts that will be written to the database.</li>
 *    <ul><li>Example: annotations are collected and removed from members through the iteration, and at the end the refset cncept is written with all the members.</li></ul>
 * </ul>
 * <p>
 * All transformers should implement a String id.
 */
public abstract class AbstractTransformer {
	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public abstract String getId();

	/**
	 * Setups preferences from the configuration XML file. Uses TransformersConfigApi.
	 *
	 * @param xmlFile the new up from xml
	 * @throws Exception the exception
	 */
	public abstract void setupFromXml(String xmlFile) throws Exception; 

	/**
	 * Transforms attributes.
	 *
	 * @param attributes the attributes
	 * @param concept the concept
	 */
	public abstract void transformAttributes(TkConceptAttributes attributes, TkConcept concept);

    /**
	 * Transforms a description.
	 *
	 * @param description the description
	 * @param concept the concept
	 */
	public abstract void transformDescription(TkDescription description, TkConcept concept);
	
	/**
	 * Transforms a relationship.
	 *
	 * @param relationship the relationship
	 * @param concept the concept
	 */
	public abstract void transformRelationship(TkRelationship relationship, TkConcept concept);
	
	/**
	 * Transforms an annotation.
	 *
	 * @param annotation the annotation
	 * @param component the component
	 */
	public abstract void transformAnnotation(TkRefexAbstractMember<?> annotation, TkComponent<?> component);
	
	/**
	 * Transforms a generic member. Called from both transformAnnotation and transformMember to avoid code duplication.
	 *
	 * @param member the member
	 * @param concept the concept
	 */
	public abstract void transformMember(TkRefexAbstractMember<?> member, TkConcept concept);
	
	/**
	 * Post process concept.
	 *
	 * @return true, if the concept should be written now to the output jbin file. False for saving skipping.
	 */
	public abstract boolean postProcessConcept(TkConcept eConcept);
	
	/**
	 * Pre process iteration.
	 */
	public abstract void preProcessIteration();
	
	/**
	 * Post process iteration.
	 *
	 * @return the list of concepts that should be written at the end of the iteration
	 */
	public abstract List<TkConcept> postProcessIteration();

	/**
	 * Transform. Executes the complete transformation cycle for one EConcept.
	 *
	 * @param eConcept the e concept
	 * @return true, if should write, according to what's returned in post-process-concept stage
	 */
	public boolean transform(TkConcept eConcept) {
		transformAttributes(eConcept.conceptAttributes, eConcept);

		if (eConcept.getConceptAttributes().getAnnotations() != null) {
			List<TkRefexAbstractMember<?>> annotationsReadOnly = new ArrayList<>(eConcept.getConceptAttributes().getAnnotations());
			for (TkRefexAbstractMember<?> loopAnnotation : annotationsReadOnly) {
				transformAnnotation(loopAnnotation, eConcept.getConceptAttributes());
			}
		}

		List<TkDescription> descriptionsReadOnly = new ArrayList<>(eConcept.getDescriptions());
		for (TkDescription loopDescription : descriptionsReadOnly) {
			if (loopDescription.getAnnotations() != null) {
				List<TkRefexAbstractMember<?>> annotationsReadOnly = new ArrayList<>(loopDescription.getAnnotations());
				for (TkRefexAbstractMember<?> loopAnnotation : annotationsReadOnly) {
					transformAnnotation(loopAnnotation, loopDescription);
				}
			}
			transformDescription(loopDescription, eConcept);
		}

		List<TkRelationship> relationshipsReadOnly =  new ArrayList<>(eConcept.getRelationships());
		for (TkRelationship loopRelationship : relationshipsReadOnly) {
			transformRelationship(loopRelationship, eConcept);
			if (loopRelationship.getAnnotations() != null) {
				List<TkRefexAbstractMember<?>> annotationsReadOnly = new ArrayList<>(loopRelationship.getAnnotations());
				for (TkRefexAbstractMember<?> loopAnnotation : annotationsReadOnly) {
					transformAnnotation(loopAnnotation, loopRelationship);
				}
			}
		}

		if (eConcept.getRefsetMembers() != null) {
		List<TkRefexAbstractMember<?>> membersReadOnly = new ArrayList<>(eConcept.getRefsetMembers());
			for (TkRefexAbstractMember<?> loopMember : membersReadOnly) {
				transformMember(loopMember, eConcept);
				if (loopMember.getAnnotations() != null) {
					List<TkRefexAbstractMember<?>> annotationsReadOnly = new ArrayList<>(loopMember.getAnnotations());
					for (TkRefexAbstractMember<?> loopAnnotation : annotationsReadOnly) {
						transformAnnotation(loopAnnotation, loopMember);
					}
				}
			}
		}

		return postProcessConcept(eConcept);
	}
}
