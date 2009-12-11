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
package org.dwfa.mojo.refset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.mojo.ConceptConstants;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.mojo.refset.writers.MemberRefsetHandler;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.spec.ConceptSpec;

/**
 * 
 * This mojo exports reference sets from an ACE database
 * 
 * @goal refset-export
 * @author Dion McMurtrie
 */
public class ReferenceSetExport extends AbstractMojo implements I_ProcessConcepts {

	/**
	 * Export specification that dictates which concepts are exported and which are not. Only reference sets
	 * whose identifying concept is exported will be exported. Only members relating to components that will 
	 * be exported will in turn be exported.
	 * <p>
	 * For example if you have a reference set identified by concept A, and members B, C and D. If the export spec
	 * does not include exporting concept A then none of the reference set will be exported. However if the
	 * export spec does include A, but not C then the reference set will be exported except it will only have
	 * members B and D - C will be omitted.
	 * @parameter
	 * @required
	 */
	ExportSpecification[] exportSpecifications;
	
	/**
	 * Defines the directory to which the UUID based reference sets are exported
	 * 
	 * @parameter
	 * @required
	 */
	File uuidRefsetOutputDirectory;
	
	/**
	 * Defines the directory to which the SCTID based reference sets are exported
	 * 
	 * @parameter
	 * @required
	 */
	File sctidRefsetOutputDirectory;
	
	/**
	 * Directory where the fixed SCTID map is located
	 * @parameter
	 * @required
	 */
	File fixedMapDirectory;
	
	/**
	 * Directory where the read/write SCTID maps are stored
	 * @parameter
	 * @required
	 */
	File readWriteMapDirectory;

    /**
	 * Release version used to embed in the refset file names - if not specified
     * then the "path version" reference set is used to determine the version
	 * @parameter
	 */
    String releaseVersion;

	private I_TermFactory tf = LocalVersionedTerminology.get();

	private I_IntSet allowedStatuses;

	private Set<I_Position> positions;

	private HashMap<String, BufferedWriter> writerMap = new HashMap<String, BufferedWriter>();

	private HashMap<Integer, RefsetType> refsetTypeMap = new HashMap<Integer, RefsetType>();

	private HashMap<Integer, String> pathReleaseVersions = new HashMap<Integer, String>();

    public void execute() throws MojoExecutionException, MojoFailureException {
		if (!fixedMapDirectory.exists() || !fixedMapDirectory.isDirectory() || !fixedMapDirectory.canRead()) {
			throw new MojoExecutionException("Cannot proceed, fixedMapDirectory must exist and be readable");
		}
		
		if (!readWriteMapDirectory.exists() || !readWriteMapDirectory.isDirectory() || !readWriteMapDirectory.canRead()) {
			throw new MojoExecutionException("Cannot proceed, readWriteMapDirectory must exist and be readable");
		}
		
		try {
			allowedStatuses = tf.newIntSet();
			positions = new HashSet<I_Position>();
			for (ExportSpecification spec : exportSpecifications) {
				for (PositionDescriptor pd : spec.getPositionsForExport()) {
					positions.add(pd.getPosition());
				}
				for (ConceptDescriptor status : spec.getStatusValuesForExport()) {
					allowedStatuses.add(status.getVerifiedConcept().getConceptId());
				}
			}

			sctidRefsetOutputDirectory.mkdirs();
			uuidRefsetOutputDirectory.mkdirs();
			
			MemberRefsetHandler.setFixedMapDirectory(fixedMapDirectory);
			MemberRefsetHandler.setReadWriteMapDirectory(readWriteMapDirectory);
			
			tf.iterateConcepts(this);
			
			for (BufferedWriter writer : writerMap.values()) {
				writer.close();
			}
			
			MemberRefsetHandler.cleanup();
			
		} catch (Exception e) {
			throw new MojoExecutionException("exporting reference sets failed for specification " + exportSpecifications, e);
		}
	}

	
	public void processConcept(I_GetConceptData concept) throws Exception {
		if (testSpecification(concept)) {
			//export the status refset for this concept
			I_ConceptAttributePart latest = getLatestAttributePart(concept);
			if (latest == null) {
				getLog().warn("Concept " + concept + " is exportable for specification " 
						+ exportSpecifications + " but has no parts valid for statuses " 
						+ allowedStatuses + " and positions " + positions);
				return;
			}
			
			exportRefsets(concept.getConceptId());
			
			//export relationship refsets
			for (I_RelVersioned rel : concept.getSourceRels()) {
				processRelationship(rel);
			}
			
			// export description refsets
			for (I_DescriptionVersioned desc : concept.getDescriptions()) {
				processDescription(desc);
			}

			//TODO commented out because it costs too many SCTIDs and we need to release pathology - to be included later
		//	extractStatus(latest, concept.getConceptId());
		//	extractDefinitionType(latest, concept.getConceptId());
		}
	}


	private I_ConceptAttributePart getLatestAttributePart(
			I_GetConceptData concept) throws IOException {
		I_ConceptAttributePart latest = null;
		for (I_ConceptAttributeTuple tuple : concept.getConceptAttributeTuples(allowedStatuses, positions)) {
			if (latest == null || latest.getVersion() < tuple.getVersion()) {
				latest = tuple.getPart();
			}
		}
		return latest;
	}


	private void processDescription(I_DescriptionVersioned versionedDesc)
			throws Exception {
		boolean exportableVersionFound = false;
		I_DescriptionPart latest = null;
		for (I_DescriptionPart part : versionedDesc.getVersions()) {
			if (testSpecification(part.getTypeId())
					&& allowedStatuses.contains(part.getStatusId())
					&& checkPath(part.getPathId())) {
				
				exportableVersionFound = true;
				if (latest == null || latest.getVersion() < part.getVersion()) {
					latest = part;
				}

			}
		}

		if (exportableVersionFound) {
			// found a valid version of this relationship for export
			// therefore export its extensions
			int descId = versionedDesc.getDescId();
			exportRefsets(descId);
			
			//TODO commented out because it costs too many SCTIDs and we need to release pathology - to be included later
			//extractStatus(latest, descId);
		}
	}


	private void processRelationship(I_RelVersioned versionedRel)
			throws Exception {
		if (testSpecification(versionedRel.getC2Id())) {
			boolean exportableVersionFound = false;
			I_RelPart latest = null;
			for (I_RelPart part : versionedRel.getVersions()) {
				if (testSpecification(part.getCharacteristicId())
						&& testSpecification(part.getPathId())
						&& testSpecification(part.getRefinabilityId())
						&& testSpecification(part.getRelTypeId())
						&& allowedStatuses.contains(part.getStatusId())
						&& checkPath(part.getPathId())) {
					
					exportableVersionFound = true;
					if (latest == null || latest.getVersion() < part.getVersion()) {
						latest = part;
					}
				}
			}
			if (exportableVersionFound) {
				// found a valid version of this relationship for export
				// therefore export its extensions
				int relId = versionedRel.getRelId();
				exportRefsets(relId);
				

				//TODO commented out because it costs too many SCTIDs and we need to release pathology - to be included later
			//	extractRelationshipRefinability(latest, relId);
			//	extractStatus(latest, relId);
			}
		}
	}

	private void extractStatus(I_AmPart latest, int relId) throws Exception {
		I_ThinExtByRefTuple tuple = getCurrentExtension(relId, ConceptConstants.STATUS_REASON_EXTENSION);
		I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) tuple;
		if (part == null) {
			// if the status is INACTIVE or ACTIVE there is no need for a reason. For simplicity, CURRENT will be treated this way too,
			if (latest.getStatusId() != getNid(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE)
				&& latest.getStatusId() != getNid(org.dwfa.cement.ArchitectonicAuxiliary.Concept.INACTIVE)
				&& latest.getStatusId() != getNid(org.dwfa.cement.ArchitectonicAuxiliary.Concept.CURRENT)) {
				//no extension at all
				part = tf.newConceptExtensionPart();
				part.setConceptId(latest.getStatusId());
				part.setPathId(latest.getPathId());
				part.setStatus(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
				part.setVersion(latest.getVersion());
				export(part, null, ConceptConstants.STATUS_REASON_EXTENSION.localize().getNid(), relId);
			}
		} else if (part.getConceptId() != latest.getStatusId()) {
			//add a new row with the latest refinability			
			part.setConceptId(latest.getStatusId());
			export((I_ThinExtByRefTuple) part);
		}
	}

	private void extractDefinitionType(I_ConceptAttributePart latest, int conceptId) throws Exception {
		I_ThinExtByRefTuple tuple = getCurrentExtension(conceptId, ConceptConstants.DEFINITION_TYPE_EXTENSION);
		I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) tuple;
		if (part == null) {
			//no extension at all
			part = tf.newConceptExtensionPart();
			part.setConceptId(latest.isDefined() ? getNid(Concept.DEFINED_DEFINITION) : getNid(Concept.PRIMITIVE_DEFINITION));
			part.setPathId(latest.getPathId());
			part.setStatus(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			part.setVersion(latest.getVersion());
			export(part, null, ConceptConstants.DEFINITION_TYPE_EXTENSION.localize().getNid(), conceptId);
		} else if (part.getConceptId() != latest.getStatusId()) {
			//add a new row with the latest refinability
			part.setConceptId(latest.isDefined() ? getNid(Concept.DEFINED_DEFINITION) : getNid(Concept.PRIMITIVE_DEFINITION));
			export((I_ThinExtByRefTuple) part);
		}
	}

	private int getNid(org.dwfa.cement.ArchitectonicAuxiliary.Concept concept) throws TerminologyException, IOException {
		return tf.uuidToNative(concept.getUids());
	}


	private void extractRelationshipRefinability(I_RelPart latest, int relId)
			throws Exception {
		I_ThinExtByRefTuple tuple = getCurrentExtension(relId, ConceptConstants.RELATIONSHIP_REFINABILITY_EXTENSION);
		I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) tuple;
		if (part == null) {
			//no extension at all
			part = tf.newConceptExtensionPart();
			part.setConceptId(latest.getRefinabilityId());
			part.setPathId(latest.getPathId());
			part.setStatus(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			part.setVersion(latest.getVersion());
			export(part, null, ConceptConstants.RELATIONSHIP_REFINABILITY_EXTENSION.localize().getNid(), relId);
		} else if (part.getConceptId() != latest.getRefinabilityId()) {
			//add a new row with the latest refinability			
			part.setConceptId(latest.getRefinabilityId());
			export((I_ThinExtByRefTuple) tuple);
		}
	}


	private I_ThinExtByRefTuple getCurrentExtension(int componentId, ConceptSpec relationshipRefinabilityExtension) throws IOException {
		int refsetId = relationshipRefinabilityExtension.localize().getNid();
		
		I_ThinExtByRefTuple latest = null;
		for (I_ThinExtByRefVersioned ext : tf.getAllExtensionsForComponent(componentId)) {
			if (ext.getRefsetId() == refsetId) {
				for (I_ThinExtByRefTuple tuple : ext.getTuples(allowedStatuses, positions, false)) {
					if (latest == null || latest.getVersion() < tuple.getVersion()) {
						latest = tuple;
					}
				}
			}
		}
		return latest;
	}


	private void exportRefsets(int componentId) throws TerminologyException, Exception {
		List<I_ThinExtByRefVersioned> extensions = tf.getAllExtensionsForComponent(componentId);
		for (I_ThinExtByRefVersioned thinExtByRefVersioned : extensions) {
			if (testSpecification(thinExtByRefVersioned.getRefsetId())) {
				for (I_ThinExtByRefTuple thinExtByRefTuple : thinExtByRefVersioned.getTuples(allowedStatuses, positions, false)) {
					export(thinExtByRefTuple);
				}
			}
		}
	}

	private void export(I_ThinExtByRefTuple thinExtByRefTuple) throws Exception {
		export(thinExtByRefTuple.getPart(), thinExtByRefTuple.getMemberId(), thinExtByRefTuple.getRefsetId(), thinExtByRefTuple.getComponentId());
	}
	
	private void export(I_ThinExtByRefPart thinExtByRefPart, Integer memberId, int refsetId, int componentId) throws Exception {
		RefsetType refsetType = refsetTypeMap.get(refsetId);
		if (refsetType == null) {
			try {
				refsetType = RefsetType.findByExtension(thinExtByRefPart);
			} catch (EnumConstantNotPresentException e) {
				getLog().warn("No handler for tuple " + thinExtByRefPart + " of type " + thinExtByRefPart.getClass(), e);
				return;
			}
			refsetTypeMap.put(refsetId, refsetType);
		}

		BufferedWriter uuidRefsetWriter = writerMap.get(refsetId + "UUID");
		BufferedWriter sctIdRefsetWriter = writerMap.get(refsetId + "SCTID");
		if (sctIdRefsetWriter == null) {
			//must not have written to this file yet
			I_GetConceptData refsetConcept = tf.getConcept(refsetId);
			String refsetName = getPreferredTerm(refsetConcept);
			
			//TODO this is not the best way, but it works for now.
			refsetName = refsetName.replace("/", "-");
			refsetName = refsetName.replace("'", "_");

            if (releaseVersion == null) {
                releaseVersion = getReleaseVersion(refsetConcept);
            }

			uuidRefsetWriter = new BufferedWriter(new FileWriter(
					new File(uuidRefsetOutputDirectory, "UUID_" + refsetName + "_" + releaseVersion + refsetType.getFileExtension())));
			sctIdRefsetWriter = new BufferedWriter(new FileWriter(
					new File(sctidRefsetOutputDirectory, "SCTID_" + refsetName + "_" + releaseVersion + refsetType.getFileExtension())));
			
			writerMap.put(refsetId + "UUID", uuidRefsetWriter);
			writerMap.put(refsetId + "SCTID", sctIdRefsetWriter);
			
			uuidRefsetWriter.write(refsetType.getRefsetHandler().getHeaderLine());
			uuidRefsetWriter.newLine();

			sctIdRefsetWriter.write(refsetType.getRefsetHandler().getHeaderLine());
			sctIdRefsetWriter.newLine();
		}
		
		//note that we are assuming that the type of refset member will be the same as previous for this file type
		//if not we'll get a class cast exception, as we probably should
		uuidRefsetWriter.write(refsetType.getRefsetHandler().formatRefsetLine(tf, thinExtByRefPart, memberId, refsetId, componentId, false));
		uuidRefsetWriter.newLine();

		sctIdRefsetWriter.write(refsetType.getRefsetHandler().formatRefsetLine(tf, thinExtByRefPart, memberId, refsetId, componentId, true));
		sctIdRefsetWriter.newLine();
	}

	private String getReleaseVersion(I_GetConceptData refsetConcept) throws Exception {
		
		if (pathReleaseVersions.containsKey(refsetConcept.getConceptId())) {
			return pathReleaseVersions.get(refsetConcept.getConceptId());
		} else {
			int pathid = getLatestAttributePart(refsetConcept).getPathId();
			
			String pathUuidStr = Integer.toString(pathid);
			try {
				String pathVersion = null;
				pathUuidStr = tf.getUids(pathid).iterator().next().toString();
				
				int pathVersionRefsetNid = tf.uuidToNative(org.dwfa.ace.refset.ConceptConstants.PATH_VERSION_REFSET.getUuids()[0]);
				int currentStatusId = tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
				for (I_ThinExtByRefVersioned extension : tf.getAllExtensionsForComponent(pathid)) {
					if (extension.getRefsetId() == pathVersionRefsetNid) {
						I_ThinExtByRefPart latestPart = getLatestVersion(extension);
						if (latestPart.getStatusId() == currentStatusId) {
							
							if (pathVersion != null) {
								throw new TerminologyException("Concept contains multiple extensions for refset" +
										org.dwfa.ace.refset.ConceptConstants.PATH_VERSION_REFSET.getDescription());
							}
							
							pathVersion = ((I_ThinExtByRefPartString) latestPart).getStringValue();
						}
					}
				}
				
				if (pathVersion == null) {
					throw new TerminologyException("Concept not a member of " + 
							org.dwfa.ace.refset.ConceptConstants.PATH_VERSION_REFSET.getDescription());					 
				}

				String releaseVersion = getPreferredTerm(tf.getConcept(pathid)) + "_" + pathVersion;
				pathReleaseVersions.put(refsetConcept.getConceptId(), releaseVersion);
				return releaseVersion;
				
			} catch (Exception e) {
				throw new RuntimeException("Failed to obtain the release version for the path " + pathUuidStr, e);
			}
		}
	}

	private I_ThinExtByRefPart getLatestVersion(I_ThinExtByRefVersioned extension) {
		I_ThinExtByRefPart latestPart = null;
		for (I_ThinExtByRefPart part : extension.getVersions()) {
			if (latestPart == null || part.getVersion() >= latestPart.getVersion()) {
				latestPart = part;
			}
		}
		return latestPart;
	}
	
	private boolean testSpecification(I_GetConceptData concept) throws Exception {
		for (ExportSpecification spec : exportSpecifications) {
			if (spec.test(concept) && getLatestAttributePart(concept) != null) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean testSpecification(int id) throws TerminologyException, IOException, Exception {
		return testSpecification(tf.getConcept(id));
	}


	private boolean checkPath(int pathId) {
		for (I_Position position : positions) {
			if (position.getPath().getConceptId() == pathId) {
				return true;
			}
		}
		return false;
	}

	private String getPreferredTerm(I_GetConceptData conceptData) throws Exception {
        I_IntList descTypeList = tf.newIntList();
        descTypeList.add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());      
        
        I_IntSet statusSet = tf.newIntSet();
        statusSet.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
        statusSet.add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
        
        I_DescriptionTuple descTuple = conceptData.getDescTuple(descTypeList, statusSet, positions);
        if (descTuple == null) {
        	UUID conceptUuid = conceptData.getUids().iterator().next();
        	throw new MojoExecutionException("Unable to obtain preferred term for concept " + conceptUuid.toString());
        }
        
		return descTuple.getText();
	}
	
	
	
}
