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
package org.ihtsdo.mojo.mojo.refset;

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
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.I_ConfigAceFrame.LANGUAGE_SORT_PREF;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.spec.ConceptSpec;
import org.ihtsdo.mojo.mojo.ConceptDescriptor;
import org.ihtsdo.mojo.mojo.PositionDescriptor;
import org.ihtsdo.mojo.mojo.refset.writers.MemberRefsetHandler;

/**
 * 
 * This mojo exports reference sets from an ACE database
 * 
 * @goal refset-export
 * @author Dion McMurtrie
 */
public class ReferenceSetExport extends AbstractMojo implements
		I_ProcessConcepts {

	/**
	 * Whether to use RF2 for the export. If not, the alternate release format
	 * will be used (this is also the default).
	 * 
	 * @parameter
	 */
	boolean useRF2 = false;

	/**
	 * RF2 Descriptor - this is required if useRF2 is set to true. This
	 * describes the module, namespace, content sub type and country information
	 * required to export in RF2.
	 * 
	 * @parameter
	 */
	RF2Descriptor rf2Descriptor;

	/**
	 * Export specification that dictates which concepts are exported and which
	 * are not. Only reference sets whose identifying concept is exported will
	 * be exported. Only members relating to components that will be exported
	 * will in turn be exported.
	 * <p>
	 * For example if you have a reference set identified by concept A, and
	 * members B, C and D. If the export spec does not include exporting concept
	 * A then none of the reference set will be exported. However if the export
	 * spec does include A, but not C then the reference set will be exported
	 * except it will only have members B and D - C will be omitted.
	 * 
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
	 * Defines the directory to which the SCTID based reference sets are
	 * exported
	 * 
	 * @parameter
	 * @required
	 */
	File sctidRefsetOutputDirectory;

	/**
	 * Directory where the fixed SCTID map is located
	 * 
	 * @parameter
	 * @required
	 */
	File fixedMapDirectory;

	/**
	 * Directory where the read/write SCTID maps are stored
	 * 
	 * @parameter
	 * @required
	 */
	File readWriteMapDirectory;

	/**
	 * Release version used to embed in the refset file names - if not specified
	 * then the "path version" reference set is used to determine the version
	 * 
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

		if (!fixedMapDirectory.exists() || !fixedMapDirectory.isDirectory()
				|| !fixedMapDirectory.canRead()) {
			throw new MojoExecutionException(
					"Cannot proceed, fixedMapDirectory must exist and be readable");
		}

		if (!readWriteMapDirectory.exists()
				|| !readWriteMapDirectory.isDirectory()
				|| !readWriteMapDirectory.canRead()) {
			throw new MojoExecutionException(
					"Cannot proceed, readWriteMapDirectory must exist and be readable");
		}

		try {
			allowedStatuses = tf.newIntSet();
			positions = new HashSet<I_Position>();
			for (ExportSpecification spec : exportSpecifications) {
				for (PositionDescriptor pd : spec.getPositionsForExport()) {
					positions.add(pd.getPosition());
				}
				for (ConceptDescriptor status : spec.getStatusValuesForExport()) {
					allowedStatuses.add(status.getVerifiedConcept()
							.getConceptId());
				}
			}

			sctidRefsetOutputDirectory.mkdirs();
			uuidRefsetOutputDirectory.mkdirs();

			MemberRefsetHandler.setFixedMapDirectory(fixedMapDirectory);
			MemberRefsetHandler.setReadWriteMapDirectory(readWriteMapDirectory);
			if (rf2Descriptor != null && rf2Descriptor.getModule() != null) {
				MemberRefsetHandler.setModule(rf2Descriptor.getModule());
			}

			tf.iterateConcepts(this);

			for (BufferedWriter writer : writerMap.values()) {
				writer.close();
			}

			MemberRefsetHandler.cleanup();

		} catch (Exception e) {
			throw new MojoExecutionException(
					"exporting reference sets failed for specification "
							+ exportSpecifications, e);
		}
	}

	public void processConcept(I_GetConceptData concept) throws Exception {
		if (testSpecification(concept)) {
			// export the status refset for this concept
			I_ConceptAttributePart latest = getLatestAttributePart(concept);
			if (latest == null) {
				getLog().warn(
						"Concept " + concept
								+ " is exportable for specification "
								+ exportSpecifications
								+ " but has no parts valid for statuses "
								+ allowedStatuses + " and positions "
								+ positions);
				return;
			}

			exportRefsets(concept.getConceptId());

			// export relationship refsets
			for (I_RelVersioned rel : concept.getSourceRels()) {
				processRelationship(rel);
			}

			// export description refsets
			for (I_DescriptionVersioned desc : concept.getDescriptions()) {
				processDescription(desc);
			}

			// TODO commented out because it costs too many SCTIDs and we need
			// to release pathology - to be included later
			// extractStatus(latest, concept.getConceptId());
			// extractDefinitionType(latest, concept.getConceptId());
		}
	}

	/**
	 * Gets the latest attribute for the concept.
	 * 
	 * Attributes are filtered by the <code>allowedStatuses</code> and
	 * <code>positions</code> lists.
	 * 
	 * @param concept
	 *            the concept to get the latest attribute for.
	 * @return latest I_ConceptAttributePart may be null.
	 * @throws IOException
	 *             looking up I_ConceptAttributePart
	 * @throws TerminologyException
	 * @throws TerminologyException
	 *             on lookup/DB errors
	 */
	// @SuppressWarnings("deprecation")
	I_ConceptAttributePart getLatestAttributePart(I_GetConceptData concept)
			throws IOException, TerminologyException {
		I_ConceptAttributePart latest = null;
		for (I_ConceptAttributeTuple tuple : concept
				.getConceptAttributeTuples(allowedStatuses,
						new PositionSetReadOnly(positions), null, null)) {
			if (latest == null || latest.getVersion() < tuple.getVersion()) {
				latest = tuple.getMutablePart();
			}
		}
		return latest;
	}

	private void processDescription(I_DescriptionVersioned versionedDesc)
			throws Exception {
		boolean exportableVersionFound = false;
		I_DescriptionPart latest = null;
		for (I_DescriptionPart part : versionedDesc.getMutableParts()) {
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

			// TODO commented out because it costs too many SCTIDs and we need
			// to release pathology - to be included later
			// extractStatus(latest, descId);
		}
	}

	@SuppressWarnings("deprecation")
	private void processRelationship(I_RelVersioned versionedRel)
			throws Exception {
		if (testSpecification(versionedRel.getC2Id())) {
			boolean exportableVersionFound = false;
			I_RelPart latest = null;
			for (I_RelPart part : versionedRel.getMutableParts()) {
				if (testSpecification(part.getCharacteristicId())
						&& testSpecification(part.getPathId())
						&& testSpecification(part.getRefinabilityId())
						&& testSpecification(part.getTypeId())
						&& allowedStatuses.contains(part.getStatusId())
						&& checkPath(part.getPathId())) {

					exportableVersionFound = true;
					if (latest == null
							|| latest.getVersion() < part.getVersion()) {
						latest = part;
					}
				}
			}
			if (exportableVersionFound) {
				// found a valid version of this relationship for export
				// therefore export its extensions
				int relId = versionedRel.getRelId();
				exportRefsets(relId);

				// TODO commented out because it costs too many SCTIDs and we
				// need to release pathology - to be included later
				// extractRelationshipRefinability(latest, relId);
				// extractStatus(latest, relId);
			}
		}
	}

	/**
	 * Gets the latest extension for the concept and refset id.
	 * 
	 * Extension are filtered by the <code>allowedStatuses</code> and
	 * <code>positions</code> lists.
	 * 
	 * @param componentId
	 *            refset member concept
	 * @param relationshipRefinabilityExtension
	 *            refset.
	 * @return I_ExtendByRefVersion the latest extension, may be null
	 * @throws IOException
	 *             DB error
	 * @throws TerminologyException
	 */
	I_ExtendByRefVersion getCurrentExtension(int componentId,
			ConceptSpec relationshipRefinabilityExtension) throws IOException,
			TerminologyException {
		int refsetId = relationshipRefinabilityExtension.localize().getNid();

		I_ExtendByRefVersion latest = null;
		for (I_ExtendByRef ext : tf.getAllExtensionsForComponent(componentId)) {
			if (ext.getRefsetId() == refsetId) {
				for (I_ExtendByRefVersion tuple : ext.getTuples(
						allowedStatuses, new PositionSetReadOnly(positions),
						null, null)) {
					if (latest == null
							|| latest.getVersion() < tuple.getVersion()) {
						latest = tuple;
					}
				}
			}
		}
		return latest;
	}

	/**
	 * Exports the refset to file.
	 * 
	 * @param thinExtByRefTuple
	 *            The concept extension to write to file.
	 * 
	 * @throws Exception
	 *             on DB or file error.
	 */
	private void exportRefsets(int componentId) throws TerminologyException,
			Exception {
		List<? extends I_ExtendByRef> extensions = tf
				.getAllExtensionsForComponent(componentId);
		for (I_ExtendByRef thinExtByRefVersioned : extensions) {
			if (testSpecification(thinExtByRefVersioned.getRefsetId())) {
				for (I_ExtendByRefVersion thinExtByRefTuple : thinExtByRefVersioned
						.getTuples(allowedStatuses, new PositionSetReadOnly(
								positions), null, null)) {
					export(thinExtByRefTuple);
				}
			}
		}
	}

	void export(I_ExtendByRefVersion thinExtByRefTuple) throws Exception {
		export(thinExtByRefTuple.getMutablePart(), thinExtByRefTuple
				.getMemberId(), thinExtByRefTuple.getRefsetId(),
				thinExtByRefTuple.getComponentId());
	}

	/**
	 * Exports the refset to file.
	 * 
	 * @param thinExtByRefPart
	 *            The concept extension to write to file.
	 * @param memberId
	 *            the id for this refset member record.
	 * @param refsetId
	 *            the refset id
	 * @param componentId
	 *            the referenced component
	 * @throws Exception
	 *             on DB errors or file write errors.
	 */
	void export(I_ExtendByRefPart thinExtByRefPart, Integer memberId,
			int refsetId, int componentId) throws Exception {
		RefsetType refsetType = refsetTypeMap.get(refsetId);
		if (refsetType == null) {
			try {
				refsetType = RefsetType.findByExtension(thinExtByRefPart);
			} catch (EnumConstantNotPresentException e) {
				getLog().warn(
						"No handler for tuple " + thinExtByRefPart
								+ " of type " + thinExtByRefPart.getClass(), e);
				return;
			}
			refsetTypeMap.put(refsetId, refsetType);
		}

		BufferedWriter uuidRefsetWriter = writerMap.get(refsetId + "UUID");
		BufferedWriter sctIdRefsetWriter = writerMap.get(refsetId + "SCTID");
		if (sctIdRefsetWriter == null) {
			// must not have written to this file yet
			I_GetConceptData refsetConcept = tf.getConcept(refsetId);
			String refsetName = getPreferredTerm(refsetConcept);

			// TODO this is not the best way, but it works for now.
			refsetName = refsetName.replace("/", "-");
			refsetName = refsetName.replace("'", "_");

			if (releaseVersion == null) {
				releaseVersion = getReleaseVersion(refsetConcept);
			}

			if (useRF2) {
				/*
				 * <FileType>_<ContentType>_<ContentSubType>_<Country|Namespace>_
				 * <Date>.<ext> e.g. der2_SCTID.Activities of daily
				 * living.concept.refset_National_UK1999999_20090131.txt
				 */
				String sctIdFilePrefix = "der2_SCTID.";
				String uuidFilePrefix = "der2_UUID.";
				String fileName = refsetName + refsetType.getFileExtension()
						+ "_" + rf2Descriptor.getContentSubType() + "_"
						+ rf2Descriptor.getCountryCode()
						+ rf2Descriptor.getNamespace() + "_" + releaseVersion
						+ ".txt";
				uuidRefsetWriter = new BufferedWriter(new FileWriter(new File(
						uuidRefsetOutputDirectory, uuidFilePrefix + fileName)));
				sctIdRefsetWriter = new BufferedWriter(
						new FileWriter(new File(sctidRefsetOutputDirectory,
								sctIdFilePrefix + fileName)));
			} else {
				uuidRefsetWriter = new BufferedWriter(new FileWriter(new File(
						uuidRefsetOutputDirectory, "UUID_" + refsetName + "_"
								+ releaseVersion
								+ refsetType.getFileExtension())));
				sctIdRefsetWriter = new BufferedWriter(new FileWriter(new File(
						sctidRefsetOutputDirectory, "SCTID_" + refsetName + "_"
								+ releaseVersion
								+ refsetType.getFileExtension())));
			}

			writerMap.put(refsetId + "UUID", uuidRefsetWriter);
			writerMap.put(refsetId + "SCTID", sctIdRefsetWriter);

			if (useRF2) {
				sctIdRefsetWriter.write(refsetType.getRefsetHandler()
						.getRF2HeaderLine());
				uuidRefsetWriter.write(refsetType.getRefsetHandler()
						.getRF2HeaderLine());
			} else {
				sctIdRefsetWriter.write(refsetType.getRefsetHandler()
						.getHeaderLine());
				uuidRefsetWriter.write(refsetType.getRefsetHandler()
						.getHeaderLine());
			}
			uuidRefsetWriter.newLine();
			sctIdRefsetWriter.newLine();
		}

		// note that we are assuming that the type of refset member will be the
		// same as previous for this file type
		// if not we'll get a class cast exception, as we probably should

		if (useRF2) {
			sctIdRefsetWriter.write(refsetType.getRefsetHandler()
					.formatRefsetLineRF2(tf, thinExtByRefPart, memberId,
							refsetId, componentId, true));
			uuidRefsetWriter.write(refsetType.getRefsetHandler()
					.formatRefsetLineRF2(tf, thinExtByRefPart, memberId,
							refsetId, componentId, false));
		} else {
			sctIdRefsetWriter.write(refsetType.getRefsetHandler()
					.formatRefsetLine(tf, thinExtByRefPart, memberId, refsetId,
							componentId, true));
			uuidRefsetWriter.write(refsetType.getRefsetHandler()
					.formatRefsetLine(tf, thinExtByRefPart, memberId, refsetId,
							componentId, false));
		}
		uuidRefsetWriter.newLine();
		sctIdRefsetWriter.newLine();
	}

	/**
	 * Gets the release version for the path concept.
	 * 
	 * @param refsetConcept
	 *            path refset concept.
	 * @return String release preferred term.
	 * @throws Exception
	 *             DB error
	 */
	protected String getReleaseVersion(I_GetConceptData refsetConcept)
			throws Exception {

		if (pathReleaseVersions.containsKey(refsetConcept.getConceptId())) {
			return pathReleaseVersions.get(refsetConcept.getConceptId());
		} else {
			int pathid = getLatestAttributePart(refsetConcept).getPathId();

			String pathUuidStr = Integer.toString(pathid);
			try {
				String pathVersion = null;
				pathUuidStr = tf.getUids(pathid).iterator().next().toString();

				int pathVersionRefsetNid = tf
						.uuidToNative(org.dwfa.ace.refset.ConceptConstants.PATH_VERSION_REFSET
								.getUuids()[0]);
				int currentStatusId = tf
						.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT
								.getUids());
				for (I_ExtendByRef extension : tf
						.getAllExtensionsForComponent(pathid)) {
					if (extension.getRefsetId() == pathVersionRefsetNid) {
						I_ExtendByRefPart latestPart = getLatestVersion(extension);
						if (latestPart.getStatusId() == currentStatusId) {

							if (pathVersion != null) {
								throw new TerminologyException(
										"Concept contains multiple extensions for refset"
												+ org.dwfa.ace.refset.ConceptConstants.PATH_VERSION_REFSET
														.getDescription());
							}

							pathVersion = ((I_ExtendByRefPartStr) latestPart)
									.getStringValue();
						}
					}
				}

				if (pathVersion == null) {
					throw new TerminologyException(
							"Concept not a member of "
									+ org.dwfa.ace.refset.ConceptConstants.PATH_VERSION_REFSET
											.getDescription());
				}

				String releaseVersion = getPreferredTerm(tf.getConcept(pathid))
						+ "_" + pathVersion;
				pathReleaseVersions.put(refsetConcept.getConceptId(),
						releaseVersion);
				return releaseVersion;

			} catch (Exception e) {
				throw new RuntimeException(
						"Failed to obtain the release version for the path "
								+ pathUuidStr, e);
			}
		}
	}

	/**
	 * Gets the latest version for this extension.
	 * 
	 * @param extension
	 *            I_ExtendByRef
	 * @return I_ExtendByRefPart latest version. may be null
	 */
	private I_ExtendByRefPart getLatestVersion(I_ExtendByRef extension) {
		I_ExtendByRefPart latestPart = null;
		for (I_ExtendByRefPart part : extension.getMutableParts()) {
			if (latestPart == null
					|| part.getVersion() >= latestPart.getVersion()) {
				latestPart = part;
			}
		}
		return latestPart;
	}

	/**
	 * Does the concept match the <code>exportSpecifications</code>
	 * 
	 * @param concept
	 *            I_GetConceptData
	 * @return true if a matching concept.
	 * @throws Exception
	 *             DB error
	 */
	boolean testSpecification(I_GetConceptData concept) throws Exception {
		for (ExportSpecification spec : exportSpecifications) {
			if (spec.test(concept) && getLatestAttributePart(concept) != null) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Does the concept match the <code>exportSpecifications</code>
	 * 
	 * @param id
	 *            concept it
	 * @return true if a matching concept.
	 * @throws TerminologyException
	 *             DB error
	 * @throws IOException
	 *             DB error
	 * @throws Exception
	 *             DB error
	 */
	boolean testSpecification(int id) throws TerminologyException, IOException,
			Exception {
		return testSpecification(tf.getConcept(id));
	}

	/**
	 * Is the path id in the list of <code>positions</code>
	 * 
	 * @param pathId
	 *            int
	 * @return true if pathId in <code>positions</code> list
	 */
	boolean checkPath(int pathId) {
		for (I_Position position : positions) {
			if (position.getPath().getConceptId() == pathId) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the concepts preferred term filtered by <code>statusSet</code>
	 * sorted by <code>TYPE_B4_LANG</code>
	 * 
	 * @param conceptData
	 *            I_GetConceptData to get the preferred term for
	 * @return String preferred term
	 * @throws Exception
	 *             DB error
	 */
	String getPreferredTerm(I_GetConceptData conceptData) throws Exception {
		I_IntList descTypeList = tf.newIntList();
		descTypeList
				.add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
						.localize().getNid());

		I_IntSet statusSet = tf.newIntSet();
		statusSet.add(ArchitectonicAuxiliary.Concept.CURRENT.localize()
				.getNid());
		statusSet
				.add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
		statusSet.add(ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED
				.localize().getNid());
		statusSet.add(ArchitectonicAuxiliary.Concept.READY_TO_PROMOTE
				.localize().getNid());
		statusSet.add(ArchitectonicAuxiliary.Concept.PROMOTED.localize()
				.getNid());

		I_DescriptionTuple descTuple = conceptData.getDescTuple(descTypeList,
				null, statusSet, new PositionSetReadOnly(positions),
				LANGUAGE_SORT_PREF.TYPE_B4_LANG, null, null);
		if (descTuple == null) {
			UUID conceptUuid = conceptData.getUids().iterator().next();
			throw new MojoExecutionException(
					"Unable to obtain preferred term for concept "
							+ conceptUuid.toString());
		}

		return descTuple.getText();
	}

	public Set<I_Position> getPositions() {
		return positions;
	}

	public void setPositions(Set<I_Position> positions) {
		this.positions = positions;
	}

}
