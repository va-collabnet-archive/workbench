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
package org.dwfa.mojo.export;

import org.dwfa.mojo.export.amt.AmtExportSpecification;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.process.I_ProcessQueue;
import org.dwfa.ace.task.commit.validator.ValidationException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.dto.ComponentDto;
import org.dwfa.maven.sctid.UuidSctidMapDb;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.PROJECT;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.mojo.export.file.AceOutputHandler;
import org.dwfa.mojo.export.file.Rf1OutputHandler;
import org.dwfa.mojo.export.file.Rf2OutputHandler;
import org.dwfa.vodb.types.ThinConPart;


/**
 * Exports the components for the database
 *
 * @goal database-export
 */
public class DatabaseExport extends AbstractMojo implements I_ProcessConcepts {

    /**
     * Class logger.
     */
    private Logger logger = Logger.getLogger(DatabaseExport.class.getName());

    /**
     * Get the exportable data from a concept
     */
    private ExportSpecification exportSpecification;

    /**
     * The release position...
     *
     * @parameter
     * @required
     */
    private PositionDescriptor[] releasePositions;

    /**
     * Positions for export
     *
     * @parameter
     * @required
     */
    private PositionDescriptor[] positionsForExport;

    /**
     * Positions to exclude from export
     *
     * @parameter
     */
    private PositionDescriptor[] excludedPositions = new PositionDescriptor[0];

    /**
     * The concept that groups all the maintained modules.
     *
     * @parameter
     */
    private ConceptDescriptor maintainedModuleParent;

    /**
     * Included hierarchy
     *
     * @parameter
     * @required
     */
    private ConceptDescriptor[]  inclusions;

    /**
     * Excluded hierarchy
     *
     * @parameter
     */
    private ConceptDescriptor[]  exclusions;

    /**
     * The default namespace for this export
     *
     * @parameter
     * @required
     */
    private String defaultNamespace;

    /**
     * The default namespace for this export
     *
     * @parameter
     * @required
     */
    private String defaultProject;

    /**
     * Directory for export files.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File exportDirectory;

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
     * UUID-SCTID database user to optionally authenticate to the database
     *
     * @parameter
     */
    private String uuidSctidDbUsername;

    /**
     * UUID-SCTID database user's password optionally used to authenticate to the database
     *
     * @parameter
     */
    private String uuidSctidDbPassword;

    /**
     * The number of threads to use.
     *
     * @parameter
     */
    private int numberOfThreads = 1;

    /**
     * Forward generate the language refset.
     *
     * @parameter
     */
    private boolean generateLangaugeRefset = true;

    /**
     * Export file output handler for RF2
     */
    private ExportOutputHandler rf2OutputHandler;

    /**
     * Export file output handler for RF1
     */
    private ExportOutputHandler rf1OutputHandler;

    /**
     * Export file output handler for Ace
     */
    private AceOutputHandler aceOutputHandler;;

    /** Da factory */
    private I_TermFactory termFactory;

    /**
     * Processed concept count.
     */
    private int processedConceptCount = 0;

    /**
     * Thread pool for processing concepts.
     */
    private I_ProcessQueue workQueue;

    /**
     * The current Batch of concepts to process.
     */
    private List<I_GetConceptData> currentBatch = new ArrayList<I_GetConceptData>();

    /**
     * Promotes to concepts for calculating promotion origins.
     */
    private I_GetConceptData promotesToConcept;

    /** The active concept. */
    private I_GetConceptData currentConcept;

    /**
     * Tuple part to use for exporting new refset content namely the ADRS.
     */
    private ThinConPart releasePart;

    /**
     * TODO need to mock this out.
     * For testing
     */
    private boolean testing = false;

    /**
     * Export Flag determines which type of Export we are running. Allowed Values are AMT or SNOMED.
     * @parameter
     * @required
     */
    private String exportFlag;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        logger.info("Start exporting concepts");

        try {
            currentConcept = getTermFactory().getConcept(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());

            System.setProperty(UuidSctidMapDb.SCT_ID_MAP_DRIVER, uuidSctidDbDriver);
            System.setProperty(UuidSctidMapDb.SCT_ID_MAP_DATABASE_CONNECTION_URL, uuidSctidDbConnectionUrl);
            if (uuidSctidDbUsername != null) {
                System.setProperty(UuidSctidMapDb.SCT_ID_MAP_USER, uuidSctidDbUsername);
            }
            if (uuidSctidDbPassword != null) {
                System.setProperty(UuidSctidMapDb.SCT_ID_MAP_PASSWORD, uuidSctidDbPassword);
            }

            workQueue = LocalVersionedTerminology.get().newProcessQueue(numberOfThreads);

            List<Position> positions = new ArrayList<Position>();

            for (PositionDescriptor positionDescriptor : positionsForExport) {
                Position position = new Position(positionDescriptor);
                position.setLastest(true);
                positions.add(position);
            }

            Map<UUID, Map<UUID, Date>> releasePathDateMap = new HashMap<UUID, Map<UUID,Date>>();
            OriginProcessor originProcessor = new OriginProcessorFactory(currentConcept,
                    releasePositions, maintainedModuleParent, releasePositions).getInstance(exportFlag);

            originProcessor.addOriginPositions(releasePathDateMap, positions, excludedPositions);

            rf2OutputHandler = new Rf2OutputHandler(new File(exportDirectory, "rf2") , releasePathDateMap);
            rf1OutputHandler = new Rf1OutputHandler(new File(exportDirectory, "rf1"), releasePathDateMap);
            aceOutputHandler = new AceOutputHandler(new File(exportDirectory, "ace"), releasePathDateMap);

            List<I_GetConceptData> inclusionRoots = new ArrayList<I_GetConceptData>();
            for (ConceptDescriptor conceptDescriptor : inclusions) {
                inclusionRoots.add(conceptDescriptor.getVerifiedConcept());
            }

            List<I_GetConceptData> exclusionRoots = new ArrayList<I_GetConceptData>();
            if (exclusions != null) {
                for (ConceptDescriptor conceptDescriptor : exclusions) {
                    exclusionRoots.add(conceptDescriptor.getVerifiedConcept());
                }
            }

            releasePart = new ThinConPart();
            releasePart.setPathId(releasePositions[0].getPosition().getPath().getConceptId());
            releasePart.setStatusId(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
            releasePart.setVersion(releasePositions[0].getPosition().getVersion());

            //TODO Create Export Specification Factory or Builder.
            if(exportFlag.equalsIgnoreCase("amt")){
                exportSpecification = new AmtExportSpecification(positions, inclusionRoots, exclusionRoots,
                        NAMESPACE.fromString(defaultNamespace), PROJECT.valueOf(defaultProject));
            } else if(exportFlag.equalsIgnoreCase("snomed")) {
                exportSpecification = new SnomedExportSpecification(positions, inclusionRoots, exclusionRoots,
                        NAMESPACE.fromString(defaultNamespace), PROJECT.valueOf(defaultProject));
                ((SnomedExportSpecification)exportSpecification).setReleasePart(releasePart);
                ((SnomedExportSpecification)exportSpecification).setGenerateLangaugeRefset(generateLangaugeRefset);
            }


        } catch (IOException e) {
            throw new MojoExecutionException("Execute error: ", e);
        } catch (SQLException e) {
            throw new MojoExecutionException("Execute error: ", e);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Execute error: ", e);
        } catch (Exception e) {
            throw new MojoExecutionException("Execute error: ", e);
        }

        try {
            getTermFactory().iterateConcepts(this);

            workQueue.execute(new RunnableProcessConcept(currentBatch));
            workQueue.awaitCompletion();

            if (!testing) {
                ((Rf2OutputHandler) rf2OutputHandler).closeFiles();
                ((Rf1OutputHandler) rf1OutputHandler).closeFiles();
                ((AceOutputHandler) aceOutputHandler).closeFiles();
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Iterate error: ", e);
        }

        logger.info("Finished exporting concepts");
    }

    /**
     * Process a concept.
     *
     * @see org.dwfa.ace.api.I_ProcessConcepts#processConcept(org.dwfa.ace.api.I_GetConceptData)
     */
    @Override
    public void processConcept(I_GetConceptData concept) throws Exception {
        currentBatch.add(concept);

        if(currentBatch.size() % 1000 == 0){
            workQueue.execute(new RunnableProcessConcept(currentBatch));
            currentBatch.clear();
        }
    }

    /**
     * @return the termFactory
     */
    private I_TermFactory getTermFactory() {
        if(termFactory == null){
            termFactory = LocalVersionedTerminology.get();
        }
        return termFactory;
    }

    /**
     * @param termFactory the termFactory to set
     */
    public void setTermFactory(I_TermFactory termFactory) {
        this.termFactory = termFactory;
    }

    /**
     * Class to use with Work Queue.
     */
    class RunnableProcessConcept implements Runnable {
        private List<I_GetConceptData> conceptsToProcess = new ArrayList<I_GetConceptData>();

        /**
         * Create a runnable with the list of jobs
         *
         * @param conceptsToProcess List of I_GetConceptData
         */
        RunnableProcessConcept(List<I_GetConceptData> conceptsToProcess) {
            this.conceptsToProcess = new ArrayList<I_GetConceptData>(conceptsToProcess);
        }

        /**
         * Process the list of concepts in the thread.
         */
        @Override
        public void run() {
            for(I_GetConceptData concept : conceptsToProcess){
                processedConceptCount++;
                if(processedConceptCount % 10000 == 0){
                    logger.info("Concepts processed: " + processedConceptCount);
                }

                ComponentDto componentDto;
                try {
                    componentDto = exportSpecification.getDataForExport(concept);
                    if(componentDto != null){
                        try{
                            rf2OutputHandler.export(componentDto);
                            rf1OutputHandler.export(componentDto);
                            aceOutputHandler.export(componentDto);
                        } catch (ValidationException ve) {
                            logger.severe(ve.getMessage());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe(e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }

    }
}
