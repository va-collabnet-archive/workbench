package org.dwfa.mojo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ProcessExtByRef;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.maven.MojoUtil;

/**
*
* @goal write-refset-descriptions
*
* @phase process-classes
* @requiresDependencyResolution compile
*/
public class WriteRefsetDescriptions extends AbstractMojo implements
		I_ProcessExtByRef {

	private static final Collection<UUID> LIMITED_STATUS_UUIDS = ArchitectonicAuxiliary.Concept.LIMITED.getUids();
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

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {

			if (MojoUtil
					.alreadyRun(getLog(), outputDirectory.getAbsolutePath())) {
				return;
			}

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

		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}

	}

	public void processExtensionByReference(I_ThinExtByRefVersioned refset)
			throws Exception {
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
		
		getLog().info("Exporting " + refsetName.getText());
		
		Writer writer = getWriter(refsetName.getText());
				
		List<I_ThinExtByRefTuple> tuples = refset.getTuples(status, null, false);
		
		for (I_ThinExtByRefTuple thinExtByRefTuple : tuples) {
			I_GetConceptData concept = termFactory.getConcept((thinExtByRefTuple.getComponentId()));
			String conceptUuids = concept.getUids().iterator().next().toString();

			List<I_DescriptionTuple> descriptionTuples = concept.getDescriptionTuples(status, preferredTerm, null);
			if (descriptionTuples.size() == 0) {
				getLog().warn("Concept " + conceptUuids + " has no active preferred term");
				status.add(termFactory.getConcept(LIMITED_STATUS_UUIDS).getConceptId());
				status.add(ArchitectonicAuxiliary.getSnomedDescriptionStatusId(LIMITED_STATUS_UUIDS));
				descriptionTuples = concept.getDescriptionTuples(status, preferredTerm, null);
				if (descriptionTuples.size() == 0) {
					getLog().warn("Concept " + conceptUuids + " has no active preferred term");
					descriptionTuples = concept.getDescriptionTuples(status, fsn, null);
					if (descriptionTuples.size() == 0) {
						getLog().warn("Concept " + conceptUuids + " has no active fully specified name");
						descriptionTuples = concept.getDescriptionTuples(status, null, null);
						if (descriptionTuples.size() == 0) {
							getLog().error("Concept " + conceptUuids + " has no active descriptions, but has " + concept.getDescriptionTuples(null, null, null));
							descriptionTuples = null;
						}
					}
				}
			}
			
			String conceptName;
			int descriptionId = 0;
			if (descriptionTuples != null) {
				I_DescriptionTuple descriptionTuple = descriptionTuples.iterator().next();
				conceptName = descriptionTuple.getText();
				descriptionId = descriptionTuple.getDescId();
			} else {
				conceptName = "Concept has no active description, only " + concept.getDescriptions();
			}
			
			String descriptionUuid;
			if (descriptionId != 0) {
				I_DescriptionVersioned conceptDesc = termFactory.getDescription(descriptionId);
				descriptionUuid = conceptDesc.toLocalFixedDesc().getUids().iterator().next().toString();
			} else {
				descriptionUuid = "No description found";
			}
			
			writer.append(descriptionUuid);
			writer.append("\t");
			writer.append(conceptUuids);
			writer.append("\t");
			writer.append(conceptName);
			writer.append("\r\n");
		}
	}

	private Writer getWriter(String text) throws IOException {
		Writer writer = fileMap.get(text);
		if (writer == null) {
			File outputFile = new File(outputDirectory, text + ".refset.text");
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