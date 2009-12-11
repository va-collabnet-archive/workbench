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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
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

	private I_TermFactory tf = LocalVersionedTerminology.get();

	private I_IntSet allowedStatuses;

	private Set<I_Position> positions;

	private HashMap<String, BufferedWriter> writerMap = new HashMap<String, BufferedWriter>();

	private HashMap<Integer, RefsetType> refsetTypeMap = new HashMap<Integer, RefsetType>();
	
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
			exportRefsets(concept.getConceptId());
			
			//export relationship refsets
			for (I_RelVersioned rel : concept.getSourceRels()) {
				processRelationship(rel);
			}
			
			// export description refsets
			for (I_DescriptionVersioned desc : concept.getDescriptions()) {
				processDescription(desc);
			}
			
			//export the status refset for this concept
			I_ConceptAttributePart latest = null;
			for (I_ConceptAttributeTuple tuple : concept.getConceptAttributeTuples(allowedStatuses, positions)) {
				if (latest == null || latest.getVersion() < tuple.getVersion()) {
					latest = tuple.getPart();
				}
			}
			if (latest == null) {
				throw new MojoExecutionException("Concept " + concept + " is exportable for specification " 
						+ exportSpecifications + " but has no parts valid for statuses " 
						+ allowedStatuses + " and positions " + positions);
			}

			extractStatus(latest, concept.getConceptId());
			extractDefinitionType(latest, concept.getConceptId());
		}
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
			
			extractStatus(latest, descId);
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
				
				extractRelationshipRefinability(latest, relId);
				extractStatus(latest, relId);
			}
		}
	}

	private void extractStatus(I_AmPart latest, int relId) throws TerminologyException, IOException, InstantiationException, IllegalAccessException {
		I_ThinExtByRefPartConcept tuple = (I_ThinExtByRefPartConcept) getCurrentExtension(relId, ConceptConstants.STATUS_REASON_EXTENSION);
		if (tuple == null) {
			// if the status is INACTIVE or ACTIVE there is no need for a reason. For simplicity, CURRENT will be treated this way too,
			if (latest.getStatusId() != getNid(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE)
				&& latest.getStatusId() != getNid(org.dwfa.cement.ArchitectonicAuxiliary.Concept.INACTIVE)
				&& latest.getStatusId() != getNid(org.dwfa.cement.ArchitectonicAuxiliary.Concept.CURRENT)) {
				//no extension at all
				tuple = tf.newConceptExtensionPart();
				tuple.setConceptId(latest.getStatusId());
				tuple.setPathId(latest.getPathId());
				tuple.setStatus(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
				tuple.setVersion(latest.getVersion());
				export(tuple, ConceptConstants.STATUS_REASON_EXTENSION.localize().getNid(), relId);
			}
		} else if (tuple.getConceptId() != latest.getStatusId()) {
			//add a new row with the latest refinability			
			tuple.setConceptId(latest.getStatusId());
			export((I_ThinExtByRefTuple) tuple);
		}
	}

	private void extractDefinitionType(I_ConceptAttributePart latest, int conceptId) throws IOException, TerminologyException, InstantiationException, IllegalAccessException {
		I_ThinExtByRefPartConcept tuple = (I_ThinExtByRefPartConcept) getCurrentExtension(conceptId, ConceptConstants.DEFINITION_TYPE_EXTENSION);
		if (tuple == null) {
			//no extension at all
			tuple = tf.newConceptExtensionPart();
			tuple.setConceptId(latest.isDefined() ? getNid(Concept.DEFINED_DEFINITION) : getNid(Concept.PRIMITIVE_DEFINITION));
			tuple.setPathId(latest.getPathId());
			tuple.setStatus(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			tuple.setVersion(latest.getVersion());
			export(tuple, ConceptConstants.DEFINITION_TYPE_EXTENSION.localize().getNid(), conceptId);
		} else if (tuple.getConceptId() != latest.getStatusId()) {
			//add a new row with the latest refinability
			tuple.setConceptId(latest.isDefined() ? getNid(Concept.DEFINED_DEFINITION) : getNid(Concept.PRIMITIVE_DEFINITION));
			export((I_ThinExtByRefTuple) tuple);
		}
	}

	private int getNid(org.dwfa.cement.ArchitectonicAuxiliary.Concept concept) throws TerminologyException, IOException {
		return tf.uuidToNative(concept.getUids());
	}


	private void extractRelationshipRefinability(I_RelPart latest, int relId)
			throws IOException, TerminologyException, InstantiationException,
			IllegalAccessException {
		I_ThinExtByRefPartConcept tuple = (I_ThinExtByRefPartConcept) getCurrentExtension(relId, ConceptConstants.RELATIONSHIP_REFINABILITY_EXTENSION);
		if (tuple == null) {
			//no extension at all
			tuple = tf.newConceptExtensionPart();
			tuple.setConceptId(latest.getRefinabilityId());
			tuple.setPathId(latest.getPathId());
			tuple.setStatus(org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			tuple.setVersion(latest.getVersion());
			export(tuple, ConceptConstants.RELATIONSHIP_REFINABILITY_EXTENSION.localize().getNid(), relId);
		} else if (tuple.getConceptId() != latest.getRefinabilityId()) {
			//add a new row with the latest refinability			
			tuple.setConceptId(latest.getRefinabilityId());
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

	private void export(I_ThinExtByRefTuple thinExtByRefTuple) throws IOException, TerminologyException, InstantiationException, IllegalAccessException {
		export(thinExtByRefTuple.getPart(), thinExtByRefTuple.getRefsetId(), thinExtByRefTuple.getComponentId());
	}
	
	private void export(I_ThinExtByRefPart thinExtByRefPart, int refsetId, int componentId) throws IOException, TerminologyException, InstantiationException, IllegalAccessException {
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
			String refsetName = tf.getConcept(refsetId).getInitialText();
			
			//TODO this is not the best way, but it works for now.
			refsetName = refsetName.replace("/", "-");
			refsetName = refsetName.replace("'", "_");
			
			uuidRefsetWriter = new BufferedWriter(new FileWriter(
					new File(uuidRefsetOutputDirectory, "UUID_" + refsetName + refsetType.getFileExtension())));
			sctIdRefsetWriter = new BufferedWriter(new FileWriter(
					new File(sctidRefsetOutputDirectory, "SCTID_" + refsetName + refsetType.getFileExtension())));
			
			writerMap.put(refsetId + "UUID", uuidRefsetWriter);
			writerMap.put(refsetId + "SCTID", sctIdRefsetWriter);
			
			uuidRefsetWriter.write(refsetType.getRefsetHandler().getHeaderLine());
			uuidRefsetWriter.newLine();

			sctIdRefsetWriter.write(refsetType.getRefsetHandler().getHeaderLine());
			sctIdRefsetWriter.newLine();
		}
		
		//note that we are assuming that the type of refset member will be the same as previous for this file type
		//if not we'll get a class cast exception, as we probably should
		uuidRefsetWriter.write(refsetType.getRefsetHandler().formatRefsetLine(tf, thinExtByRefPart, refsetId, componentId, false));
		uuidRefsetWriter.newLine();

		sctIdRefsetWriter.write(refsetType.getRefsetHandler().formatRefsetLine(tf, thinExtByRefPart, refsetId, componentId, true));
		sctIdRefsetWriter.newLine();
	}

	private boolean testSpecification(I_GetConceptData concept) throws Exception {
		for (ExportSpecification spec : exportSpecifications) {
			if (spec.test(concept)) {
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


}
