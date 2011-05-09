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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ProcessExtByRef;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.maven.MojoUtil;
import org.dwfa.tapi.TerminologyException;

/**
 *
 * @goal write-refset-descriptions
 *
 * @phase process-classes
 * @requiresDependencyResolution compile
 * @author Dion McMurtrie
 */
public class WriteRefsetDescriptions extends AbstractMojo implements
I_ProcessExtByRef {

	private static final Collection<UUID> CURRENT_STATUS_UUIDS = ArchitectonicAuxiliary.Concept.CURRENT.getUids();
	private static final Collection<UUID> PREFERED_TERM_UUIDS = ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids();
	private static final Collection<UUID> FULLY_SPECIFIED_UUIDS = ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids();
	/**
	 * Location of the directory to output data files to.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;
	private I_TermFactory termFactory;
	private Map<String, Writer> fileMap = new HashMap<String, Writer>();
	private BufferedWriter noDescriptionWriter;
	private Map<String, Integer> progressMap = new HashMap<String, Integer>();

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
		try {

			if (MojoUtil
					.alreadyRun(getLog(), outputDirectory.getAbsolutePath(), 
							this.getClass(), targetDirectory)) {
				return;
			}

			noDescriptionWriter = new BufferedWriter(new FileWriter(new File(outputDirectory, "Concepts with no descriptions.txt")));

			termFactory = LocalVersionedTerminology.get();
			System.out.println("Exporting reference sets as description files");
			if (!outputDirectory.exists()) {
				outputDirectory.mkdirs();
			}

			termFactory.iterateExtByRefs(this);

			for (Writer writer : fileMap.values()) {
				writer.flush();
				writer.close();
			}

			noDescriptionWriter.flush();
			noDescriptionWriter.close();

		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}

	}

	public void processExtensionByReference(I_ThinExtByRefVersioned refset) throws Exception {

		I_GetConceptData refsetConcept = termFactory.getConcept(refset.getRefsetId());
		
		if (refset.getTypeId() != RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize().getNid()) {
			getLog().info("Skipping non-concept type refset " + refsetConcept.getId().getUIDs().iterator().next());
			return;
		}
		
		List<I_ConceptAttributePart> refsetAttibuteParts = refsetConcept.getConceptAttributes().getVersions();
		I_ConceptAttributePart latestAttributePart = null;
		for (I_ConceptAttributePart attributePart : refsetAttibuteParts) {
			if (latestAttributePart == null || attributePart.getVersion() >= latestAttributePart.getVersion()) {
				latestAttributePart = attributePart;
			}
		}
		if (latestAttributePart.getConceptStatus() != ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid()) {
			getLog().info("Skipping non-current refset " + refsetConcept.getId().getUIDs().iterator().next());
			return;
		}
		
		I_IntSet status = termFactory.newIntSet();
		status.add(termFactory.getConcept(CURRENT_STATUS_UUIDS).getConceptId());
		status.add(ArchitectonicAuxiliary.getSnomedDescriptionStatusId(CURRENT_STATUS_UUIDS));

		I_IntSet fsn = termFactory.newIntSet();
		fsn.add(termFactory.getConcept(FULLY_SPECIFIED_UUIDS).getConceptId());
		fsn.add(ArchitectonicAuxiliary.getSnomedDescriptionTypeId(FULLY_SPECIFIED_UUIDS));

		I_IntSet preferredTerm = termFactory.newIntSet();
		preferredTerm.add(termFactory.getConcept(PREFERED_TERM_UUIDS).getConceptId());
		preferredTerm.add(ArchitectonicAuxiliary.getSnomedDescriptionTypeId(PREFERED_TERM_UUIDS));

		I_DescriptionTuple refsetName = assertExactlyOne(termFactory.getConcept((refset.getRefsetId())).getDescriptionTuples(status, fsn, null));

		logProgress(refsetName.getText());

		Writer writer = getWriter(refsetName.getText());

		I_ThinExtByRefPart part = getLatestVersionIfCurrent(refset);

		if (part!=null) {

			I_GetConceptData concept = termFactory.getConcept((refset.getComponentId()));
			String conceptUuids = concept.getUids().iterator().next().toString();

			I_GetConceptData value = termFactory.getConcept(((I_ThinExtByRefPartConcept) part).getConceptId());

			List<I_DescriptionTuple> descriptionTuples = concept.getDescriptionTuples(status, preferredTerm, null);
			if (descriptionTuples.size() == 0) {
				getLog().warn("Concept " + conceptUuids + " has no active preferred term");
				noDescriptionWriter.append("Concept " + conceptUuids + " has no active preferred term");
				noDescriptionWriter.append("\r\n");
			} else {
				String conceptName;
				int descriptionId = 0;
				if (descriptionTuples != null) {
					I_DescriptionTuple descriptionTuple = descriptionTuples.iterator().next();
					conceptName = descriptionTuple.getText();
					descriptionId = descriptionTuple.getDescId();
				} else {
					conceptName = "Concept has no active description, only " + concept.getDescriptions();
				}

				if (value.getConceptId()!=ConceptConstants.PARENT_MARKER.localize().getNid()) {
					writer.append(getSnomedId(descriptionId));
					writer.append("\t");
					writer.append(getSnomedId(concept.getConceptId()));
					writer.append("\t");
					writer.append(conceptName);
					writer.append("\t");
					writer.append(value.toString());
					writer.append("\r\n");
				}
			}
		}
	}

	private void logProgress(String refsetName) {
		Integer progress = progressMap.get(refsetName);
		if (progress == null) {
			progress = 0;
		}
		
		progressMap.put(refsetName, progress++);
		
		if (progress % 1000 == 0) {
			getLog().info("Exported " + progress + " of refset " + refsetName);
		}
	}

	public I_ThinExtByRefPart getLatestVersionIfCurrent(I_ThinExtByRefVersioned ext) throws TerminologyException, IOException {
		I_ThinExtByRefPart latest = null;
		List<? extends I_ThinExtByRefPart> versions = ext.getVersions();
		for (I_ThinExtByRefPart version : versions) {

			if (latest == null) {
				latest = version;
			} else {
				if (latest.getVersion()<version.getVersion()) {
					latest = version;
				}				
			}			
		}

		if (!(latest.getStatus()==termFactory.getConcept(CURRENT_STATUS_UUIDS).getConceptId())) {
			latest = null;
		}

		return latest;
	}

	private String getSnomedId(int nid) throws IOException, TerminologyException {

		if (nid == 0) {
			return "no identifier";
		}

		I_IdVersioned idVersioned = termFactory.getId(nid);
		for (I_IdPart idPart : idVersioned.getVersions()) {
			if (idPart.getSource() == termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids())) {
				return idPart.getSourceId().toString();
			}
		}

		return "no SCTID found";
	}

	private Writer getWriter(String text) throws IOException {
		Writer writer = fileMap.get(text);
		if (writer == null) {
			File outputFile = new File(outputDirectory, text + ".refset.text");
			System.out.println("making directory - " + outputFile.getParentFile());
			outputFile.getParentFile().mkdirs();
			outputFile.createNewFile();
			writer = new BufferedWriter(new FileWriter(outputFile));
			fileMap.put(text, writer);
		}
		return writer;
	}

	private <T> T assertExactlyOne(
			Collection<T> collection) {
		assert collection.size() == 1 : "Collection " + collection + " was expected to only have one element";
		return collection.iterator().next();
	}
}
