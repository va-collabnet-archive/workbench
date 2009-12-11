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
import java.util.Collection;
import java.util.Date;
import java.util.List;

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
import org.dwfa.cement.ArchitectonicAuxiliary;

/**
 * Copies from all the specified paths and their children to the new path. Note that this
 * mojo will only copy content that is explicitly on the origin paths, not inherited from
 * a parent path.
 * 
 * @goal copy-from-path-to-path
 * 
 */
public class CopyFromPathToPath extends AbstractMojo implements I_ProcessConcepts {

	/**
	 * Paths to copy the data from
	 * 
	 * @parameter
	 * @required
	 */
	ConceptDescriptor[] fromPaths;

	/**
	 * Path to copy the data to
	 * 
	 * @parameter
	 * @required
	 */
	ConceptDescriptor toPath;
	
	/**
	 * This status will be used to change all content to if set, otherwise the status of the
	 * components on the origin path will be used
	 * @parameter
	 */
	ConceptDescriptor status = null;
	
	/**
	 * The release time to stamp all copies with, otherwise NOW will be used
	 * 
	 * @parameter
	 */
	Date releaseTime = null;
	
	/**
	 * Indicate if all history or only the latest state of the objects should be copied - defaults to false
	 * 
	 * @parameter
	 */
	boolean latestStateOnly = false;
	
	private I_IntSet fromPathIds;
	private int toPathId;
	private int versionTime;
	private int statusId = 0;
	private I_TermFactory tf;

	private I_WriteDirectToDb directInterface;

	private int conceptAttributeCount;

	private int descriptionCount;

	private int extCount;

	private int idCount;

	private int imageCount;

	private int relCount;

	private int conceptCount;

	public void execute() throws MojoExecutionException, MojoFailureException {
		tf = LocalVersionedTerminology.get();
		try {
			List<I_GetConceptData> fromPathConcepts = new ArrayList<I_GetConceptData>();
			for (ConceptDescriptor fromPath : fromPaths) {
				fromPathConcepts.add(fromPath.getVerifiedConcept());
			}
			
			fromPathIds = tf.newIntSet();
			getFromPathIds(fromPathConcepts);
			
			toPathId = toPath.getVerifiedConcept().getConceptId();
			
			if (status != null) {
				statusId = status.getVerifiedConcept().getConceptId();
			}
			
			if (releaseTime != null) {
				versionTime = tf.convertToThinVersion(releaseTime.getTime());
			} else {
				versionTime = tf.convertToThinVersion(System.currentTimeMillis());
			}
			
			directInterface = tf.getDirectInterface();
			
			getLog().info("Starting to iterate concept attributes to copy from " + fromPaths + " to " + toPath);
			tf.iterateConcepts(this);
			
		} catch (Exception e) {
			throw new MojoExecutionException("failed copying from paths "
					+ fromPaths + " to path " + toPath, e);
		}

	}
	
	public void processConcept(I_GetConceptData arg0) throws Exception {
		if (++conceptCount % 1000 == 0) {
			getLog().info("processed concept " + conceptCount);
		}
		
		processConceptAttributes(arg0.getConceptAttributes());
		processDescription(arg0.getDescriptions());
		for (I_ThinExtByRefVersioned extension : tf.getAllExtensionsForComponent(arg0.getConceptId())) {
			processExtensionByReference(extension);
		}
		processId(arg0.getId());
		processImages(arg0.getImages());
		processRelationship(arg0.getSourceRels());
	}

	private void getFromPathIds(Collection<I_GetConceptData> pathDescriptors) throws Exception {
		for (I_GetConceptData fromPath : pathDescriptors) {
			fromPathIds.add(fromPath.getConceptId());
			
			I_IntSet isAIntSet = tf.newIntSet();
			isAIntSet.add(ConceptConstants.SNOMED_IS_A.localize().getNid());
			isAIntSet.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			getFromPathIds(fromPath.getSourceRelTargets(null, isAIntSet, null, true));
		}
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
			if (fromPathIds.contains(t.getPathId())) {
				if (latestStateOnly) {
					if (latestPart != null && t.getVersion() > latestPart.getVersion()) {
						latestPart = t;
					}
				} else {
					duplicateConceptAttributeTuple(t);
					datachanged = true;
				}
			}
		}
		if (latestStateOnly && latestPart != null) {
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
		newPart.setVersion(versionTime);
		if (statusId != 0) {
			newPart.setConceptStatus(statusId);
		}
		latestPart.getConVersioned().addVersion(newPart);
	}

	private void processDescription(List<I_DescriptionVersioned> descriptions) throws Exception {
		for (I_DescriptionVersioned descriptionVersioned : descriptions) {
			processDescription(descriptionVersioned);
			for (I_ThinExtByRefVersioned extension : tf.getAllExtensionsForComponent(descriptionVersioned.getDescId())) {
				processExtensionByReference(extension);
			}
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

			if (fromPathIds.contains(t.getPathId())) {
				if (latestStateOnly) {
					if (latestPart  != null && t.getVersion() > latestPart.getVersion()) {
						latestPart = t;
					}
				} else {
					duplicateDescriptionTuple(t);
					datachanged = true;
				}
			}
		}
		if (latestStateOnly && latestPart != null) {
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
		newPart.setVersion(versionTime);
		if (statusId != 0) {
			newPart.setStatusId(statusId);
		}
		t.getDescVersioned().addVersion(newPart);
	}

	public void processExtensionByReference(I_ThinExtByRefVersioned extByRef)
			throws Exception {

		if (++extCount % 1000 == 0) {
			getLog().info("processed extension " + extCount);
		}
		
		boolean datachanged = false;
		I_ThinExtByRefTuple latestPart = null;
		for (I_ThinExtByRefTuple t : extByRef.getTuples(null, null, true)) {
			if (fromPathIds.contains(t.getPathId())) {
				if (latestStateOnly) {
					if (latestPart   != null && t.getVersion() > latestPart.getVersion()) {
						latestPart = t;
					}
				} else {
					duplicateExtensionTuple(t);
					datachanged = true;
				}
			}
		}
		if (latestStateOnly && latestPart != null) {
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
		newPart.setVersion(versionTime);
		if (statusId != 0) {
			newPart.setStatus(statusId);
		}
		t.getCore().addVersion(newPart);
	}

	public void processId(I_IdVersioned idVersioned) throws Exception {

		if (++idCount % 1000 == 0) {
			getLog().info("processed id " + idCount);
		}
		
		boolean datachanged = false;
		I_IdTuple latestPart = null;
		for (I_IdTuple t : idVersioned.getTuples()) {

			if (fromPathIds.contains(t.getPathId())) {
				if (latestStateOnly) {
					if (latestPart    != null && t.getVersion() > latestPart.getVersion()) {
						latestPart = t;
					}
				} else {
					duplicateIdTuple(t);
					datachanged = true;
				}
			}
		}
		if (latestStateOnly && latestPart != null) {
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
		newPart.setVersion(versionTime);
		if (statusId != 0) {
			newPart.setIdStatus(statusId);
		}
		t.getIdVersioned().addVersion(newPart);
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

			if (fromPathIds.contains(t.getPathId())) {
				if (latestStateOnly) {
					if (latestPart     != null && t.getVersion() > latestPart.getVersion()) {
						latestPart = t;
					}
				} else {
					duplicateImageTuple(t);
					datachanged = true;
				}
			}
		}
		if (latestStateOnly && latestPart != null) {
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
		newPart.setVersion(versionTime);
		if (statusId != 0) {
			newPart.setStatusId(statusId);
		}
		t.getVersioned().addVersion(newPart);
	}

	private void processRelationship(List<I_RelVersioned> sourceRels) throws Exception {
		for (I_RelVersioned relVersioned : sourceRels) {
			processRelationship(relVersioned);
			for (I_ThinExtByRefVersioned extension : tf.getAllExtensionsForComponent(relVersioned.getRelId())) {
				processExtensionByReference(extension);
			}
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

			if (fromPathIds.contains(t.getPathId())) {
				if (latestStateOnly) {
					if (latestPart      != null && t.getVersion() > latestPart.getVersion()) {
						latestPart = t;
					}
				} else {
					duplicateRelationshipTuple(t);
					datachanged = true;
				}
			}
		}
		if (latestStateOnly && latestPart != null) {
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
		newPart.setVersion(versionTime);
		if (statusId != 0) {
			newPart.setStatusId(statusId);
		}
		t.getRelVersioned().addVersion(newPart);
	}

}
