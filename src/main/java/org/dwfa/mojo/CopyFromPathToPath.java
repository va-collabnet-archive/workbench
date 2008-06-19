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
import org.dwfa.ace.api.I_ProcessConceptAttributes;
import org.dwfa.ace.api.I_ProcessDescriptions;
import org.dwfa.ace.api.I_ProcessExtByRef;
import org.dwfa.ace.api.I_ProcessIds;
import org.dwfa.ace.api.I_ProcessImages;
import org.dwfa.ace.api.I_ProcessRelationships;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
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
public class CopyFromPathToPath extends AbstractMojo implements
		I_ProcessConceptAttributes, I_ProcessDescriptions, I_ProcessExtByRef,
		I_ProcessIds, I_ProcessImages, I_ProcessRelationships {

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
	
	private I_IntSet fromPathIds;
	private int toPathId;
	private int versionTime;
	private int statusId = 0;
	private I_TermFactory tf;

	public void execute() throws MojoExecutionException, MojoFailureException {
		tf = LocalVersionedTerminology.get();
		try {
			List<I_GetConceptData> fromPathConcepts = new ArrayList<I_GetConceptData>();
			for (ConceptDescriptor fromPath : fromPaths) {
				fromPathConcepts.add(fromPath.getVerifiedConcept());
			}
			getFromPathIds(fromPathConcepts);
			
			toPathId = toPath.getVerifiedConcept().getConceptId();
			
			if (status != null) {
				statusId = status.getVerifiedConcept().getConceptId();
			}
			
			if (releaseTime != null) {
				versionTime = tf.convertToThinVersion(releaseTime.getTime());
			} else {
				versionTime = Integer.MAX_VALUE;
			}
			
			tf.iterateConceptAttributes(this);
			tf.iterateDescriptions(this);
			tf.iterateExtByRefs(this);
			tf.iterateIds(this);
			tf.iterateImages(this);
			tf.iterateRelationships(this);
		} catch (Exception e) {
			throw new MojoExecutionException("failed copying from paths "
					+ fromPaths + " to path " + toPath, e);
		}

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
		for (I_ConceptAttributeTuple t : conceptAttributeVersioned.getTuples()) {
			if (fromPathIds.contains(t.getPathId())) {
				I_ConceptAttributePart newPart = t.duplicatePart();
				newPart.setPathId(toPathId);
				newPart.setVersion(versionTime);
				if (statusId != 0) {
					newPart.setConceptStatus(statusId);
				}
				t.getConVersioned().addVersion(newPart);
			}
		}
	}

	public void processDescription(I_DescriptionVersioned descriptionVersioned)
			throws Exception {
		for (I_DescriptionTuple t : descriptionVersioned.getTuples()) {

			if (fromPathIds.contains(t.getPathId())) {

				I_DescriptionPart newPart = t.duplicatePart();
				newPart.setPathId(toPathId);
				newPart.setVersion(versionTime);
				if (statusId != 0) {
					newPart.setStatusId(statusId);
				}
				t.getDescVersioned().addVersion(newPart);
			}
		}
	}

	public void processExtensionByReference(I_ThinExtByRefVersioned extByRef)
			throws Exception {
		for (I_ThinExtByRefTuple t : extByRef.getTuples(null, null, true)) {
			if (fromPathIds.contains(t.getPathId())) {
				I_ThinExtByRefPart newPart = t.duplicatePart();
				newPart.setPathId(toPathId);
				newPart.setVersion(versionTime);
				if (statusId != 0) {
					newPart.setStatus(statusId);
				}
				t.getCore().addVersion(newPart);
			}
		}
	}

	public void processId(I_IdVersioned idVersioned) throws Exception {
		for (I_IdTuple t : idVersioned.getTuples()) {

			if (fromPathIds.contains(t.getPathId())) {
				I_IdPart newPart = t.duplicatePart();
				newPart.setPathId(toPathId);
				newPart.setVersion(versionTime);
				if (statusId != 0) {
					newPart.setIdStatus(statusId);
				}
				t.getIdVersioned().addVersion(newPart);
			}
		}
	}

	public void processImages(I_ImageVersioned imageVersioned) throws Exception {
		for (I_ImageTuple t : imageVersioned.getTuples()) {

			if (fromPathIds.contains(t.getPathId())) {
				I_ImagePart newPart = t.duplicatePart();
				newPart.setPathId(toPathId);
				newPart.setVersion(versionTime);
				if (statusId != 0) {
					newPart.setStatusId(statusId);
				}
				t.getVersioned().addVersion(newPart);
			}
		}
	}

	public void processRelationship(I_RelVersioned relVersioned)
			throws Exception {
		for (I_RelTuple t : relVersioned.getTuples()) {

			if (fromPathIds.contains(t.getPathId())) {
				I_RelPart newPart = t.duplicatePart();
				newPart.setPathId(toPathId);
				newPart.setVersion(versionTime);
				if (statusId != 0) {
					newPart.setStatusId(statusId);
				}
				t.getRelVersioned().addVersion(newPart);
			}
		}
	}

}
