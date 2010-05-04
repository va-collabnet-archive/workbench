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
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
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
import org.dwfa.maven.sctid.UuidSctidMapDb;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.mojo.refset.ExportSpecification;

/**
 * * ExportDatabase <br/>
 * <p>
 * The <code>ExportDatabase</code> class Exports database tables to flat files.
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
     * If not specified the Path Version Reference Set will be used to determine
     * the release version.
     *
     * @parameter
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
     * Whether to validate the positions
     *
     * @parameter expression=true
     */
    private boolean validatePositions;

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
     * URL used to connect to the UUID-SCTID database
     *
     * @parameter
     * @required
     */
    private String uuidSctidDbConnectionUrl;

    /**
     * UUID-SCTID database driver fully qualified class name
     *
     * @parameter
     * @required
     */
    private String uuidSctidDbDriver;

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    /**
     * Indicates if the concepts, descriptions and relationships exported must
     * be a cohesive
     * self supporting set (true), or if the resulting export may contain
     * references to entities
     * not represented in the export (false).
     *
     * @parameter expression=true
     */
    private boolean exportCohesiveSet;

    /**
     * output file for the exported id map
     *
     * @parameter expression="${project.build.directory}/generated-resources/sct-uuid-maps/exported-ids-sct-map.txt"
     */
    private File exportedIdMapFile;

    /**
     * Indicates if SCTIDs should be generated for entities exported that have
     * no SCTIDs already
     *
     * @parameter
     */
    private boolean generateSctIds = false;

    /**
     * Concept to use as a path for the SCTIDs generated if generateSctIds is
     * set to true.
     *
     * @parameter
     */
    private ConceptDescriptor pathForIds;

    /**
     * Namespace for the SCTIDs if generateSctIds is set to true.
     *
     * @parameter
     */
    private String namespace;

    /**
     * Specifies the description types that are exportable - note that this is
     * above and beyond any description
     * type concepts that are exportable according to the export specification
     *
     * @parameter
     */
    private ConceptDescriptor[] descriptionTypesForExport;

    /**
     * Used to configure the release date/version to use for given paths,
     * paths not specified will be exported with the version recorded in the
     * database.
     * Note this configuration will be ignored and overridden by the releaseDate
     * parameter
     *
     * @parameter
     */
    private PathReleaseDateConfig[] pathReleaseDateConfig;

    /**
     * Export the full history
     *
     * @parameter
     */
    private boolean fullExport = false;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            System.setProperty(UuidSctidMapDb.SCT_ID_MAP_DRIVER, uuidSctidDbDriver);
            System.setProperty(UuidSctidMapDb.SCT_ID_MAP_DATABASE_CONNECTION_URL, uuidSctidDbConnectionUrl);

            if (MojoUtil.alreadyRun(getLog(), outputDirectory + conceptDataFileName + descriptionsDataFileName
                + relationshipsDataFileName, this.getClass(), targetDirectory)) {
                return;
            }

            I_TermFactory termFactory = LocalVersionedTerminology.get();

            HashSet<I_Position> positions = new HashSet<I_Position>(positionsForExport.length);
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
            getLog().info(" processing concepts for positions: " + positions + " with status: " + statusValueList);

            if (outputDirectory.endsWith("/") == false) {
                outputDirectory = outputDirectory + "/";
            }
            File outputDirFile = new File(outputDirectory);
            outputDirFile.mkdirs();
            Writer errorWriter = new BufferedWriter(new FileWriter(outputDirectory + errorLogFileName));

            File conceptFile = new File(outputDirectory + conceptDataFileName);
            File relationshipFile = new File(outputDirectory + relationshipsDataFileName);
            File descriptionFile = new File(outputDirectory + descriptionsDataFileName);
            File idsFile = new File(outputDirectory + idsDataFileName);
            idsFile.getParentFile().mkdirs();

            exportedIdMapFile.getParentFile().mkdirs();

            Writer conceptWriter = new BufferedWriter(new FileWriter(conceptFile));
            Writer relationshipWriter = new BufferedWriter(new FileWriter(relationshipFile));
            Writer descriptionWriter = new BufferedWriter(new FileWriter(descriptionFile));
            Writer idsWriter = new BufferedWriter(new FileWriter(idsFile));
            Writer idMapWriter = new BufferedWriter(new FileWriter(exportedIdMapFile));

            ExportIterator expItr = new ExportIterator(conceptWriter, descriptionWriter, relationshipWriter, idsWriter,
                idMapWriter, errorWriter, positions, statusValues, specs, getLog());
            expItr.setReleaseDate(releaseDate);
            expItr.setValidatePositions(validatePositions);
            expItr.setExportCohesiveSet(exportCohesiveSet);
            expItr.setDescriptionTypes(descriptionTypesForExport);
            expItr.setPathReleaseDateConfig(pathReleaseDateConfig);
            expItr.setFullExport(fullExport);
            if (generateSctIds) {
                expItr.enableSctIdGeneration(NAMESPACE.fromString(namespace), pathForIds.getVerifiedConcept());
            }
            LocalVersionedTerminology.get().iterateConcepts(expItr);

            if (generateSctIds) {
                expItr.disableSctIdGeneration();
            }
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
