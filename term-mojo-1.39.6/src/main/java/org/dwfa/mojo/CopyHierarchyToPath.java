/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.mojo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdTuple;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_WriteDirectToDb;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.mojo.refset.ExportSpecification;

/**
 * Given a root node, this mojo will copy the latest version of every component in this hierarchy
 * o the destination path if it isn't already on that path.
 * 
 * @goal copy-hierarchy-to-path
 * 
 */
public class CopyHierarchyToPath extends AbstractMojo implements I_ProcessConcepts {

	/**
	 * Path to copy the data to
	 * 
	 * @parameter
	 * @required
	 */
	ConceptDescriptor toPath = null;
	
	/**
	 * Root node to copy data from
	 * @parameter
	 * @required
	 */
	ConceptDescriptor rootNode = null;

	/**
	 * Relationship type for the hierarchy - defaults to SNOMED "Is a"
	 * @parameter
	 */
	ConceptDescriptor[] hierarchyRelationshipTypes = null;
	
	/**
	 * Allowed statuses for the hierarchy - defaults to active or a child of active
	 * @parameter
	 */
	ConceptDescriptor[] hierarchyStatuses = null;
	
	/**
	 * Exclusion from the hierarchy
	 * @parameter
	 */
	ExportSpecification[] exclusions = null;
	
	/**
	 * Flag that indicates if the just the latest data or all history should be copied - defaults to latest only
	 * @parameter
	 */
	boolean latestStateOnly = true;
	
	private int toPathId;
	
	private I_TermFactory tf;

	private I_WriteDirectToDb directInterface;

	private int conceptAttributeCount;

	private int descriptionCount;

	private int extCount;

	private int idCount;

	private int imageCount;

	private int relCount;

	private int conceptCount;

	private I_IntSet allowedStatus;

	public void execute() throws MojoExecutionException, MojoFailureException {
		tf = LocalVersionedTerminology.get();

		try {
		
			if (hierarchyRelationshipTypes == null) {
				
				hierarchyRelationshipTypes = new ConceptDescriptor[]{
						new ConceptDescriptor("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25", "Is a (attribute)")
				};
			}
			
			if (hierarchyStatuses == null) {
				
				// Initialise the array of allowed statuses (if none are provide from the configuration)
				// with the status concept "active" and its children
				
				ConceptDescriptor activeStatus = new ConceptDescriptor("32dc7b19-95cc-365e-99c9-5095124ebe72","active");
				ConceptDescriptor currentStatus = new ConceptDescriptor("2faa9261-8fb2-11db-b606-0800200c9a66","current");
				
				hierarchyStatuses = new ConceptDescriptor[] { activeStatus, currentStatus };
				
				Set<I_GetConceptData> children = activeStatus.getVerifiedConcept().getDestRelOrigins(
						toIntSet(hierarchyStatuses), toIntSet(hierarchyRelationshipTypes), null, false);
				
				ArrayList<ConceptDescriptor> statusDescriptors = new ArrayList<ConceptDescriptor>();
				statusDescriptors.add(activeStatus);
				for (I_GetConceptData child : children) {
					ConceptDescriptor childStatus = new ConceptDescriptor();
					childStatus.setDescription(child.getInitialText());
					childStatus.setUuid(child.getUids().get(0).toString());
					statusDescriptors.add(childStatus);
				}
				
				hierarchyStatuses = statusDescriptors.toArray(hierarchyStatuses);
			}
				
			allowedStatus = toIntSet(hierarchyStatuses);
		
			toPathId = toPath.getVerifiedConcept().getConceptId();
			directInterface = tf.getDirectInterface();
			
			processAllChildren(rootNode.getVerifiedConcept(), toIntSet(hierarchyRelationshipTypes), allowedStatus);
		} catch (Exception e) {
			throw new MojoExecutionException("Failed executing hierarchy copy due to exception", e);
		}

	}
	
	private I_IntSet toIntSet(ConceptDescriptor[] concepts) throws Exception {
		I_IntSet intset = tf.newIntSet();
		for (ConceptDescriptor conceptDescriptor : concepts) {
			intset.add(conceptDescriptor.getVerifiedConcept().getConceptId());
		}
		return intset;
	}

	private void processAllChildren(I_GetConceptData concept, I_IntSet allowedType, I_IntSet allowedStatus) throws Exception {
		processConcept(concept);
		
		Set<I_GetConceptData> children = concept.getDestRelOrigins(allowedStatus, allowedType, null, false);
		for (I_GetConceptData getConceptData : children) {
			processAllChildren(getConceptData, allowedType, allowedStatus);
		}
	}

	public void processConcept(I_GetConceptData concept) throws Exception {
		if (++conceptCount % 1000 == 0) {
			getLog().info("processed concept " + conceptCount);
		}
		
		if (inExclusions(concept)) {
			getLog().info("Suppressed copy of " + concept + " due to exclusions");
		} else {
			getLog().info("concept " + concept + " copied to path " + toPath);
			
			processConceptAttributes(concept.getConceptAttributes());
			processDescription(concept.getDescriptions());
			for (I_ThinExtByRefVersioned ext : tf.getRefsetExtensionMembers(concept.getConceptId())) {
				processExtensionByReference(ext);
			}
			processId(concept.getId());
			processImages(concept.getImages());
			processRelationship(concept.getSourceRels());
		}
	}

	private boolean inExclusions(I_GetConceptData concept) throws Exception {
		if (exclusions == null) {
			return false;
		}
		for (ExportSpecification spec : exclusions) {
			if (spec.test(concept)) {
				return true;
			}
		}
		return false;
	}

	public void processConceptAttributes(
			I_ConceptAttributeVersioned conceptAttributeVersioned)
			throws Exception {
		
		if (++conceptAttributeCount % 1000 == 0) {
			getLog().info("processed concept attribute " + conceptAttributeCount);
		}
		
		boolean datachanged = false;
		I_ConceptAttributeTuple latestPart = null;
		for (I_ConceptAttributeTuple t : conceptAttributeVersioned.getTuples()) {
			if (latestStateOnly) {
				if (latestPart == null || t.getVersion() > latestPart.getVersion()) {
					latestPart = t;
				}
			} else if (toPathId != t.getPathId()) {
				duplicateConceptAttributeTuple(t);
				datachanged = true;
			}
		}
		if (latestStateOnly && latestPart != null && latestPart.getPathId() != toPathId
				&& allowedStatus.contains(latestPart.getConceptStatus())) {
			duplicateConceptAttributeTuple(latestPart);
			datachanged = true;
		}
		if (datachanged) {
			directInterface.writeConceptAttributes(conceptAttributeVersioned);
		}
	}

	private void duplicateConceptAttributeTuple(
			I_ConceptAttributeTuple latestPart) {
		I_ConceptAttributePart newPart = latestPart.duplicatePart();
		newPart.setPathId(toPathId);
		latestPart.getConVersioned().addVersion(newPart);
		getLog().info("concept attribute part copied " + latestPart);
	}

	private void processDescription(List<I_DescriptionVersioned> descriptions) throws Exception {
		for (I_DescriptionVersioned descriptionVersioned : descriptions) {
			processDescription(descriptionVersioned);
		}
	}
	
	public void processDescription(I_DescriptionVersioned descriptionVersioned)
			throws Exception {
		
		if (++descriptionCount % 1000 == 0) {
			getLog().info("processed description " + descriptionCount);
		}
		
		boolean datachanged = false;
		I_DescriptionTuple latestPart = null;
		
		for (I_DescriptionTuple t : descriptionVersioned.getTuples()) {
			if (latestStateOnly) {
				if (latestPart == null || t.getVersion() > latestPart.getVersion()) {
					latestPart = t;
				}
			} else if (toPathId != t.getPathId()) {
				duplicateDescriptionTuple(t);
				datachanged = true;
			}
		}
		
		if (latestStateOnly && latestPart != null && latestPart.getPathId() != toPathId
				&& allowedStatus.contains(latestPart.getStatusId())) {
			duplicateDescriptionTuple(latestPart);
			datachanged = true;
		}

		if (datachanged) {
			directInterface.writeDescription(descriptionVersioned);
		}
	}

	private void duplicateDescriptionTuple(I_DescriptionTuple t) {
		I_DescriptionPart newPart = t.duplicatePart();
		newPart.setPathId(toPathId);
		t.getDescVersioned().addVersion(newPart);

		getLog().info("concept description part copied " + t);
	}

	public void processExtensionByReference(I_ThinExtByRefVersioned extByRef)
			throws Exception {

		if (++extCount % 1000 == 0) {
			getLog().info("processed extension " + extCount);
		}
		
		boolean datachanged = false;
		I_ThinExtByRefTuple latestPart = null;
		for (I_ThinExtByRefTuple t : extByRef.getTuples(null, null, true)) {
			if (latestStateOnly) {
				if (latestPart == null || t.getVersion() > latestPart.getVersion()) {
					latestPart = t;
				}
			} else if (toPathId != t.getPathId()) {
				duplicateExtensionTuple(t);
				datachanged = true;
			}
		}
		if (latestStateOnly && latestPart != null && latestPart.getPathId() != toPathId
				&& allowedStatus.contains(latestPart.getStatusId())) {
			duplicateExtensionTuple(latestPart);
			datachanged = true;
		}
		
		if (datachanged) {
			directInterface.writeExt(extByRef);
		}
	}

	private void duplicateExtensionTuple(I_ThinExtByRefTuple t) {
		I_ThinExtByRefPart newPart = t.duplicatePart();
		newPart.setPathId(toPathId);
		t.getCore().addVersion(newPart);

		getLog().info("concept refset part copied " + t);
	}

	public void processId(I_IdVersioned idVersioned) throws Exception {

		if (++idCount % 1000 == 0) {
			getLog().info("processed id " + idCount);
		}
		
		boolean datachanged = false;
		I_IdTuple latestPart = null;
		for (I_IdTuple t : idVersioned.getTuples()) {
			if (latestStateOnly) {
				if (latestPart == null || t.getVersion() > latestPart.getVersion()) {
					latestPart = t;
				}
			} else if (toPathId != t.getPathId()) {
				duplicateIdTuple(t);
				datachanged = true;
			}
		}
		if (latestStateOnly && latestPart != null && latestPart.getPathId() != toPathId
				&& allowedStatus.contains(latestPart.getIdStatus())) {
			duplicateIdTuple(latestPart);
			datachanged = true;
		}

		if (datachanged) {
			directInterface.writeId(idVersioned);
		}
	}

	private void duplicateIdTuple(I_IdTuple t) {
		I_IdPart newPart = t.duplicatePart();
		newPart.setPathId(toPathId);
		t.getIdVersioned().addVersion(newPart);

		getLog().info("concept id part copied " + t);
	}

	private void processImages(List<I_ImageVersioned> images) throws Exception {
		for (I_ImageVersioned imageVersioned : images) {
			processImages(imageVersioned);
		}
	}
	
	public void processImages(I_ImageVersioned imageVersioned) throws Exception {

		if (++imageCount % 1000 == 0) {
			getLog().info("processed image " + imageCount);
		}
		
		boolean datachanged = false;
		I_ImageTuple latestPart = null;
		for (I_ImageTuple t : imageVersioned.getTuples()) {
			if (latestStateOnly) {
				if (latestPart == null || t.getVersion() > latestPart.getVersion()) {
					latestPart = t;
				}
			} else if (toPathId != t.getPathId()) {
				duplicateImageTuple(t);
				datachanged = true;
			}
		}
		if (latestStateOnly && latestPart != null && latestPart.getPathId() != toPathId
				&& allowedStatus.contains(latestPart.getStatusId())) {
			duplicateImageTuple(latestPart);
			datachanged = true;
		}

		if (datachanged) {
			directInterface.writeImage(imageVersioned);
		}
	}

	private void duplicateImageTuple(I_ImageTuple t) {
		I_ImagePart newPart = t.duplicatePart();
		newPart.setPathId(toPathId);
		t.getVersioned().addVersion(newPart);
		
		getLog().info("concept image part copied " + t);
	}

	private void processRelationship(List<I_RelVersioned> sourceRels) throws Exception {
		for (I_RelVersioned relVersioned : sourceRels) {
			processRelationship(relVersioned);
		}
	}
	
	public void processRelationship(I_RelVersioned relVersioned)
			throws Exception {

		if (++relCount % 1000 == 0) {
			getLog().info("processed relationship " + relCount);
		}
		
		boolean datachanged = false;
		I_RelTuple latestPart = null;
		for (I_RelTuple t : relVersioned.getTuples()) {
			if (latestStateOnly) {
				if (latestPart == null || t.getVersion() > latestPart.getVersion()) {
					latestPart = t;
				}
			} else if (toPathId != t.getPathId()) {
				duplicateRelationshipTuple(t);
				datachanged = true;
			}
		}
		if (latestStateOnly && latestPart != null && latestPart.getPathId() != toPathId
				&& allowedStatus.contains(latestPart.getStatusId())) {
			duplicateRelationshipTuple(latestPart);
			datachanged = true;
		}

		if (datachanged) {
			directInterface.writeRel(relVersioned);
		}
	}

	private void duplicateRelationshipTuple(I_RelTuple t) {
		I_RelPart newPart = t.duplicatePart();
		newPart.setPathId(toPathId);
		t.getRelVersioned().addVersion(newPart);
		
		getLog().info("concept relationship part copied " + t);
	}

}
