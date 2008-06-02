package org.dwfa.mojo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.maven.MojoUtil;

/**
 * * ExportDatabase <br/>
 * <p>
 * The <code>ExportDatabase</code> class Exports database tables to flat
 * files.
 * </p>
 * <p>
 * </p>
 * 
 * 
 * 
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @author PeterVawser
 * @goal exportdata
 */
public class ExportDatabase extends AbstractMojo {

	/**
	 * Date format to use in output files
	 * 
	 * @parameter
	 * @required
	 */
	private String releaseDate;

	/**
	 * Location of the directory to output data files to.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private String outputDirectory;

	/**
	 * File name for concept table data output file
	 * 
	 * @parameter expression="ids.txt"
	 */
	private String idsDataFileName;

	/**
	 * File name for concept table data output file
	 * 
	 * @parameter expression="concepts.txt"
	 */
	private String conceptDataFileName;

	/**
	 * File name for relationship table data output file
	 * 
	 * @parameter expression="relationships.txt"
	 */
	private String relationshipsDataFileName;

	/**
	 * File name for description table data output file
	 * 
	 * @parameter expression="descriptions.txt"
	 */
	private String descriptionsDataFileName;

	/**
	 * File name for description table data output file
	 * 
	 * @parameter expression="errorLog.txt"
	 */
	private String errorLogFileName;

	/**
	 * The set of specifications used to determine if a concept should be
	 * exported.
	 * 
	 * @parameter
	 * @required
	 */
	private ExportSpecification[] specs;

	/**
	 * Positions to export data.
	 * 
	 * @parameter
	 * @required
	 */
	private PositionDescriptor[] positionsForExport;

	/**
	 * Status values to include in export
	 * 
	 * @parameter
	 * @required
	 */
	private ConceptDescriptor[] statusValuesForExport;

    /**
     * @parameter default-value="${project.build.directory}"
     * @required
     * @readonly
     */
     private File buildDirectory;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {

			if (MojoUtil.alreadyRun(getLog(), outputDirectory
					+ conceptDataFileName + descriptionsDataFileName
					+ relationshipsDataFileName)) {
				return;
			}

			I_TermFactory termFactory = LocalVersionedTerminology.get();

			HashSet<I_Position> positions = new HashSet<I_Position>(
					positionsForExport.length);
			for (PositionDescriptor pd : positionsForExport) {
				positions.add(pd.getPosition());
			}
			I_IntSet statusValues = termFactory.newIntSet();
			List<I_GetConceptData> statusValueList = new ArrayList<I_GetConceptData>();
			for (ConceptDescriptor status : statusValuesForExport) {
				I_GetConceptData statusConcept = status.getVerifiedConcept();
				statusValues.add(statusConcept.getConceptId());
				statusValueList.add(statusConcept);
			}
			getLog().info(
					" processing concepts for positions: " + positions
							+ " with status: " + statusValueList);

			Writer errorWriter = new BufferedWriter(new FileWriter(
					outputDirectory + errorLogFileName));

			File conceptFile = new File(outputDirectory + conceptDataFileName);
			File relationshipFile = new File(outputDirectory
					+ relationshipsDataFileName);
			File descriptionFile = new File(outputDirectory
					+ descriptionsDataFileName);
			File idsFile = new File(outputDirectory + idsDataFileName);
			idsFile.getParentFile().mkdirs();
			File idMapFile = new File(buildDirectory + "/generated-resources/sct-uuid-maps", "exported-ids-sct-map.txt");
			idMapFile.getParentFile().mkdirs();
			
			Writer conceptWriter = new BufferedWriter(new FileWriter(
					conceptFile));
			Writer relationshipWriter = new BufferedWriter(new FileWriter(
					relationshipFile));
			Writer descriptionWriter = new BufferedWriter(new FileWriter(
					descriptionFile));
			Writer idsWriter = new BufferedWriter(new FileWriter(
					idsFile));
			Writer idMapWriter = new BufferedWriter(new FileWriter(idMapFile));

			ExportIterator expItr = new ExportIterator(conceptWriter,
					descriptionWriter, relationshipWriter, idsWriter, idMapWriter, errorWriter,
					positions, statusValues, specs, getLog());
			expItr.setReleaseDate(releaseDate);
			LocalVersionedTerminology.get().iterateConcepts(expItr);

			conceptWriter.close();
			relationshipWriter.close();
			descriptionWriter.close();
			idsWriter.close();
			idMapWriter.close();
			errorWriter.close();

		} catch (Exception e) {
			throw new MojoExecutionException("Unable to export database due to exception", e);
		}

	}// End method execute

}// End class ExportDatabase
